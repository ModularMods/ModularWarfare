package com.modularwarfare.client.fpp.basic.models.layers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.models.ModelCustomArmor;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.network.BackWeaponsManager;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
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

        if (player instanceof AbstractClientPlayer) {
            ItemStack gun = BackWeaponsManager.INSTANCE
                    .getItemToRender((AbstractClientPlayer) player);
            if (gun != ItemStack.EMPTY && !gun.isEmpty()) {
                BaseType type = ((BaseItem) gun.getItem()).baseType;
                {
                    GlStateManager.pushMatrix();
                    if (ClientRenderHooks.customRenderers[type.id] != null) {
                        GlStateManager.translate(0, -0.6, 0.35);
                        boolean isSneaking = player.isSneaking();
                        if (player instanceof EntityPlayerSP) {
                            ((EntityPlayerSP) player).movementInput.sneak = false;
                        } else {
                            player.setSneaking(false);
                        }
                        ClientRenderHooks.customRenderers[type.id].renderItem(CustomItemRenderType.BACK, null, gun, player.world,
                                player, partialTicks);
                        if (player instanceof EntityPlayerSP) {
                            ((EntityPlayerSP) player).movementInput.sneak = isSneaking;
                        } else {
                            player.setSneaking(isSneaking);
                        }
                    }
                    GlStateManager.popMatrix();
                }
            }

        }
    }

    public void renderBody(final EntityPlayer player, final ArmorType armorType, final float scale) {
        if (armorType.hasModel()) {
            final ModelCustomArmor armorModel = (ModelCustomArmor) armorType.bipedModel;
            GlStateManager.pushMatrix();
            if (player.isSneaking()) {
                GlStateManager.translate(0.0f, 0.2f, 0.0f);
                GlStateManager.rotate(30.0f, 1.0f, 0.0f, 0.0f);
            }
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableRescaleNormal();
            final int skinId = 0;
            String path = skinId > 0 ? "skins/" + armorType.modelSkins[skinId].getSkin() : armorType.modelSkins[0].getSkin();
            Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "skins/armor/" + path + ".png"));
            GL11.glScalef(1.0f, 1.0f, 1.0f);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            armorModel.render("armorModel", this.renderer.getMainModel().bipedBody, 0.0625f, 1f);
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.popMatrix();
        }
    }


    public boolean shouldCombineTextures() {
        return true;
    }
}