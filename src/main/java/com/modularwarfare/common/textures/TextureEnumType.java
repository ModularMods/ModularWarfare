package com.modularwarfare.common.textures;

import com.google.gson.annotations.SerializedName;

public enum TextureEnumType {

    /**
     * enum TextureEnumType
     * FLASH, SCOPE
     */
    @SerializedName("flash") Flash("flash"),
    @SerializedName("overlay") Overlay("overlay");


    public String typeName;

    TextureEnumType(String typeName) {
        this.typeName = typeName;
    }
}