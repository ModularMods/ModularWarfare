package com.modularwarfare.client.patch.obfuscate;

public class ObfuscateDummyInterop implements ObfuscateCompatInterop {
    @Override
    public boolean isModLoaded() {
        return false;
    }

    @Override
    public boolean isFixApplied() {
        return false;
    }

    @Override
    public void setFixed() {
    }
}
