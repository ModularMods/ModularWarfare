package com.modularwarfare.common.guns;


import com.google.gson.annotations.SerializedName;

public enum WeaponSoundType {

    /**
     * The sound played upon dry firing
     */
    @SerializedName("weaponDryFire") DryFire("weaponDryFire", 8, "defemptyclick"),

    /**
     * The sound played upon shooting
     */
    @SerializedName("weaponFire") Fire("weaponFire", 64, null),

    /**
     * The sound played upon shooting with a silencer
     */
    @SerializedName("weaponFireSuppressed") FireSuppressed("weaponFireSuppressed", 32, null),

    /**
     * The sound to play upon shooting on last round
     */
    @SerializedName("weaponFireLast") FireLast("weaponFireLast", 16, null),

    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponReload") Reload("weaponReload", 16, "reload"),

    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponBolt") Pump("weaponBolt", 8, null),

    /**
     * The sound to play upon load bullet
     */
    @SerializedName("weaponBulletLoad") BulletLoad("weaponBulletLoad", 8, null),

    /**
     * The sound to play when a bullet hit a block
     */
    @SerializedName("crack") Crack("crack", 10, "crack"),

    /**
     * The sound to play when equip a gun
     */
    @SerializedName("human.equip.gun") Equip("equip", 8, "human.equip.gun"),

    /**
     * The sound to play when a bullet hit an entity (played to the shooter)
     */
    @SerializedName("hitmarker") Hitmarker("hitmarker", 8, "hitmarker"),

    /**
     * The sound to play when an entity is damaged
     */
    @SerializedName("penetration") Penetration("penetration", 20, "penetration"),

    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponLoad") Load("weaponLoad", 12, "load"),

    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponUnload") Unload("weaponUnload", 12, "unload"),

    /**
     * The sound to play upon reloading when empty
     */
    @SerializedName("weaponReloadEmpty") ReloadEmpty("weaponReloadEmpty", 12, null),

    /**
     * The sound to play upon charging
     */
    @SerializedName("weaponCharge") Charge("weaponCharge", 16, null),

    /**
     * The sound to play upon switching fire modes
     */
    @SerializedName("weaponModeSwitch") ModeSwitch("weaponModeSwitch", 8, "defweaponmodeswitch"),

    /**
     * The sound of flyby
     */
    @SerializedName("bulletFlyBy") FlyBy("bulletFlyBy", 3, "flyby"),

    /**
     * The sound of flyby
     */
    @SerializedName("casing") Casing("casing", 3, "casing"),

    /**
     * The sound of flyby
     */
    @SerializedName("casing_gauge") Casing_Gauge("casing_gauge", 3, "casing_gauge"),


    /**
     * The sound of spray
     */
    @SerializedName("spray") Spray("spray", 8, "spray"),

    /**
     * The fire sound of pack-a-punched weapons
     */
    @SerializedName("punched") Punched("punched", 64, "punched"),


    /**
     * The sound played when entering in attachment mode
     */
    @SerializedName("attachmentOpen") AttachmentOpen("attachmentOpen", 10, "attachment.open"),
    /**
     * The sound played when applyin an attachment
     */
    @SerializedName("attachmentApply") AttachmentApply("attachmentApply", 10, "attachment.apply"),

    /**
     * The sound to play when a bullet hit a block
     */
    @SerializedName("impact.dirt") ImpactDirt("impact.dirt", 10, "impact.dirt"),
    /**
     * The sound to play when a bullet hit a block
     */
    @SerializedName("impact.glass") ImpactGlass("impact.glass", 10, "impact.glass"),
    /**
     * The sound to play when a bullet hit a block
     */
    @SerializedName("impact.metal") ImpactMetal("impact.metal", 10, "impact.metal"),
    /**
     * The sound to play when a bullet hit a block
     */
    @SerializedName("impact.stone") ImpactStone("impact.stone", 10, "impact.stone"),
    /**
     * The sound to play when a bullet hit a block
     */
    @SerializedName("impact.water") ImpactWater("impact.water", 10, "impact.water"),
    /**
     * The sound to play when a bullet hit a block
     */
    @SerializedName("impact.wood") ImpactWood("impact.wood", 10, "impact.wood");


    public String eventName;
    public Integer defaultRange;
    public String defaultSound;

    WeaponSoundType(String eventName, int defaultRange, String defaultSound) {
        this.eventName = eventName;
        this.defaultRange = defaultRange;
        this.defaultSound = defaultSound;
    }

    public static WeaponSoundType fromString(String input) {
        for (WeaponSoundType soundType : values()) {
            if (soundType.toString().equalsIgnoreCase(input)) {
                return soundType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return eventName;
    }

}
