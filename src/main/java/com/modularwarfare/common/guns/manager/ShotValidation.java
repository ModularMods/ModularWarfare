package com.modularwarfare.common.guns.manager;

import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponFireMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ShotValidation {

    public static boolean isValidShoot(final long clientFireTickDelay, final float recoilPitch, final float recoilYaw, final float recoilAimReducer, final float bulletSpread, GunType type) {
        return (clientFireTickDelay == type.fireTickDelay) && (type.recoilPitch == recoilPitch) && (type.recoilYaw == recoilYaw) && (type.recoilAimReducer == recoilAimReducer) && (type.bulletSpread == bulletSpread);
    }

    public static boolean verifShot(EntityPlayer entityPlayer, ItemStack gunStack, ItemGun itemGun, WeaponFireMode fireMode, final int clientFireTickDelay, final float recoilPitch, final float recoilYaw, final float recoilAimReducer, final float bulletSpread){
        GunType gunType = itemGun.type;

        // Can fire checks
        if (isValidShoot(clientFireTickDelay, recoilPitch, recoilYaw, recoilAimReducer, bulletSpread, itemGun.type)) {

            if (ItemGun.isServerReloading(entityPlayer) || (!itemGun.type.allowSprintFiring && entityPlayer.isSprinting()) || !itemGun.type.hasFireMode(fireMode))
                return false;
        }
        return true;
    }

}
