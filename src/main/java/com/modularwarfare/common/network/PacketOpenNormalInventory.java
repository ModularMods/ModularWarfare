package com.modularwarfare.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;

public class PacketOpenNormalInventory extends PacketBase {
    @Override
    public void encodeInto(final ChannelHandlerContext ctx, final ByteBuf data) {
    }

    @Override
    public void decodeInto(final ChannelHandlerContext ctx, final ByteBuf data) {
    }

    @Override
    public void handleServerSide(final EntityPlayerMP entityPlayer) {
        final IThreadListener mainThread = (IThreadListener) entityPlayer.world;
        mainThread.addScheduledTask((Runnable) new Runnable() {
            @Override
            public void run() {
                entityPlayer.openContainer.onContainerClosed(entityPlayer);
                entityPlayer.openContainer = entityPlayer.inventoryContainer;
            }
        });
    }

    @Override
    public void handleClientSide(final EntityPlayer entityPlayer) {
    }
}
