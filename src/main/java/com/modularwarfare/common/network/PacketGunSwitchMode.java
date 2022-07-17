package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponFireMode;
import com.modularwarfare.common.network.PacketClientAnimation.AnimationType;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketGunSwitchMode extends PacketBase {

    public PacketGunSwitchMode() {
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {

    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        if (entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
            ItemGun itemGun = (ItemGun) entityPlayer.getHeldItemMainhand().getItem();
            GunType gunType = itemGun.type;
            WeaponFireMode fireMode = GunType.getFireMode(entityPlayer.getHeldItemMainhand());

            if (fireMode == null || gunType.fireModes.length <= 1)
                return;

            int spot = 0;
            int length = gunType.fireModes.length;
            for (int i = 0; i < length; i++) {
                WeaponFireMode foundFireMode = gunType.fireModes[i];
                if (foundFireMode == fireMode) {
                    spot = i;
                }
            }
            spot = spot + 1 >= length ? 0 : spot + 1;
            itemGun.onGunSwitchMode(entityPlayer, entityPlayer.world, entityPlayer.getHeldItemMainhand(), itemGun, gunType.fireModes[spot]);
        }
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {
        // UNUSED
    }

    public static void switchClient(EntityPlayer entityPlayer) {
        if (entityPlayer.getHeldItemMainhand() != null
                && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
            ItemGun itemGun = (ItemGun) entityPlayer.getHeldItemMainhand().getItem();
            GunType gunType = itemGun.type;
            WeaponFireMode fireMode = GunType.getFireMode(entityPlayer.getHeldItemMainhand());

            if (fireMode == null || gunType.fireModes.length <= 1)
                return;

            int spot = 0;
            int length = gunType.fireModes.length;
            for (int i = 0; i < length; i++) {
                WeaponFireMode foundFireMode = gunType.fireModes[i];
                if (foundFireMode == fireMode) {
                    spot = i;
                }
            }
            spot = spot + 1 >= length ? 0 : spot + 1;
            itemGun.onGunSwitchMode(entityPlayer, entityPlayer.world, entityPlayer.getHeldItemMainhand(), itemGun,
                    gunType.fireModes[spot]);
        }
    }
    
}
