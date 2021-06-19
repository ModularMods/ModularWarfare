package com.modularwarfare.utility;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;

import java.util.List;

public class ModUtil {

    public static final int INVENTORY_SLOT_SIZE_PIXELS = 18;
    public static final int BACKPACK_SLOT_OFFSET_X = 76;
    public static final int BACKPACK_SLOT_OFFSET_Y = 7;
    public static final int BACKPACK_CONTENT_OFFSET_X = 180;
    public static final int BACKPACK_CONTENT_OFFSET_Y = 18;

    public static void renderLightModel(final IBakedModel model, final int alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.4f, -0.4f, -0.4f);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        ItemCameraTransforms.applyTransformSide(model.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND), false);
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.ITEM);
        for (final EnumFacing enumfacing : EnumFacing.values()) {
            renderLightQuads(vertexbuffer, model.getQuads((IBlockState) null, enumfacing, 0L), alpha);
        }
        renderLightQuads(vertexbuffer, model.getQuads((IBlockState) null, (EnumFacing) null, 0L), alpha);
        tessellator.draw();
        GlStateManager.cullFace(GlStateManager.CullFace.BACK);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.popMatrix();
    }

    public static int getBrightness(final Entity ent) {
        final BlockPos blockpos = new BlockPos(Math.floor(ent.posX), ent.posY, Math.floor(ent.posZ));
        final World world = Minecraft.getMinecraft().world;
        final int skyLightSub = world.calculateSkylightSubtracted(1.0f);
        final int blockLight = world.getLightFor(EnumSkyBlock.BLOCK, blockpos);
        final int skyLight = world.getLightFor(EnumSkyBlock.SKY, blockpos) - skyLightSub;
        return Math.max(blockLight, skyLight);
    }

    private static void renderLightQuads(final BufferBuilder renderer, final List<BakedQuad> quads, final int alpha) {
        int i = 0;
        final int argb = ColorUtils.getARGB(255, 255, 255, alpha);
        for (int j = quads.size(); i < j; ++i) {
            LightUtil.renderQuadColor(renderer, quads.get(i), argb);
        }
    }

}
