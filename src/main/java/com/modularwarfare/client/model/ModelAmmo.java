package com.modularwarfare.client.model;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.configs.AmmoRenderConfig;
import com.modularwarfare.client.fpp.basic.models.objects.RenderVariables;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.MWModelBase;
import com.modularwarfare.loader.api.ObjModelLoader;
import org.lwjgl.util.vector.Vector3f;

import java.util.HashMap;

public class ModelAmmo extends MWModelBase {

    public AmmoRenderConfig config;

    public Vector3f thirdPersonOffset = new Vector3f();

    public HashMap<Integer, RenderVariables> magCountOffset = new HashMap<Integer, RenderVariables>();

    public ModelAmmo(AmmoRenderConfig config, BaseType type) {
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

    public void renderAmmo(float f) {
        renderPart("ammoModel", f);
    }


}
