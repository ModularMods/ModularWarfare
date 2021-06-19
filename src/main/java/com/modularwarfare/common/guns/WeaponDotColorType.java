package com.modularwarfare.common.guns;

import com.google.gson.annotations.SerializedName;

public enum WeaponDotColorType {

    @SerializedName("red") RED,

    @SerializedName("blue") BLUE,

    @SerializedName("green") GREEN;


    public static WeaponDotColorType fromString(String modeName) {
        for (WeaponDotColorType dotColorType : values()) {
            if (dotColorType.name().equalsIgnoreCase(modeName)) {
                return dotColorType;
            }
        }
        return null;
    }

}
