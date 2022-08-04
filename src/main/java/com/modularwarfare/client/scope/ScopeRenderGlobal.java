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

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class ScopeRenderGlobal extends RenderGlobal {
    private Field fieldRenderDistanceChunks;
    private boolean shouldLoadRenderers = true;
    public ScopeRenderGlobal(Minecraft mcIn) {
        super(mcIn);
        try {
            fieldRenderDistanceChunks = getClass().getSuperclass().getDeclaredField("field_72739_F"); // Fix NotSuchFieldException
            fieldRenderDistanceChunks.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        try {
            fieldRenderDistanceChunks.set(this, 2);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        shouldLoadRenderers = false;
        super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
    }
    @Override
    public void loadRenderers() {
        if (shouldLoadRenderers) {
            super.loadRenderers();
        }
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