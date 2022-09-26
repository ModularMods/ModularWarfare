package com.modularwarfare.client.patch.galacticraft;

import com.modularwarfare.client.model.layers.RenderLayerBackpack;
import com.modularwarfare.client.model.layers.RenderLayerBody;
import com.modularwarfare.client.model.layers.RenderLayerHeldGun;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;

import java.lang.reflect.Field;

public class GCInteropImpl implements GCCompatInterop {

    public boolean fixApplied;

    public GCInteropImpl() {
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
    @Override
    public void addLayers(final RenderPlayer rp) {
        rp.addLayer(new RenderLayerBackpack(rp, rp.getMainModel().bipedBodyWear));
        rp.addLayer(new RenderLayerBody(rp, rp.getMainModel().bipedBodyWear));
        rp.addLayer(new RenderLayerHeldGun(rp));
    }
    @Override
    public boolean isGCLayer(final LayerRenderer<EntityPlayer> layer) {
        try {
            return layer.getClass().equals(Class.forName("micdoodle8.mods.galacticraft.core.client.render.entities.layer.LayerOxygenTanks")) || layer.getClass().equals(Class.forName("micdoodle8.mods.galacticraft.core.client.render.entities.layer.LayerOxygenGear")) || layer.getClass().equals(Class.forName("micdoodle8.mods.galacticraft.core.client.render.entities.layer.LayerOxygenMask")) || layer.getClass().equals(Class.forName("micdoodle8.mods.galacticraft.core.client.render.entities.layer.LayerOxygenParachute")) || layer.getClass().equals(Class.forName("micdoodle8.mods.galacticraft.core.client.render.entities.layer.LayerFrequencyModule.class"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public void applyFix() {
        try {
            Field field = Class.forName("micdoodle8.mods.galacticraft.core.util.CompatibilityManager").getField("RenderPlayerAPILoaded");
            field.set(Boolean.class, true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
