package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.model.InstantBulletRenderer;
import com.modularwarfare.common.vector.Vector3f;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketGunTrailAskServer extends PacketBase {

    double posX;
    double posY;
    double posZ;
    double motionX;
    double motionZ;

    double dirX;
    double dirY;
    double dirZ;
    double range;
    float bulletspeed;

    boolean isPunched;

    public PacketGunTrailAskServer() {
    }

    public PacketGunTrailAskServer(double X, double Y, double Z, double motionX, double motionZ, double x, double y, double z, double range, float bulletspeed, boolean isPunched) {
        this.posX = X;
        this.posY = Y;
        this.posZ = Z;

        this.motionX = motionX;
        this.motionZ = motionZ;

        this.dirX = x;
        this.dirY = y;
        this.dirZ = z;
        this.range = range;
        this.bulletspeed = bulletspeed;
        this.isPunched = isPunched;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeDouble(posX);
        data.writeDouble(posY);
        data.writeDouble(posZ);

        data.writeDouble(motionX);
        data.writeDouble(motionZ);

        data.writeDouble(dirX);
        data.writeDouble(dirY);
        data.writeDouble(dirZ);

        data.writeDouble(range);
        data.writeFloat(bulletspeed);
        data.writeBoolean(isPunched);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        posX = data.readDouble();
        posY = data.readDouble();
        posZ = data.readDouble();

        motionX = data.readDouble();
        motionZ = data.readDouble();

        dirX = data.readDouble();
        dirY = data.readDouble();
        dirZ = data.readDouble();

        range = data.readDouble();
        bulletspeed = data.readFloat();
        isPunched = data.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        ModularWarfare.NETWORK.sendToDimension(new PacketGunTrail(posX, posY, posZ, motionX, motionZ, dirX, dirY, dirZ, range, 10, isPunched), entityPlayer.world.provider.getDimension());
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

    }

}
