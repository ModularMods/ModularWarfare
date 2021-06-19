package com.modularwarfare.common.guns;

import com.google.gson.annotations.SerializedName;

public enum WeaponType {

    /**
     * enum weaponType
     * CUSTOM, PISTOL, MP, SMG, CARBINE, RIFLE, AR, DMR, SNIPER, SHOTGUN, etc
     */
    @SerializedName("custom") Custom("custom"),
    @SerializedName("pistol") Pistol("pistol"),
    @SerializedName("revolver") Revolver("revolver"),
    @SerializedName("mp") MP("mp"),
    @SerializedName("smg") SMG("smg"),
    @SerializedName("carbine") Carbine("carbine"),
    @SerializedName("rifle") RIFLE("rifle"),
    @SerializedName("ar") AR("ar"),
    @SerializedName("dmr") DMR("dmr"),
    @SerializedName("semisniper") SemiSniper("semisniper"),
    @SerializedName("boltsniper") BoltSniper("boltsniper"),
    @SerializedName("shotgun") Shotgun("shotgun");


    public String typeName;

    WeaponType(String typeName) {
        this.typeName = typeName;
    }

    public static WeaponType fromEventName(String typeName) {
        if (typeName != null) {
            for (WeaponType soundType : values()) {
                if (soundType.typeName.equalsIgnoreCase(typeName)) {
                    return soundType;
                }
            }
        }
        return null;
    }

}