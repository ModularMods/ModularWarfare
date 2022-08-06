package com.modularwarfare.common.network;

import com.modularwarfare.common.entity.decals.EntityBulletHole;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketBulletHoleDespawnTime extends PacketBase {


    private int seconds;

    public PacketBulletHoleDespawnTime(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(this.seconds);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.seconds = data.readInt();
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        EntityBulletHole.timeAliveSeconds = seconds;
    }
}
