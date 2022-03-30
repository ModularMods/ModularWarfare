package com.modularwarfare.client.fpp.enhanced;

import com.google.gson.annotations.SerializedName;

public enum AnimationType {

    @SerializedName("default") DEFAULT,
    @SerializedName("draw") DRAW,
    @SerializedName("aimIn") AIM_IN,
    @SerializedName("aimOut") AIM_OUT,
    @SerializedName("reload") RELOAD;

    public static AnimationType fromString(String modeName) {
        for (AnimationType animationType : values()) {
            if (animationType.name().equalsIgnoreCase(modeName)) {
                return animationType;
            }
        }
        return null;
    }

}
