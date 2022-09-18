package com.modularwarfare.utility;

import java.util.function.Consumer;

import com.modularwarfare.ModConfig;
import com.modularwarfare.common.guns.AmmoType;
import com.modularwarfare.common.guns.ItemAmmo;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.handler.data.VarInt;
import com.modularwarfare.common.type.BaseItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ReloadHelper {

    public static int getBulletOnMag(ItemStack ammoStack,Integer currentMagcount) {
        if(!(ammoStack.getItem() instanceof ItemAmmo)) {
            return 0;
        }
        if(!ammoStack.hasTagCompound()) {
            return 0;
        }
        ItemAmmo itemAmmo = (ItemAmmo) ammoStack.getItem();
        AmmoType ammoType = itemAmmo.type;
        NBTTagCompound nbtTagCompound = ammoStack.getTagCompound();
        int ammoCount = 0;
        if (!nbtTagCompound.hasKey("magcount")) {
            ammoCount = ammoStack.getTagCompound().getInteger("ammocount");
        } else {
            if(currentMagcount!=null) {
                ammoCount = ammoStack.getTagCompound().getInteger("ammocount"+currentMagcount);
            }else {
                for (int i = 1; i <= ammoType.magazineCount + 1; i++) {
                    ammoCount += ammoStack.getTagCompound().getInteger("ammocount" + i);
                }
            }
        }
        return ammoCount;
    }
    
    public static boolean setBulletOnMag(ItemStack ammoStack,Integer currentMagcount,int ammout) {
        if(!(ammoStack.getItem() instanceof ItemAmmo)) {
            return false;
        }
        if(!ammoStack.hasTagCompound()) {
            return false;
        }
        ItemAmmo itemAmmo = (ItemAmmo) ammoStack.getItem();
        AmmoType ammoType = itemAmmo.type;
        NBTTagCompound nbtTagCompound = ammoStack.getTagCompound();
        int ammoCount = 0;
        if (!nbtTagCompound.hasKey("magcount")) {
            ammoStack.getTagCompound().setInteger("ammocount",ammout);
        } else {
            if (currentMagcount != null) {
                ammoStack.getTagCompound().setInteger("ammocount" + currentMagcount.intValue(), ammout);
            } else {
                int mag = ammoStack.getTagCompound().getInteger("magcount");
                ammoStack.getTagCompound().setInteger("ammocount" + mag, ammout);
            }
        }
        return true;
    }
    
    public static boolean isSameTypeAmmo(ItemStack stackA,ItemStack stackB) {
        Item itemA=stackA.getItem();
        Item itemB=stackB.getItem();
        if(!(itemA instanceof BaseItem)) {
            return false;
        }
        if(!itemA.equals(itemB)) {
            return false;
        }
        String ammoTypeA = ((BaseItem) itemA).baseType.internalName;
        String ammoTypeB = ((BaseItem) itemB).baseType.internalName;
        if(stackA.getItemDamage()!=stackB.getItemDamage()) {
            return false;
        }
        return ammoTypeA.equals(ammoTypeB);
    }
    
    public static boolean checkUnloadAmmo(EntityPlayerMP entityPlayer, ItemStack gunStack) {
        if (ItemGun.hasAmmoLoaded(gunStack)) {
            return true;
        }
        return false;
    }
    
    public static Integer checkUnloadBullets(EntityPlayerMP entityPlayer, ItemStack targetStack) {
        NBTTagCompound nbtTagCompound = targetStack.getTagCompound();
        //boolean isAmmo = targetStack.getItem() instanceof ItemAmmo;
    
        if (nbtTagCompound.hasKey("bullet")) {
            ItemStack returningBullet = new ItemStack(nbtTagCompound.getCompoundTag("bullet"));
            int bulletsToUnload = 0;
    
            if (!nbtTagCompound.hasKey("magcount")) {
                bulletsToUnload = targetStack.getTagCompound().getInteger("ammocount");
            } else {
                AmmoType ammoType = ((ItemAmmo) targetStack.getItem()).type;
                for (int i = 1; i <= ammoType.magazineCount + 1; i++) {
                    bulletsToUnload += nbtTagCompound.getInteger("ammocount" + i);
                }
            }
            return bulletsToUnload;
        }
        return null;
    }
    
    public static boolean unloadAmmo(EntityPlayerMP entityPlayer, ItemStack gunStack) {
        NBTTagCompound nbtTagCompound = gunStack.getTagCompound();
        if (ItemGun.hasAmmoLoaded(gunStack)) {
            ItemStack returningAmmo = new ItemStack(nbtTagCompound.getCompoundTag("ammo"));
            ItemAmmo returningAmmoItem = (ItemAmmo) returningAmmo.getItem();
            if (returningAmmoItem.type.subAmmo != null || ItemAmmo.hasAmmo(returningAmmo) || returningAmmoItem.type.allowEmptyMagazines) {
                int currentAmmoCount = ItemGun.getMagazineBullets(gunStack);
                returningAmmo.setItemDamage(returningAmmo.getMaxDamage() - currentAmmoCount);
                if(!entityPlayer.inventory.addItemStackToInventory(returningAmmo)) {
                    entityPlayer.dropItem(returningAmmo, false);
                }

            }
            nbtTagCompound.removeTag("ammo");
            return true;
        }
        return false;
    }

    public static Integer unloadBullets(EntityPlayerMP entityPlayer, ItemStack targetStack) {
        return unloadBullets(entityPlayer, targetStack, null);
    }
    
    public static Integer unloadBullets(EntityPlayerMP entityPlayer, ItemStack targetStack,Integer expectBulletsToUnload) {
        NBTTagCompound nbtTagCompound = targetStack.getTagCompound();
        //boolean isAmmo = targetStack.getItem() instanceof ItemAmmo;
    
        if (nbtTagCompound.hasKey("bullet")) {
            ItemStack returningBullet = new ItemStack(nbtTagCompound.getCompoundTag("bullet"));
            int bulletsToUnload = 0;
            int bulletsReturnCount=0;
            boolean removeFlag=true;
    
            
            if (expectBulletsToUnload != null) {
                bulletsToUnload = expectBulletsToUnload;
                bulletsReturnCount = bulletsToUnload;
                if (!nbtTagCompound.hasKey("magcount")) {
                    int count = nbtTagCompound.getInteger("ammocount") - expectBulletsToUnload;
                    if (count < 0) {
                        bulletsReturnCount+=count;
                        count = 0;
                    }
                    nbtTagCompound.setInteger("ammocount", count);
                    if(count>0) {
                        removeFlag=false;
                    }
                } else {
                    int maxCount = expectBulletsToUnload;
                    AmmoType ammoType = ((ItemAmmo) targetStack.getItem()).type;
                    for (int i = 1; i < ammoType.magazineCount + 1 && maxCount > 0; i++) {
                        int count = nbtTagCompound.getInteger("ammocount" + i);
                        if (maxCount >= count) {
                            nbtTagCompound.setInteger("ammocount" + i, 0);
                            maxCount -= count;
                        } else {
                            nbtTagCompound.setInteger("ammocount" + i, count - maxCount);
                            maxCount = 0;
                            removeFlag=false;
                        }
                    }
                    bulletsReturnCount -= maxCount;
                }
            } else {
                if (!nbtTagCompound.hasKey("magcount")) {
                    bulletsReturnCount = targetStack.getTagCompound().getInteger("ammocount");
                    nbtTagCompound.setInteger("ammocount", 0);
                } else {
                    AmmoType ammoType = ((ItemAmmo) targetStack.getItem()).type;
                    for (int i = 1; i < ammoType.magazineCount + 1; i++) {
                        bulletsReturnCount += nbtTagCompound.getInteger("ammocount" + i);
                        nbtTagCompound.setInteger("ammocount" + i, 0);
                    }
                }
            }
    
            int animBulletsToReload = bulletsReturnCount;
            while (bulletsReturnCount > 0) {
                if (bulletsReturnCount <= 64) {
                    ItemStack clonedBullet = returningBullet.copy();
                    clonedBullet.setCount(bulletsReturnCount);
                    entityPlayer.inventory.addItemStackToInventory(clonedBullet);
                    bulletsReturnCount -= bulletsReturnCount;
                } else {
                    ItemStack clonedBullet = returningBullet.copy();
                    clonedBullet.setCount(64);
                    entityPlayer.inventory.addItemStackToInventory(clonedBullet);
                    bulletsReturnCount -= 64;
                }
            }
            if(removeFlag) {
                nbtTagCompound.removeTag("bullet");  
                nbtTagCompound.removeTag("ammo");  
            }
            return animBulletsToReload;
        }
        return null;
    }

    public static int inventoryItemCount(EntityPlayer player,ItemStack stack) {
        VarInt count=new VarInt();
        Consumer<ItemStack> consumer = (s) -> {
            if (ItemStack.areItemsEqual(s,stack)&&ItemStack.areItemStackTagsEqual(s, stack)) {
                count.i += s.getCount();
            }
        };
        player.inventory.offHandInventory.forEach(consumer);
        player.inventory.mainInventory.forEach(consumer);
        player.inventory.armorInventory.forEach(consumer);
        return count.i;
    }

    public static boolean removeItemstack(EntityPlayer player, ItemStack stack, int count) {
        int maxCount = inventoryItemCount(player, stack);
        VarInt varCount = new VarInt();
        varCount.i = count;
        if (varCount.i > maxCount) {
            return false;
        }
        Consumer<ItemStack> consumer = (s) -> {
            if (varCount.i > 0) {
                if (ItemStack.areItemsEqual(s, stack)&&ItemStack.areItemStackTagsEqual(s, stack)) {
                    if (varCount.i >= s.getCount()) {
                        varCount.i -= s.getCount();
                        s.setCount(0);
                    } else {
                        s.setCount(s.getCount() - varCount.i);
                        varCount.i = 0;
                    }
                }

            }
        };
        player.inventory.offHandInventory.forEach(consumer);
        player.inventory.mainInventory.forEach(consumer);
        player.inventory.armorInventory.forEach(consumer);
        return true;
    }

}
