package com.modularwarfare.api;

import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public class WeaponReloadEvent extends WeaponEvent {

    public WeaponReloadEvent(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon) {
        super(entityPlayer, stackWeapon, itemWeapon);
    }

    /**
     * WeaponReloadEvent.Pre is fired before the weapon actually reloads. Canceling this event will stop the weapon reload.<br>
     * <br>
     * This event is {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    @Cancelable
    public static class Pre extends WeaponReloadEvent {
        private final boolean offhandReload;
        private final boolean multiMagReload;
        private int reloadTime;

        public Pre(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, boolean offhandReload, boolean multiMagReload) {
            super(entityPlayer, stackWeapon, itemWeapon);
            this.offhandReload = offhandReload;
            this.multiMagReload = multiMagReload;

            GunType type = itemWeapon.type;
            this.reloadTime = (int) (offhandReload ? type.offhandReloadTime != null ? (int) type.offhandReloadTime * 0.8f : type.reloadTime : type.reloadTime);
        }

        public int getReloadTime() {
            return reloadTime;
        }

        public void setReloadTime(int updatedReloadTime) {
            this.reloadTime = updatedReloadTime;
        }

        public boolean isOffhandReload() {
            return offhandReload;
        }

        public boolean isMultiMagReload() {
            return multiMagReload;
        }
    }

    /**
     * WeaponReloadEvent.Post is fired once the weapon has started reloading.<br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    public static class Post extends WeaponReloadEvent {
        private final boolean offhandReload;
        private final boolean multiMagReload;
        private final boolean loadOnly;
        private final boolean unloadOnly;
        private final int reloadAmount;
        private int reloadTime;

        public Post(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, boolean offhandReload, boolean multiMagReload, boolean loadOnly, boolean unloadOnly, int reloadTime, int reloadAmount) {
            super(entityPlayer, stackWeapon, itemWeapon);
            this.offhandReload = offhandReload;
            this.multiMagReload = multiMagReload;
            this.loadOnly = loadOnly;
            this.unloadOnly = unloadOnly;
            this.reloadTime = reloadTime;
            this.reloadAmount = reloadAmount;
        }

        public Post(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, boolean offhandReload, boolean multiMagReload, boolean loadOnly, boolean unloadOnly, int reloadTime) {
            this(entityPlayer, stackWeapon, itemWeapon, offhandReload, multiMagReload, loadOnly, unloadOnly, reloadTime, 1);
        }

        public Post(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, boolean offhandReload, boolean loadOnly, boolean unloadOnly, int reloadTime, int reloadAmount) {
            this(entityPlayer, stackWeapon, itemWeapon, offhandReload, false, loadOnly, unloadOnly, reloadTime, reloadAmount);
        }

        public boolean isOffhandReload() {
            return offhandReload;
        }

        public boolean isMultiMagReload() {
            return multiMagReload;
        }

        public boolean isLoadOnly() {
            return loadOnly;
        }

        public boolean isUnload() {
            return unloadOnly;
        }

        public int getReloadTime() {
            return reloadTime;
        }

        public int getReloadCount() {
            return reloadAmount;
        }

    }

}
