package com.modularwarfare.common.particle;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.utility.RenderHelperMW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ParticleExplosion extends Particle {

    public float lastSwing = 0;
    private float alpha_rubble = 0;

    public ParticleExplosion(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6, 0.0D, 0.0D, 0.0D);
        Random rand = new Random();
        this.motionX *= 0.800000011920929D;
        this.motionY = 0;
        this.motionZ *= 0.800000011920929D;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleScale *= this.rand.nextFloat() * 2.0F + 0.2F;
        this.particleMaxAge = 220;
    }

    @Override
    public int getBrightnessForRender(float par1) {
        int i = world != null ? super.getBrightnessForRender(par1) : 0;
        short short1 = 240;
        int j = i >> 16 & 255;
        return short1 | j << 16;
    }

    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float par2, float par3, float par4, float par5, float par6, float par7){
        GL11.glPushMatrix();
        GL11.glPushAttrib(8192);

        RenderHelper.disableStandardItemLighting();;

        if (this.lastSwing != RenderParameters.SMOOTH_SWING) {

            this.lastSwing = RenderParameters.SMOOTH_SWING;

            EntityPlayer player = Minecraft.getMinecraft().player;

            double d = Math.random();
            double d2 = Math.random();

            double dist = player.getDistance(this.posX, this.posY, this.posZ);


            if(dist <= 16) {
                float distFactor = (float) (1/dist);
                player.rotationPitch += distFactor * (d < .5 ? this.alpha_rubble : -this.alpha_rubble) * 5;
                player.rotationYaw += distFactor * (d2 < .5 ? this.alpha_rubble : -this.alpha_rubble) * 5;
            }

            if (this.particleAge < 6) {
                this.alpha_rubble += 0.1;
            } else {
                this.alpha_rubble -= 0.1f;
            }
        }

        if (this.alpha_rubble < 0) {
            this.alpha_rubble = 0;
        }

        RenderHelperMW.renderSmoke(new ResourceLocation(ModularWarfare.MOD_ID, "textures/particles/explosion.png"),
                this.posX,
                this.posY + (this.particleAge / 100),
                this.posZ,
                1,
                80 + this.particleAge * 80, 80 +  (this.particleAge * 50),
                "0xFFFFFF",
                this.alpha_rubble);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {

        if (world != null) {

            if (this.particleAge == 1) {
                this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 20.0F, 0.9F + this.rand.nextFloat() * 0.15F, true);
            }

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.particleAge++ >= this.particleMaxAge) {
                this.setExpired();
            }

            this.motionX *= 0.9990000128746033D;
            this.motionY *= 0.9990000128746033D;
            this.motionZ *= 0.9990000128746033D;

            this.move(this.motionX / 40, this.motionY, this.motionZ / 40);

            if (this.onGround) {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {
        public Particle createParticle(final int particleID, final World worldIn, final double xCoordIn, final double yCoordIn, final double zCoordIn, final double xSpeedIn, final double ySpeedIn, final double zSpeedIn, final int... p_178902_15_) {
            return new ParticleExplosion(worldIn, xCoordIn, yCoordIn, zCoordIn);
        }
    }
}
