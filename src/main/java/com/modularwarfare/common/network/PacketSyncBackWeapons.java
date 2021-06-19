package com.modularwarfare.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketSyncBackWeapons extends PacketBase {

    private NBTTagCompound tag;

    public PacketSyncBackWeapons() {
        this.tag = BackWeaponsManager.INSTANCE.serializeNBT();
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        ByteBufUtils.writeTag(data, this.tag);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        BackWeaponsManager.INSTANCE.deserializeNBT(ByteBufUtils.readTag(data));
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
    }
}
