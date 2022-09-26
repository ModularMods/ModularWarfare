package com.modularwarfare.client.model;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.RenderBonesEvent;
import com.modularwarfare.api.RenderMWArmorEvent;
import com.modularwarfare.client.fpp.basic.configs.ArmorRenderConfig;
import com.modularwarfare.client.model.ModelCustomArmor.Bones.BonePart.EnumBoneType;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.MWModelBipedBase;
import com.modularwarfare.loader.api.ObjModelLoader;
import com.modularwarfare.loader.api.model.ObjModelRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

public class ModelCustomArmor extends MWModelBipedBase {

    public static Bones bones = new Bones(0, false);
    public static Bones bonesSmall = new Bones(0, true);
    private BaseType type;
    public Entity renderingEntity;

    public ArmorRenderConfig config;

    public ModelCustomArmor(ArmorRenderConfig config, BaseType type) {
        this.config = config;
        if (this.config.modelFileName.endsWith(".obj")) {
            if (type.isInDirectory) {
                this.staticModel = ObjModelLoader
                        .load(type.contentPack + "/obj/" + type.getAssetDir() + "/" + this.config.modelFileName);
            } else {
                this.staticModel = ObjModelLoader.load(type,
                        "obj/" + type.getAssetDir() + "/" + this.config.modelFileName);
            }
        } else {
            ModularWarfare.LOGGER.info("Internal error: " + this.config.modelFileName + " is not a valid format.");
        }
        this.type = type;
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        GL11.glPushMatrix();
        renderingEntity = entity;
        isSneak = entity.isSneaking();
        Bones bones = this.bones;
        if (entity instanceof AbstractClientPlayer) {
            if (((AbstractClientPlayer) entity).getSkinType().equals("slim")) {
                bones = this.bonesSmall;
            }
        }
        bones.armor = this;
        bones.setModelAttributes(this);
        bones.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        if (config.extra.isSuit) {
            showHead(true);
            showChest(true);
            showLegs(true);
            showFeet(true);
            setShowMode(bones.bipedHead, true);
            setShowMode(bones.bipedBody, true);
            setShowMode(bones.bipedLeftArm, true);
            setShowMode(bones.bipedRightArm, true);
            setShowMode(bones.bipedLeftLeg, true);
            setShowMode(bones.bipedRightLeg, true);
        } else {
            copyShowMode(bones.bipedHead, this.bipedHead);
            copyShowMode(bones.bipedBody, this.bipedBody);
            copyShowMode(bones.bipedLeftArm, this.bipedLeftArm);
            copyShowMode(bones.bipedRightArm, this.bipedRightArm);
            copyShowMode(bones.bipedLeftLeg, this.bipedLeftLeg);
            copyShowMode(bones.bipedRightLeg, this.bipedRightLeg);
        }
        bones.render(entity, f, f1, f2, f3, f4, f5);

        GL11.glPopMatrix();
    }

    public void renderRightArm(AbstractClientPlayer clientPlayer, ModelBiped baseBiped) {
        Bones bones = this.bones;
        if (clientPlayer.getSkinType().equals("slim")) {
            bones = this.bonesSmall;
        }
        bones.armor = this;
        float f = 1.0F;
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        float f1 = 0.0625F;
        ModelPlayer modelplayer = bones;
        modelplayer.bipedRightArm.isHidden = false;
        modelplayer.bipedRightArm.showModel = true;
        GlStateManager.enableBlend();
        modelplayer.swingProgress = 0.0F;
        modelplayer.isSneak = false;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.bipedRightArm.rotateAngleX = baseBiped.bipedRightArm.rotateAngleX;
        modelplayer.bipedRightArm.rotateAngleY = baseBiped.bipedRightArm.rotateAngleY;
        modelplayer.bipedRightArm.rotateAngleZ = baseBiped.bipedRightArm.rotateAngleZ;
        modelplayer.bipedRightArm.render(0.0625F);
        GlStateManager.disableBlend();
    }

