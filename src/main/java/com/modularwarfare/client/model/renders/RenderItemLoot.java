package com.modularwarfare.client.model.renders;

import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.model.ModelAttachment;
import com.modularwarfare.client.model.ModelGun;
import com.modularwarfare.common.entity.item.EntityItemLoot;
import com.modularwarfare.common.guns.*;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

public class RenderItemLoot extends Render<EntityItemLoot> {
    public static final Factory FACTORY;

    static {
        FACTORY = new Factory();
    }

    private final RenderItem itemRenderer;
    private Random random;

    public RenderItemLoot(final RenderManager renderManagerIn, final RenderItem p_i46167_2_) {
        super(renderManagerIn);
        this.random = new Random();
        this.itemRenderer = p_i46167_2_;
        this.shadowSize = 0f;
        this.shadowOpaque = 0f;
    }

    private int transformModelCount(final EntityItemLoot itemIn, final double p_177077_2_, final double p_177077_4_, final double p_177077_6_, final float p_177077_8_, final IBakedModel p_177077_9_) {
        final ItemStack itemstack = itemIn.getItem();
        final Item item = itemstack.getItem();
        if (item == null) {
            return 0;
        }
        final boolean flag = p_177077_9_.isGui3d();
        final int i = this.getModelCount(itemstack);
        final float f1 = this.shouldBob() ? (MathHelper.sin((itemIn.getAge() + p_177077_8_) / 10.0f + itemIn.hoverStart) * 0.1f + 0.1f) : 0.0f;
        final float f2 = p_177077_9_.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
        GlStateManager.translate((float) p_177077_2_, (float) p_177077_4_, (float) p_177077_6_);
        if (flag || this.renderManager.options != null) {
            final IBlockState bsDown = itemIn.world.getBlockState(new BlockPos(itemIn.posX, itemIn.posY - 0.25, itemIn.posZ));
            final boolean inWater = itemIn.isInWater() || bsDown.getBlock() instanceof BlockLiquid || bsDown.getBlock() instanceof IFluidBlock;
            if (!itemIn.onGround && !inWater) {
                final float f3 = ((itemIn.getAge() + p_177077_8_) / 20.0f + itemIn.hoverStart) * 57.295776f;
                GlStateManager.rotate(itemIn.hoverStart * 360.0f, 0.0f, 1.0f, 0.0f);
            } else {
                GlStateManager.rotate(itemIn.hoverStart * 360.0f, 0.0f, 1.0f, 0.0f);
            }
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        return i;
    }

    protected int getModelCount(final ItemStack stack) {
        int i = 1;
        if (stack.getCount() > 48) {
            i = 5;
        } else if (stack.getCount() > 32) {
            i = 4;
        } else if (stack.getCount() > 16) {
            i = 3;
        } else if (stack.getCount() > 1) {
            i = 2;
        }
        return i;
    }

    public void doRender(final EntityItemLoot entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks) {
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        final ItemStack itemstack = entity.getItem();
        if (itemstack.getItem() instanceof ItemGun) {
            GlStateManager.alphaFunc(516, 0.1F);
            GlStateManager.enableBlend();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.pushMatrix();
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.15, 0.2, -0.08);
            double height = 0.2;
            GlStateManager.translate(0, height, 0);
            GlStateManager.rotate(entity.rotationPitch, 1, 0, 0.0F);
            GlStateManager.translate(0, -height, 0);

            ItemGun gun = (ItemGun) itemstack.getItem();
            GunType gunType = gun.type;
            ModelGun model = (ModelGun) gunType.model;
            float modelScale = model.config.extra.modelScale;
            GlStateManager.scale(modelScale*0.8, modelScale*0.8, modelScale*0.8);
            float worldScale = 1F / 16F;
            if (model != null) {
                int skinId = 0;
                if (itemstack.hasTagCompound()) {
                    if (itemstack.getTagCompound().hasKey("skinId")) {
                        skinId = itemstack.getTagCompound().getInteger("skinId");
                    }
                }

                String path = skinId > 0 ? gunType.modelSkins[skinId].getSkin() : gunType.modelSkins[0].getSkin();
                ClientRenderHooks.customRenderers[1].bindTexture("guns", path);
                model.renderPart("gunModel", worldScale);
                model.renderPart("slideModel", worldScale);
                model.renderPart("boltModel", worldScale);
                model.renderPart("defaultBarrelModel", worldScale);
                model.renderPart("defaultStockModel", worldScale);
                model.renderPart("defaultGripModel", worldScale);
                model.renderPart("defaultGadgetModel", worldScale);
                if (ItemGun.hasAmmoLoaded(itemstack)) {
                    model.renderPart("ammoModel", worldScale);
                }

                boolean hasScopeAttachment = false;
                GlStateManager.pushMatrix();
                for (AttachmentEnum attachment : AttachmentEnum.values()) {
                    GlStateManager.pushMatrix();
                    ItemStack itemStack = GunType.getAttachment(itemstack, attachment);
                    if (itemStack != null && itemStack.getItem() != Items.AIR) {
                        AttachmentType attachmentType = ((ItemAttachment) itemStack.getItem()).type;
                        ModelAttachment attachmentModel = (ModelAttachment) attachmentType.model;
                        if (attachmentType.attachmentType == AttachmentEnum.Sight)
                            hasScopeAttachment = true;
                        if (attachmentModel != null) {

                            Vector3f adjustedScale = new Vector3f(attachmentModel.config.extra.modelScale, attachmentModel.config.extra.modelScale, attachmentModel.config.extra.modelScale);
                            GL11.glScalef(adjustedScale.x, adjustedScale.y, adjustedScale.z);

                            if (model.config.attachments.attachmentPointMap != null && model.config.attachments.attachmentPointMap.size() >= 1) {
                                if (model.config.attachments.attachmentPointMap.containsKey(attachment)) {
                                    Vector3f attachmentVecTranslate = model.config.attachments.attachmentPointMap.get(attachment).get(0);
                                    Vector3f attachmentVecRotate = model.config.attachments.attachmentPointMap.get(attachment).get(1);
                                    GL11.glTranslatef(attachmentVecTranslate.x / attachmentModel.config.extra.modelScale, attachmentVecTranslate.y / attachmentModel.config.extra.modelScale, attachmentVecTranslate.z / attachmentModel.config.extra.modelScale);

                                    GL11.glRotatef(attachmentVecRotate.x, 1F, 0F, 0F); //ROLL LEFT-RIGHT
                                    GL11.glRotatef(attachmentVecRotate.y, 0F, 1F, 0F); //ANGLE LEFT-RIGHT
                                    GL11.glRotatef(attachmentVecRotate.z, 0F, 0F, 1F); //ANGLE UP-DOWN
                                }
                            }

                            if (model.config.attachments.positionPointMap != null) {
                                for (String internalName : model.config.attachments.positionPointMap.keySet()) {
                                    if (internalName.equals(attachmentType.internalName)) {
                                        Vector3f trans = model.config.attachments.positionPointMap.get(internalName).get(0);
                                        Vector3f rot = model.config.attachments.positionPointMap.get(internalName).get(1);
                                        GL11.glTranslatef(trans.x / attachmentModel.config.extra.modelScale * worldScale, trans.y / attachmentModel.config.extra.modelScale * worldScale, trans.z / attachmentModel.config.extra.modelScale * worldScale);

                                        GL11.glRotatef(rot.x, 1F, 0F, 0F); //ROLL LEFT-RIGHT
                                        GL11.glRotatef(rot.y, 0F, 1F, 0F); //ANGLE LEFT-RIGHT
                                        GL11.glRotatef(rot.z, 0F, 0F, 1F); //ANGLE UP-DOWN
                                    }
                                }
                            }

                            skinId = 0;
                            if (itemStack.hasTagCompound()) {
                                if (itemStack.getTagCompound().hasKey("skinId")) {
                                    skinId = itemStack.getTagCompound().getInteger("skinId");
                                }
                            }
                            path = skinId > 0 ? attachmentType.modelSkins[skinId].getSkin() : attachmentType.modelSkins[0].getSkin();
                            ClientRenderHooks.customRenderers[3].bindTexture("attachments", path);
                            attachmentModel.renderAttachment(worldScale);
                        }
                    }
                    GlStateManager.popMatrix();
                }
                if (!hasScopeAttachment)
                    model.renderPart("defaultScopeModel", worldScale);

                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();

            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        } else {
            int i;
            if (itemstack != null && itemstack.getItem() != null) {
                i = Item.getIdFromItem(itemstack.getItem()) + itemstack.getMetadata();
            } else {
                i = 187;
            }
            this.random.setSeed(i);
            boolean flag = false;
            if (this.bindEntityTexture(entity)) {
                this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
                flag = true;
            }
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1f);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.pushMatrix();
            IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entity.world, null);
            final int j = this.transformModelCount(entity, x, y, z, partialTicks, ibakedmodel);
            final boolean flag2 = ibakedmodel.isGui3d();
            if (!flag2) {
                final float f3 = -0.0f * (j - 1) * 0.5f;
                final float f4 = -0.0f * (j - 1) * 0.5f;
                final float f5 = -0.09375f * (j - 1) * 0.5f;
                GlStateManager.translate(f3, f4, f5);
            }
            if (this.renderOutlines) {
                GlStateManager.enableColorMaterial();
                GlStateManager.enableOutlineMode(this.getTeamColor(entity));
            }
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0, 0, -0.03);

