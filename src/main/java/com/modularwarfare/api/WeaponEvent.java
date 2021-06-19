package com.modularwarfare.api;

import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class WeaponEvent extends Event {

    private final EntityPlayer entityPlayer;
    private final ItemStack stackWeapon;
    private final ItemGun itemWeapon;

    public WeaponEvent(EntityPlayer entityPlayer, ItemStack stackWeapon, ItemGun itemWeapon) {
        this.entityPlayer = entityPlayer;
        this.stackWeapon = stackWeapon;
        this.itemWeapon = itemWeapon;
    }

    public EntityPlayer getWeaponUser() {
        return entityPlayer;
    }

    public ItemStack getWeaponStack() {
        return stackWeapon;
    }

    public ItemGun getWeaponItem() {
        return itemWeapon;
    }

}
