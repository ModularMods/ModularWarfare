package com.modularwarfare.common.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityShotFX extends Particle {
    private boolean isCollided;

    public EntityShotFX(final World world, final double d, final double d1, final double d2, final double d3, final double d4, final double d5, final double mult) {
        super(world, d, d1, d2, d3, d4, d5);
        this.particleGravity = 1.2f;
        this.particleRed = 0f;
        this.particleBlue = 0f;
        this.particleGreen = 0f;
        final double scale = 1.5 * ((1.0 + mult) / 2.0);
        this.multipleParticleScaleBy((float) scale);
        final double expandBB = this.getBoundingBox().getAverageEdgeLength() * (scale - 1.0);
        this.getBoundingBox().expand(expandBB * 2.0, expandBB * 2.0, expandBB * 2.0);
        this.multiplyVelocity(1.2f);
        this.motionY += this.rand.nextFloat() * 0.15f;
        this.motionZ *= 0.4f / (this.rand.nextFloat() * 0.9f + 0.1f);
        this.motionX *= 0.4f / (this.rand.nextFloat() * 0.9f + 0.1f);
        this.particleMaxAge = (int) (200.0f + 20.0f / (this.rand.nextFloat() * 0.9f + 0.1f));
        this.setParticleTextureIndex(19 + this.rand.nextInt(4));
        this.isCollided = false;
    }

    public void renderParticle(final BufferBuilder tessellator, final Entity e, final float f, final float f1, final float f2, final float f3, final float f4, final float f5) {
        super.renderParticle(tessellator, e, f, f1, f2, f3, f4, f5);
    }

    public int getBrightnessForRender(final float f) {
        final int i = super.getBrightnessForRender(f);
        float f2 = this.particleAge / this.particleMaxAge;
        f2 *= f2;
        f2 *= f2;
        final int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        k += (int) (f2 * 15.0f * 16.0f);
        if (k > 240) {
            k = 240;
        }
        return j | k << 16;
    }

    public float getBrightness(final float f) {
        final float f2 = super.getBrightnessForRender(f);
        float f3 = this.particleAge / this.particleMaxAge;
        f3 *= f3;
        f3 *= f3;
        return f2 * (1.0f - f3) + f3;
    }

    public void move(double x, double y, double z) {
        final double d0 = y;
        final double origX = x;
        final double origZ = z;
        if (this.canCollide) {
            final List<AxisAlignedBB> list = (List<AxisAlignedBB>) this.world.getCollisionBoxes((Entity) null, this.getBoundingBox().expand(x, y, z));
            for (final AxisAlignedBB axisalignedbb : list) {
                y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
                this.isCollided = true;
            }
            this.setBoundingBox(this.getBoundingBox().offset(0.0, y, 0.0));
            for (final AxisAlignedBB axisalignedbb2 : list) {
                x = axisalignedbb2.calculateXOffset(this.getBoundingBox(), x);
                this.isCollided = true;
            }
            this.setBoundingBox(this.getBoundingBox().offset(x, 0.0, 0.0));
            for (final AxisAlignedBB axisalignedbb3 : list) {
                z = axisalignedbb3.calculateZOffset(this.getBoundingBox(), z);
                this.isCollided = true;
            }
            this.setBoundingBox(this.getBoundingBox().offset(0.0, 0.0, z));
        } else {
            this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
        }
        this.resetPositionToBB();
        this.onGround = (d0 != y && d0 < 0.0);
        if (origX != x) {
            this.motionX = 0.0;
        }
        if (origZ != z) {
            this.motionZ = 0.0;
        }
    }

    public void onUpdate() {
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (!this.isCollided) {
            this.motionY -= 0.04 * this.particleGravity;
            this.move(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863;
            this.motionY *= 0.9800000190734863;
            this.motionZ *= 0.9800000190734863;
            if (this.onGround) {
                this.motionX *= 0.699999988079071;
                this.motionZ *= 0.699999988079071;
                this.posY += 0.1;
            }
        } else {
            this.setExpired();
        }
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {
        public Particle createParticle(final int particleID, final World worldIn, final double xCoordIn, final double yCoordIn, final double zCoordIn, final double xSpeedIn, final double ySpeedIn, final double zSpeedIn, final int... p_178902_15_) {
            return new EntityShotFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, zSpeedIn);
        }
    }
}
