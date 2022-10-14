package com.modularwarfare.common.textures;

import com.google.gson.annotations.SerializedName;

public enum TextureEnumType {

    /**
     * enum TextureEnumType
     * FLASH, SCOPE, HANDS
     */
    @SerializedName("flash") Flash("flash"),
    @SerializedName("overlay") Overlay("overlay"),
    @SerializedName("hands") Hands("hands");


    public String typeName;

    TextureEnumType(String typeName) {
        this.typeName = typeName;
    }
}