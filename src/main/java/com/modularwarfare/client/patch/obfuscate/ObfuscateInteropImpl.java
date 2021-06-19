package com.modularwarfare.client.patch.obfuscate;

public class ObfuscateInteropImpl implements ObfuscateCompatInterop {
    public boolean fixApplied;

    public ObfuscateInteropImpl() {
        this.fixApplied = false;
    }

    @Override
    public boolean isModLoaded() {
        return true;
    }

    @Override
    public boolean isFixApplied() {
        return this.fixApplied;
    }

    @Override
    public void setFixed() {
        this.fixApplied = true;
    }
}
