package com.modularwarfare.common.network;

import com.modularwarfare.common.entity.decals.EntityBulletHole;
import com.modularwarfare.common.entity.decals.EntityDecal;
import com.modularwarfare.common.particle.EntityBloodFX;
import com.modularwarfare.common.particle.EntityShotFX;
import com.modularwarfare.common.particle.ParticleExplosion;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class PacketDecal extends PacketBase {

    private int decalIndex;
    private int decalSide;
    private double decalX;
    private double decalY;
    private double decalZ;
    private boolean flag;

    public PacketDecal() {
    }

    public PacketDecal(int decalIndex, EntityDecal.EnumDecalSide side, double x, double y, double z, boolean flag) {
        this.decalIndex = decalIndex;
        this.decalSide = side.getId();
        this.decalX = x;
        this.decalY = y;
        this.decalZ = z;
        this.flag = flag;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(this.decalIndex);
        data.writeInt(this.decalSide);
        data.writeDouble(this.decalX);
        data.writeDouble(this.decalY);
        data.writeDouble(this.decalZ);
        data.writeBoolean(this.flag);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.decalIndex = data.readInt();
        this.decalSide = data.readInt();
        this.decalX = data.readDouble();
        this.decalY = data.readDouble();
        this.decalZ = data.readDouble();
        this.flag = data.readBoolean();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleClientSide(EntityPlayer entityPlayer) {

        EntityDecal decal = null;
        if (decalIndex == 0) {
            decal = new EntityBulletHole(Minecraft.getMinecraft().world);
        }

        if (decal != null) {
            decal.setSide(EntityDecal.EnumDecalSide.values()[decalSide]);
            decal.setPosition(decalX, decalY, decalZ);
            Minecraft.getMinecraft().world.spawnEntity(decal);

            for (int i = 0; i < 5; i++) {
                Particle smoke = new EntityShotFX(Minecraft.getMinecraft().world, decalX, decalY, decalZ, 1.0f * new Random().nextFloat(), 1.0f * new Random().nextFloat(), 1.0f * new Random().nextFloat(), 2.0f * new Random().nextFloat());
                Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
            }
        }

    }

}
