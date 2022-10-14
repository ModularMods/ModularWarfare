package com.modularwarfare.objects;

import com.modularwarfare.common.guns.WeaponSoundType;

public class SoundEntry {

    public WeaponSoundType soundEvent;
    public String soundName;
    public int soundDelay = 0;
    public float soundVolumeMultiplier = 1f;
    public float soundFarVolumeMultiplier = 1f;
    public float soundPitch = 1f;
    public float soundRandomPitch = 5f;
    public Integer soundRange = 16;

    public String soundNameDistant;
    public Integer soundMaxRange;

}