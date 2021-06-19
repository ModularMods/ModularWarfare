package com.modularwarfare.client.model.layers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.model.ModelCustomArmor;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderLayerBody implements LayerRenderer<EntityPlayer> {
    private final ModelRenderer modelRenderer;
    private RenderPlayer renderer;

    public RenderLayerBody(final RenderPlayer renderer, final ModelRenderer modelRenderer) {
        this.modelRenderer = modelRenderer;
        this.renderer = renderer;
    }

    public void doRenderLayer(final EntityPlayer player, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {
        final int[] slots = new int[]{1};

        if (player.hasCapability(CapabilityExtra.CAPABILITY, null)) {
            final IExtraItemHandler extraSlots = player.getCapability(CapabilityExtra.CAPABILITY, null);
            for (final int slot : slots) {
                final ItemStack itemStackSpecialArmor = extraSlots.getStackInSlot(slot);
                if (!itemStackSpecialArmor.isEmpty() && itemStackSpecialArmor.getItem() instanceof ItemSpecialArmor) {
                    this.renderBody(player, ((ItemSpecialArmor) itemStackSpecialArmor.getItem()).type, scale);
                }
            }
        }
    }

    public void renderBody(final EntityPlayer player, final ArmorType armorType, final float scale) {
        if (armorType.hasModel()) {
            final ModelCustomArmor armorModel = (ModelCustomArmor) armorType.bipedModel;
            GlStateManager.pushMatrix();
            if (player.isSneaking()) {
                GlStateManager.translate(0.0f, 0.3f, 0.0f);
                GlStateManager.translate(0.0f, 0.0f, 0.0f);
                GlStateManager.rotate(30.0f, 1.0f, 0.0f, 0.0f);
            }
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableRescaleNormal();
            final int skinId = 0;
            String path = skinId > 0 ? "skins/" + armorType.modelSkins[skinId].getSkin() : armorType.modelSkins[0].getSkin();
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "skins/armor/" + path + ".png"));
            GL11.glScalef(1.0f, 1.0f, 1.0f);
            armorModel.render("armorModel", this.renderer.getMainModel().bipedBody, 0.0625f, 1f);
            GlStateManager.popMatrix();
        }
    }


    public boolean shouldCombineTextures() {
        return true;
    }
}
