package com.modularwarfare.common.guns;


import com.google.gson.annotations.SerializedName;

public enum WeaponSoundType {

    /**
     * idle loop sound
     */
    @SerializedName("weaponIdle") Idle("weaponIdle", 8, null),
    
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

    @SerializedName("weaponPreReload") PreReload("weaponPreReload", 16, null),
    
    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponReload") Reload("weaponReload", 16, "reload"),
    
    @SerializedName("weaponReloadSecond") ReloadSecond("weaponReloadSecond", 16, null),
    
    @SerializedName("weaponPostReload") PostReload("weaponPostReload", 16, null),
    
    @SerializedName("weaponPostReloadEmpty") PostReloadEmpty("weaponPostReloadEmpty", 16, null),

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

    @SerializedName("weaponPreLoad") PreLoad("weaponPreLoad", 12, null),
    
    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponLoad") Load("weaponLoad", 12, "load"),
    
    @SerializedName("weaponPostLoad") PostLoad("weaponPostLoad", 12, null),

    @SerializedName("weaponPreUnload") PreUnload("weaponPreUnload", 12, null),
    
    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponUnload") Unload("weaponUnload", 12, "unload"),

    /**
     * The sound to play upon reloading
     */
    @SerializedName("weaponDraw") Draw("weaponDraw", 12, "draw"),


    @SerializedName("weaponPostUnload") PostUnload("weaponPostUnload", 12, null),

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
    @SerializedName("impact.wood") ImpactWood("impact.wood", 10, "impact.wood"),

    /**
     * The sound played when drawing a melee weapon
     */
    @SerializedName("meleeDraw") MeleeDraw("meleeDraw", 5, "melee.draw"),
    /**
     * The sound played when inspecting a melee weapon
     */
    @SerializedName("meleeInspect") MeleeInspect("meleeInspect", 5, "melee.inspect"),
    /**
     * The sound played when attacking a melee weapon
     */
    @SerializedName("meleeAttack") MeleeAttack("meleeAttack", 5, "melee.attack");


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
