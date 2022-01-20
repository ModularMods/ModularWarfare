package com.modularwarfare.common.guns;

import com.google.gson.annotations.SerializedName;
import com.modularwarfare.ModConfig;
import com.modularwarfare.common.type.BaseType;

import javax.xml.soap.Text;

public class SkinType {


    public String internalName;
    public String displayName;
    public String skinAsset;
    public Sampling sampling = Sampling.FLAT;

    public enum Sampling {
        @SerializedName("flat") FLAT,
        @SerializedName("linear") LINEAR;
    }

    public Texture[] textures = new Texture[0];

    public enum Texture {
        @SerializedName("basic") BASIC("skins/%s/%s.png"),
        @SerializedName("glow") GLOW("skins/%s/%s_glow.png"),
        @SerializedName("specular") SPECULAR("skins/%s/%s_s.png"),
        @SerializedName("normal") NORMAL("skins/%s/%s_n.png");

        public String format;

        Texture(String format){
            this.format = format;
        }
    }

    public SkinType(){
        /**
         * Disable by default the texture preloading
         */
        //textures[0] = Texture.BASIC;
    }

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
