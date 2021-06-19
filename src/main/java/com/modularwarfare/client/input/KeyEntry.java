package com.modularwarfare.client.input;

import net.minecraft.client.settings.KeyBinding;

public class KeyEntry {

    public KeyType keyType;
    public KeyBinding keyBinding;

    public KeyEntry(KeyType keyType) {
        this.keyType = keyType;
        this.keyBinding = new KeyBinding(keyType.displayName, keyType.keyCode, "ModularWarfare");
    }

}
