package com.modularwarfare.client.fpp.basic.models.layers;

import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.guns.ItemAttachment;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class RenderLayerHeldGun extends LayerHeldItem {

    public RenderLayerHeldGun(RenderLivingBase<?> livingEntityRendererIn) {
        super(livingEntityRendererIn);
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
            this.translateToHand(EnumHandSide.RIGHT);
            GlStateManager.translate(-0.06, 0.38, -0.02);
            if (ClientRenderHooks.customRenderers[type.id] != null) {
                ClientRenderHooks.customRenderers[type.id].renderItem(CustomItemRenderType.EQUIPPED, null, itemstack,
                        entitylivingbaseIn.world, entitylivingbaseIn, partialTicks);
            }
            GlStateManager.popMatrix();

        }
    }

}