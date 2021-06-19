package com.modularwarfare.client.patch.galacticraft;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;

public class GCDummyInterop implements GCCompatInterop {
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

    @Override
    public void addLayers(final RenderPlayer rp) {
    }

    @Override
    public boolean isGCLayer(final LayerRenderer<EntityPlayer> layer) {
        return false;
    }

    @Override
    public void applyFix() {
    }
}
