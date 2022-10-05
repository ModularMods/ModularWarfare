package com.modularwarfare.melee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.addon.AddonLoaderManager;
import com.modularwarfare.addon.IContentAddon;
import com.modularwarfare.api.*;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.common.type.ContentTypes;
import com.modularwarfare.common.type.TypeEntry;
import com.modularwarfare.melee.client.ClientEvents;
import com.modularwarfare.melee.client.RenderMelee;
import com.modularwarfare.melee.common.melee.ItemMelee;
import com.modularwarfare.melee.common.melee.MeleeType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;

public class ModularWarfareMelee implements IContentAddon {

    public static HashMap<String, ItemMelee> meleeTypes = new HashMap<String, ItemMelee>();

    //Melee entry id
    public static int meleeEntryId;

    @Override
    public void construct(Side side, AddonLoaderManager contentManager) {
    }

    @Override
    public void preInit(FMLPreInitializationEvent event, AddonLoaderManager contentManager) {
        System.out.println("Preinit and loading event bus");
        MinecraftForge.EVENT_BUS.register(this);

        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new ClientEvents());
        }
    }

    @Override
    public void init(FMLPostInitializationEvent event, AddonLoaderManager contentManager) {
        if (event.getSide() == Side.CLIENT) {
            /**
             * Init Melee Renderer
             */
            for (TypeEntry entry : ContentTypes.values) {
                if (entry.name.equalsIgnoreCase("melee")) {
                    meleeEntryId = entry.id;
                    ClientRenderHooks.customRenderers[entry.id] = new RenderMelee();
                    System.out.println("Registered Custom Render Melee at " + entry.id);
                }
            }
        }
    }

    @Override
    public void unload() {
    }

    @Override
    public String getName() {
        return "melee-addon";
    }

    @Override
    public String getVersion() {
        return "1.0.0f";
    }

    @Override
    public String getAddonID() {
        return "melee-addon";
    }


    @SubscribeEvent
    public void onRegisterType(TypeRegisterEvent event) {
        ModularWarfare.LOGGER.info("Loading MeleeType ...");
        ContentTypes.registerType("melee", MeleeType.class, (type, reload) -> {
            ContentTypes.assignType(ModularWarfareMelee.meleeTypes, ItemMelee.factory, (MeleeType) type, reload);
        });
    }

    @SubscribeEvent
    public void onRegisterItem(ItemRegisterEvent event) {
        for (ItemMelee itemMelee : meleeTypes.values()) {
            event.registry.register(itemMelee);
            event.tabOrder.add(itemMelee);
        }
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
        for (ItemMelee itemMelee : meleeTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemMelee, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemMelee.type.internalName));
        }
    }

}
