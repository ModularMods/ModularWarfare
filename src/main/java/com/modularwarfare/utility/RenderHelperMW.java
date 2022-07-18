package com.modularwarfare.utility;

import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

public class RenderHelperMW {

    private static final DecimalFormat energyValue = new DecimalFormat("###,###,###,###,###");

    public static void renderItemStack(ItemStack stack, int x, int y, float partialTicks, boolean isJustOne) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.enableGUIStandardItemLighting();
        if (stack != null) {
            float f1 = (float) stack.getAnimationsToGo() - partialTicks;
            if (f1 > 0.0F) {
                GL11.glPushMatrix();
                float f2 = 1.0F + f1 / 5.0F;
                GL11.glTranslatef((float) (x + 8), (float) (y + 12), 0.0F);
                GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
            }
            Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
            if (f1 > 0.0F) GL11.glPopMatrix();
            ItemStack fits = new ItemStack(stack.getItem(), 1);
            if (isJustOne)
                Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, fits, x, y, null);
            else
                Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, stack, x, y, null);
        }
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
    }

    public static void renderText(String text, int posX, int posY, int color) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRenderer.drawString(text, posX, posY, color);
    }

    public static void renderTextWithShadow(String text, int posX, int posY, int color) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.fontRenderer.drawStringWithShadow(text, posX, posY, color);
    }

    public static void renderCenteredText(String text, int posX, int posY, int color) {
        Minecraft mc = Minecraft.getMinecraft();
        renderText(text, posX - mc.fontRenderer.getStringWidth(text) / 2, posY, color);
    }

    public static void renderCenteredTextWithShadow(String text, int posX, int posY, int color) {
        Minecraft mc = Minecraft.getMinecraft();
        renderTextWithShadow(text, posX - mc.fontRenderer.getStringWidth(text) / 2, posY, color);
    }

    public static void renderTextScaled(String text, int posX, int posY, int color, double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated(posX, posY, 0.0D);
        GL11.glScaled(givenScale, givenScale, givenScale);
        renderText(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public static void renderCenteredTextScaled(String text, int posX, int posY, int color, double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated(posX, posY, 0.0D);
        GL11.glScaled(givenScale, givenScale, givenScale);
        renderCenteredText(text, 0, 0, color);
        GL11.glPopMatrix();
    }

    public static void renderCenteredTextScaledWithShadow(String text, int posX, int posY, int color, double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated(posX, posY, 0.0D);
        GL11.glScaled(givenScale, givenScale, givenScale);
        renderCenteredTextWithShadow(text, 0, 0, color);
        GL11.glPopMatrix();
    }

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

    public static void renderRect(int givenPosX, int givenPosY, int givenWidth, int givenHeight, int givenColor) {
        GL11.glPushMatrix();

        givenWidth = givenPosX + givenWidth;
        givenHeight = givenPosY + givenHeight;
        if (givenPosX < givenWidth) {
            int i = givenPosX;
            givenPosX = givenWidth;
            givenWidth = i;
        }
        if (givenPosY < givenHeight) {
            int j = givenPosY;
            givenPosY = givenHeight;
            givenHeight = j;
        }
        float f = (givenColor >> 16 & 0xFF) / 255.0F;
        float f1 = (givenColor >> 8 & 0xFF) / 255.0F;
        float f2 = (givenColor & 0xFF) / 255.0F;
        float f3 = (givenColor >> 24 & 0xFF) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(givenPosX, givenHeight, 0.0D).endVertex();
        bufferbuilder.pos(givenWidth, givenHeight, 0.0D).endVertex();
        bufferbuilder.pos(givenWidth, givenPosY, 0.0D).endVertex();
        bufferbuilder.pos(givenPosX, givenPosY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GL11.glPopMatrix();
    }

    public static void renderRect(int givenPosX, int givenPosY, int givenWidth, int givenHeight) {
        GL11.glPushMatrix();

        givenWidth = givenPosX + givenWidth;
        givenHeight = givenPosY + givenHeight;
        if (givenPosX < givenWidth) {
            int i = givenPosX;
            givenPosX = givenWidth;
            givenWidth = i;
        }
        if (givenPosY < givenHeight) {
            int j = givenPosY;
            givenPosY = givenHeight;
            givenHeight = j;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(givenPosX, givenHeight, 0.0D).endVertex();
        bufferbuilder.pos(givenWidth, givenHeight, 0.0D).endVertex();
        bufferbuilder.pos(givenWidth, givenPosY, 0.0D).endVertex();
        bufferbuilder.pos(givenPosX, givenPosY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GL11.glPopMatrix();
    }

    public static void renderRectAlphaComp(int givenPosX, int givenPosY, int givenWidth, int givenHeight, int givenColor, int alpha) {
        GL11.glPushMatrix();

        givenWidth = givenPosX + givenWidth;
        givenHeight = givenPosY + givenHeight;
        if (givenPosX < givenWidth) {
            int i = givenPosX;
            givenPosX = givenWidth;
            givenWidth = i;
        }
        if (givenPosY < givenHeight) {
            int j = givenPosY;
            givenPosY = givenHeight;
            givenHeight = j;
        }
        float f = (givenColor >> 16 & 0xFF) / 255.0F;
        float f1 = (givenColor >> 8 & 0xFF) / 255.0F;
        float f2 = (givenColor & 0xFF) / 255.0F;
        float f3 = alpha / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(givenPosX, givenHeight, 0.0D).endVertex();
        bufferbuilder.pos(givenWidth, givenHeight, 0.0D).endVertex();
        bufferbuilder.pos(givenWidth, givenPosY, 0.0D).endVertex();
        bufferbuilder.pos(givenPosX, givenPosY, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GL11.glPopMatrix();
    }

    public static void renderRectWithOutline(int givenPosX, int givenPosY, int givenWidth, int givenHeight, int givenColor, int givenOutlineColor, int outlineThickness) {
        GL11.glPushMatrix();
        renderRect(givenPosX - outlineThickness, givenPosY - outlineThickness, givenWidth + outlineThickness * 2, givenHeight + outlineThickness * 2, givenOutlineColor);
        renderRect(givenPosX, givenPosY, givenWidth, givenHeight, givenColor);
        GL11.glPopMatrix();
    }

    public static void renderRectWithGradient(int givenPosX, int givenPosY, int givenWidth, int givenHeight, int startColor, int endColor, double givenZLevel) {
        GL11.glPushMatrix();

        givenWidth = givenPosX + givenWidth;
        givenHeight = givenPosY + givenHeight;

        float f = (startColor >> 24 & 0xFF) / 255.0F;
        float f1 = (startColor >> 16 & 0xFF) / 255.0F;
        float f2 = (startColor >> 8 & 0xFF) / 255.0F;
        float f3 = (startColor & 0xFF) / 255.0F;
        float f4 = (endColor >> 24 & 0xFF) / 255.0F;
        float f5 = (endColor >> 16 & 0xFF) / 255.0F;
        float f6 = (endColor >> 8 & 0xFF) / 255.0F;
        float f7 = (endColor & 0xFF) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(givenWidth, givenPosY, givenZLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(givenPosX, givenPosY, givenZLevel).color(f1, f2, f3, f).endVertex();
        bufferbuilder.pos(givenPosX, givenHeight, givenZLevel).color(f5, f6, f7, f4).endVertex();
        bufferbuilder.pos(givenWidth, givenHeight, givenZLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();

        GL11.glPopMatrix();
    }

    public static void renderPositionedImageNoDepth(ResourceLocation par1, double par2, double par3, double par4, float par5, float width, float height) {
        GL11.glPushMatrix();

        GL11.glDepthMask(false);
        GL11.glDisable(2929);
        renderPositionedImage(par1, par2, par3, par4, par5, width, height);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);

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

        GL11.glScalef(-0.03F, -0.03F, 0.03F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);

        renderImage(-width / 2.0F, -height / 2.0F, par1, width, height);

        GL11.glDisable(3042);
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

        GL11.glScalef(-0.03F, -0.03F, 0.03F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);

        renderCenteredTextScaled(givenText, 0, 0, givenColor, par5);

        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void renderImageCenteredScaled(double givenX, double givenY, ResourceLocation givenTexture, double givenWidth, double givenHeight, double givenScale) {
        GL11.glPushMatrix();
        GL11.glTranslated(givenX, givenY, 0.0D);
        GL11.glScaled(givenScale, givenScale, givenScale);
        renderImageCentered(givenX - givenWidth / 2.0D, givenY, givenTexture, givenWidth, givenHeight);
        GL11.glPopMatrix();
    }

    public static void renderImageCentered(double givenX, double givenY, ResourceLocation givenTexture, double givenWidth, double givenHeight) {
        GL11.glPushMatrix();
        renderImage(givenX - givenWidth / 2.0D, givenY, givenTexture, givenWidth, givenHeight);
        GL11.glPopMatrix();
    }

    public static void renderImage(double x, double y, ResourceLocation image, double width, double height) {

        GL11.glColor3f(1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().renderEngine.bindTexture(image);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(3042);
        GL11.glEnable(2832);
        GL11.glHint(3153, 4353);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDisable(3042);
        GL11.glDisable(2832);
    }

    public static void renderImageSpecial(double x, double y, double i, double j, double k, double l, float alpha, ResourceLocation image) {

        GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);

        Minecraft.getMinecraft().renderEngine.bindTexture(image);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(3042);
        GL11.glEnable(2832);
        GL11.glHint(3153, 4353);

        double w = i;
        double h = j;
        double we = k;
        double he = l;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x + w, y + he, -90.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + we, y + he, -90.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + we, y + h, -90.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(x + w, y + h, -90.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDisable(3042);
        GL11.glDisable(2832);
    }

    public static void renderImageAlpha(double x, double y, ResourceLocation image, double width, double height, double alpha) {

        GL11.glColor4d(255.0, 255.0, 255.0, alpha);

        Minecraft.getMinecraft().renderEngine.bindTexture(image);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(3042);
        GL11.glEnable(2832);
        GL11.glHint(3153, 4353);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDisable(3042);
        GL11.glDisable(2832);
    }

    public static void renderTranspImage(double x, double y, ResourceLocation image, double width, double height) {

        GL11.glColor3f(1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().renderEngine.bindTexture(image);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(3042);
        GL11.glEnable(2832);
        GL11.glHint(3153, 4353);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDisable(3042);
        GL11.glDisable(2832);
    }

    public static void renderCenteredTextScaledWithOutlineFade(final String text, final int posX, final int posY, final double par4, final int givenColor, final double givenFade) {
        final Minecraft mc = Minecraft.getMinecraft();
        final double width = mc.fontRenderer.getStringWidth(text) / 2 * par4;
        GL11.glPushMatrix();
        GL11.glTranslated(posX - width, posY, 0.0);
        GL11.glScaled(par4, par4, par4);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, -1, -1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 1, -1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, -1, 1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 1, 1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 0, -1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, -1, 0, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 1, 0, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 0, 1, 0);
        mc.fontRenderer.drawString(text, 0, 0, givenColor);
        GL11.glPopMatrix();
    }

    public static void renderCenteredTextScaledWithOutline(final String text, final int posX, final int posY, final double par4, final int givenColor) {
        final Minecraft mc = Minecraft.getMinecraft();
        final double width = mc.fontRenderer.getStringWidth(text) / 2 * par4;
        GL11.glPushMatrix();
        GL11.glTranslated(posX - width, posY, 0.0);
        GL11.glScaled(par4, par4, par4);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, -1, -1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 1, -1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, -1, 1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 1, 1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 0, -1, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, -1, 0, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 1, 0, 0);
        mc.fontRenderer.drawString(TextFormatting.BLACK + text, 0, 1, 0);
        mc.fontRenderer.drawString(text, 0, 0, givenColor);
        GL11.glPopMatrix();
    }

    public static void renderPositionedTextInView(final String par1, final double par2, final double par3, final double par4, final float par5) {
        renderPositionedTextInView(par1, par2, par3, par4, par5, 1.0f);
    }

    public static void renderPositionedTextInView(final String par1, final double par2, final double par3, final double par4, final float par5, final float alpha) {
        final Minecraft mc = Minecraft.getMinecraft();
        final EntityPlayer player = mc.player;
        GL11.glPushMatrix();
        GL11.glTranslated(par2, par3, par4);

        GL11.glTranslated(-mc.getRenderManager().viewerPosX, -mc.getRenderManager().viewerPosY, -mc.getRenderManager().viewerPosZ);
        GL11.glNormal3f(0.0f, 1.0f, 0.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        final int width = mc.fontRenderer.getStringWidth(par1);
        GL11.glRotatef(-player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(player.rotationPitch, 1.0f, 0.0f, 0.0f);
        GL11.glScalef(-0.01f, -0.01f, 0.01f);
        GL11.glDisable(2896);

        GL11.glDepthMask(false);

        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        final Color color = new Color(1.0f, 1.0f, 1.0f, alpha);
        final FontRenderer fr = mc.fontRenderer;
        fr.drawString(par1, -(width / 2), 0, color.getRGB());
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glEnable(2896);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    public static void renderPlayerHead(final String playerName, final int xPos, final int yPos, final String givenPlayerUUID) {
        final ResourceLocation resourceLocation = new ResourceLocation("textures/hologram/steve.png");
        if (playerName.length() > 0) {
            getDownloadImageSkin(resourceLocation, givenPlayerUUID);
        }
        GL11.glPushMatrix();
        renderRect(xPos - 1, yPos - 1, 20, 21, 1140850688);
        Minecraft.getMinecraft().renderEngine.bindTexture(resourceLocation);
        GL11.glTranslated(xPos, yPos, 30.0);
        GL11.glScaled(0.75, 0.39, 0.0);
        final double scale = 0.75;
        GL11.glScaled(scale, scale, scale);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.disableStandardItemLighting();
        Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(0, 0, 32, 64, 32, 64);
        Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(0, 0, 160, 64, 32, 64);
        GL11.glPopMatrix();
    }

    public static void renderLine(final double posX, final double posY, final double posZ, final double posX2, final double posY2, final double posZ2, final int givenColor, final float width) {
        final Minecraft mc = Minecraft.getMinecraft();
        final float red = (givenColor >> 16 & 0xFF) / 255.0f;
        final float blue = (givenColor >> 8 & 0xFF) / 255.0f;
        final float green = (givenColor & 0xFF) / 255.0f;
        final float alpha = (givenColor >> 24 & 0xFF) / 255.0f;
        final double d0 = mc.player.prevPosX + (mc.player.posX - mc.player.prevPosX);
        final double d2 = mc.player.prevPosY + (mc.player.posY - mc.player.prevPosY);
        final double d3 = mc.player.prevPosZ + (mc.player.posZ - mc.player.prevPosZ);
        GL11.glPushMatrix();
        GL11.glDisable(2896);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glLineWidth(width);
        GL11.glTranslated(-d0, -d2, -d3);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glBegin(3);
        GL11.glVertex3d(posX, posY, posZ);
        GL11.glVertex3d(posX2, posY2, posZ2);
        GL11.glEnd();
        GL11.glEnable(2896);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glPopMatrix();
    }

    private static final String pad(String s) {
        return s.length() == 1 ? "0" + s : s;
    }

    public static int toHex(org.lwjgl.util.Color color) {
        String alpha = pad(Integer.toHexString(color.getAlpha()));
        String red = pad(Integer.toHexString(color.getRed()));
        String green = pad(Integer.toHexString(color.getGreen()));
        String blue = pad(Integer.toHexString(color.getBlue()));
        String hex = "0x" + alpha + red + green + blue;
        return Integer.parseInt(hex, 16);
    }

    public static void renderPlayer(final int x, final int y, final float givenScale, final float givenRotation) {
        GL11.glPushMatrix();
        //PLAYER_RENDERER.renderPlayerModel(x, y, givenScale, givenRotation);
        GL11.glPopMatrix();
    }

    public static ThreadDownloadImageData getDownloadImageSkin(final ResourceLocation resourceLocationIn, final String givenUUID) {
        final TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
        Object object = texturemanager.getTexture(resourceLocationIn);
        if (object == null) {
            object = new ThreadDownloadImageData(null, String.format("https://crafatar.com/skins/%s.png", StringUtils.stripControlCodes(givenUUID)), new ResourceLocation("textures/hologram/steve.png"), new ImageBufferDownload());
            texturemanager.loadTexture(resourceLocationIn, (ITextureObject) object);
        }
        return (ThreadDownloadImageData) object;
    }

    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x + 0, y + height, 0.0D).tex((float) (textureX + 0) * f, (float) (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y + height, 0.0D).tex((float) (textureX + width) * f, (float) (textureY + height) * f1).endVertex();
        buffer.pos(x + width, y + 0, 0.0D).tex((float) (textureX + width) * f, (float) (textureY + 0) * f1).endVertex();
        buffer.pos(x + 0, y + 0, 0.0D).tex((float) (textureX + 0) * f, (float) (textureY + 0) * f1).endVertex();
        tessellator.draw();
    }

    public static void renderBackgroundImage(final double offsx, final double offsy, final int width, final int height, final float brightness, final ResourceLocation background) {
        GL11.glPushMatrix();
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(brightness, brightness, brightness, 1.0f);
        GL11.glDisable(3008);
        GL11.glEnable(3042);

        Minecraft.getMinecraft().renderEngine.bindTexture(background);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(3042);
        GL11.glEnable(2832);
        GL11.glHint(3153, 4353);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(offsx, offsy + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(offsx + width, offsy + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(offsx + width, offsy, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(offsx, offsy, 0.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glEnable(3008);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPopMatrix();
    }

    public static long getTime() {
        return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }

    public static void renderMainMenuPlayer(final int width, final int height, final float offsetx, final float offsety, final int par1, final int par2) {

    }

    public static void drawGradientRect(int left, int top, int right, int bottom, int colour1, int colour2, float fade, double zLevel) {
        float f = ((colour1 >> 24 & 255) / 255.0F) * fade;
        float f1 = (float) (colour1 >> 16 & 255) / 255.0F;
        float f2 = (float) (colour1 >> 8 & 255) / 255.0F;
        float f3 = (float) (colour1 & 255) / 255.0F;
        float f4 = ((colour2 >> 24 & 255) / 255.0F) * fade;
        float f5 = (float) (colour2 >> 16 & 255) / 255.0F;
        float f6 = (float) (colour2 >> 8 & 255) / 255.0F;
        float f7 = (float) (colour2 & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        vertexbuffer.pos(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        vertexbuffer.pos(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        vertexbuffer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static String formatNumber(double value) {
        if (value < 1000D) return String.valueOf(value);
        else if (value < 1000000D) return Math.round(value) / 1000D + "K";
        else if (value < 1000000000D) return Math.round(value / 1000D) / 1000D + "M";
        else if (value < 1000000000000D) return Math.round(value / 1000000D) / 1000D + "B";
        else return Math.round(value / 1000000000D) / 1000D + "T";
    }

    public static String formatNumber(long value) {
        if (value < 1000L) return String.valueOf(value);
        else if (value < 1000000L) return Math.round(value) / 1000D + "K";
        else if (value < 1000000000L) return Math.round(value / 1000L) / 1000D + "M";
        else if (value < 1000000000000L) return Math.round(value / 1000000L) / 1000D + "G";
        else if (value < 1000000000000000L) return Math.round(value / 1000000000L) / 1000D + "T";
        else if (value < 1000000000000000000L) return Math.round(value / 1000000000000L) / 1000D + "P";
        else if (value <= Long.MAX_VALUE) return Math.round(value / 1000000000000000L) / 1000D + "E";
        else return "Something is very broken!!!!";
    }

    /**
     * Add commas to a number e.g. 161253126 > 161,253,126
     */
    public static String addCommas(int value) {
        return energyValue.format(value);
    }

    /**
     * Add commas to a number e.g. 161253126 > 161,253,126
     */
    public static String addCommas(long value) {
        return energyValue.format(value);
    }

    public static boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY) {
        return ((mouseX >= x && mouseX < x + xSize) && (mouseY >= y && mouseY < y + ySize));
    }

    public static void drawHoveringText(List list, int x, int y, FontRenderer font, int guiWidth, int guiHeight) {
        net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(list, x, y, guiWidth, guiHeight, -1, font);
    }


    public static void drawColouredRect(int posX, int posY, int xSize, int ySize, int colour) {
        drawGradientRect(posX, posY, posX + xSize, posY + ySize, colour, colour, 1F, 0);
    }

    public static void drawTextInWorld(Minecraft mc, RenderManager renderManager, String text, double x, double y, double z, int color) {
        drawTextInWorld(mc, renderManager, text, x, y, z, color, 0x7f000000, 1F);
    }

    public static void drawTextInWorld(Minecraft mc, RenderManager renderManager, String text, double x, double y, double z, int color, int backgroundColour, float scale) {
        int strWidth = renderManager.getFontRenderer().getStringWidth(text);
        int strCenter = strWidth / 2;
        int yOffset = -4;

        GL11.glPushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F * scale, -0.025F * scale, 0.025F * scale);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.pushMatrix();
        drawColouredRect(-strCenter - 1, yOffset - 1, strWidth + 1, 9, backgroundColour);
        mc.fontRenderer.drawString(text, -strCenter, yOffset, color);
        GlStateManager.popMatrix();

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    public static void drawNameplate(FontRenderer fontRendererIn, String str, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal, boolean isSneaking) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-0.025F, -0.025F, 0.025F);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        if (!isSneaking) {
            GlStateManager.disableDepth();
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int i = fontRendererIn.getStringWidth(str) / 2;
        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(-i - 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.50F).endVertex();
        bufferbuilder.pos(-i - 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.50F).endVertex();
        bufferbuilder.pos(i + 1, 8 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.50F).endVertex();
        bufferbuilder.pos(i + 1, -1 + verticalShift, 0.0D).color(0.0F, 0.0F, 0.0F, 0.50F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        if (!isSneaking) {
            fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, 553648127);
            GlStateManager.enableDepth();
        }

        GlStateManager.depthMask(true);
        fontRendererIn.drawString(str, -fontRendererIn.getStringWidth(str) / 2, verticalShift, isSneaking ? 553648127 : -1);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }


    public static void renderSmoke(ResourceLocation par1, double par2, double par3, double par4, float par5, int width, int height, String color, double alpha) {

        EntityPlayer player = Minecraft.getMinecraft().player;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        float scale2 = 0.02F;

        float d0 = (float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) par5);
        float d1 = (float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) par5);
        float d2 = (float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) par5);

        GL11.glTranslatef((float) par2, (float) par3, (float) par4);
        GL11.glTranslatef(-d0, -d1, -d2);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        //Scaling and fitting for the text above item
        GL11.glScalef(-scale2, -scale2, scale2);
        GL11.glDepthMask(false);

        float realTick = RenderParameters.SMOOTH_SWING;

        for (int i1 = 0; i1 < 4; i1++) {

            float val = (float) (Math.sin(realTick / 100) * 3);

            if (i1 % 2 == 0) {
                val = -val;
            }

            for (int i = 0; i < 9; i++) {

                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_ALPHA_TEST);

                renderImageAlpha(-width / 2, -height / 2, par1, width, height, alpha);

                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glRotatef(64, 0, 1, 0);
                GL11.glRotatef(val, 1, 0, 0);
            }

            GL11.glRotatef(90, 1, 0, 0);
        }

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void renderPositionedImageInViewWithDepth(ResourceLocation img, double x, double y, double z, float width, float height, float givenAlpha) {

        EntityPlayer player = Minecraft.getMinecraft().player;

        GL11.glPushMatrix();

        GL11.glTranslated(x, y, z);
        GL11.glTranslated(-Minecraft.getMinecraft().getRenderManager().viewerPosX, -Minecraft.getMinecraft().getRenderManager().viewerPosY, -Minecraft.getMinecraft().getRenderManager().viewerPosZ);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        //Rotate to player pos
        GL11.glRotatef(-player.rotationYaw, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(player.rotationPitch, 1.0F, 0.0F, 0.0F);

        //Scaling and fitting for the text above item
        GL11.glScalef(-0.03f, -0.03f, 0.03f);

        renderImageAlpha(-width / 2, -height / 2, img, width, height, givenAlpha);
        GL11.glPopMatrix();
    }

    public static void renderRectFlash(double x, double y, double width, double height) {

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GL11.glEnable(3042);
        GL11.glEnable(2832);
        GL11.glHint(3153, 4353);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);

        bufferbuilder.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        bufferbuilder.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        bufferbuilder.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();

        tessellator.draw();

        GL11.glDisable(3042);
        GL11.glDisable(2832);
    }
}
