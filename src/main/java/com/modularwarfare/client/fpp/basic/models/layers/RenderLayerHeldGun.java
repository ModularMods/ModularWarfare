package com.modularwarfare.client.fpp.basic.models.layers;

import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.fpp.enhanced.models.EnhancedModel;
import com.modularwarfare.client.fpp.enhanced.renderers.RenderGunEnhanced;
import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemAttachment;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponAnimationType;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;

public class RenderLayerHeldGun extends LayerHeldItem {

    HashSet<String> partsWithAmmo;
    HashSet<String> partsWithoutAmmo;

    public RenderLayerHeldGun(RenderLivingBase<?> livingEntityRendererIn) {
        super(livingEntityRendererIn);
        partsWithAmmo = new HashSet<>();
        partsWithAmmo.add("flashModel");
        partsWithAmmo.add("leftArmModel");
        partsWithAmmo.add("leftArmLayerModel");
        partsWithAmmo.add("leftArmSlimModel");
        partsWithAmmo.add("leftArmLayerSlimModel");
        partsWithAmmo.add("rightArmModel");
        partsWithAmmo.add("rightArmLayerModel");
        partsWithAmmo.add("rightArmSlimModel");
        partsWithAmmo.add("rightArmLayerSlimModel");
        for(int i=0;i<RenderGunEnhanced.BULLET_MAX_RENDER;i++) {
            partsWithAmmo.add("bulletModel_"+i);
        }

        partsWithoutAmmo = new HashSet<>();
        partsWithoutAmmo.add("flashModel");
        partsWithoutAmmo.add("leftArmModel");
        partsWithoutAmmo.add("leftArmLayerModel");
        partsWithoutAmmo.add("leftArmSlimModel");
        partsWithoutAmmo.add("leftArmLayerSlimModel");
        partsWithoutAmmo.add("rightArmModel");
        partsWithoutAmmo.add("rightArmLayerModel");
        partsWithoutAmmo.add("rightArmSlimModel");
        partsWithoutAmmo.add("rightArmLayerSlimModel");
        for(int i=0;i<RenderGunEnhanced.BULLET_MAX_RENDER;i++) {
            partsWithoutAmmo.add("bulletModel_"+i);
        }
        partsWithoutAmmo.add("ammoModel");
        partsWithoutAmmo.add("ammoModel");
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount,
                              float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ItemStack itemstack = entitylivingbaseIn.getHeldItemMainhand();
        if (itemstack != ItemStack.EMPTY && !itemstack.isEmpty()) {
            if (!(itemstack.getItem() instanceof BaseItem)) {
                return;
            }
            BaseType type = ((BaseItem) itemstack.getItem()).baseType;
            if (!type.hasModel()) {
                return;
            }
            if (itemstack.getItem() instanceof ItemAttachment) {
                return;
            }
            if (itemstack.getItem() instanceof ItemBackpack) {
                return;
            }

            GlStateManager.pushMatrix();
            if (entitylivingbaseIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            if(((GunType)type).animationType == WeaponAnimationType.BASIC) {
                this.translateToHand(EnumHandSide.RIGHT);
                GlStateManager.translate(-0.06, 0.38, -0.02);
                if (ClientRenderHooks.customRenderers[type.id] != null) {
                    ClientRenderHooks.customRenderers[type.id].renderItem(CustomItemRenderType.EQUIPPED, null, itemstack,
                            entitylivingbaseIn.world, entitylivingbaseIn, partialTicks);
                }
            } else if(((GunType)type).animationType == WeaponAnimationType.ENHANCED) {

                GunType gunType = (GunType) type;
                EnhancedModel model = type.enhancedModel;
                if (model == null)
                    return;

                GunEnhancedRenderConfig config = gunType.enhancedModel.config;

                if(config.animations.containsKey(AnimationType.DEFAULT)) {
                    this.translateToHand(EnumHandSide.RIGHT);
                    GlStateManager.translate(-0.06, 0.38, -0.02);

                    GL11.glRotatef(-90F, 0F, 1F, 0F);
                    GL11.glRotatef(90F, 0F, 0F, 1F);
                    GL11.glTranslatef(0.25F, 0.2F, -0.05F);
                    GL11.glScalef(1/16F, 1/16F, 1/16F);

                    model.updateAnimation((float) config.animations.get(AnimationType.DEFAULT).getStartTime(config.FPS));

                    int skinId = 0;
                    if (itemstack.hasTagCompound()) {
                        if (itemstack.getTagCompound().hasKey("skinId")) {
                            skinId = itemstack.getTagCompound().getInteger("skinId");
                        }
                    }
                    String gunPath = skinId > 0 ? gunType.modelSkins[skinId].getSkin() : gunType.modelSkins[0].getSkin();
                    ClientProxy.gunEnhancedRenderer.bindTexture("guns", gunPath);

                    if(ItemGun.hasAmmoLoaded(itemstack)){
                        model.renderPartExcept(partsWithAmmo);
                    } else {
                        model.renderPartExcept(partsWithoutAmmo);
                    }
                }

            }

            GlStateManager.popMatrix();

        }
    }

}