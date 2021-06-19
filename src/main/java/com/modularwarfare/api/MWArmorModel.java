package com.modularwarfare.api;

import com.google.gson.annotations.SerializedName;

public enum MWArmorModel {

    @SerializedName("headModel") HEAD("headModel"),

    @SerializedName("bodyModel") BODY("bodyModel"),

    @SerializedName("leftLegModel") LEFTLEG("leftLegModel"),
    @SerializedName("rightLegModel") RIGHTLEG("rightLegModel"),

    @SerializedName("leftArmModel") LEFTARM("leftArmModel"),
    @SerializedName("rightArmModel") RIGHTARM("rightArmModel"),

    @SerializedName("leftFootModel") LEFTFOOT("leftFootModel"),
    @SerializedName("rightFootModel") RIGHTFOOT("rightFootModel");

    public String part;

    private MWArmorModel(String part) {
        this.part = part;
    }


}