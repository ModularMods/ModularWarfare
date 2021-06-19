package com.modularwarfare.utility;

import net.minecraft.client.renderer.GlStateManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;

/**
 * Dont forget to credit EnderIO.
 */

public final class ColorUtils {
    private ColorUtils() {
    }

    public static @Nonnull
    Vector4f toFloat(@Nonnull final Color color) {
        float[] rgba = color.getComponents(null);
        return new Vector4f(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    public static @Nonnull
    Vector3f toFloat(final int rgb) {
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;
        return new Vector3f(r / 255F, g / 255F, b / 255F);
    }

    public static @Nonnull
    Vector4f toFloat4(final int rgb) {
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;
        return new Vector4f(r / 255F, g / 255F, b / 255F, 1);
    }

    public static int[] toRGB(final int rgb) {
        int r = rgb >> 16 & 255;
        int g = rgb >> 8 & 255;
        int b = rgb & 255;

        return new int[]{r, g, b};
    }

    public static int getRGB(@Nullable final Color color) {
        // Note: Constants in java.awt.Color are not @Nonnull-annotated
        return color == null ? 0 : getRGB(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int getRGBA(@Nullable final Color color) {
        return color == null ? 0 : getRGBA(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public static int getARGB(@Nullable final Color color) {
        return color == null ? 0 : getRGBA(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
    }

    public static int getRGB(final @Nonnull Vector3f rgb) {
        return getRGB(rgb.x, rgb.y, rgb.z);
    }

    public static int getRGB(final float r, final float g, final float b) {
        return getRGB((int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    public static int getRGBA(final @Nonnull Vector4f col) {
        return getRGBA(col.x, col.y, col.z, col.w);
    }

    public static int getRGBA(final float r, final float g, final float b, final float a) {
        return getRGBA((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }

    public static int getARGB(final float r, final float g, final float b, final float a) {
        return getARGB((int) (a * 255), (int) (r * 255), (int) (g * 255), (int) (b * 255));
    }

    public static int getRGB(final int r, final int g, final int b) {
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int getARGB(final int r, final int g, final int b, final int a) {
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public static int getRGBA(final int r, final int g, final int b, final int a) {
        return (r & 0xFF) << 24 | (g & 0xFF) << 16 | (b & 0xFF) << 8 | (a & 0xFF);
    }

    /**
     * Turns an int into a glColor4f function.
     *
     * @param color the color represented by an integer
     * @author Buildcraft team
     */
    public static void setGLColorFromInt(final int color) {
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GlStateManager.color(red, green, blue, 1.0F);
    }

    public static int toHex(final int r, final int g, final int b) {
        int hex = 0;
        hex = hex | ((r) << 16);
        hex = hex | ((g) << 8);
        hex = hex | (b);
        return hex;
    }
}
