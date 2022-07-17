package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.WeaponReloadEvent;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.handler.data.DataGunReloadEnhancedTask;
import com.modularwarfare.utility.ReloadHelper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;

public class PacketGunReload extends PacketBase {
    private static final ItemStack UNLOAD_EMPTY=new ItemStack(Blocks.DIRT, 1);

    public boolean unload = false;

    public PacketGunReload() {
    }

    public PacketGunReload(boolean unload) {
        this.unload = unload;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeBoolean(unload);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        unload = data.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        if (entityPlayer.getHeldItemMainhand() != null) {
            if (entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
                ItemStack gunStack = entityPlayer.getHeldItemMainhand();
                ItemGun itemGun = (ItemGun) entityPlayer.getHeldItemMainhand().getItem();
                GunType gunType = itemGun.type;
                InventoryPlayer inventory = entityPlayer.inventory;
                
                if(gunType.animationType==WeaponAnimationType.ENHANCED) {
                    if (gunType.acceptedAmmo != null) {
                        handleMagGunReloadEnhanced(entityPlayer, gunStack, itemGun, gunType, inventory);
                    } else {
                        handleBulletGunReloadEnhanced(entityPlayer, gunStack, itemGun, gunType, inventory);  
                    }
                }else {
                    if (gunType.acceptedAmmo != null) {
                        handleMagGunReload(entityPlayer, gunStack, itemGun, gunType, inventory);
                    } else {
                        handleBulletGunReload(entityPlayer, gunStack, itemGun, gunType, inventory);  
                    }
                }
            } else if (entityPlayer.getHeldItemMainhand().getItem() instanceof ItemAmmo) {
                handleAmmoReload(entityPlayer);
            }
        }
    }

    public void handleAmmoReload(EntityPlayerMP entityPlayer) {
        ItemStack ammoStack = entityPlayer.getHeldItemMainhand();
        ItemAmmo itemAmmo = (ItemAmmo) entityPlayer.getHeldItemMainhand().getItem();
        AmmoType ammoType = itemAmmo.type;
        InventoryPlayer inventory = entityPlayer.inventory;

        if (ServerTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID()))
            return;

        if(entityPlayer.getHeldItemMainhand().getCount() > 1){
            entityPlayer.sendMessage(new TextComponentString("You can only load bullets on a single magazine."));
            return;
        }

        if (ammoType.subAmmo != null) {
            if (!unload) {
                NBTTagCompound nbtTagCompound = ammoStack.getTagCompound();
                boolean offhandedReload = false;
                int highestBulletCount = 0;
                ItemStack bulletStackToLoad = null;
                Integer bulletStackSlotToLoad = null;

                /** Offhand Reload */
                if (inventory.offHandInventory.get(0) != ItemStack.EMPTY) {
                    ItemStack itemStack = inventory.offHandInventory.get(0);
                    if (itemStack != null && itemStack.getItem() instanceof ItemBullet) {
                        ItemBullet itemBullet = (ItemBullet) itemStack.getItem();
                        for (String ammoName : ammoType.subAmmo) {
                            if (ammoName.equalsIgnoreCase(itemBullet.baseType.internalName)) {
                                offhandedReload = true;
                                bulletStackToLoad = itemStack;
                                break;
                            }
                        }
                    }
                }

                /** Search for bullets */
                if (!offhandedReload) {
                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                        ItemStack itemStack = inventory.getStackInSlot(i);
                        if (itemStack != null && itemStack.getItem() instanceof ItemBullet) {
                            ItemBullet itemBullet = (ItemBullet) itemStack.getItem();
                            for (String bulletName : ammoType.subAmmo) {
                                if (bulletName.equalsIgnoreCase(itemBullet.baseType.internalName)) {
                                    int count = itemStack.getCount();
                                    if (count > highestBulletCount) {
                                        bulletStackToLoad = itemStack;
                                        highestBulletCount = count;
                                        bulletStackSlotToLoad = i;
                                    }
                                }
                            }
                        }
                    }
                }

                /** End of search, start to reload */
                if (bulletStackToLoad == null)
                    return;

                ItemBullet bulletItemToLoad = (ItemBullet) bulletStackToLoad.getItem();

                if (nbtTagCompound.hasKey("bullet")) {
                    ItemStack currentBullet = new ItemStack(nbtTagCompound.getCompoundTag("bullet"));
                    if (!ReloadHelper.isSameTypeAmmo(currentBullet, bulletStackToLoad))
                        ReloadHelper.unloadBullets(entityPlayer, ammoStack);
                }

                ItemStack loadingItemStack = bulletStackToLoad.copy();
                int reserve = bulletStackToLoad.getCount();

                if (!nbtTagCompound.hasKey("magcount")) {
                    int ammoCount = ammoStack.getTagCompound().getInteger("ammocount");
                    int amountToLoad = ammoType.ammoCapacity - ammoCount;
                    int loadingCount;
                    if (amountToLoad >= loadingItemStack.getCount()) {
                        loadingCount = loadingItemStack.getCount();
                        reserve = 0;
                    } else {
                        loadingCount = amountToLoad;
                        reserve = loadingItemStack.getCount() - loadingCount;
                    }
                    ammoStack.getTagCompound().setInteger("ammocount", ammoCount + loadingCount);
                } else {
                    for (int i = 1; i < ammoType.magazineCount + 1; i++) {
                        int ammoCount = ammoStack.getTagCompound().getInteger("ammocount" + i);
                        if (ammoCount < ammoType.ammoCapacity) {
                            int amountToLoad = ammoType.ammoCapacity - ammoCount;
                            int loadingCount;
                            if (amountToLoad >= reserve) {
                                loadingCount = reserve;
                                reserve = 0;
                            } else {
                                loadingCount = amountToLoad;
                                reserve = reserve - loadingCount;
                            }
                            ammoStack.getTagCompound().setInteger("ammocount" + i, ammoCount + loadingCount);
                            if (reserve == 0) {
                                break;
                            }
                        }
                    }
                }
                loadingItemStack.setCount(1);
                loadingItemStack.setItemDamage(0);
                ammoStack.getTagCompound().setTag("bullet", loadingItemStack.writeToNBT(new NBTTagCompound()));
                bulletStackToLoad.setCount(reserve);

                if (!entityPlayer.capabilities.isCreativeMode) {
                    if (offhandedReload)
                        inventory.offHandInventory.set(0, reserve >= 1 ? bulletStackToLoad : ItemStack.EMPTY);
                    else
                        inventory.setInventorySlotContents(bulletStackSlotToLoad, reserve >= 1 ? bulletStackToLoad : ItemStack.EMPTY);
                }
            } else {
                ReloadHelper.unloadBullets(entityPlayer, ammoStack);
            }

