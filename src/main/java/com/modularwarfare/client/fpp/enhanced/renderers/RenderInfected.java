package com.modularwarfare.client.fpp.enhanced.renderers;

import com.modularwarfare.common.entity.EntityInfected;
import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.RenderedGltfModel;
import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public class RenderInfected extends Render<EntityInfected> implements IGltfModelReceiver {

    protected List<Runnable> commands;

    protected List<Animation> animations;

    public RenderInfected(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getModelLocation() {
        return new ResourceLocation("modularwarfare", "gltf/zombie.glb");
    }

    @Override
    public void onModelLoaded(RenderedGltfModel renderedModel) {
        commands = renderedModel.sceneCommands.get(0);
        animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityInfected entity) {
        return new ResourceLocation("textures/entity/zombie/zombie.png");
    }

    @Override
    public boolean shouldRender(EntityInfected livingEntity, ICamera camera, double camX, double camY, double camZ) {
        if (super.shouldRender(livingEntity, camera, camX, camY, camZ))
        {
            return true;
        }
        else if (livingEntity.getLeashed() && livingEntity.getLeashHolder() != null)
        {
            Entity entity = livingEntity.getLeashHolder();
            return camera.isBoundingBoxInFrustum(entity.getRenderBoundingBox());
        }
        else
        {
            return false;
        }
    }

    @Override
    public void doRender(EntityInfected entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if(commands != null) {


            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glTranslated(x, y, z);
            GL11.glScalef(0.25F,0.25F,0.25F);
            GL11.glRotatef(180, 0,1,0);

            float f = this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
            float f1 = this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks);
            float f2 = f1 - f;
            float f7 = entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
            float f8 = this.handleRotationFloat(entity, partialTicks);
            this.applyRotations(entity, f8, f, partialTicks);

            for (Animation animation : animations) {
                animation.update(net.minecraftforge.client.model.animation.Animation.getWorldTime(entity.world, partialTicks) % animation.getEndTimeS());
            }

            commands.forEach((command) -> command.run());
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks)
    {
        float f;

        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F);

        while (f >= 180.0F) f -= 360.0F;

        return prevYawOffset + partialTicks * f;
    }

    protected float handleRotationFloat(Entity livingBase, float partialTicks)
    {
        return (float)livingBase.ticksExisted + partialTicks;
    }

    protected void applyRotations(EntityLivingBase entityLiving, float p_77043_2_, float rotationYaw, float partialTicks)
    {
        GlStateManager.rotate(180.0F - rotationYaw, 0.0F, 1.0F, 0.0F);

        if (entityLiving.deathTime > 0)
        {
            float f = ((float)entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);

            if (f > 1.0F)
            {
                f = 1.0F;
            }

            GlStateManager.rotate(f * 90F, 0.0F, 0.0F, 1.0F);
        }
        else
        {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName());

            if (s != null && ("Dinnerbone".equals(s) || "Grumm".equals(s)) && (!(entityLiving instanceof EntityPlayer) || ((EntityPlayer)entityLiving).isWearing(EnumPlayerModelParts.CAPE)))
            {
                GlStateManager.translate(0.0F, entityLiving.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }
    }

}