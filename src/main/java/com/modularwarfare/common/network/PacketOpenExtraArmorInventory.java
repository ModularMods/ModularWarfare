package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;

public class PacketOpenExtraArmorInventory extends PacketBase {
    @Override
    public void encodeInto(final ChannelHandlerContext ctx, final ByteBuf data) {
    }

    @Override
    public void decodeInto(final ChannelHandlerContext ctx, final ByteBuf data) {
    }

    @Override
    public void handleServerSide(final EntityPlayerMP entityPlayer) {
        final IThreadListener mainThread = (IThreadListener) entityPlayer.world;
        mainThread.addScheduledTask(new Runnable() {
            @Override
            public void run() {
                entityPlayer.openContainer.onContainerClosed((EntityPlayer) entityPlayer);
                entityPlayer.openGui(ModularWarfare.INSTANCE, 0, entityPlayer.world, 0, 0, 0);
            }
        });
    }

    @Override
    public void handleClientSide(final EntityPlayer entityPlayer) {
    }
}
