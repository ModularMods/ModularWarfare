package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.utility.MWSound;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;

public class PacketPlaySound extends PacketBase {

    public int posX;
    public int posY;
    public int posZ;
    public String soundName;
    public float volume;
    public float pitch;

    public PacketPlaySound() {
    }

    public PacketPlaySound(BlockPos blockPos, String soundName, float volume, float pitch) {
        this.posX = blockPos.getX();
        this.posY = blockPos.getY();
        this.posZ = blockPos.getZ();
        this.soundName = soundName;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(posX);
        data.writeInt(posY);
        data.writeInt(posZ);
        writeUTF(data, soundName);
        data.writeFloat(volume);
        data.writeFloat(pitch);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        posX = data.readInt();
        posY = data.readInt();
        posZ = data.readInt();
        soundName = readUTF(data);
        volume = data.readFloat();
        pitch = data.readFloat();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        // UNUSED
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {
        ModularWarfare.PROXY.playSound(new MWSound(new BlockPos(posX, posY, posZ), soundName, volume, pitch));
    }

}