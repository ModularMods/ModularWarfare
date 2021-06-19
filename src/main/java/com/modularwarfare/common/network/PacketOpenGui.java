package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketOpenGui extends PacketBase {

    public int guiID;

    public PacketOpenGui() {
    }

    public PacketOpenGui(final int guiID) {
        this.guiID = guiID;
    }


    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(this.guiID);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.guiID = data.readInt();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        entityPlayer.getServerWorld().addScheduledTask(() -> entityPlayer.openGui(ModularWarfare.INSTANCE, 0, entityPlayer.getServerWorld(), 0, 0, 0));
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

    }

}