    public void renderLeftArm(AbstractClientPlayer clientPlayer, ModelBiped baseBiped) {
        Bones bones = this.bones;
        if (clientPlayer.getSkinType().equals("slim")) {
            bones = this.bonesSmall;
        }
        bones.armor = this;
        float f = 1.0F;
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        float f1 = 0.0625F;
        ModelPlayer modelplayer = bones;
        modelplayer.bipedLeftArm.isHidden = false;
        modelplayer.bipedLeftArm.showModel = true;
        GlStateManager.enableBlend();
        modelplayer.isSneak = false;
        modelplayer.swingProgress = 0.0F;
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, clientPlayer);
        modelplayer.bipedLeftArm.rotateAngleX = baseBiped.bipedLeftArm.rotateAngleX;
        modelplayer.bipedLeftArm.rotateAngleY = baseBiped.bipedLeftArm.rotateAngleY;
        modelplayer.bipedLeftArm.rotateAngleZ = baseBiped.bipedLeftArm.rotateAngleZ;
        modelplayer.bipedLeftArm.render(0.0625F);
        GlStateManager.disableBlend();
    }

    private void copyShowMode(ModelRenderer a, ModelRenderer b) {
        a.showModel = b.showModel;
        a.isHidden = b.isHidden;
    }

    private void setShowMode(ModelRenderer a, boolean b) {
        a.showModel = b;
        a.isHidden = !b;
    }

    public void render(String modelPart, ModelRenderer bodyPart, float f5, float scale) {
        if (this.staticModel != null) {
            GlStateManager.pushMatrix();
            ObjModelRenderer part = this.staticModel.getPart(modelPart);
            if (part != null) {
                if (part != null) {
                    ObjModelRenderer.glowType = "armor";
                    ObjModelRenderer.glowPath = type.modelSkins[0].getSkin();
                    boolean glow = ObjModelRenderer.glowTxtureMode;
                    ObjModelRenderer.glowTxtureMode = true;
                    part.render(f5);
                    ObjModelRenderer.glowTxtureMode = glow;
                }
            }

            GlStateManager.popMatrix();
        }
    }

    public void showHead(boolean result) {
        showGroup("headModel", result);
        showGroup("headSlimModel", result);
    }

    public void showChest(boolean result) {
        showGroup("bodyModel", result);
        showGroup("leftArmModel", result);
        showGroup("rightArmModel", result);
        showGroup("bodySlimModel", result);
        showGroup("leftArmSlimModel", result);
        showGroup("rightArmSlimModel", result);
    }

    public void showLegs(boolean result) {
        showGroup("leftLegSlimModel", result);
        showGroup("rightLegSlimModel", result);
    }

    public void showFeet(boolean result) {
        showGroup("leftFootSlimModel", result);
        showGroup("rightFootSlimModel", result);
    }

    public void showGroup(String part, boolean result) {
        if (this.getStaticModel() != null) {
            ObjModelRenderer modpart = this.getStaticModel().getPart(part);
            if (modpart != null) {
                modpart.isHidden = !result;
            }
        }
    }

    public ModelBiped getMainModel() {
        return this;
    }

    public static class Bones extends net.minecraft.client.model.ModelPlayer {
        public ModelCustomArmor armor = null;
        public boolean isSlim;

        public Bones(float modelSize, boolean smallArmsIn) {
            super(modelSize, smallArmsIn);
            this.leftArmPose = ModelBiped.ArmPose.EMPTY;
            this.rightArmPose = ModelBiped.ArmPose.EMPTY;
            this.textureWidth = 64;
            this.textureHeight = 64;
            this.bipedHead = new BonePart(EnumBoneType.HEAD, this, 0, 0);
            this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize);
            this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedBody = new BonePart(EnumBoneType.BODY, this, 16, 16);
            this.bipedBody.addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, modelSize);
            this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
            this.bipedRightArm = new BonePart(EnumBoneType.RIGHTARM, this, 40, 16);
            this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
            this.bipedRightLeg = new BonePart(EnumBoneType.RIGHTLEG, this, 0, 16);
            this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
            this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);

            if (smallArmsIn) {
                this.bipedLeftArm = new BonePart(EnumBoneType.LEFTARM, this, 32, 48);
                this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
                this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
                this.bipedRightArm = new BonePart(EnumBoneType.RIGHTARM, this, 40, 16);
                this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, modelSize);
                this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
            } else {
                this.bipedLeftArm = new BonePart(EnumBoneType.LEFTARM, this, 32, 48);
                this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, modelSize);
                this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
            }

            this.bipedLeftLeg = new BonePart(EnumBoneType.LEFTLEG, this, 16, 48);
            this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, modelSize);
            this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
            this.isSlim = smallArmsIn;
        }

        @Override
        public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                float headPitch, float scaleFactor, Entity entityIn) {
            // TODO Auto-generated method stub
            super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
                    entityIn);
            MinecraftForge.EVENT_BUS.post(new RenderBonesEvent.RotationAngles(this, limbSwing, limbSwingAmount,
                    ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn));
        }

        @Override
        public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                float headPitch, float scale) {
            // TODO Auto-generated method stub
            GlStateManager.pushMatrix();
            if (entityIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            this.bipedHead.render(scale);
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.popMatrix();
        }

        public static class BonePart extends net.minecraft.client.model.ModelRenderer {
            public static float customOffestX = 0;
            public static float customOffestY = 0;
            public static float customOffestZ = 0;
            private static ModelRenderer NonePart = null;
            private EnumBoneType type;
            private int displayList;
            private boolean compiled;
            private Bones baseModel;

            public BonePart(EnumBoneType type, Bones model, int texOffX, int texOffY) {
                super(model, texOffX, texOffY);
                if (NonePart == null) {
                    NonePart = new ModelRenderer(model);
                }
                this.type = type;
                this.baseModel = model;
                // TODO Auto-generated constructor stub
            }

            @Override
            public void render(float scale) {

                GlStateManager.translate(this.offsetX, this.offsetY, this.offsetZ);
                GlStateManager.pushMatrix();
                GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale,
                        this.rotationPointZ * scale);

                if (this.rotateAngleZ != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
                }

                if (this.rotateAngleY != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
                }

                if (this.rotateAngleX != 0.0F) {
                    GlStateManager.rotate(this.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
                }
                //GlStateManager.translate(-this.cubeList.get(0).posX1*scale,-this.cubeList.get(0).posY1*scale,-this.cubeList.get(0).posZ1*scale);

                GlStateManager.translate(customOffestX, customOffestY, customOffestZ);
                int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                MinecraftForge.EVENT_BUS.post(new RenderBonesEvent.Pre(this.baseModel.armor, type, scale));
                MinecraftForge.EVENT_BUS.post(new RenderMWArmorEvent.Pre(this.baseModel.armor, type, scale));

                switch (type) {
                case HEAD:
                    if (!this.isHidden) {
                        if (this.showModel) {
                            if (baseModel.armor.config.extra.slimSupported && baseModel.isSlim) {
                                baseModel.armor.render("headSlimModel", NonePart, scale, 1);
                            } else {
                                baseModel.armor.render("headModel", NonePart, scale, 1);
                            }
                        }
                    }
                    break;
                case BODY:
                    if (!this.isHidden) {
                        if (this.showModel) {
                            if (baseModel.armor.config.extra.slimSupported && baseModel.isSlim) {
                                baseModel.armor.render("bodySlimModel", NonePart, scale, 1);
                            } else {
                                baseModel.armor.render("bodyModel", NonePart, scale, 1);
                            }
                        }
                    }
                    break;
                case LEFTARM:
                    GlStateManager.translate(-5.0F * scale, -2.0F * scale, 0);
                    if (!this.isHidden) {
                        if (this.showModel) {
                            if (baseModel.armor.config.extra.slimSupported && baseModel.isSlim) {
                                baseModel.armor.render("leftArmSlimModel", NonePart, scale, 1);
                            } else {
                                baseModel.armor.render("leftArmModel", NonePart, scale, 1);
                            }
                        }
                    }
                    break;
                case RIGHTARM:
                    GlStateManager.translate(5.0F * scale, -2.0F * scale, 0.0F);
                    if (!this.isHidden) {
                        if (this.showModel) {
                            if (baseModel.armor.config.extra.slimSupported && baseModel.isSlim) {
                                baseModel.armor.render("rightArmSlimModel", NonePart, scale, 1);
                            } else {
                                baseModel.armor.render("rightArmModel", NonePart, scale, 1);
                            }
                        }
                    }
                    break;
                case LEFTLEG:
                    GlStateManager.translate(-1.9F * scale, -12.0F * scale, 0.0F);
                    if (!this.isHidden) {
                        if (this.showModel) {
                            if (baseModel.armor.config.extra.slimSupported && baseModel.isSlim) {
                                baseModel.armor.render("leftLegSlimModel", NonePart, scale, 1);
                                baseModel.armor.render("leftFootSlimModel", NonePart, scale, 1);
                            } else {
                                baseModel.armor.render("leftLegModel", NonePart, scale, 1);
                                baseModel.armor.render("leftFootModel", NonePart, scale, 1);
                            }
                        }
                    }
                    break;
                case RIGHTLEG:
                    GlStateManager.translate(1.9F * scale, -12.0F * scale, 0.0F);
                    if (!this.isHidden) {
                        if (this.showModel) {
                            if (baseModel.armor.config.extra.slimSupported && baseModel.isSlim) {
                                baseModel.armor.render("rightLegSlimModel", NonePart, scale, 1);
                                baseModel.armor.render("rightFootSlimModel", NonePart, scale, 1);
                            } else {
                                baseModel.armor.render("rightLegModel", NonePart, scale, 1);
                                baseModel.armor.render("rightFootModel", NonePart, scale, 1);
                            }
                        }
                    }
                    break;
                }

                MinecraftForge.EVENT_BUS.post(new RenderBonesEvent.Post(this.baseModel.armor, type, scale));
                MinecraftForge.EVENT_BUS.post(new RenderMWArmorEvent.Post(this.baseModel.armor, type, scale));
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
                GlStateManager.popMatrix();
                GlStateManager.translate(-this.offsetX, -this.offsetY, -this.offsetZ);

            }

            public static enum EnumBoneType {
                HEAD, BODY, LEFTARM, RIGHTARM, LEFTLEG, RIGHTLEG
            }

        }
    }
}