package com.modularwarfare.common.grenades;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.configs.GrenadeRenderConfig;
import com.modularwarfare.client.model.ModelGrenade;
import com.modularwarfare.common.type.BaseType;

public class GrenadeType extends BaseType {


    public GrenadesEnumType grenadeType = GrenadesEnumType.Frag;
    public float fuseTime = 5.0f;
    public boolean damageWorld = false;
    public int explosionPower = 8;
    public float throwStrength = 1f;
    public boolean throwerVulnerable = false;

    public float smokeTime = 10f;

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
