package com.modularwarfare.client.fpp.basic.models.objects;

import com.modularwarfare.ModularWarfare;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class CustomItemRenderer {

    protected static TextureManager renderEngine = Minecraft.getMinecraft().renderEngine;
    private HashMap<String, ResourceLocation> cachedSkins = new HashMap<String, ResourceLocation>();

    public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data) {
    }

    public void bindTexture(String type, String fileName) {
        String pathFormat = "skins/%s/%s.png";

        if (renderEngine == null)
            renderEngine = Minecraft.getMinecraft().renderEngine;

        try {
            ResourceLocation resourceLocation = new ResourceLocation(ModularWarfare.MOD_ID, String.format(pathFormat, type, fileName));
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
            e.printStackTrace();
        }
    }
}
