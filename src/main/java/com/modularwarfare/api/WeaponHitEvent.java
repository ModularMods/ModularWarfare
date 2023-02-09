package com.modularwarfare.api;

import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.List;

@Deprecated
public class WeaponHitEvent extends WeaponEvent {

    public WeaponHitEvent(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon) {
        super(entityPlayer, stackWeapon, itemWeapon);
    }

    /**
     * WeaponHitEvent.Pre is fired before the weapon actually fires. Canceling this event will stop the weapon firing.<br>
     * <br>
     * This event is {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    @Cancelable
    public static class Pre extends WeaponHitEvent {
        private boolean isHeadshot;
        private float damage;
        private Entity victim;

        public Pre(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, boolean isHeadshot, float damage, Entity victim) {
            super(entityPlayer, stackWeapon, itemWeapon);
            this.isHeadshot = isHeadshot;
            this.damage = damage;

            this.victim = victim;
        }

        public float getDamage() {
            return damage;
        }

        public void setDamage(float damage) {
            this.damage = damage;
        }

        public boolean isHeadhot() {
            return this.isHeadshot;
        }

        public Entity getVictim() {
            return victim;
        }

        public void setVictim(Entity entity) {
            this.victim = victim;
        }
    }

    /**
     * WeaponFireEvent.Post is fired once the weapon has fired with a list of affected objects. These lists can be modified to change the outcome.<br>
     * <br>
     * This event is not {@link Cancelable}.<br>
     * This event does not use {@link HasResult}.<br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
     */
    public static class Post extends WeaponHitEvent {
        private List<Entity> affectedEntities;
        private float finalDamage;

        public Post(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon, List<Entity> affectedEntities, float finalDamage) {
            super(entityPlayer, stackWeapon, itemWeapon);
            this.affectedEntities = affectedEntities;
            this.finalDamage = finalDamage;
        }

        public List<Entity> getAffectedEntities() {
            return affectedEntities;
        }

        public void setAffectedEntities(List<Entity> updatedList) {
            this.affectedEntities = updatedList;
        }

        public float getFinalDamage() {
            return finalDamage;
        }

    }

}