            if (itemstack.getItem() instanceof ItemBullet) {
                GlStateManager.scale(0.6f, 0.6f, 0.6f);
            }
            if (itemstack.getItem() instanceof ItemGun) {
                GlStateManager.scale(0.9f, 0.9f, 0.9f);
            }

            for (int k = 0; k < j; ++k) {
                if (flag2) {
                    GlStateManager.pushMatrix();
                    if (k > 0) {
                        final float f6 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                        final float f7 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                        final float f8 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                        GlStateManager.translate(this.shouldSpreadItems() ? f6 : 0.0f, this.shouldSpreadItems() ? f7 : 0.0f, f8);
                    }
                    ibakedmodel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                    this.itemRenderer.renderItem(itemstack, ibakedmodel);
                    GlStateManager.popMatrix();
                } else {
                    GlStateManager.pushMatrix();
                    if (k > 0) {
                        final float f9 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                        final float f10 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                        GlStateManager.translate(f9, f10, 0.0f);
                    }
                    ibakedmodel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                    this.itemRenderer.renderItem(itemstack, ibakedmodel);
                    GlStateManager.popMatrix();
                    GlStateManager.translate(0.0f, 0.0f, 0.09375f);
                }
            }
            if (this.renderOutlines) {
                GlStateManager.disableOutlineMode();
                GlStateManager.disableColorMaterial();
            }
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            this.bindEntityTexture(entity);
            if (flag) {
                this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
            }
            super.doRender(entity, x, y, z, entityYaw, partialTicks);
        }
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    protected ResourceLocation getEntityTexture(final EntityItemLoot entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }

    public boolean shouldSpreadItems() {
        return true;
    }

    public boolean shouldBob() {
        return false;
    }

    public static class Factory implements IRenderFactory<EntityItemLoot> {
        public Render<? super EntityItemLoot> createRenderFor(final RenderManager manager) {
            return new RenderItemLoot(manager, Minecraft.getMinecraft().getRenderItem());
        }
    }
}
