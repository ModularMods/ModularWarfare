package com.modularwarfare.client.patch.obfuscate;

public interface ObfuscateCompatInterop {
    boolean isModLoaded();

    boolean isFixApplied();

    void setFixed();
}
