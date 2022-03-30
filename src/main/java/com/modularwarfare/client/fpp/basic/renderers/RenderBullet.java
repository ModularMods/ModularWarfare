package com.modularwarfare.client.fpp.basic.renderers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.entity.EntityBulletClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import javax.annotation.Nullable;

import static org.lwjgl.opengl.GL11.*;


public class RenderBullet extends Render<EntityBulletClient> {

    public static final Factory FACTORY = new Factory();

    private static final ResourceLocation texture = new ResourceLocation(ModularWarfare.MOD_ID + ":textures/shoot.png");


    protected RenderBullet(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityBulletClient entity) {
        return null;
    }

    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        if (this.renderManager.options != null) {
            this.doRenderBullet((EntityBulletClient) entityIn, x, y, z, yaw, partialTicks);
        }
    }

    private void doRenderBullet(EntityBulletClient bullet, double x, double y, double z, float yaw, float partialTicks) {
        GlStateManager.pushMatrix();

        bullet.renderLifeTime += 1F + partialTicks;
        float distance = bullet.getDistance(Minecraft.getMinecraft().player);
        if (distance < 15f)
            distance /= 15f;
        else distance = 1f;
        if (bullet.renderLifeTime > 2.6f) {

            GL11.glPushMatrix();
            GL11.glTranslatef((float) x, (float) y, (float) z);
            GL11.glDisable(GL_CULL_FACE);
            GL11.glEnable(GL_BLEND);
            GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(false);
            GL11.glAlphaFunc(GL_GREATER, 0.1F);
            GL11.glRotatef(bullet.prevRotationYaw + (bullet.rotationYaw - bullet.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(bullet.prevRotationPitch + (bullet.rotationPitch - bullet.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);
            float scale = 0.004f;
            glScalef(scale, scale, scale);
            GL13.glActiveTexture(GL13.GL_TEXTURE4);
            Minecraft.getMinecraft().renderEngine.bindTexture(texture);
            glColor4f(3f, 3f, 3f, 1f * distance);

            Tessellator t = Tessellator.getInstance();
            BufferBuilder buffer = t.getBuffer();

            float sizeX = 100f;
            float sizeY = 20f;

            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-sizeX, sizeY, 1).tex(0.0D, 1.0D).endVertex();
            buffer.pos(sizeX, sizeY, 1).tex(1.0D, 1.0D).endVertex();
            buffer.pos(sizeX, -sizeY, 1).tex(1.0D, 0.0D).endVertex();
            buffer.pos(-sizeX, -sizeY, 1).tex(0.0D, 0.0D).endVertex();
            t.draw();

            glRotatef(90, 1, 0, 0);
            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-sizeX, sizeY, 1).tex(0.0D, 1.0D).endVertex();
            buffer.pos(sizeX, sizeY, 1).tex(1.0D, 1.0D).endVertex();
            buffer.pos(sizeX, -sizeY, 1).tex(1.0D, 0.0D).endVertex();
            buffer.pos(-sizeX, -sizeY, 1).tex(0.0D, 0.0D).endVertex();
            t.draw();

            glRotatef(90, 0, 1, 0);
            glScalef(0.05f, 1.0f, 1.0f);
            sizeY = 40f;

            buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
            buffer.pos(-sizeX, sizeY, 1).tex(0.0D, 1.0D).endVertex();
            buffer.pos(sizeX, sizeY, 1).tex(1.0D, 1.0D).endVertex();
            buffer.pos(sizeX, -sizeY, 1).tex(1.0D, 0.0D).endVertex();
            buffer.pos(-sizeX, -sizeY, 1).tex(0.0D, 0.0D).endVertex();
            t.draw();

            glDepthMask(true);
            glDisable(GL_BLEND);
            glAlphaFunc(GL_GREATER, 0.1F);
            glEnable(GL_CULL_FACE);
            GL11.glPopMatrix();
        }
        GlStateManager.popMatrix();
    }

    public static class Factory implements IRenderFactory {
        public Render createRenderFor(RenderManager manager) {
            return new RenderBullet(manager);
        }
    }


}
