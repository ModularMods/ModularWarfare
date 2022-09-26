package com.modularwarfare.client.renderers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.model.ModelBullet;
import com.modularwarfare.common.entity.EntityBullet;
import com.modularwarfare.common.guns.ItemBullet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;


public class RenderProjectile extends Render<EntityBullet> {

    public static final Factory FACTORY = new Factory();

    protected RenderProjectile(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityBullet entity) {
        return new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/bullets/" + entity.getBulletName() + ".png");
    }

    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        if (this.renderManager.options != null) {
            this.doRenderProjectile((EntityBullet) entityIn, x, y, z, yaw, partialTicks);
        }
    }

    private void doRenderProjectile(EntityBullet entityIn, double x, double y, double z, float yaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.rotate(entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(entityIn.prevRotationPitch + (entityIn.rotationPitch - entityIn.prevRotationPitch) * partialTicks, 0.0F, 0.0F, 1.0F);

        final float worldScale = 1F / 16F;

        if (ModularWarfare.bulletTypes.containsKey(entityIn.getBulletName())) {
            ItemBullet itemBullet = ModularWarfare.bulletTypes.get(entityIn.getBulletName());
            ModelBullet bullet = (ModelBullet) itemBullet.type.model;
            ClientRenderHooks.customRenderers[1].bindTexture("bullets", entityIn.getBulletName());
            bullet.renderBullet(worldScale);
        }

        GlStateManager.popMatrix();
    }

    public static class Factory implements IRenderFactory {
        public Render createRenderFor(RenderManager manager) {
            return new RenderProjectile(manager);
        }
    }


}