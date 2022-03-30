package com.modularwarfare.common.guns;

import com.google.gson.annotations.SerializedName;

public enum WeaponAnimationType {

    @SerializedName("basic") BASIC,

    @SerializedName("enhanced") ENHANCED;

    public static WeaponAnimationType fromString(String modeName) {
        for (WeaponAnimationType animationType : values()) {
            if (animationType.name().equalsIgnoreCase(modeName)) {
                return animationType;
            }
        }
        return null;
    }

}
