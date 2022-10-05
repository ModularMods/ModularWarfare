package com.modularwarfare.melee.common.melee;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.enhanced.models.EnhancedModel;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.melee.client.configs.MeleeRenderConfig;

public class MeleeType extends BaseType {

    public float damage;
    public boolean resetAttackOnClick = false;
    public boolean destroyBlocks = false;
    public boolean swing = true;

    public MeleeType() {
        maxStackSize = 1;
    }

    @Override
    public void loadExtraValues() {
        maxStackSize = 1;

        loadBaseValues();
    }

    @Override
    public void reloadModel() {
        enhancedModel = new EnhancedModel(ModularWarfare.getRenderConfig(this, MeleeRenderConfig.class), this);
    }

    @Override
    public String getAssetDir() {
        return "melee";
    }
}
