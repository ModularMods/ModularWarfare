package com.modularwarfare.client.scope;


import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.model.ModelAttachment;
import com.modularwarfare.client.model.renders.RenderParameters;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.utility.OptifineHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class ScopeUtils {

    public static final int quality = 1024;
    public static int MIRROR_TEX;
    public static ResourceLocation NOT_COMPATIBLE = new ResourceLocation(ModularWarfare.MOD_ID, "textures/gui/notcompatible.png");
    private static Minecraft mc = Minecraft.getMinecraft();
    private static ScopeRenderGlobal scopeRenderGlobal = new ScopeRenderGlobal(mc);

    public boolean hasBeenReseted = true;
    public float mouseSensitivityBackup;
    private Field renderEndNanoTime;

    public ScopeUtils() {
        GL11.glPushMatrix();

        MIRROR_TEX = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, MIRROR_TEX);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, quality, quality, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);


        GL11.glPopMatrix();

        try {
            this.renderEndNanoTime = EntityRenderer.class.getDeclaredField("renderEndNanoTime");
        } catch (Exception ignored) {
        }
        if (this.renderEndNanoTime == null) try {
            this.renderEndNanoTime = EntityRenderer.class.getDeclaredField("field_78534_ac");
        } catch (Exception ignored) {
        }
        if (this.renderEndNanoTime != null) {
            this.renderEndNanoTime.setAccessible(true);
        }
    }


    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GL11.glPushMatrix();
            if (mc.player != null && mc.currentScreen == null) {
                //If player has gun, update scope
                if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun && RenderParameters.adsSwitch != 0 && mc.gameSettings.thirdPersonView == 0) {
                    if (GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Sight) != null) {
                        final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Sight).getItem();
                        if (itemAttachment != null) {
                            if (itemAttachment.type != null) {
                                if (itemAttachment.type.sight.scopeType != WeaponScopeType.REDDOT) {
                                    if (!OptifineHelper.isShadersEnabled()) {
                                        renderWorld(mc, itemAttachment, event.renderTickTime);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            GL11.glPopMatrix();
        }
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                if (ClientRenderHooks.isAimingScope) {
                    if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun && RenderParameters.adsSwitch != 0 && mc.gameSettings.thirdPersonView == 0) {
                        if (GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Sight) != null) {
                            final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Sight).getItem();
                            if (itemAttachment != null) {
                                if (itemAttachment.type != null) {
                                    mc.gameSettings.mouseSensitivity = mouseSensitivityBackup * ((ModelAttachment) itemAttachment.type.model).config.sight.mouseSensitivityFactor;
                                    hasBeenReseted = false;
                                }
                            }
                        }
                    }
                } else if (!hasBeenReseted) {
                    mc.gameSettings.mouseSensitivity = mouseSensitivityBackup;
                    hasBeenReseted = true;
                } else if (mouseSensitivityBackup != mc.gameSettings.mouseSensitivity) {
                    mouseSensitivityBackup = mc.gameSettings.mouseSensitivity;
                }
        }
    }

    public void renderWorld(Minecraft mc, ItemAttachment itemAttachment, float partialTick) {

        float zoom = (50.0f / ((ModelAttachment) itemAttachment.type.model).config.sight.fovZoom);

        GL11.glPushMatrix();

        //Get the current Display and Height/Width
        int width = Display.getWidth();
        int height = Display.getHeight();

        //If necessary, clip width and height to be used for converting the screen into correct size
        if (width > height) {
            width = height;
            if (width > 1024) {
                width = 1024;
                height = 1024;
            }
        } else if (height > width) {
            height = width;
            if (width > 1024) {
                width = 1024;
                height = 1024;
            }
        }

        RenderGlobal renderBackup = mc.renderGlobal;
        //Save the current settings to be reset later
        long endTime = 0;
        int w = mc.displayWidth;
        int h = mc.displayHeight;
        boolean hide = mc.gameSettings.hideGUI;
        int mipmapBackup = mc.gameSettings.mipmapLevels;
        int view = mc.gameSettings.thirdPersonView;
        int limit = mc.gameSettings.limitFramerate;
        RayTraceResult mouseOver = mc.objectMouseOver;
        float fov = mc.gameSettings.fovSetting;
        boolean fboBackup = mc.gameSettings.fboEnable;
        boolean bobbingBackup = mc.gameSettings.viewBobbing;
        float mouseSensitivityBackup = mc.gameSettings.mouseSensitivity;

        mc.renderGlobal = scopeRenderGlobal;

        //Change game settings for the Scope
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.thirdPersonView = 0;
        mc.gameSettings.mipmapLevels = 3;
        mc.gameSettings.fovSetting = zoom;
        mc.gameSettings.fboEnable = false;
        mc.gameSettings.viewBobbing = false;

        mc.displayHeight = height;
        mc.displayWidth = width;
        //Make sure the FOV isn't less than 1
        if (mc.gameSettings.fovSetting < 0) {
            mc.gameSettings.fovSetting = 1;
        }

        if (limit != 0 && renderEndNanoTime != null) {
            try {
                endTime = renderEndNanoTime.getLong(mc.entityRenderer);
            } catch (Exception ignored) {
            }
        }


        int fps = Math.max(30, mc.gameSettings.limitFramerate);
        mc.entityRenderer.renderWorld(partialTick, endTime + (1000000000 / fps));

        GlStateManager.disableDepth();

        //Bind mirror texture and apply the screen to it as a texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, MIRROR_TEX);
        GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 0, 0, width, height, 0);


        if (limit != 0 && renderEndNanoTime != null) {
            try {
                renderEndNanoTime.setLong(mc.entityRenderer, endTime);
            } catch (Exception ignored) {
            }
        }


        //Go back to the original Settings
        mc.objectMouseOver = mouseOver;
        mc.gameSettings.limitFramerate = limit;
        mc.gameSettings.thirdPersonView = view;
        mc.gameSettings.hideGUI = hide;
        mc.gameSettings.mipmapLevels = mipmapBackup;
        mc.displayWidth = w;
        mc.displayHeight = h;
        mc.gameSettings.fboEnable = fboBackup;
        mc.gameSettings.viewBobbing = bobbingBackup;
        mc.gameSettings.mouseSensitivity = mouseSensitivityBackup;

        mc.gameSettings.fovSetting = fov;
        mc.renderGlobal = renderBackup;


        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) {
            scopeRenderGlobal.setWorldAndLoadRenderers((WorldClient) event.getWorld());
        }
    }

}
