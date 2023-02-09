package com.modularwarfare.api;

import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.List;

@Event.HasResult
@Deprecated
public class WeaponFireEvent extends WeaponEvent {

    public WeaponFireEvent(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon) {
        super(entityPlayer, stackWeapon, itemWeapon);
    }

    /**
     * WeaponFireEvent.PreClient is fired before the weapon actually fires. Canceling this event will stop the weapon firing.<br>
     * <br>
     * This event is {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    @Cancelable
    public static class PreClient extends WeaponFireEvent {
        private int weaponRange;

        public PreClient(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, int weaponRange) {
            super(entityPlayer, stackWeapon, itemWeapon);
            this.weaponRange = weaponRange;
        }

        public int getWeaponRange() {
            return weaponRange;
        }

        public void setWeaponRange(int updatedRange) {
            this.weaponRange = updatedRange;
        }
    }

    /**
     * WeaponFireEvent.PreServer is fired before the weapon actually fires. Canceling this event will stop the weapon firing.<br>
     * <br>
     * This event is {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    @Cancelable
    public static class PreServer extends WeaponFireEvent {
        private int weaponRange;

        public PreServer(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, int weaponRange) {
            super(entityPlayer, stackWeapon, itemWeapon);
            this.weaponRange = weaponRange;
        }

        public int getWeaponRange() {
            return weaponRange;
        }

        public void setWeaponRange(int updatedRange) {
            this.weaponRange = updatedRange;
        }
    }

    /**
     * WeaponFireEvent.Post is fired once the weapon has fired with a list of affected objects. These lists can be modified to change the outcome.<br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    public static class Post extends WeaponFireEvent {
        private List<Entity> affectedEntities;
        private int fireTickDelay;
        private float damage;

        public Post(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, List<Entity> affectedEntities) {
            super(entityPlayer, stackWeapon, itemWeapon);
            this.affectedEntities = affectedEntities;

            GunType type = itemWeapon.type;

            damage = type.gunDamage;

            fireTickDelay = type.fireTickDelay;
        }

        public List<Entity> getAffectedEntities() {
            return affectedEntities;
        }

        public void setAffectedEntities(List<Entity> updatedList) {
            this.affectedEntities = updatedList;
        }

        public float getDamage() {
            return damage;
        }

        public void setDamage(float updatedDamage) {
            this.damage = updatedDamage;
        }

        public float getTickDelay() {
            return fireTickDelay;
        }
    }

}
