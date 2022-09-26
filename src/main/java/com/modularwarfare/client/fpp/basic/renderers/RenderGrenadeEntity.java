package com.modularwarfare.client.fpp.basic.renderers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.model.ModelGrenade;
import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.grenades.ItemGrenade;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;


public class RenderGrenadeEntity extends Render<EntityGrenade> {

    public static final Factory FACTORY = new Factory();

    protected RenderGrenadeEntity(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityGrenade entity) {
        return null;
    }

    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        if (this.renderManager.options != null) {
            this.doRenderGrenade((EntityGrenade) entityIn, x, y, z, yaw, partialTicks);
        }
    }

    private void doRenderGrenade(EntityGrenade entityIn, double x, double y, double z, float yaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();

        GlStateManager.translate((float) x + 0.15F, (float) y + 0.1F, (float) z);

        if (entityIn.onGround) {
            GlStateManager.rotate(90, 0, 0, 1);
            //GlStateManager.translate(-0.2, -1.0, 0.4);
        } else {
            GlStateManager.rotate(entityIn.ticksExisted * 10F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(entityIn.ticksExisted * 8F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(entityIn.ticksExisted * 15F, 0.0F, 0.0F, 1.0F);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        final float worldScale = 1F / 16F;

        if (ModularWarfare.grenadeTypes.containsKey(entityIn.getGrenadeName())) {
            ItemGrenade itemGrenade = ModularWarfare.grenadeTypes.get(entityIn.getGrenadeName());
            ModelGrenade grenade = (ModelGrenade) (ModularWarfare.grenadeTypes.get(entityIn.getGrenadeName()).type.model);
            ClientRenderHooks.customRenderers[1].bindTexture("grenades", itemGrenade.type.internalName);
            grenade.renderPart("grenadeModel", worldScale);
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public static class Factory implements IRenderFactory {
        public Render createRenderFor(RenderManager manager) {
            return new RenderGrenadeEntity(manager);
        }
    }


}
