package com.modularwarfare.client.model;

import com.modularwarfare.common.guns.BulletType;
import com.modularwarfare.loader.MWModelBase;
import com.modularwarfare.loader.api.ObjModelLoader;

public class ModelShell extends MWModelBase {


    public ModelShell(BulletType bulletType, boolean loadDefault) {
        if (bulletType.isInDirectory) {
            if (!loadDefault) {
                this.staticModel = ObjModelLoader.load(bulletType.contentPack + "/obj/shells/" + bulletType.shellModelFileName);
            } else {
                this.staticModel = ObjModelLoader.load(bulletType.contentPack + "/obj/shells/" + bulletType.defaultModel);
            }
        } else {
            if (!loadDefault) {
                this.staticModel = ObjModelLoader.load(bulletType, "obj/shells/" + bulletType.shellModelFileName);
            } else {
                this.staticModel = ObjModelLoader.load(bulletType, "obj/shells/" + bulletType.defaultModel);
            }
        }
    }

    public void renderShell(float f) {
        renderPart("shellModel", f);
    }

}
