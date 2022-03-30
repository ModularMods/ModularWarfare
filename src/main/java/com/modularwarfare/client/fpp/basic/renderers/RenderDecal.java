package com.modularwarfare.client.fpp.basic.renderers;

import com.modularwarfare.common.entity.decals.EntityBulletHole;
import com.modularwarfare.common.entity.decals.EntityDecal;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.BlockStairs.EnumShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;

import java.util.Iterator;

public class RenderDecal extends Render<EntityDecal> {

    public static final Factory FACTORY = new Factory();

    protected RenderDecal(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.0F;
    }


    public void doRenderDecal(EntityDecal var1, double var2, double var4, double var6, float var8, float var9) {
        float transparency = 0.75F;
        if (!var1.isPermanent()) {
            transparency = var1.getAgeRatio() * 0.85F;
        }

        if (var1 instanceof EntityBulletHole) {
            transparency = var1.getAgeRatio() * 1.0F;
        }

        switch (var1.getSideID()) {
            case 0:
                this.renderDecalFloor(var1, var2, var4, var6, transparency, var9);
            case 1:
                this.renderDecalNorth(var1, var2, var4, var6, transparency, var9);
                this.renderDecalEast(var1, var2, var4, var6, transparency, var9);
                this.renderDecalSouth(var1, var2, var4, var6, transparency, var9);
                this.renderDecalWest(var1, var2, var4, var6, transparency, var9);
                break;
            case 2:
                this.renderDecalFloor(var1, var2, var4, var6, transparency, var9);
                break;
            case 3:
                this.renderDecalNorth(var1, var2, var4, var6, transparency, var9);
                break;
            case 4:
                this.renderDecalEast(var1, var2, var4, var6, transparency, var9);
                break;
            case 5:
                this.renderDecalSouth(var1, var2, var4, var6, transparency, var9);
                break;
            case 6:
                this.renderDecalWest(var1, var2, var4, var6, transparency, var9);
                break;
            case 7:
                this.renderDecalCeiling(var1, var2, var4, var6, transparency, var9);
                break;
        }

    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDecal entity) {
        return entity.getDecalTexture();
    }

    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
        if (this.renderManager.options != null) {
            this.doRenderDecal((EntityDecal) entityIn, x, y, z, yaw, partialTicks);
        }

    }

    private World getWorldFromRenderManager() {
        return this.renderManager.world;
    }

    private void renderDecalFloor(EntityDecal entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entityIn));
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 1.0D;
        double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        int i = MathHelper.floor(d5 - f);
        int j = MathHelper.floor(d5 + f);
        int k = MathHelper.floor(d0 - f);
        int l = MathHelper.ceil(d0);
        int i1 = MathHelper.floor(d1 - f);
        int j1 = MathHelper.floor(d1 + f);
        double d2 = x - d5;
        double d3 = y - d0;
        double d4 = z - d1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        Iterator var33 = BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)).iterator();

        while (var33.hasNext()) {
            BlockPos blockpos = (BlockPos) var33.next();
            IBlockState iblockstate = world.getBlockState(blockpos.down());
            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && !world.getBlockState(blockpos).isOpaqueCube()) {
                this.renderDecalSingleFloor(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4, vertexbuffer);
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderDecalCeiling(EntityDecal entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entityIn));
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 1.0D;
        double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        int i = MathHelper.floor(d5 - f);
        int j = MathHelper.floor(d5 + f);
        int k = MathHelper.floor(d0 - f);
        int l = MathHelper.ceil(d0);
        int i1 = MathHelper.floor(d1 - f);
        int j1 = MathHelper.floor(d1 + f);
        double d2 = x - d5;
        double d3 = y - d0;
        double d4 = z - d1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        Iterator var33 = BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)).iterator();

        while (var33.hasNext()) {
            BlockPos blockpos = (BlockPos) var33.next();
            IBlockState iblockstate = world.getBlockState(blockpos.up());
            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && !world.getBlockState(blockpos).isOpaqueCube()) {
                this.renderDecalSingleCeiling(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4, vertexbuffer);
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderDecalSingleCeiling(IBlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_, BufferBuilder vertexbuffer) {
        boolean bool = false;

        bool = state.isSideSolid(this.getWorldFromRenderManager(), p_188299_8_, EnumFacing.UP);

        float f;
        float f1;
        float f2;
        float f3;
        double d0;
        AxisAlignedBB axisalignedbb;
        double d1;
        double d2;
        double d3;
        double d4;
        double d5;
        if (bool) {
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                d1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                d2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                d3 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_ - 0.015625D;
                if (state.getBlock() instanceof BlockSlab && !state.getBlock().isNormalCube(state, this.getWorldFromRenderManager(), p_188299_8_)) {
                    d3 -= 0.5D;
                }

                d4 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                d5 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                f = (float) ((p_188299_2_ - d1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - d2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - d4) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_6_ - d5) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(d2, d3, d4).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d2, d3, d5).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d1, d3, d5).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d1, d3, d4).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        } else if (state.getBlock() instanceof BlockSlab || state.getBlock() instanceof BlockStairs) {
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                d1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                d2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                d3 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_ - 0.015625D + 0.5D;
                d4 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                d5 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                f = (float) ((p_188299_2_ - d1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - d2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - d4) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_6_ - d5) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(d2, d3, d4).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d2, d3, d5).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d1, d3, d5).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d1, d3, d4).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                if (state.getBlock() instanceof BlockStairs && state.getValue(BlockStairs.SHAPE) == EnumShape.STRAIGHT) {
                    d3 += 0.5D;
                    EnumFacing facing = (EnumFacing) state.getValue(BlockHorizontal.FACING);
                    switch (facing) {
                        case NORTH:
                            d5 -= 0.5D;
                            break;
                        case EAST:
                            d1 += 0.5D;
                            break;
                        case SOUTH:
                            d4 += 0.5D;
                            break;
                        case WEST:
                            d2 -= 0.5D;
                    }

                    f = (float) ((p_188299_2_ - d1) / 2.0D / p_188299_10_ + 0.5D);
                    f1 = (float) ((p_188299_2_ - d2) / 2.0D / p_188299_10_ + 0.5D);
                    f2 = (float) ((p_188299_6_ - d4) / 2.0D / p_188299_10_ + 0.5D);
                    f3 = (float) ((p_188299_6_ - d5) / 2.0D / p_188299_10_ + 0.5D);
                    vertexbuffer.pos(d2, d3, d4).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    vertexbuffer.pos(d2, d3, d5).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    vertexbuffer.pos(d1, d3, d5).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    vertexbuffer.pos(d1, d3, d4).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                }
            }
        }

    }

    private void renderDecalSingleFloor(IBlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_, BufferBuilder vertexbuffer) {
        boolean bool = false;

        bool = state.isSideSolid(this.getWorldFromRenderManager(), p_188299_8_, EnumFacing.UP);

        float f;
        float f1;
        float f2;
        float f3;
        double d0;
        AxisAlignedBB axisalignedbb;
        double d1;
        double d2;
        double d3;
        double d4;
        double d5;
        if (bool) {
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                d1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                d2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                d3 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_ + 0.015625D;
                if (state.getBlock() instanceof BlockSlab && !state.getBlock().isNormalCube(state, this.getWorldFromRenderManager(), p_188299_8_)) {
                    d3 -= 0.5D;
                }

                d4 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                d5 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                f = (float) ((p_188299_2_ - d1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - d2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - d4) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_6_ - d5) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(d1, d3, d4).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d1, d3, d5).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d2, d3, d5).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d2, d3, d4).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        } else if (state.getBlock() instanceof BlockSlab || state.getBlock() instanceof BlockStairs) {
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                d1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                d2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                d3 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_ + 0.015625D - 0.5D;
                d4 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                d5 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                f = (float) ((p_188299_2_ - d1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - d2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - d4) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_6_ - d5) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(d1, d3, d4).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d1, d3, d5).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d2, d3, d5).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(d2, d3, d4).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                if (state.getBlock() instanceof BlockStairs && state.getValue(BlockStairs.SHAPE) == EnumShape.STRAIGHT) {
                    d3 += 0.5D;
                    EnumFacing facing = (EnumFacing) state.getValue(BlockHorizontal.FACING);
                    switch (facing) {
                        case NORTH:
                            d5 -= 0.5D;
                            break;
                        case EAST:
                            d1 += 0.5D;
                            break;
                        case SOUTH:
                            d4 += 0.5D;
                            break;
                        case WEST:
                            d2 -= 0.5D;
                    }

                    f = (float) ((p_188299_2_ - d1) / 2.0D / p_188299_10_ + 0.5D);
                    f1 = (float) ((p_188299_2_ - d2) / 2.0D / p_188299_10_ + 0.5D);
                    f2 = (float) ((p_188299_6_ - d4) / 2.0D / p_188299_10_ + 0.5D);
                    f3 = (float) ((p_188299_6_ - d5) / 2.0D / p_188299_10_ + 0.5D);
                    vertexbuffer.pos(d1, d3, d4).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    vertexbuffer.pos(d1, d3, d5).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    vertexbuffer.pos(d2, d3, d5).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    vertexbuffer.pos(d2, d3, d4).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                }
            }
        }

    }

    private void renderDecalNorth(EntityDecal entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entityIn));
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 1.0D;
        double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        int i = MathHelper.floor(d5 - f);
        int j = MathHelper.floor(d5 + f);
        int k = MathHelper.floor(d0 - f);
        int l = MathHelper.floor(d0 + f);
        int i1 = MathHelper.floor(d1 - 0.25D);
        int j1 = MathHelper.floor(d1 + 0.25D);
        double d2 = x - d5;
        double d3 = y - d0;
        double d4 = z - d1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        Iterator var33 = BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)).iterator();

        while (var33.hasNext()) {
            BlockPos blockpos = (BlockPos) var33.next();
            IBlockState iblockstate = world.getBlockState(blockpos.north());
            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && !world.getBlockState(blockpos).isOpaqueCube()) {
                this.renderDecalSingleNorth(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderDecalSingleNorth(IBlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_) {
        boolean bool = false;
        double offset = 0.0D;

        bool = state.isSideSolid(this.getWorldFromRenderManager(), p_188299_8_, EnumFacing.SOUTH);

        Tessellator tessellator;
        BufferBuilder vertexbuffer;
        double d0;
        AxisAlignedBB axisalignedbb;
        double x1;
        double x2;
        double y1;
        double y2;
        double z1;
        float f;
        float f1;
        float f2;
        float f3;
        if (bool) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                x2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z1 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_ + 0.015625D;
                f = (float) ((p_188299_2_ - x1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - x2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x2, y1, z1 - offset).tex(f1, f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y2, z1 - offset).tex(f1, f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y2, z1 - offset).tex(f, f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y1, z1 - offset).tex(f, f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        } else if (state.getBlock() instanceof BlockSlab || state.getBlock() instanceof BlockStairs) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                x2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z1 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_ + 0.015625D;
                if (state.getBlock() instanceof BlockStairs) {
                    EnumFacing facing = (EnumFacing) state.getValue(BlockHorizontal.FACING);
                    if (facing == EnumFacing.NORTH) {
                        double newz1 = z1 - 0.5D;
                        f = (float) ((p_188299_2_ - x1) / 2.0D / p_188299_10_ + 0.5D);
                        f1 = (float) ((p_188299_2_ - x2) / 2.0D / p_188299_10_ + 0.5D);
                        f2 = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                        f3 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                        vertexbuffer.pos(x2, y1, newz1).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(x2, y2, newz1).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(x1, y2, newz1).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(x1, y1, newz1).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    }

                    if (state.getValue(BlockStairs.HALF) == EnumHalf.TOP) {
                        y1 += 0.5D;
                    } else {
                        y2 -= 0.5D;
                    }
                }

                f = (float) ((p_188299_2_ - x1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - x2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x2, y1, z1).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y2, z1).tex((double) f1, (double) f).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y2, z1).tex((double) f, (double) f).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y1, z1).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        }

    }

    private void renderDecalEast(EntityDecal entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entityIn));
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 1.0D;
        double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        int i = MathHelper.floor(d5 - 0.25D);
        int j = MathHelper.floor(d5 + 0.25D);
        int k = MathHelper.floor(d0 - f);
        int l = MathHelper.floor(d0 + f);
        int i1 = MathHelper.floor(d1 - f);
        int j1 = MathHelper.floor(d1 + 1.0D);
        double d2 = x - d5;
        double d3 = y - d0;
        double d4 = z - d1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        Iterator var33 = BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)).iterator();

        while (var33.hasNext()) {
            BlockPos blockpos = (BlockPos) var33.next();
            IBlockState iblockstate = world.getBlockState(blockpos.east());
            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && !world.getBlockState(blockpos).isOpaqueCube()) {
                this.renderDecalSingleEast(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderDecalSingleEast(IBlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_) {
        boolean bool = false;
        double offset = 0.0D;

        bool = state.isSideSolid(this.getWorldFromRenderManager(), p_188299_8_, EnumFacing.WEST);

        Tessellator tessellator;
        BufferBuilder vertexbuffer;
        double d0;
        AxisAlignedBB axisalignedbb;
        double x2;
        double y1;
        double y2;
        double z1;
        double z2;
        float f;
        float f1;
        float f2;
        float f3;
        if (bool) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_ - 0.015625D;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z1 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                z2 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                f = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - z1) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_6_ - z2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x2 + offset, y1, z2).tex(f, f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2 + offset, y2, z2).tex(f1, f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2 + offset, y2, z1).tex(f1, f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2 + offset, y1, z1).tex(f, f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        } else if (state.getBlock() instanceof BlockSlab || state.getBlock() instanceof BlockStairs) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_ - 0.015625D;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z1 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                z2 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                if (state.getBlock() instanceof BlockStairs) {
                    EnumFacing facing = (EnumFacing) state.getValue(BlockHorizontal.FACING);
                    if (facing == EnumFacing.EAST) {
                        double newx2 = x2 + 0.5D;
                        f = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                        f1 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                        f2 = (float) ((p_188299_6_ - z1) / 2.0D / p_188299_10_ + 0.5D);
                        f3 = (float) ((p_188299_6_ - z2) / 2.0D / p_188299_10_ + 0.5D);
                        vertexbuffer.pos(newx2, y1, z2).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(newx2, y2, z2).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(newx2, y2, z1).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(newx2, y1, z1).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    }

                    if (state.getValue(BlockStairs.HALF) == EnumHalf.TOP) {
                        y1 += 0.5D;
                    } else {
                        y2 -= 0.5D;
                    }
                }

                f = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - z1) / 2.0D / p_188299_10_ + 0.5D);
                f = (float) ((p_188299_6_ - z2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x2, y1, z2).tex((double) f, (double) f).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y2, z2).tex((double) f1, (double) f).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y2, z1).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y1, z1).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        }

    }

    private void renderDecalSouth(EntityDecal entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entityIn));
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 1.0D;
        double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        int i = MathHelper.floor(d5 - f);
        int j = MathHelper.floor(d5 + f);
        int k = MathHelper.floor(d0 - f);
        int l = MathHelper.floor(d0 + f);
        int i1 = MathHelper.floor(d1 - 0.25D);
        int j1 = MathHelper.floor(d1 + 0.25D);
        double d2 = x - d5;
        double d3 = y - d0;
        double d4 = z - d1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        Iterator var33 = BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)).iterator();

        while (var33.hasNext()) {
            BlockPos blockpos = (BlockPos) var33.next();
            IBlockState iblockstate = world.getBlockState(blockpos.south());
            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && !world.getBlockState(blockpos).isOpaqueCube()) {
                this.renderDecalSingleSouth(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderDecalSingleSouth(IBlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_) {
        boolean bool = false;
        double offset = 0.0D;

        bool = state.isSideSolid(this.getWorldFromRenderManager(), p_188299_8_, EnumFacing.NORTH);

        Tessellator tessellator;
        BufferBuilder vertexbuffer;
        double d0;
        AxisAlignedBB axisalignedbb;
        double x1;
        double x2;
        double y1;
        double y2;
        double z2;
        float f;
        float f1;
        float f2;
        float f3;
        if (bool) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                x2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z2 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_ - 0.015625D;
                f = (float) ((p_188299_2_ - x1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - x2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x1, y1, z2 + offset).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y2, z2 + offset).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y2, z2 + offset).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y1, z2 + offset).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        } else if (state.getBlock() instanceof BlockSlab || state.getBlock() instanceof BlockStairs) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_;
                x2 = (double) p_188299_8_.getX() + axisalignedbb.maxX + p_188299_11_;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z2 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_ - 0.015625D;
                if (state.getBlock() instanceof BlockStairs) {
                    EnumFacing facing = (EnumFacing) state.getValue(BlockHorizontal.FACING);
                    if (facing == EnumFacing.SOUTH) {
                        double newz2 = z2 + 0.5D;
                        f = (float) ((p_188299_2_ - x1) / 2.0D / p_188299_10_ + 0.5D);
                        f1 = (float) ((p_188299_2_ - x2) / 2.0D / p_188299_10_ + 0.5D);
                        f2 = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                        f3 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                        vertexbuffer.pos(x1, y1, newz2).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(x1, y2, newz2).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(x2, y2, newz2).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(x2, y1, newz2).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    }

                    if (state.getValue(BlockStairs.HALF) == EnumHalf.TOP) {
                        y1 += 0.5D;
                    } else {
                        y2 -= 0.5D;
                    }
                }

                f = (float) ((p_188299_2_ - x1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_2_ - x2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x1, y1, z2).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y2, z2).tex((double) f, (double) f).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y2, z2).tex((double) f1, (double) f).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x2, y1, z2).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        }

    }

    private void renderDecalWest(EntityDecal entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        this.renderManager.renderEngine.bindTexture(this.getEntityTexture(entityIn));
        World world = this.getWorldFromRenderManager();
        GlStateManager.depthMask(false);
        double f = 1.0D;
        double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        int i = MathHelper.floor(d5 - 0.25D);
        int j = MathHelper.floor(d5 + 0.25D);
        int k = MathHelper.floor(d0 - f);
        int l = MathHelper.floor(d0 + f);
        int i1 = MathHelper.floor(d1 - f);
        int j1 = MathHelper.floor(d1 + 1.0D);
        double d2 = x - d5;
        double d3 = y - d0;
        double d4 = z - d1;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
        Iterator var33 = BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1)).iterator();

        while (var33.hasNext()) {
            BlockPos blockpos = (BlockPos) var33.next();
            IBlockState iblockstate = world.getBlockState(blockpos.west());
            if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && !world.getBlockState(blockpos).isOpaqueCube()) {
                this.renderDecalSingleWest(iblockstate, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
            }
        }

        tessellator.draw();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private void renderDecalSingleWest(IBlockState state, double p_188299_2_, double p_188299_4_, double p_188299_6_, BlockPos p_188299_8_, float p_188299_9_, double p_188299_10_, double p_188299_11_, double p_188299_13_, double p_188299_15_) {
        boolean bool = false;
        double offset = 0.0D;

        bool = state.isSideSolid(this.getWorldFromRenderManager(), p_188299_8_, EnumFacing.EAST);


        Tessellator tessellator;
        BufferBuilder vertexbuffer;
        double d0;
        AxisAlignedBB axisalignedbb;
        double x1;
        double y1;
        double y2;
        double z1;
        double z2;
        float f;
        float f1;
        float f2;
        float f3;
        if (bool) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_ + 0.015625D;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z1 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                z2 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                f = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - z1) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_6_ - z2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x1 - offset, y1, z1).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1 - offset, y2, z1).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1 - offset, y2, z2).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1 - offset, y1, z2).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        } else if (state.getBlock() instanceof BlockSlab || state.getBlock() instanceof BlockStairs) {
            tessellator = Tessellator.getInstance();
            vertexbuffer = tessellator.getBuffer();
            d0 = (double) p_188299_9_;
            if (d0 >= 0.0D) {
                if (d0 > 1.0D) {
                    d0 = 1.0D;
                }

                axisalignedbb = state.getBoundingBox(this.getWorldFromRenderManager(), p_188299_8_);
                x1 = (double) p_188299_8_.getX() + axisalignedbb.minX + p_188299_11_ + 0.015625D;
                y1 = (double) p_188299_8_.getY() + axisalignedbb.minY + p_188299_13_;
                y2 = (double) p_188299_8_.getY() + axisalignedbb.maxY + p_188299_13_;
                z1 = (double) p_188299_8_.getZ() + axisalignedbb.minZ + p_188299_15_;
                z2 = (double) p_188299_8_.getZ() + axisalignedbb.maxZ + p_188299_15_;
                if (state.getBlock() instanceof BlockStairs) {
                    EnumFacing facing = state.getValue(BlockHorizontal.FACING);
                    if (facing == EnumFacing.WEST) {
                        double newx1 = x1 - 0.5D;
                        f = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                        f1 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                        f2 = (float) ((p_188299_6_ - z1) / 2.0D / p_188299_10_ + 0.5D);
                        f3 = (float) ((p_188299_6_ - z2) / 2.0D / p_188299_10_ + 0.5D);
                        vertexbuffer.pos(newx1, y1, z1).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(newx1, y2, z1).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(newx1, y2, z2).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                        vertexbuffer.pos(newx1, y1, z2).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                    }

                    if (state.getValue(BlockStairs.HALF) == EnumHalf.TOP) {
                        y1 += 0.5D;
                    } else {
                        y2 -= 0.5D;
                    }
                }

                f = (float) ((p_188299_4_ - y1) / 2.0D / p_188299_10_ + 0.5D);
                f1 = (float) ((p_188299_4_ - y2) / 2.0D / p_188299_10_ + 0.5D);
                f2 = (float) ((p_188299_6_ - z1) / 2.0D / p_188299_10_ + 0.5D);
                f3 = (float) ((p_188299_6_ - z2) / 2.0D / p_188299_10_ + 0.5D);
                vertexbuffer.pos(x1, y1, z1).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y2, z1).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y2, z2).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
                vertexbuffer.pos(x1, y1, z2).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0).normal(0.0F, 0.0F, 0.0F).endVertex();
            }
        }

    }

    public static class Factory implements IRenderFactory {
        public Render createRenderFor(RenderManager manager) {
            return new RenderDecal(manager);
        }
    }
}
