package com.modularwarfare.loader;

import com.modularwarfare.loader.api.model.AbstractObjModel;
import com.modularwarfare.loader.api.model.ObjModelRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class MWModelBase extends ModelBase {

    /**
     * Main obj staticModel used for rendering.
     */
    public AbstractObjModel staticModel;

    public MWModelBase() {
    }

    public MWModelBase(AbstractObjModel model) {
        this.staticModel = model;
    }

    /**
     * Copies the angles from one object to another. This is used when objects should stay aligned with each other, like
     * the hair over a players head.
     */
    public static void copyModelAngles(ObjModelRenderer source, ObjModelRenderer dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
    }

    /**
     * Copies the angles from one object to another. This is used when objects should stay aligned with each other, like
     * the hair over a players head.
     */
    public static void copyModelAngles(ObjModelRenderer source, ModelRenderer dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
    }

    /**
     * Copies the angles from one object to another. This is used when objects should stay aligned with each other, like
     * the hair over a players head.
     */
    public static void copyModelAngles(ModelRenderer source, ObjModelRenderer dest) {
        dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
    }

    public AbstractObjModel getStaticModel() {
        return staticModel;
    }

    @SideOnly(Side.CLIENT)
    public void renderPart(String part, float f) {
        if (this.staticModel != null) {
            if (this.staticModel.getPart(part) != null) {
                render(this.staticModel.getPart(part), f);
                ObjModelRenderer glowPart = this.staticModel
                        .getPart(new StringBuilder().append(part).append("_glow").toString());
                if (glowPart != null) {
                    render(glowPart, f);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void render(ObjModelRenderer model, float f) {
        GL11.glPushMatrix();

        if (model != null)
            model.render(f);

        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    public void renderAll(float f) {
        GL11.glPushMatrix();
        GL11.glRotatef(90F, 1.0F, 0F, 0F);
        if (staticModel != null)
            staticModel.renderAll(f);
        GL11.glPopMatrix();
    }
}