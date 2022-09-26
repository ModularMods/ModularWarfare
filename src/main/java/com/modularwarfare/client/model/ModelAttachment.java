package com.modularwarfare.client.model;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.configs.AttachmentRenderConfig;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.MWModelBase;
import com.modularwarfare.loader.api.ObjModelLoader;

public class ModelAttachment extends MWModelBase {

    public AttachmentRenderConfig config;

    /**
     * For big scopes, so that the player actually looks through them properly
     */
    public float renderOffset = 0F;

    public ModelAttachment(AttachmentRenderConfig config, BaseType type) {
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

    public void renderAttachment(float f) {
        renderPart("attachmentModel", f);
    }

    public void renderScope(float f) {
        renderPart("scopeModel", f);
    }

    public void renderOverlay(float f) {
        renderPart("overlayModel", f);
    }
    
    public void renderOverlaySolid(float f) {
        renderPart("overlaySolidModel", f);
    }

}
