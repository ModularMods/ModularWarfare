package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.WeaponReloadEvent;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemAmmo;
import com.modularwarfare.common.guns.ItemBullet;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponAnimationType;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.handler.data.DataGunReloadEnhancedTask;
import com.modularwarfare.utility.ReloadHelper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;

public class PacketGunReloadEnhancedStop extends PacketBase {
    public int reloadValidCount;
    public boolean unloaded;
    public boolean loaded;

    public PacketGunReloadEnhancedStop() {
        // TODO Auto-generated constructor stub
    }

    public PacketGunReloadEnhancedStop(int reloadValidCount, boolean unloaded, boolean loaded) {
        this.reloadValidCount = reloadValidCount;
        this.unloaded = unloaded;
        this.loaded = loaded;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(reloadValidCount);
        data.writeBoolean(unloaded);
        data.writeBoolean(loaded);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        reloadValidCount = data.readInt();
        unloaded = data.readBoolean();
        loaded = data.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        if (entityPlayer.getHeldItemMainhand() != null) {
            if (entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
                if (!ServerTickHandler.reloadEnhancedTask.containsKey(entityPlayer.getUniqueID())) {
                    return;
                }
                DataGunReloadEnhancedTask task = ServerTickHandler.reloadEnhancedTask.get(entityPlayer.getUniqueID());
                ItemStack gunStack = entityPlayer.inventory.mainInventory.get(task.gunSlot);
                ItemGun itemGun = (ItemGun) gunStack.getItem();
                GunType gunType = itemGun.type;
                InventoryPlayer inventory = entityPlayer.inventory;

                if (task.reloadGun != gunStack) {
                    ServerTickHandler.reloadEnhancedTask.remove(entityPlayer.getUniqueID());
                    return;
                }
                
                if (gunType.animationType.equals(WeaponAnimationType.ENHANCED)) {
                    if (gunType.acceptedAmmo != null) {
                        handleMagGunReloadEnhanced(entityPlayer, gunStack, itemGun, gunType, inventory);
                    } else {
                        handleBulletGunReloadEnhanced(entityPlayer, gunStack, itemGun, gunType, inventory);
                    }
                    entityPlayer.sendSlotContents(entityPlayer.inventoryContainer,
                            entityPlayer.inventoryContainer.inventorySlots.size() - 1 - 9 + task.gunSlot, gunStack);
                    //entityPlayer.sendContainerToPlayer(entityPlayer.inventoryContainer);
                    ModularWarfare.NETWORK.sendTo(new PacketGunReloadEnhancedTask(ItemStack.EMPTY), entityPlayer);
                }

            }
        }

    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        // TODO Auto-generated method stub

    }

    public void handleBulletGunReloadEnhanced(EntityPlayerMP entityPlayer, ItemStack gunStack, ItemGun itemGun,
            GunType gunType, InventoryPlayer inventory) {
        if (!ServerTickHandler.reloadEnhancedTask.containsKey(entityPlayer.getUniqueID())) {
            return;
        }
        DataGunReloadEnhancedTask task = ServerTickHandler.reloadEnhancedTask.get(entityPlayer.getUniqueID());
        if (task.reloadGun != gunStack) {
            ServerTickHandler.reloadEnhancedTask.remove(entityPlayer.getUniqueID());
            return;
        }
        
        if (reloadValidCount > task.reloadCount || reloadValidCount < 0) {
            ServerTickHandler.reloadEnhancedTask.remove(entityPlayer.getUniqueID());
            return;
        }
        
        NBTTagCompound nbtTagCompound = gunStack.getTagCompound();
        
        if (unloaded) {
            if (task.isUnload) {
                ReloadHelper.unloadBullets(entityPlayer, gunStack,reloadValidCount);
            } else if (nbtTagCompound.hasKey("bullet")) {
                ItemBullet bulletItemToLoad = (ItemBullet) task.prognosisAmmo.getItem();
                ItemStack currentBullet = new ItemStack(nbtTagCompound.getCompoundTag("bullet"));
                ItemBullet currentBulletItem = (ItemBullet) currentBullet.getItem();
                if (!currentBulletItem.baseType.internalName.equalsIgnoreCase(bulletItemToLoad.baseType.internalName))
                    ReloadHelper.unloadBullets(entityPlayer, gunStack);
            }
        }
        
        if (task.isUnload) {
            return;
        }
        

        if (gunType.acceptedBullets != null) {
            if (gunStack.getTagCompound() != null) {
                if (gunType.internalAmmoStorage != null) {
                    if (gunStack.getTagCompound().getCompoundTag("bullet") != null) {
                        if (gunStack.getTagCompound().getInteger("ammocount") >= gunType.internalAmmoStorage) {
                            ServerTickHandler.reloadEnhancedTask.remove(entityPlayer.getUniqueID());
                            return;
                        }
                    }
                }
            }

            if (loaded) {
                if (ReloadHelper.removeItemstack(entityPlayer, task.prognosisAmmo, reloadValidCount)) {
                    ItemStack loadingItemStack = task.prognosisAmmo.copy();
                    int ammoCount = gunStack.getTagCompound().getInteger("ammocount");
                    gunStack.getTagCompound().setInteger("ammocount", ammoCount + reloadValidCount);
                    gunStack.getTagCompound().setTag("bullet", loadingItemStack.writeToNBT(new NBTTagCompound()));
                }
            }

        }
    }

    public void handleMagGunReloadEnhanced(EntityPlayerMP entityPlayer, ItemStack gunStack, ItemGun itemGun,
            GunType gunType, InventoryPlayer inventory) {
        if (!ServerTickHandler.reloadEnhancedTask.containsKey(entityPlayer.getUniqueID())) {
            return;
        }
        DataGunReloadEnhancedTask task = ServerTickHandler.reloadEnhancedTask.get(entityPlayer.getUniqueID());
        if (task.reloadGun != gunStack) {
            ServerTickHandler.reloadEnhancedTask.remove(entityPlayer.getUniqueID());
            return;
        }
        
        /** Unload old ammo stack */
        if (unloaded) {
            if(!(task.currentAmmo&&loaded)) {
                if (ItemGun.hasAmmoLoaded(gunStack)) {
                    ReloadHelper.unloadAmmo(entityPlayer, gunStack);
                }  
            }
        }

        if (task.isUnload) {
            return;
        }

        NBTTagCompound nbtTagCompound = gunStack.getTagCompound();
        boolean hasAmmoLoaded = ItemGun.hasAmmoLoaded(gunStack);
        boolean offhandedReload = false;
        ItemStack ammoStackToLoad = task.prognosisAmmo;
        Integer ammoStackSlotToLoad = null;
        Integer highestAmmoCount = 0;
        Integer multiMagToLoad = task.multiMagToLoad;

        /** Weapon Pre-Reload event */
        boolean multiMagReload = task.currentAmmo && hasAmmoLoaded
                && ammoStackToLoad.getTagCompound().hasKey("magcount");

        /** Loading of new ammo stack */
        if (loaded) {
            if (task.currentAmmo || ReloadHelper.removeItemstack(entityPlayer, ammoStackToLoad, 1)) {
                ItemStack loadingItemStack = ammoStackToLoad.copy();
                loadingItemStack.setCount(1);
                if (multiMagReload && multiMagToLoad != null)
                    loadingItemStack.getTagCompound().setInteger("magcount", multiMagToLoad);

                nbtTagCompound.setTag("ammo", loadingItemStack.writeToNBT(new NBTTagCompound()));
            }
        }
    }
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "PacketGunReloadEnhancedStop["+reloadValidCount+","+unloaded+","+loaded+"]";
    }
}
