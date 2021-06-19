package com.modularwarfare.common.guns;

import com.modularwarfare.common.type.BaseType;

public class SkinType {

    public String internalName;
    public String displayName;
    public String skinAsset;

    public static SkinType getDefaultSkin(BaseType baseType) {
        SkinType skinType = new SkinType();
        skinType.internalName = baseType.internalName;
        skinType.skinAsset = skinType.getSkin();
        skinType.displayName = baseType.displayName + " - Default";
        return skinType;
    }

    public String getSkin() {
        return skinAsset != null ? skinAsset : internalName;
    }

    @Override
    public String toString() {
        return skinAsset;
    }

}
