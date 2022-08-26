package com.modularwarfare.common.network;

import com.modularwarfare.client.hud.GunUI;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketLoreDisable extends PacketBase  {

    private boolean disable = false;

    public PacketLoreDisable() {}

    public PacketLoreDisable(boolean disable) {
        this.disable = disable;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeBoolean(this.disable);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.disable = data.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        GunUI.renderToolTip = !this.disable;
    }
}
