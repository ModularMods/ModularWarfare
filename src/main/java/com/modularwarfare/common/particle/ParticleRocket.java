package com.modularwarfare.common.particle;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.gui.api.GuiUtils;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ParticleRocket extends Particle {

    private int index = 1;
    private float alpha = 1;

    public ParticleRocket(World par1World, double par2, double par4, double par6) {
        super(par1World, par2, par4, par6, 0.0D, 0.0D, 0.0D);
        Random rand = new Random();
        this.motionX *= 0.800000011920929D;
        this.motionY = 0;
        this.motionZ *= 0.800000011920929D;
        this.motionY = this.rand.nextFloat() * 0.4F + 0.05F;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleScale *= this.rand.nextFloat() * 2.0F + 0.2F;
        this.canCollide = false;
        this.particleMaxAge = 250;
        this.index = 1 + rand.nextInt((4 - 1) + 1);
        this.alpha = 0;
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

        GL11.glDepthMask(false);
        RenderHelperMW.renderPositionedImageInViewWithDepth(new ResourceLocation(ModularWarfare.MOD_ID, "textures/particles/rocket" + this.index + ".png"),
                this.posX,
                this.posY + .5,
                this.posZ,
                50,
                50,
                this.alpha);

        GL11.glDepthMask(true);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void onUpdate() {

        if (world != null) {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.particleAge++ >= this.particleMaxAge) {
                this.setExpired();
            }

            if (this.particleAge < 5) {
                this.alpha += 0.1f;
            } else {
                this.alpha -= 0.004f;
            }
            float f = (float) this.particleAge / (float) this.particleMaxAge;

            this.motionX *= 0.9990000128746033D;
            this.motionZ *= 0.9990000128746033D;


            this.motionZ *= 0.9900000190734863D;

            this.move(this.motionX/20, this.motionY/20, this.motionZ/20);

            if (this.onGround) {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
            }
        }
    }


    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {
        public Particle createParticle(final int particleID, final World worldIn, final double xCoordIn, final double yCoordIn, final double zCoordIn, final double xSpeedIn, final double ySpeedIn, final double zSpeedIn, final int... p_178902_15_) {
            return new ParticleRocket(worldIn, xCoordIn, yCoordIn, zCoordIn);
        }
    }
}