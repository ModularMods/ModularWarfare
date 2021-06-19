package com.modularwarfare.common.network;

import com.modularwarfare.api.AnimationUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketAimingReponse extends PacketBase {


    public String playername;
    public boolean aiming;

    public PacketAimingReponse() {
    }

    public PacketAimingReponse(String playername, boolean aiming) {
        this.playername = playername;
        this.aiming = aiming;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        ByteBufUtils.writeUTF8String(data, playername);
        data.writeBoolean(aiming);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        playername = ByteBufUtils.readUTF8String(data);
        aiming = data.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {
        if (aiming) {
            AnimationUtils.isAiming.put(playername, aiming);
        } else {
            AnimationUtils.isAiming.remove(playername);
        }
    }

}