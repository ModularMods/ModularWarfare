package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.handler.ServerTickHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketAimingRequest extends PacketBase {


    public String playername;
    public boolean aiming;

    public PacketAimingRequest() {
    }

    public PacketAimingRequest(String playername, boolean aiming) {
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
        if (!ServerTickHandler.playerAimShootCooldown.contains(playername)) {
            ModularWarfare.NETWORK.sendToAll(new PacketAimingReponse(playername, aiming));
        }
        ServerTickHandler.playerAimInstant.put(playername, aiming);
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

    }

}