            String key = itemAmmo.type.magazineCount > 1 ? "ammocount" + ammoStack.getTagCompound().getInteger("magcount") : "ammocount";
            int ammoCount = ammoStack.getTagCompound().getInteger(key);
            ammoStack.setItemDamage(ammoStack.getMaxDamage() - ammoCount);
        }
    }

    public void handleBulletGunReload(EntityPlayerMP entityPlayer, ItemStack gunStack, ItemGun itemGun, GunType gunType, InventoryPlayer inventory) {
        if (ServerTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID()))
            return;

        if (gunType.acceptedBullets != null) {
            if (!unload) {
                if (gunStack.getTagCompound() != null) {
                    if (gunType.internalAmmoStorage != null) {
                        if (gunStack.getTagCompound().getCompoundTag("bullet") != null) {
                            if (gunStack.getTagCompound().getInteger("ammocount") >= gunType.internalAmmoStorage) {
                                return;
                            }
                        }
                    }
                }

                NBTTagCompound nbtTagCompound = gunStack.getTagCompound();
                boolean offhandedReload = false;
                int highestBulletCount = 0;
                ItemStack bulletStackToLoad = null;
                Integer bulletStackSlotToLoad = null;

                /** Offhand Reload */
                if (inventory.offHandInventory.get(0) != ItemStack.EMPTY) {
                    ItemStack itemStack = inventory.offHandInventory.get(0);
                    if (itemStack != null && itemStack.getItem() instanceof ItemBullet) {
                        ItemBullet itemBullet = (ItemBullet) itemStack.getItem();
                        for (String bulletName : gunType.acceptedBullets) {
                            if (bulletName.equalsIgnoreCase(itemBullet.baseType.internalName)) {
                                offhandedReload = true;
                                bulletStackToLoad = itemStack;
                                break;
                            }
                        }
                    }
                }

                /** Search for bullets */
                if (!offhandedReload) {
                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                        ItemStack itemStack = inventory.getStackInSlot(i);
                        if (itemStack != null && itemStack.getItem() instanceof ItemBullet) {
                            ItemBullet itemBullet = (ItemBullet) itemStack.getItem();
                            for (String bulletName : gunType.acceptedBullets) {
                                if (bulletName.equalsIgnoreCase(itemBullet.baseType.internalName)) {
                                    int count = itemStack.getCount();
                                    if (count > highestBulletCount) {
                                        bulletStackToLoad = itemStack;
                                        highestBulletCount = count;
                                        bulletStackSlotToLoad = i;
                                    }
                                }
                            }
                        }
                    }
                }

                /** End of search, start to reload */
                if (bulletStackToLoad == null)
                    return;

                boolean loadOnly = false;
                WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, offhandedReload, false);
                MinecraftForge.EVENT_BUS.post(preReloadEvent);
                if (preReloadEvent.isCanceled())
                    return;

                ItemBullet bulletItemToLoad = (ItemBullet) bulletStackToLoad.getItem();

                if (nbtTagCompound.hasKey("bullet")) {
                    ItemStack currentBullet = new ItemStack(nbtTagCompound.getCompoundTag("bullet"));
                    if (!ReloadHelper.isSameTypeAmmo(currentBullet, bulletStackToLoad))
                        ReloadHelper.unloadBullets(entityPlayer, gunStack);
                } else {
                    loadOnly = true;
                }

                ItemStack loadingItemStack = bulletStackToLoad.copy();
                int reserve = bulletStackToLoad.getCount();
                int ammoCount = gunStack.getTagCompound().getInteger("ammocount");
                int amountToLoad = gunType.internalAmmoStorage - ammoCount;
                int loadingCount;
                if (amountToLoad >= loadingItemStack.getCount()) {
                    loadingCount = loadingItemStack.getCount();
                    reserve = 0;
                } else {
                    loadingCount = amountToLoad;
                    reserve = loadingItemStack.getCount() - loadingCount;
                }
                gunStack.getTagCompound().setInteger("ammocount", ammoCount + loadingCount);
                gunStack.getTagCompound().setTag("bullet", loadingItemStack.writeToNBT(new NBTTagCompound()));
                bulletStackToLoad.setCount(reserve);

                if (!entityPlayer.capabilities.isCreativeMode) {
                    if (offhandedReload)
                        inventory.offHandInventory.set(0, reserve >= 1 ? bulletStackToLoad : ItemStack.EMPTY);
                    else
                        inventory.setInventorySlotContents(bulletStackSlotToLoad, reserve >= 1 ? bulletStackToLoad : ItemStack.EMPTY);
                }

                WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, offhandedReload, loadOnly, false, preReloadEvent.getReloadTime(), loadingCount);
                MinecraftForge.EVENT_BUS.post(postReloadEvent);
                ServerTickHandler.playerReloadCooldown.put(entityPlayer.getUniqueID(), preReloadEvent.getReloadTime());
                int reloadType = (postReloadEvent.isLoadOnly() ? ReloadType.Load : (postReloadEvent.isUnload() ? ReloadType.Unload : ReloadType.Full)).i;
                ModularWarfare.NETWORK.sendTo(new PacketClientAnimation(gunType.internalName, postReloadEvent.getReloadTime(), postReloadEvent.getReloadCount(), reloadType), entityPlayer);

				/*
				if(gunType.weaponType == WeaponType.Revolver) {
					Scheduler.server().schedule(new Runnable() {
						@Override
						public void run() {
							for (int i = 0; i < amountToLoad; i++) {
								EntityShell shell = new EntityShell(entityPlayer.world, entityPlayer, itemGun, (ItemBullet) loadingItemStack.getItem());

								shell.setHeadingFromThrower(entityPlayer, entityPlayer.rotationPitch, entityPlayer.rotationYaw, 1.0F, 0.1F, 15);
								entityPlayer.world.spawnEntity(shell);
							}
						}
					}, 30L);
				}
				*/

            } else {
                WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, false, false);
                MinecraftForge.EVENT_BUS.post(preReloadEvent);
                if (preReloadEvent.isCanceled())
                    return;

                Integer bulletCount = ReloadHelper.unloadBullets(entityPlayer, gunStack);
                if (bulletCount != null) {
                    WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, false, false, true, preReloadEvent.getReloadTime(), bulletCount);
                    MinecraftForge.EVENT_BUS.post(postReloadEvent);

                    if (postReloadEvent.isUnload())
                        gunType.playSound(entityPlayer, WeaponSoundType.Unload, gunStack);
                }
            }
        }
    }

    public void handleMagGunReload(EntityPlayerMP entityPlayer, ItemStack gunStack, ItemGun itemGun, GunType gunType, InventoryPlayer inventory) {
        if (ServerTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID()))
            return;

        if (!unload) {
            NBTTagCompound nbtTagCompound = gunStack.getTagCompound();
            boolean hasAmmoLoaded = ItemGun.hasAmmoLoaded(gunStack);
            boolean offhandedReload = false;
            ItemStack ammoStackToLoad = null;
            Integer ammoStackSlotToLoad = null;
            Integer highestAmmoCount = 0;
            Integer multiMagToLoad = null;

            /** Offhand Reload */
            if (inventory.offHandInventory.get(0) != ItemStack.EMPTY) {
                ItemStack itemStack = inventory.offHandInventory.get(0);
                if (itemStack != null && itemStack.getItem() instanceof ItemAmmo) {
                    ItemAmmo itemAmmo = (ItemAmmo) itemStack.getItem();
                    for (String ammoName : gunType.acceptedAmmo) {
                        if (ammoName.equalsIgnoreCase(itemAmmo.baseType.internalName)) {
                            offhandedReload = true;
                            ammoStackToLoad = itemStack;
                            break;
                        }
                    }
                }
            }

            /** Compounded Magazines */
            if (hasAmmoLoaded) {
                ItemStack currentAmmoStack = new ItemStack(nbtTagCompound.getCompoundTag("ammo"));
                ItemAmmo currentAmmoItem = (ItemAmmo) currentAmmoStack.getItem();
                NBTTagCompound currentAmmoTag = currentAmmoStack.getTagCompound();

                if (currentAmmoTag.hasKey("magcount")) {
                    Integer selectedMagazine = null;
                    int highestAmmo = -1;

                    for (int j = 1; j < currentAmmoItem.type.magazineCount + 1; j++) {
                        int ammoCount = currentAmmoTag.getInteger("ammocount" + j);
                        if (ammoCount > highestAmmo) {
                            selectedMagazine = j;
                            highestAmmo = ammoCount;
                        }
                    }

                    if (selectedMagazine != null) {
                        currentAmmoTag.setInteger("magcount", selectedMagazine);
                        ammoStackToLoad = currentAmmoStack;
                        highestAmmoCount = highestAmmo;
                    }
                }
            }

            /** Scan inventory  */
            if (!offhandedReload) {
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack itemStack = inventory.getStackInSlot(i);
                    if (itemStack != null && itemStack.getItem() instanceof ItemAmmo) {
                        ItemAmmo itemAmmo = (ItemAmmo) itemStack.getItem();
                        for (String ammoName : gunType.acceptedAmmo) {
                            if (ammoName.equalsIgnoreCase(itemAmmo.baseType.internalName)) {
                                if (itemAmmo.type.magazineCount > 1) {
                                    for (int j = 1; j < itemAmmo.type.magazineCount + 1; j++) {
                                        int ammoCount = itemStack.getTagCompound().getInteger("ammocount" + j);
                                        if (ammoCount > highestAmmoCount) {
                                            ammoStackToLoad = itemStack;
                                            ammoStackSlotToLoad = i;
                                            highestAmmoCount = ammoCount;
                                            multiMagToLoad = j;
                                        }
                                    }
                                } else {
                                    int ammoCount = itemStack.getTagCompound().getInteger("ammocount");
                                    if (ammoCount > highestAmmoCount) {
                                        ammoStackToLoad = itemStack;
                                        highestAmmoCount = ammoCount;
                                        ammoStackSlotToLoad = i;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /** End of search, start to reload */
            if (ammoStackToLoad == null)
                return;

            /** If not a multi mag ammo set to null if one was spotted */
            if (!ammoStackToLoad.getTagCompound().hasKey("magcount"))
                multiMagToLoad = null;

            /** Weapon Pre-Reload event */
            boolean multiMagReload = hasAmmoLoaded && ammoStackToLoad.getTagCompound().hasKey("magcount");
            boolean loadOnly = false;
            WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, offhandedReload, multiMagReload);
            MinecraftForge.EVENT_BUS.post(preReloadEvent);
            if (preReloadEvent.isCanceled())
                return;

            if (gunStack.getTagCompound().hasKey("shotsremaining") && gunStack.getTagCompound().getInteger("shotsremaining") > 0) {
                gunStack.getTagCompound().setInteger("shotsremaining", 0);
            }

            /** Unload old ammo stack */
            if (!multiMagReload || multiMagToLoad != null) {
                if (ItemGun.hasAmmoLoaded(gunStack)) {
                    ReloadHelper.unloadAmmo(entityPlayer, gunStack);
                } else {
                    loadOnly = true;
                }
            }

            /** Loading of new ammo stack */
            ItemStack loadingItemStack = ammoStackToLoad.copy();
            loadingItemStack.setCount(1);

            if (multiMagReload && multiMagToLoad != null)
                loadingItemStack.getTagCompound().setInteger("magcount", multiMagToLoad);

            nbtTagCompound.setTag("ammo", loadingItemStack.writeToNBT(new NBTTagCompound()));

            ammoStackToLoad.setCount(ammoStackToLoad.getCount() - 1);

            if (ammoStackSlotToLoad != null) {
                if (!entityPlayer.capabilities.isCreativeMode) {
                    if (offhandedReload)
                        inventory.offHandInventory.set(0, ammoStackToLoad.getCount() >= 1 ? ammoStackToLoad : ItemStack.EMPTY);
                    else
                        inventory.setInventorySlotContents(ammoStackSlotToLoad, ammoStackToLoad.getCount() >= 1 ? ammoStackToLoad : ItemStack.EMPTY);
                }
            }

            int reloadTime = preReloadEvent.getReloadTime();

            if (loadingItemStack.getItem() instanceof ItemAmmo) {
                ItemAmmo itemAmmo = (ItemAmmo) loadingItemStack.getItem();
                reloadTime *= itemAmmo.type.reloadTimeFactor;
            }

            /** Post Reload */
            WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, offhandedReload, multiMagReload, loadOnly, false, reloadTime);
            MinecraftForge.EVENT_BUS.post(postReloadEvent);

            if (postReloadEvent.isLoadOnly()) {
                gunType.playSound(entityPlayer, WeaponSoundType.Load, gunStack);
            } else if (!postReloadEvent.isLoadOnly() && !postReloadEvent.isUnload()) {
                gunType.playSound(entityPlayer, WeaponSoundType.Reload, gunStack);
            }
            int reloadType = (postReloadEvent.isLoadOnly() ? ReloadType.Load : postReloadEvent.isUnload() ? ReloadType.Unload : ReloadType.Full).i;
            ModularWarfare.NETWORK.sendTo(new PacketClientAnimation(gunType.internalName, reloadTime, postReloadEvent.getReloadCount(), reloadType), entityPlayer);
            ServerTickHandler.playerReloadCooldown.put(entityPlayer.getUniqueID(), reloadTime);
        } else {

            WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, false, false);
            MinecraftForge.EVENT_BUS.post(preReloadEvent);
            if (preReloadEvent.isCanceled())
                return;

            if (ReloadHelper.unloadAmmo(entityPlayer, gunStack)) {
                WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, false, false, false, true, preReloadEvent.getReloadTime());
                MinecraftForge.EVENT_BUS.post(postReloadEvent);

                if (postReloadEvent.isUnload()) {
                    gunType.playSound(entityPlayer, WeaponSoundType.Unload, gunStack);
                }
            }
        }

    }

    public void handleBulletGunReloadEnhanced(EntityPlayerMP entityPlayer, ItemStack gunStack, ItemGun itemGun, GunType gunType, InventoryPlayer inventory) {
        if (ServerTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID()))
            return;

        if (gunType.acceptedBullets != null) {
            if (!unload) {
                ItemStack bulletStackToLoad = null;
                Integer bulletStackSlotToLoad = null;
                
                if (gunStack.getTagCompound() != null) {
                    if (gunType.internalAmmoStorage != null) {
                        if (gunStack.getTagCompound().getCompoundTag("bullet") != null) {
                            
                            if(gunStack.getTagCompound().getInteger("ammocount")<=0) {
                                ReloadHelper.unloadBullets(entityPlayer, gunStack);
                            }
                            
                            bulletStackToLoad=new ItemStack(gunStack.getTagCompound().getCompoundTag("bullet"));
                            
                            if (gunStack.getTagCompound().getInteger("ammocount") >= gunType.internalAmmoStorage) {
                                return;
                            }
                        }
                    }
                }

                NBTTagCompound nbtTagCompound = gunStack.getTagCompound();
                boolean offhandedReload = false;
                int highestBulletCount = 0;
                
                if(bulletStackToLoad==null||bulletStackToLoad.isEmpty()) {
                    /** Offhand Reload */
                    
                    if (inventory.offHandInventory.get(0) != ItemStack.EMPTY) {
                        ItemStack itemStack = inventory.offHandInventory.get(0);
                        if (itemStack != null && itemStack.getItem() instanceof ItemBullet) {
                            ItemBullet itemBullet = (ItemBullet) itemStack.getItem();
                            for (String bulletName : gunType.acceptedBullets) {
                                if (bulletName.equalsIgnoreCase(itemBullet.baseType.internalName)) {
                                    offhandedReload = true;
                                    bulletStackToLoad = itemStack;
                                    break;
                                }
                            }
                        }
                    }
                    

                    /** Search for bullets */
                    
                    if (!offhandedReload) {
                        for (int i = 0; i < inventory.getSizeInventory(); i++) {
                            ItemStack itemStack = inventory.getStackInSlot(i);
                            if (itemStack != null && itemStack.getItem() instanceof ItemBullet) {
                                ItemBullet itemBullet = (ItemBullet) itemStack.getItem();
                                for (String bulletName : gunType.acceptedBullets) {
                                    if (bulletName.equalsIgnoreCase(itemBullet.baseType.internalName)) {
                                        int count = itemStack.getCount();
                                        if (count > highestBulletCount) {
                                            bulletStackToLoad = itemStack;
                                            highestBulletCount = count;
                                            bulletStackSlotToLoad = i;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                

                /** End of search, start to reload */
                if (bulletStackToLoad == null)
                    return;
                
                ItemStack loadingItemStack = bulletStackToLoad.copy();
                loadingItemStack.setCount(1);
                int loadingCount=ReloadHelper.inventoryItemCount(entityPlayer, loadingItemStack);
                if(loadingCount<=0) {
                    return;
                }

                boolean loadOnly = false;
                WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, offhandedReload, false);
                MinecraftForge.EVENT_BUS.post(preReloadEvent);
                if (preReloadEvent.isCanceled())
                    return;

                ItemBullet bulletItemToLoad = (ItemBullet) bulletStackToLoad.getItem();

                /*
                if (nbtTagCompound.hasKey("bullet")) {
                    ItemStack currentBullet = new ItemStack(nbtTagCompound.getCompoundTag("bullet"));
                    ItemBullet currentBulletItem = (ItemBullet) currentBullet.getItem();
                    if (!currentBulletItem.baseType.internalName.equalsIgnoreCase(bulletItemToLoad.baseType.internalName))
                        unloadBullets(entityPlayer, gunStack);
                } else {
                    loadOnly = true;
                }*/
                if (!nbtTagCompound.hasKey("bullet")) {
                    loadOnly = true;
                }

                
                int ammoCount = gunStack.getTagCompound().getInteger("ammocount");
                int amountToLoad = gunType.internalAmmoStorage - ammoCount;
                if(loadingCount>amountToLoad) {
                    loadingCount=amountToLoad;
                }
                
                /*
                int reserve = bulletStackToLoad.getCount();
                int ammoCount = gunStack.getTagCompound().getInteger("ammocount");
                int amountToLoad = gunType.internalAmmoStorage - ammoCount;
                int loadingCount;
                if (amountToLoad >= loadingItemStack.getCount()) {
                    loadingCount = loadingItemStack.getCount();
                    reserve = 0;
                } else {
                    loadingCount = amountToLoad;
                    reserve = loadingItemStack.getCount() - loadingCount;
                }
                gunStack.getTagCompound().setInteger("ammocount", ammoCount + loadingCount);
                gunStack.getTagCompound().setTag("bullet", loadingItemStack.writeToNBT(new NBTTagCompound()));
                bulletStackToLoad.setCount(reserve);

                if (!entityPlayer.capabilities.isCreativeMode) {
                    if (offhandedReload)
                        inventory.offHandInventory.set(0, reserve >= 1 ? bulletStackToLoad : ItemStack.EMPTY);
                    else
                        inventory.setInventorySlotContents(bulletStackSlotToLoad, reserve >= 1 ? bulletStackToLoad : ItemStack.EMPTY);
                }
                */
                
                WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, offhandedReload, loadOnly, false, preReloadEvent.getReloadTime(), loadingCount);
                MinecraftForge.EVENT_BUS.post(postReloadEvent);
                int reloadType = (postReloadEvent.isLoadOnly() ? ReloadType.Load : (postReloadEvent.isUnload() ? ReloadType.Unload : ReloadType.Full)).i;
                ServerTickHandler.playerReloadCooldown.put(entityPlayer.getUniqueID(), preReloadEvent.getReloadTime());
                ServerTickHandler.reloadEnhancedTask.put(entityPlayer.getUniqueID(), new DataGunReloadEnhancedTask(entityPlayer.inventory.currentItem,gunStack, loadingItemStack.copy(),postReloadEvent.getReloadCount()));
                ModularWarfare.NETWORK.sendTo(new PacketGunReloadEnhancedTask(loadingItemStack), entityPlayer);
                ModularWarfare.NETWORK.sendTo(new PacketClientAnimation(gunType.internalName, postReloadEvent.getReloadTime(), postReloadEvent.getReloadCount(), reloadType), entityPlayer);

            } else {
                WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, false, false);
                MinecraftForge.EVENT_BUS.post(preReloadEvent);
                if (preReloadEvent.isCanceled())
                    return;
                
                Integer bulletCount = ReloadHelper.checkUnloadBullets(entityPlayer, gunStack);
                if (bulletCount != null && bulletCount > 0) {
                    WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, false, false, true, preReloadEvent.getReloadTime(), bulletCount);
                    MinecraftForge.EVENT_BUS.post(postReloadEvent);

                    if (postReloadEvent.isUnload())
                        gunType.playSound(entityPlayer, WeaponSoundType.Unload, gunStack);
                    int reloadType = (postReloadEvent.isLoadOnly() ? ReloadType.Load : postReloadEvent.isUnload() ? ReloadType.Unload : ReloadType.Full).i;
                    ServerTickHandler.reloadEnhancedTask.put(entityPlayer.getUniqueID(), new DataGunReloadEnhancedTask(entityPlayer.inventory.currentItem,gunStack,true,bulletCount));
                    ModularWarfare.NETWORK.sendTo(new PacketGunReloadEnhancedTask(UNLOAD_EMPTY), entityPlayer);
                    ModularWarfare.NETWORK.sendTo(new PacketClientAnimation(gunType.internalName, 0, bulletCount, reloadType), entityPlayer);
                }else if(bulletCount!=null) {
                    handleBulletGunReload(entityPlayer, gunStack, itemGun, gunType, inventory);
                }
            }
        }
    }

    public void handleMagGunReloadEnhanced(EntityPlayerMP entityPlayer, ItemStack gunStack, ItemGun itemGun, GunType gunType, InventoryPlayer inventory) {
        if (ServerTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID()))
            return;

        if (!unload) {
            NBTTagCompound nbtTagCompound = gunStack.getTagCompound();
            boolean hasAmmoLoaded = ItemGun.hasAmmoLoaded(gunStack);
            boolean offhandedReload = false;
            ItemStack ammoStackToLoad = null;
            Integer ammoStackSlotToLoad = null;
            Integer highestAmmoCount = 0;
            Integer multiMagToLoad = null;
            boolean currentAmmo=false;
            ItemStack currentAmmoToLoad=null;
            ItemStack multiAmmoToLoad=null;

            /** Offhand Reload */
            if (inventory.offHandInventory.get(0) != ItemStack.EMPTY) {
                ItemStack itemStack = inventory.offHandInventory.get(0);
                if (itemStack != null && itemStack.getItem() instanceof ItemAmmo) {
                    ItemAmmo itemAmmo = (ItemAmmo) itemStack.getItem();
                    for (String ammoName : gunType.acceptedAmmo) {
                        if (ammoName.equalsIgnoreCase(itemAmmo.baseType.internalName)) {
                            offhandedReload = true;
                            ammoStackToLoad = itemStack;
                            break;
                        }
                    }
                }
            }

            /** Compounded Magazines */
            if (hasAmmoLoaded) {
                ItemStack currentAmmoStack = new ItemStack(nbtTagCompound.getCompoundTag("ammo")).copy();
                ItemAmmo currentAmmoItem = (ItemAmmo) currentAmmoStack.getItem();
                NBTTagCompound currentAmmoTag = currentAmmoStack.getTagCompound();

                if (currentAmmoTag.hasKey("magcount")) {
                    Integer selectedMagazine = null;
                    int highestAmmo = -1;

                    for (int j = 1; j < currentAmmoItem.type.magazineCount + 1; j++) {
                        int ammoCount = currentAmmoTag.getInteger("ammocount" + j);
                        if (ammoCount > highestAmmo) {
                            selectedMagazine = j;
                            multiMagToLoad = j;
                            highestAmmo = ammoCount;
                        }
                    }

                    if (selectedMagazine != null) {
                        currentAmmoTag.setInteger("magcount", selectedMagazine);
                        ammoStackToLoad = currentAmmoStack;
                        currentAmmoToLoad = currentAmmoStack;
                        currentAmmo=true;
                        highestAmmoCount = highestAmmo;
                    }
                }
            }

            /** Scan inventory  */
            if (!offhandedReload) {
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack itemStack = inventory.getStackInSlot(i);
                    if (itemStack != null && itemStack.getItem() instanceof ItemAmmo) {
                        ItemAmmo itemAmmo = (ItemAmmo) itemStack.getItem();
                        for (String ammoName : gunType.acceptedAmmo) {
                            if (ammoName.equalsIgnoreCase(itemAmmo.baseType.internalName)) {
                                if (itemAmmo.type.magazineCount > 1) {
                                    for (int j = 1; j < itemAmmo.type.magazineCount + 1; j++) {
                                        int ammoCount = itemStack.getTagCompound().getInteger("ammocount" + j);
                                        if (ammoCount > highestAmmoCount) {
                                            ammoStackToLoad = itemStack;
                                            multiAmmoToLoad = itemStack;
                                            ammoStackSlotToLoad = i;
                                            highestAmmoCount = ammoCount;
                                            multiMagToLoad = j;
                                        }
                                    }
                                } else {
                                    int ammoCount = itemStack.getTagCompound().getInteger("ammocount");
                                    if (ammoCount > highestAmmoCount) {
                                        ammoStackToLoad = itemStack;
                                        highestAmmoCount = ammoCount;
                                        ammoStackSlotToLoad = i;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /** End of search, start to reload */
            if (ammoStackToLoad == null)
                return;

            /** If not a multi mag ammo set to null if one was spotted */
            if (!ammoStackToLoad.getTagCompound().hasKey("magcount"))
                multiMagToLoad = null;

            /** Weapon Pre-Reload event */
            boolean multiMagReload = hasAmmoLoaded && ammoStackToLoad.getTagCompound().hasKey("magcount");
            boolean loadOnly = false;
            WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, offhandedReload, multiMagReload);
            MinecraftForge.EVENT_BUS.post(preReloadEvent);
            if (preReloadEvent.isCanceled())
                return;

            if (gunStack.getTagCompound().hasKey("shotsremaining") && gunStack.getTagCompound().getInteger("shotsremaining") > 0) {
                gunStack.getTagCompound().setInteger("shotsremaining", 0);
            }

            /** Unload old ammo stack */
            /*
            if (!multiMagReload || multiMagToLoad != null) {
                if (ItemGun.hasAmmoLoaded(gunStack)) {
                    unloadAmmo(entityPlayer, gunStack);
                } else {
                    loadOnly = true;
                }
            }
            */
            if (!multiMagReload || multiMagToLoad != null) {
                if (!ItemGun.hasAmmoLoaded(gunStack)) {
                    loadOnly = true;
                }
            }

            /** Loading of new ammo stack */
            
            ItemStack loadingItemStack = ammoStackToLoad.copy();
            loadingItemStack.setCount(1);
            /*

            if (multiMagReload && multiMagToLoad != null)
                loadingItemStack.getTagCompound().setInteger("magcount", multiMagToLoad);

            nbtTagCompound.setTag("ammo", loadingItemStack.writeToNBT(new NBTTagCompound()));

            ammoStackToLoad.setCount(ammoStackToLoad.getCount() - 1);

            if (ammoStackSlotToLoad != null) {
                if (!entityPlayer.capabilities.isCreativeMode) {
                    if (offhandedReload)
                        inventory.offHandInventory.set(0, ammoStackToLoad.getCount() >= 1 ? ammoStackToLoad : ItemStack.EMPTY);
                    else
                        inventory.setInventorySlotContents(ammoStackSlotToLoad, ammoStackToLoad.getCount() >= 1 ? ammoStackToLoad : ItemStack.EMPTY);
                }
            }
            */

            int reloadTime = preReloadEvent.getReloadTime();

            if (loadingItemStack.getItem() instanceof ItemAmmo) {
                ItemAmmo itemAmmo = (ItemAmmo) loadingItemStack.getItem();
                reloadTime *= itemAmmo.type.reloadTimeFactor;
            }

            /** Post Reload */
            WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, offhandedReload, multiMagReload, loadOnly, false, reloadTime);
            MinecraftForge.EVENT_BUS.post(postReloadEvent);
            int reloadType = (postReloadEvent.isLoadOnly() ? ReloadType.Load : postReloadEvent.isUnload() ? ReloadType.Unload : ReloadType.Full).i;
            
            if (postReloadEvent.isLoadOnly()) {
                gunType.playSound(entityPlayer, WeaponSoundType.Load, gunStack);
            } else if (!postReloadEvent.isLoadOnly() && !postReloadEvent.isUnload()) {
                gunType.playSound(entityPlayer, WeaponSoundType.Reload, gunStack);
            }
            
            ServerTickHandler.playerReloadCooldown.put(entityPlayer.getUniqueID(), reloadTime);
            ServerTickHandler.reloadEnhancedTask.put(entityPlayer.getUniqueID(), new DataGunReloadEnhancedTask(entityPlayer.inventory.currentItem,gunStack, loadingItemStack.copy(),postReloadEvent.getReloadCount(),ammoStackToLoad==currentAmmoToLoad,ammoStackToLoad==multiAmmoToLoad,multiMagToLoad));
            ModularWarfare.NETWORK.sendTo(new PacketGunReloadEnhancedTask(loadingItemStack,ammoStackToLoad==currentAmmoToLoad), entityPlayer);
            ModularWarfare.NETWORK.sendTo(new PacketClientAnimation(gunType.internalName, reloadTime, postReloadEvent.getReloadCount(), reloadType), entityPlayer);
        } else {

            WeaponReloadEvent.Pre preReloadEvent = new WeaponReloadEvent.Pre(entityPlayer, gunStack, itemGun, false, false);
            MinecraftForge.EVENT_BUS.post(preReloadEvent);
            if (preReloadEvent.isCanceled())
                return;

            if (ReloadHelper.checkUnloadAmmo(entityPlayer, gunStack)) {
                WeaponReloadEvent.Post postReloadEvent = new WeaponReloadEvent.Post(entityPlayer, gunStack, itemGun, false, false, false, true, preReloadEvent.getReloadTime());
                MinecraftForge.EVENT_BUS.post(postReloadEvent);

                if (postReloadEvent.isUnload()) {
                    gunType.playSound(entityPlayer, WeaponSoundType.Unload, gunStack);
                }
                int reloadType = (postReloadEvent.isLoadOnly() ? ReloadType.Load : postReloadEvent.isUnload() ? ReloadType.Unload : ReloadType.Full).i;
                ServerTickHandler.reloadEnhancedTask.put(entityPlayer.getUniqueID(), new DataGunReloadEnhancedTask(entityPlayer.inventory.currentItem,gunStack,true));
                ModularWarfare.NETWORK.sendTo(new PacketGunReloadEnhancedTask(UNLOAD_EMPTY), entityPlayer);
                ModularWarfare.NETWORK.sendTo(new PacketClientAnimation(gunType.internalName, 0, 1, reloadType), entityPlayer);
            }
        }

    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {
        // UNUSED
    }

}