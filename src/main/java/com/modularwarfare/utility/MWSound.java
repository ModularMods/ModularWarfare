package com.modularwarfare.utility;

import net.minecraft.util.math.BlockPos;

public class MWSound {

    public BlockPos blockPos;
    public String soundName;
    public float volume;
    public float pitch;

    public MWSound(BlockPos blockPos, String sound, float volume, float pitch) {
        this.blockPos = blockPos;
        this.soundName = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

}
