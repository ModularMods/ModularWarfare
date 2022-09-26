package com.modularwarfare.client.model;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.configs.GrenadeRenderConfig;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.MWModelBase;
import com.modularwarfare.loader.api.ObjModelLoader;
import com.modularwarfare.loader.api.model.ObjModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class ModelGrenade extends MWModelBase {

    public GrenadeRenderConfig config;

    public ModelGrenade(GrenadeRenderConfig config, BaseType type) {
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

    public void render(String modelPart, float scale, float modelScale) {
        GlStateManager.pushMatrix();

        ObjModelRenderer part = this.staticModel.getPart(modelPart);
        if (part != null) {
            if (part != null) {
                part.render(scale * modelScale);
            }
        }
        GlStateManager.popMatrix();

    }

}