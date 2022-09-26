package com.modularwarfare.client.hud;

import com.modularwarfare.utility.OptifineHelper;
import com.modularwarfare.utility.RenderHelperMW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

public class FlashSystem {

    private static Minecraft mc = Minecraft.getMinecraft();

    public static final int quality = 1024;
    public static int FLASHED_TEX;
    public static boolean hasTookScreenshot = false;
    public static int flashValue;
    private Field renderEndNanoTime;

    public FlashSystem() {
        GL11.glPushMatrix();

        FLASHED_TEX = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, FLASHED_TEX);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, quality, quality, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
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
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            if (flashValue != 0) {
                int width = Display.getWidth();
                int height = Display.getHeight();
                int x = 0;
                int y = 0;
                GL11.glPushMatrix();
                GL11.glScalef(0.5f, 0.5f,0);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();

                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.color(1.0f, 1.0f, 1.0f, (FlashSystem.flashValue / 255.0F));
                GlStateManager.bindTexture(FLASHED_TEX);
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, 0.0D).endVertex();
                bufferbuilder.pos(x + width, y + height, 0.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.pos(x + width, y, 0.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 1.0D).endVertex();
                tessellator.draw();
                GlStateManager.disableBlend();
                GL11.glPopMatrix();
                RenderHelperMW.renderRectAlphaComp(0, 0, mc.displayWidth, mc.displayHeight, 0xFFFFFF, FlashSystem.flashValue);
            }
        }
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            if(!hasTookScreenshot && flashValue > 0) {
                GL11.glPushMatrix();
                if (mc.player != null && mc.currentScreen == null) {
                    if (!OptifineHelper.isShadersEnabled()) {
                        takeScreenShot(mc, event.renderTickTime);
                    }
                }
                GL11.glPopMatrix();
                hasTookScreenshot = true;
            }
        }
    }

    public void takeScreenShot(Minecraft mc, float partialTick) {
        float zoom = mc.gameSettings.fovSetting;

        GL11.glPushMatrix();

        //Get the current Display and Height/Width
        int width = Display.getWidth();
        int height = Display.getHeight();

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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, FLASHED_TEX);
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

}
