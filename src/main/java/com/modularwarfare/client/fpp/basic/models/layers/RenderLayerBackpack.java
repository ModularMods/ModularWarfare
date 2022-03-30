package com.modularwarfare.client.fpp.basic.models.layers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.models.ModelBackpack;
import com.modularwarfare.common.backpacks.ItemBackpack;
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
public class RenderLayerBackpack implements LayerRenderer<EntityPlayer> {
    private final ModelRenderer modelRenderer;
    private RenderPlayer renderer;

    public RenderLayerBackpack(final RenderPlayer renderer, final ModelRenderer modelRenderer) {
        this.modelRenderer = modelRenderer;
        this.renderer = renderer;
    }

    public void doRenderLayer(final EntityPlayer player, final float limbSwing, final float limbSwingAmount, final float partialTicks, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scale) {

        if (player.hasCapability(CapabilityExtra.CAPABILITY, null)) {
            final IExtraItemHandler extraSlots = player.getCapability(CapabilityExtra.CAPABILITY, null);
            final ItemStack itemstackBackpack = extraSlots.getStackInSlot(0);

            if (!itemstackBackpack.isEmpty()) {

                ItemBackpack backpack = (ItemBackpack) itemstackBackpack.getItem();
                GlStateManager.pushMatrix();

                if (player.isSneaking()) {
                    GlStateManager.translate(0.0f, 0.3f, 0.0f);
                    GlStateManager.translate(0.0f, 0.0f, 0.0f);
                    GlStateManager.rotate(30.0f, 1.0f, 0.0f, 0.0f);
                }
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.rotate(180, 0, 1, 0);
                GlStateManager.scale(scale, scale, scale);

                int skinId = 0;
                if (itemstackBackpack.hasTagCompound()) {
                    if (itemstackBackpack.getTagCompound().hasKey("skinId")) {
                        skinId = itemstackBackpack.getTagCompound().getInteger("skinId");
                    }
                }

                String path = skinId > 0 ? backpack.type.modelSkins[skinId].getSkin() : backpack.type.modelSkins[0].getSkin();

                Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "skins/backpacks/" + path + ".png"));

                ModelBackpack model = (ModelBackpack) backpack.type.model;
                model.render("backpackModel", 1f, ((ModelBackpack) backpack.type.model).config.extra.modelScale);

                GlStateManager.disableLighting();
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                model.render("backpackModel", 1f, ((ModelBackpack) backpack.type.model).config.extra.modelScale);
                GlStateManager.shadeModel(GL11.GL_FLAT);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();
            }
        }
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}
