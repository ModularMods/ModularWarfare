package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketPlayHitmarker extends PacketBase {

    public boolean headshot;

    public PacketPlayHitmarker() {
    }

    public PacketPlayHitmarker(boolean headshot) {
        this.headshot = headshot;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeBoolean(headshot);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        headshot = data.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        // UNUSED
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {
        ModularWarfare.PROXY.playHitmarker(headshot);
    }

}