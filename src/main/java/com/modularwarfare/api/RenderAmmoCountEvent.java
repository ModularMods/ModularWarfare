package com.modularwarfare.api;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * RenderAmmoCountEvent is fired once the ammo hud is rendering. Canceling this event will stop the rendering of the ammo hud<br>
 * <br>
 * This event is not {@link Cancelable}.<br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 * @author gameusefly
 */
@Cancelable
public class RenderAmmoCountEvent extends Event {

    private final int width;
    private final int height;

    public RenderAmmoCountEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }


}
