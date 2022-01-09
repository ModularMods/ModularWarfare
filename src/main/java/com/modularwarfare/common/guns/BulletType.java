package com.modularwarfare.common.guns;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.config.BulletRenderConfig;
import com.modularwarfare.client.model.ModelBullet;
import com.modularwarfare.client.model.ModelShell;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.MWModelBase;

import java.util.HashMap;

public class BulletType extends BaseType {

    // Bullet Type
    public HashMap<String, BulletProperty> bulletProperties;

    public float bulletDamageFactor = 1.0f;
    public float bulletAccuracyFactor = 1.0f;
    public boolean isSlug = false;

    /**
     * If the ammo model need to be rendered on guns
     */
    public boolean renderBulletModel = false;

    public transient String defaultModel = "default.obj";
    public String shellModelFileName = defaultModel;
    public transient MWModelBase shell;

    public String shellSound = "casing";

    @Override
    public void loadExtraValues() {
        if (maxStackSize == null)
            maxStackSize = 64;

        loadBaseValues();
    }

    @Override
    public void reloadModel() {
        if (renderBulletModel) {
            model = new ModelBullet(ModularWarfare.getRenderConfig(this, BulletRenderConfig.class), this);
        }
        if (!shellModelFileName.equals(defaultModel)) {
            shell = new ModelShell(this, false);
        } else {
            shell = new ModelShell(this, true);
        }
    }

    @Override
    public String getAssetDir() {
        return "bullets";
    }

}
