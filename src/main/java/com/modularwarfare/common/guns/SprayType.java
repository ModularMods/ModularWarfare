package com.modularwarfare.common.guns;

import com.modularwarfare.common.type.BaseType;

public class SprayType extends BaseType {

    public String skinName;
    public int usableMaxAmount = 5;

    @Override
    public void loadExtraValues() {
        if (maxStackSize == null)
            maxStackSize = 1;

        loadBaseValues();

    }

    @Override
    public void reloadModel() {
    }

    @Override
    public String getAssetDir() {
        return "sprays";
    }

}
