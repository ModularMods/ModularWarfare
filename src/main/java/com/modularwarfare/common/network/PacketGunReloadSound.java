package com.modularwarfare.common.network;

import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.common.handler.ServerTickHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketGunReloadSound extends PacketBase {

    public WeaponSoundType soundType;

    public PacketGunReloadSound() {
    }

    public PacketGunReloadSound(WeaponSoundType soundType) {
        this.soundType = soundType;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        ByteBufUtils.writeUTF8String(data, soundType.eventName);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        soundType = WeaponSoundType.fromString(ByteBufUtils.readUTF8String(data));
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        if (entityPlayer.getHeldItemMainhand() != null) {
            if (entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
                ItemStack gunStack = entityPlayer.getHeldItemMainhand();
                ItemGun itemGun = (ItemGun) entityPlayer.getHeldItemMainhand().getItem();
                GunType gunType = itemGun.type;
                InventoryPlayer inventory = entityPlayer.inventory;

                if (!ServerTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID())) {
                    //return;
                }

                if (soundType == null)
                    return;
                //System.out.println(soundType);
                gunType.playSound(entityPlayer, soundType, gunStack);
            }
        }
    }


    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {
        // UNUSED
    }

}