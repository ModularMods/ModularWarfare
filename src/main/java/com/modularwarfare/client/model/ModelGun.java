package com.modularwarfare.client.model;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.configs.GunRenderConfig;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.MWModelBase;
import com.modularwarfare.loader.api.ObjModelLoader;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class ModelGun extends MWModelBase {

    //lighting stuff for glow
    private static float lightmapLastX;
    private static float lightmapLastY;
    private static boolean optifineBreak = false;
    public GunRenderConfig config;
    //Fire mode switch variables
    public boolean switchIsOnSlide = false;
    public Vector3f switchRotationPoint = new Vector3f(0, 0, 0);

    //Reload animation variables
    public float switchSemiRot;
    public float switchBurstRot;
    public float switchAutoRot;
    /**
     * If true, then the scope attachment will move with the top slide
     */
    public boolean scopeIsOnSlide = false;
    /**
     * For shotgun pup handles, rifle bolts and hammer pullbacks
     */
    public int hammerDelay = 0;
    /**
     * For animated triggers
     */
    public float triggerRotation = 0F;
    /**
     * The rotation point for the trigger
     */
    public Vector3f triggerRotationPoint = new Vector3f();
    public float triggerDistance = 0.02f;
    /**
     * For animated triggers
     */
    public float leverRotation = 0F;
    /**
     * The rotation point for the trigger
     */
    public Vector3f leverRotationPoint = new Vector3f();
    /**
     * The amount the revolver barrel flips out by
     */
    public float cylinderRotation = 0F;
    /**
     * The rotation point for the revolver cylinder
     */
    public Vector3f cylinderRotationPoint = new Vector3f();
    /**
     * The rotation point for the hammer
     */
    public Vector3f hammerRotationPoint = new Vector3f();
    public float hammerAngle = 75F;
    /**
     * If true, lock the slide when the last bullet is fired
     */
    public boolean slideLockOnEmpty = true;

    public ModelGun(GunRenderConfig config, BaseType type) {
        this.config = config;
        if (this.config.modelFileName.endsWith(".obj")) {
            if (type.isInDirectory) {
                this.staticModel = ObjModelLoader.load(type.contentPack + "/obj/" + type.getAssetDir() + "/" + this.config.modelFileName);
            } else {
                this.staticModel = ObjModelLoader.load(type, "obj/" + type.getAssetDir() + "/" + this.config.modelFileName);
            }
        } else {
            ModularWarfare.LOGGER.info("Internal error: " + this.config.modelFileName + " is not a valid format.");
        }
    }

    public static void glowOn() {
        glowOn(15);
    }

    public static void glowOn(int glow) {
        GL11.glPushAttrib(64);
        try {
            lightmapLastX = OpenGlHelper.lastBrightnessX;
            lightmapLastY = OpenGlHelper.lastBrightnessY;
        } catch (NoSuchFieldError e) {
            optifineBreak = true;
        }
        RenderHelper.disableStandardItemLighting();

        float glowRatioX = Math.min(glow / 15.0F * 240.0F + lightmapLastX, 240.0F);
        float glowRatioY = Math.min(glow / 15.0F * 240.0F + lightmapLastY, 240.0F);
        if (!optifineBreak) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, glowRatioX, glowRatioY);
        }
    }

    public static void glowOff() {
        RenderHelper.enableStandardItemLighting();
        if (!optifineBreak) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmapLastX, lightmapLastY);
        }
        GL11.glPopAttrib();
    }

    public boolean hasArms() {
        return config.arms.leftArm.armPos != null || config.arms.rightArm.armPos != null;
    }

    public boolean isType(GunRenderConfig.Arms.EnumArm arm, GunRenderConfig.Arms.EnumAction action) {
        return config.arms.actionArm == arm && config.arms.actionType == action;
    }

}