package com.modularwarfare.common.grenades;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.config.GrenadeRenderConfig;
import com.modularwarfare.client.model.ModelGrenade;
import com.modularwarfare.common.type.BaseType;

public class GrenadeType extends BaseType {

    public int fuseTime = 5;
    public boolean damageWorld = false;
    public int explosionPower = 5;
    public float throwStrength = 1f;
    public boolean throwerVulnerable = false;

    @Override
    public void loadExtraValues() {
        if (maxStackSize == null)
            maxStackSize = 1;

        loadBaseValues();

    }

    @Override
    public void reloadModel() {
        model = new ModelGrenade(ModularWarfare.getRenderConfig(this, GrenadeRenderConfig.class), this);
    }

    @Override
    public String getAssetDir() {
        return "grenades";
    }

}
