package com.modularwarfare.common.grenades;

import com.google.gson.annotations.SerializedName;

public enum GrenadesEnumType {

    /**
     * enum weaponType
     * CUSTOM, PISTOL, MP, SMG, CARBINE, RIFLE, AR, DMR, SNIPER, SHOTGUN, etc
     */
    @SerializedName("frag") Frag("frag"),
    @SerializedName("smoke") Smoke("smoke"),
    @SerializedName("stun") Stun("stun");


    public String typeName;

    GrenadesEnumType(String typeName) {
        this.typeName = typeName;
    }
}