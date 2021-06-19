package com.modularwarfare.client.patch.galacticraft;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;

public interface GCCompatInterop {
    boolean isModLoaded();

    boolean isFixApplied();

    void setFixed();

    void addLayers(final RenderPlayer p0);

    boolean isGCLayer(final LayerRenderer<EntityPlayer> p0);

    void applyFix();
}
