package com.modularwarfare.api;

import net.minecraftforge.fml.common.eventhandler.Event;

public class OnTickRenderEvent extends Event {

    public float smooth;

    public OnTickRenderEvent(float smooth) {
        this.smooth = smooth;
    }
}
