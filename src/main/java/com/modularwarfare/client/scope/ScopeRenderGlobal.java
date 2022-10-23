package com.modularwarfare.client.scope;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ScopeRenderGlobal extends RenderGlobal {
    private boolean shouldLoadRenderers = true;

    public ScopeRenderGlobal(Minecraft mcIn) {
        super(mcIn);
    }

    @Override
    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        shouldLoadRenderers = false;
        super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
    }

    @Override
    public void loadRenderers() {
        //what for?
        /*
        if (shouldLoadRenderers) {
            super.loadRenderers();
        }
        */
        super.loadRenderers();
        shouldLoadRenderers = true;
    }

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {
    }

    @Override
    public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
    }

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {
    }

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
    }
}