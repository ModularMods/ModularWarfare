package com.modularwarfare.client.patch.customnpc;

import com.modularwarfare.client.gui.GuiInventoryModified;
import com.modularwarfare.client.model.layers.RenderLayerHeldGun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CustomNPCListener {

    public static boolean CNPCLayersInitialised = false;

    private static <T extends EntityLivingBase> void addCNPCLayers(Class<? extends Entity> entityClass) {
        Render<T> renderer = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(entityClass);
        RenderLayerHeldGun layer = new RenderLayerHeldGun((RenderLivingBase<T>) renderer);
        ((RenderLivingBase<T>) renderer).addLayer(layer);
    }

    @SubscribeEvent
    public void guiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (Loader.isModLoaded("customnpcs")) {
            if (event.getGui() instanceof GuiInventory || event.getGui() instanceof GuiInventoryModified) {
                GuiContainer gui = (GuiContainer) event.getGui();
                event.getButtonList().add(new GuiQuestButton(55, gui, 93, 60, 18, 19,
                        I18n.format((event.getGui() instanceof GuiInventory) ? "QUEST" : "QUEST")));
            }
        }
    }

    @SubscribeEvent
    public void initLayersCNPCs(@SuppressWarnings("unused") RenderLivingEvent.Pre<EntityLivingBase> event) {
        try {
            Class classz = Class.forName("noppes.npcs.entity.EntityCustomNpc");
            Class classzz = Class.forName("noppes.npcs.entity.EntityNPC64x32");
            if(event.getEntity().getClass().equals(classz)) {
                if (!CNPCLayersInitialised) {
                    addCNPCLayers(classz);
                    addCNPCLayers(classzz);
                    CNPCLayersInitialised = true;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
