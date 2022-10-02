package com.modularwarfare.client.fpp.basic.models.objects;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.loader.api.model.ObjModelRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;

public class CustomItemRenderer {

    public static ResourceLocation NULL_TEX = new ResourceLocation(ModularWarfare.MOD_ID, "textures/nulltex.png");
    protected static TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
    private HashMap<String, ResourceLocation> cachedSkins = new HashMap<String, ResourceLocation>();
    private ArrayList<String> cachedBadSkins = new ArrayList<String>();

    /**
     * Specific to Enhanced Models
     */
    public ResourceLocation bindingTexture;
    public float r=1;
    public float g=1;
    public float b=1;
    public float a=1;

    public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data) {
    }

    public void bindTexture(String type, String fileName) {
        ObjModelRenderer.glowType=type;
        ObjModelRenderer.glowPath=fileName;
        bindTexture(type, fileName, false, true);
    }

    public boolean bindTextureGlow(String type, String fileName) {
        if (cachedBadSkins.contains(type + "_" + fileName + "_glow")) {
            return false;
        }
        bindTexture(type, fileName + "_glow", true, false);
        return true;
    }

    public void bindTexture(String type, String fileName, boolean saveBad, boolean printException) {
        String pathFormat = "skins/%s/%s.png";

        if (renderEngine == null)
            renderEngine = Minecraft.getMinecraft().renderEngine;
        if (cachedBadSkins.contains(type + "_" + fileName)) {
            renderEngine.bindTexture(NULL_TEX);
            return;
        }

        try {
            ResourceLocation resourceLocation = new ResourceLocation(ModularWarfare.MOD_ID,
                    String.format(pathFormat, type, fileName));
            if (cachedSkins.containsKey(type + "_" + fileName)) {
                renderEngine.bindTexture(cachedSkins.get(type + "_" + fileName));
                return;
            } else if (renderEngine.getTexture(resourceLocation) == null) {
                ITextureObject itextureobject = new SimpleTexture(resourceLocation);
                itextureobject.loadTexture(Minecraft.getMinecraft().getResourceManager());
            }

            renderEngine.bindTexture(resourceLocation);
        } catch (Exception e) {
            ResourceLocation resourceLocation = new ResourceLocation(ModularWarfare.MOD_ID,
                    String.format(pathFormat, "default", type, fileName));
            cachedSkins.put(type + "_" + fileName, resourceLocation);
            if (saveBad) {
                cachedBadSkins.add(type + "_" + fileName);
            }
            if (printException) {
                e.printStackTrace();
            }
        }
    }
}