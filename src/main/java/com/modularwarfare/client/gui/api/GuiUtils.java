package com.modularwarfare.client.gui.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class GuiUtils {

    /**
     * Render Text
     *
     * @param text   - Given Text (String)
     * @param givenX - Given Text Position X
     * @param givenY - Given Text Position Y
     * @param color  - Given Color
     */
    public static void renderText(String text, int givenX, int givenY, int color) {
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRenderer.drawString(text, givenX, givenY, color);
        GL11.glPopMatrix();
    }

    /**
     * Render Text With Shadow
     *
     * @param text   - Given Text (String)
     * @param givenX - Given Text Position X
     * @param givenY - Given Text Position Y
     * @param color  - Given Color
     */
    public static void renderTextWithShadow(String text, int givenX, int givenY, int color) {
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRenderer.drawStringWithShadow(text, givenX, givenY, color);
        GL11.glPopMatrix();
    }

    /**
     * Render Centered Text
     *
     * @param text   - Given Text (String)
     * @param givenX - Given Text Position X
     * @param givenY - Given Text Position Y
     * @param color  - Given Color
     */
    public static void renderCenteredText(String text, int givenX, int givenY, int color) {
        GL11.glPushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        renderText(text, givenX - mc.fontRenderer.getStringWidth(text) / 2, givenY, color);
        GL11.glPopMatrix();
    }

    /**
     * Render Text Scaled
     *
     * @param text       - Given Text (String)
     * @param givenX     - Given Position X
     * @param givenY     - Given Position Y
     * @param color      - Given Color
     * @param givenScale - Given Scale
     */
    public static void renderTextScaled(String text, int givenX, int givenY, int color, double givenScale) {

        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        renderText(text, 0, 0, color);
        GL11.glPopMatrix();

    }

    /**
     * Render Centered Text Scaled
     *
     * @param text       - Given Text (String)
     * @param givenX     - Given Text Position X
     * @param givenY     - Given Text Position Y
     * @param color      - Given Text Color
     * @param givenScale - Given Scale
     */
    public static void renderCenteredTextScaled(String text, int givenX, int givenY, int color, double givenScale) {

        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        renderCenteredText(text, 0, 0, color);
        GL11.glPopMatrix();

    }

    public static void renderCenteredTextScaledWithOutline(String text, int givenX, int givenY, int color, int givenOutlineColor, double givenScale) {

        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0);
        GL11.glScaled(givenScale, givenScale, givenScale);
        renderCenteredTextWithOutline(text, 0, 0, color, givenOutlineColor);
        GL11.glPopMatrix();

    }

    /**
     * Render Text With an Outline
     *
     * @param text         - Given Text (String)
     * @param x            - Given Text Position X
     * @param y            - Given Text Position Y
     * @param color        - Given Text Color
     * @param outlineColor - Given Outline Color
     */
    public static void renderTextWithOutline(String text, int x, int y, int color, int outlineColor) {

        renderText(text, x - 1, y + 1, outlineColor);
        renderText(text, x, y + 1, outlineColor);
        renderText(text, x + 1, y + 1, outlineColor);
        renderText(text, x - 1, y, outlineColor);
        renderText(text, x + 1, y, outlineColor);
        renderText(text, x - 1, y - 1, outlineColor);
        renderText(text, x, y - 1, outlineColor);
        renderText(text, x + 1, y - 1, outlineColor);

        renderText(text, x, y, color);

    }

    /**
     * Render Text Scaled With an Outline
     *
     * @param text         - Given Text (String)
     * @param x            - Given Text Position X
     * @param y            - Given Text Position Y
     * @param color        - Given Text Color
     * @param outlineColor - Given Outline Color
     * @param givenScale   - Given Text Scale
     */
    public static void renderTextScaledWithOutline(String text, int x, int y, int color, int outlineColor, double givenScale) {

        renderTextScaled(text, x - 1, y + 1, outlineColor, givenScale);
        renderTextScaled(text, x, y + 1, outlineColor, givenScale);
        renderTextScaled(text, x + 1, y + 1, outlineColor, givenScale);
        renderTextScaled(text, x - 1, y, outlineColor, givenScale);
        renderTextScaled(text, x + 1, y, outlineColor, givenScale);
        renderTextScaled(text, x - 1, y - 1, outlineColor, givenScale);
        renderTextScaled(text, x, y - 1, outlineColor, givenScale);
        renderTextScaled(text, x + 1, y - 1, outlineColor, givenScale);

        renderTextScaled(text, x, y, color, givenScale);

    }

    /**
     * Render Centered Text With an Outline
     *
     * @param text         - Given Text (String)
     * @param x            - Given Text Position X
     * @param y            - Given Text Position Y
     * @param color        - Given Text Color
     * @param outlineColor - Given Outline Color
     */
    public static void renderCenteredTextWithOutline(String text, int x, int y, int color, int outlineColor) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;

        renderText(text, x - 1 - fr.getStringWidth(text) / 2, y + 1, outlineColor);
        renderText(text, x - fr.getStringWidth(text) / 2, y + 1, outlineColor);
        renderText(text, x + 1 - fr.getStringWidth(text) / 2, y + 1, outlineColor);
        renderText(text, x - 1 - fr.getStringWidth(text) / 2, y, outlineColor);
        renderText(text, x + 1 - fr.getStringWidth(text) / 2, y, outlineColor);
        renderText(text, x - 1 - fr.getStringWidth(text) / 2, y - 1, outlineColor);
        renderText(text, x - fr.getStringWidth(text) / 2, y - 1, outlineColor);
        renderText(text, x + 1 - fr.getStringWidth(text) / 2, y - 1, outlineColor);

        renderText(text, x - fr.getStringWidth(text) / 2, y, color);
    }

    /**
     * Render a Rectangle
     *
     * @param givenX      - Given Start Position X
     * @param givenY      - Given start Position Y
     * @param givenWidth  - Given Rectangle Width
     * @param givenHeight - Given Rectangle Height
     * @param givenColor  - Given Rectangle Color
     */
    public static void renderRect(int givenX, int givenY, int givenWidth, int givenHeight, int givenColor) {

        givenWidth = givenX + givenWidth;
        givenHeight = givenY + givenHeight;

        if (givenX < givenWidth) {
            int i = givenX;
            givenX = givenWidth;
            givenWidth = i;
        }

        if (givenY < givenHeight) {
            int j = givenY;
            givenY = givenHeight;
            givenHeight = j;
        }

        float f3 = (float) (givenColor >> 24 & 255) / 255.0F;

        float f = (float) (givenColor >> 16 & 255) / 255.0F;
        float f1 = (float) (givenColor >> 8 & 255) / 255.0F;
        float f2 = (float) (givenColor & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double) givenX, (double) givenHeight, 0.0D).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenHeight, 0.0D).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenY, 0.0D).endVertex();
        bufferbuilder.pos((double) givenX, (double) givenY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();

    }

    public static void renderRect(int givenX, int givenY, int givenWidth, int givenHeight, int givenColor, float givenAlpha) {

        givenWidth = givenX + givenWidth;
        givenHeight = givenY + givenHeight;

        if (givenX < givenWidth) {
            int i = givenX;
            givenX = givenWidth;
            givenWidth = i;
        }

        if (givenY < givenHeight) {
            int j = givenY;
            givenY = givenHeight;
            givenHeight = j;
        }

        float f3 = givenAlpha;

        float f = (float) (givenColor >> 16 & 255) / 255.0F;
        float f1 = (float) (givenColor >> 8 & 255) / 255.0F;
        float f2 = (float) (givenColor & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double) givenX, (double) givenHeight, 0.0D).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenHeight, 0.0D).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenY, 0.0D).endVertex();
        bufferbuilder.pos((double) givenX, (double) givenY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }

    public static void renderRectWithFade(int givenX, int givenY, int givenWidth, int givenHeight, int givenColor, float givenFade) {

        givenWidth = givenX + givenWidth;
        givenHeight = givenY + givenHeight;

        if (givenX < givenWidth) {
            int i = givenX;
            givenX = givenWidth;
            givenWidth = i;
        }

        if (givenY < givenHeight) {
            int j = givenY;
            givenY = givenHeight;
            givenHeight = j;
        }

        float f3 = (float) (givenColor >> 24 & 255) / 255.0F;
        float f = (float) (givenColor >> 16 & 255) / 255.0F;
        float f1 = (float) (givenColor >> 8 & 255) / 255.0F;
        float f2 = (float) (givenColor & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.color(f, f1, f2, givenFade);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double) givenX, (double) givenHeight, 0.0D).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenHeight, 0.0D).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenY, 0.0D).endVertex();
        bufferbuilder.pos((double) givenX, (double) givenY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

    }

    /**
     * Draw a Rectangle with an Outline
     *
     * @param givenX            - Given Start Position X
     * @param givenY            - Given start Position Y
     * @param givenWidth        - Given Rectangle Width
     * @param givenHeight       - Given Rectangle Height
     * @param givenColor        - Given Rectangle Color
     * @param givenOutlineColor - Given Rectangle Outline Color
     * @param outlineThickness  - Given Outline Thickness
     */
    public static void renderRectWithOutline(int givenX, int givenY, int givenWidth, int givenHeight, int givenColor, int givenOutlineColor, int outlineThickness) {
        renderRect(givenX - outlineThickness, givenY - outlineThickness, givenWidth + (outlineThickness * 2), givenHeight + (outlineThickness * 2), givenOutlineColor);
        renderRect(givenX, givenY, givenWidth, givenHeight, givenColor);
    }

    public static void renderOutline(int givenX, int givenY, int givenWidth, int givenHeight, int givenOutlineColor, int outlineThickness){
        renderRect(givenX - outlineThickness, givenY - outlineThickness, givenWidth + (outlineThickness * 2) , outlineThickness, givenOutlineColor);
        renderRect(givenX - outlineThickness + 1, (givenY+givenHeight) - outlineThickness +1, givenWidth + (outlineThickness * 2)-2, outlineThickness, givenOutlineColor);

        renderRect(givenX - outlineThickness, givenY - outlineThickness + 1, outlineThickness, givenHeight + (outlineThickness * 2) - 1, givenOutlineColor);
        renderRect((givenX+givenWidth) + outlineThickness -1, givenY - outlineThickness + 1, outlineThickness, givenHeight + (outlineThickness * 2) - 1, givenOutlineColor);

    }

    public static void renderRectWithGradient(int givenX, int givenY, int givenWidth, int givenHeight, int startColor, int endColor, double givenZLevel) {

        GlStateManager.pushMatrix();

        givenWidth = givenX + givenWidth;
        givenHeight = givenY + givenHeight;

        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double) givenWidth, (double) givenY, givenZLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double) givenX, (double) givenY, givenZLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos((double) givenX, (double) givenHeight, givenZLevel).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenHeight, givenZLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();

        GlStateManager.popMatrix();

    }

    public static void renderRectWithGradientWithAlpha(int givenX, int givenY, int givenWidth, int givenHeight, int startColor, int endColor, double givenZLevel, float givenAlphaStart, float givenAlphaEnd) {

        givenWidth = givenX + givenWidth;
        givenHeight = givenY + givenHeight;

        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double) givenWidth, (double) givenY, givenZLevel).color(f1, f2, f3, givenAlphaStart).endVertex();
        bufferbuilder.pos((double) givenX, (double) givenY, givenZLevel).color(f1, f2, f3, givenAlphaStart).endVertex();
        bufferbuilder.pos((double) givenX, (double) givenHeight, givenZLevel).color(f5, f6, f7, givenAlphaEnd).endVertex();
        bufferbuilder.pos((double) givenWidth, (double) givenHeight, givenZLevel).color(f5, f6, f7, givenAlphaEnd).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();

    }

    public static void renderPositionedImageNoDepth(ResourceLocation par1, double par2, double par3, double par4, float par5, float width, float height) {

        GL11.glPushMatrix();

        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        renderPositionedImage(par1, par2, par3, par4, par5, width, height);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();

        GL11.glPopMatrix();

    }

    public static void renderPositionedImage(ResourceLocation par1, double par2, double par3, double par4, float par5, float width, float height) {

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        GL11.glPushMatrix();

        GL11.glTranslated(par2, par3, par4);
        GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        GL11.glRotatef(-player.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(player.rotationPitch, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.03f, -0.03f, 0.03f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        renderImage(-width / 2, -height / 2, par1, width, height);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

    }

    public static void renderPositionedTextScaled(String givenText, double par2, double par3, double par4, float par5, int givenColor) {

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;

        GL11.glPushMatrix();

        GL11.glTranslated(par2, par3, par4);
        GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        GL11.glRotatef(-player.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(player.rotationPitch, 1.0F, 0.0F, 0.0F);

        GL11.glScalef(-0.03f, -0.03f, 0.03f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        renderCenteredTextScaled(givenText, 0, 0, givenColor, par5);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

    }

    public static void renderImage(double x, double y, ResourceLocation image, double width, double height) {

        renderColor(0xFFFFFF);

        Minecraft.getMinecraft().renderEngine.bindTexture(image);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_FASTEST);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_POINT_SMOOTH);

    }

    public static void renderImageTransparent(double x, double y, ResourceLocation image, double width, double height, double alpha) {

        renderColor(0xFFFFFF, alpha);

        Minecraft.getMinecraft().renderEngine.bindTexture(image);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_FASTEST);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_POINT_SMOOTH);

    }

    public static void renderImageCentered(double givenX, double givenY, ResourceLocation givenTexture, double givenWidth, double givenHeight) {

        GL11.glPushMatrix();
        renderImage(givenX - (givenWidth / 2), givenY - (givenHeight / 2), givenTexture, givenWidth, givenHeight);
        GL11.glPopMatrix();

    }

    public static void renderImageCenteredTransparent(double givenX, double givenY, ResourceLocation givenTexture, double givenWidth, double givenHeight, double alpha) {

        GlStateManager.pushMatrix();
        renderImageTransparent(givenX - (givenWidth / 2), givenY - (givenHeight / 2), givenTexture, givenWidth, givenHeight, alpha);
        GlStateManager.popMatrix();

    }

    public static void renderImageCenteredScaled(double givenX, double givenY, ResourceLocation givenTexture, double givenWidth, double givenHeight, float givenScale) {

        GlStateManager.pushMatrix();

        GlStateManager.translate(givenX - (givenX * givenScale), givenY - (givenY * givenScale), 0);
        GlStateManager.scale(givenScale, givenScale, givenScale);
        renderImageCentered(givenX, givenY, givenTexture, givenWidth, givenHeight);

        GlStateManager.popMatrix();

    }

    /**
     * Render an image with Transparent Capability
     *
     * @param givenX       - Given X Position
     * @param givenY       - Given Y Position
     * @param givenTexture - Given Texture
     * @param givenWidth   - Given Width
     * @param givenHeight  - Given Height
     * @param givenAlpha   - Given Alpha
     */
    public static void renderImageTransparent(int givenX, int givenY, ResourceLocation givenTexture, int givenWidth, int givenHeight, double givenAlpha) {
        GL11.glPushMatrix();
        GL11.glColor4d(255, 255, 255, givenAlpha);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        renderImage(givenX, givenY, givenTexture, givenWidth, givenHeight);
        GL11.glDisable(GL11.GL_POINT_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4d(255, 255, 255, 255);
        GL11.glPopMatrix();
    }

    /**
     * Get Scoreboard Title - get the Title of the Scoreboard GUI Element
     *
     * @param mc - Given Minecraft Instance
     * @return - Returns the Scoreboard as a String Value (with no formatting)
     */
    public static String getScoreboardTitle(Minecraft mc) {

        if (mc.world != null && mc.world.getScoreboard() != null) {

            ScoreObjective scoreobjective = mc.world.getScoreboard().getObjectiveInDisplaySlot(1);

            if (scoreobjective != null) {
                String scoreTitle = scoreobjective.getDisplayName()
                        .replace("§", "")
                        .replaceAll("[a-z]", "")
                        .replaceAll("[0-9]", "");

                return scoreTitle;

            }

        } else {
            return null;
        }

        return null;

    }

    public static void renderColor(int par1) {
        Color color = Color.decode("" + par1);
        float red = color.getRed() / 255.0F;
        float green = color.getGreen() / 255.0F;
        float blue = color.getBlue() / 255.0F;
        GL11.glColor3f(red, green, blue);
    }

    public static void renderColor(int par1, double alpha) {
        Color color = Color.decode("" + par1);
        double red = color.getRed() / 255.0;
        double green = color.getGreen() / 255.0;
        double blue = color.getBlue() / 255.0;
        GL11.glColor4d(red, green, blue, alpha);
    }

    public static void renderCenteredTextWithShadow(String text, int x, int y, int color, int outlineColor) {
        GlStateManager.pushMatrix();
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;
        renderTextWithShadow(text, x - fr.getStringWidth(text) / 2, y, color);
        GlStateManager.popMatrix();
    }

    public static boolean isInBox(int x, int y, int width, int height, int checkX, int checkY) {
        return checkX >= x && checkY >= y && checkX <= x + width && checkY <= y + height;
    }

    public static void renderBoundingBox(AxisAlignedBB givenBB) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();

        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static void renderBoundingBoxFilled(AxisAlignedBB givenBB) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        tessellator.draw();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        tessellator.draw();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        tessellator.draw();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        tessellator.draw();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        tessellator.draw();

        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.minX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.minZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.maxY, givenBB.maxZ).endVertex();
        worldRenderer.pos(givenBB.maxX, givenBB.minY, givenBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static double getDistanceToClientCamera(double x, double y, double z) {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        double d3 = renderManager.viewerPosX - x;
        double d4 = renderManager.viewerPosY - y;
        double d5 = renderManager.viewerPosZ - z;
        return (double) MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
    }

    public static BufferedImage downloadBanner(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            System.out.println("Errors reading online image: '" + url + "'");
        }
        return null;
    }

}
