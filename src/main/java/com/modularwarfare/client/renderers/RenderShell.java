package com.modularwarfare.client.renderers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.model.ModelShell;
import com.modularwarfare.common.entity.decals.EntityShell;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemBullet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import javax.annotation.Nullable;


public class RenderShell extends Render<EntityShell> {

    public static final Factory FACTORY = new Factory();

    protected RenderShell(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityShell entity) {
        return new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/shells/" + entity.getBulletName() + ".png");
    }

    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        if (this.renderManager.options != null) {
            this.doRenderShell((EntityShell) entityIn, x, y, z, yaw, partialTicks);
        }
    }

    private void doRenderShell(EntityShell entityIn, double x, double y, double z, float yaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();


        GlStateManager.translate((float) x, (float) y + 0.02, (float) z);

        GlStateManager.rotate(entityIn.prevRotationYaw + (entityIn.rotationYaw - entityIn.prevRotationYaw) * partialTicks,
                0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-entityIn.rotationPitch, 1.0F, 0.0F, 0.0F);

        RenderHelper.enableStandardItemLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        final float worldScale = 1F / 16F;


        if (ModularWarfare.bulletTypes.containsKey(entityIn.getBulletName())) {
            ItemBullet itemBullet = ModularWarfare.bulletTypes.get(entityIn.getBulletName());
            ModelShell shell = (ModelShell) (ModularWarfare.bulletTypes.get(entityIn.getBulletName()).type.shell);
            boolean flag=true;
            if(itemBullet.type.sameTextureAsGun) {
                GunType gunType=ModularWarfare.gunTypes.get(entityIn.getGunName()).type;
                if(gunType.modelSkins!=null&&gunType.modelSkins.length>entityIn.getGunSkinID()) {
                    flag=false;
                    ClientRenderHooks.customRenderers[1].bindTexture("gun",gunType.modelSkins[entityIn.getGunSkinID()].getSkin());
                }
            }
            if(flag) {
                if (itemBullet.type.shellModelFileName.equals(itemBullet.type.defaultModel)) {
                    ClientRenderHooks.customRenderers[1].bindTexture("bullets", "default");
                } else {
                    String path=entityIn.getBulletName();
                    if(itemBullet.type.modelSkins!=null&&itemBullet.type.modelSkins.length>0) {
                        path= itemBullet.type.modelSkins[0].getSkin();
                    }
                    ClientRenderHooks.customRenderers[1].bindTexture("bullets",path);
                }  
            }
            shell.renderShell(worldScale);
        }

        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public static class Factory implements IRenderFactory {
        public Render createRenderFor(RenderManager manager) {
            return new RenderShell(manager);
        }
    }


}
