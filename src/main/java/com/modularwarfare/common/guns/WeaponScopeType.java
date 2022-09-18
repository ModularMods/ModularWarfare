package com.modularwarfare.common.guns;

import com.google.gson.annotations.SerializedName;

/**
 * WTF???
 * R.I.P
 * */
@Deprecated
public enum WeaponScopeType {

    @SerializedName("default") DEFAULT,

    @SerializedName("reddot") REDDOT,

    @SerializedName("2x") TWO,

    @SerializedName("4x") FOUR,

    @SerializedName("8x") EIGHT,

    @SerializedName("15x") FIFTEEN;

    public static WeaponScopeType fromString(String modeName) {
        for (WeaponScopeType scopeType : values()) {
            if (scopeType.name().equalsIgnoreCase(modeName)) {
                return scopeType;
            }
        }
        return null;
    }

}
