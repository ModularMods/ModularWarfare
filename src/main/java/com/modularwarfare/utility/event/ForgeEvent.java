package com.modularwarfare.utility.event;

import net.minecraftforge.common.MinecraftForge;

public class ForgeEvent {

    /**
     * Event registration helper class
     *
     * @param subType
     */
    public ForgeEvent() {
        MinecraftForge.EVENT_BUS.register(this);
    }

}
