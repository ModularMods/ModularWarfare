package com.modularwarfare.api;

import net.minecraftforge.fml.common.eventhandler.Event;

public class GunBobbingEvent extends Event {
    public float bobbing;

    public GunBobbingEvent(float bobbing) {
        // TODO Auto-generated constructor stub
        this.bobbing = bobbing;
    }
}
