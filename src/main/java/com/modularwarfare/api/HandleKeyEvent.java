package com.modularwarfare.api;

import com.modularwarfare.client.input.KeyType;
import net.minecraftforge.fml.common.eventhandler.Event;

public class HandleKeyEvent extends Event {

    public KeyType keyType;
    public HandleKeyEvent(KeyType keyType) {
        this.keyType = keyType;
    }
}
