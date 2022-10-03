package com.modularwarfare.melee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.addon.AddonLoaderManager;
import com.modularwarfare.addon.IContentAddon;
import com.modularwarfare.api.*;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.input.KeyType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.type.ContentTypes;
import com.modularwarfare.common.type.TypeEntry;
import com.modularwarfare.melee.client.RenderMelee;
import com.modularwarfare.melee.client.configs.MeleeRenderConfig;
import com.modularwarfare.melee.common.melee.ItemMelee;
import com.modularwarfare.melee.common.melee.MeleeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class ModularWarfareMelee implements IContentAddon {

    public static HashMap<String, ItemMelee> meleeTypes = new HashMap<String, ItemMelee>();


    @Override
    public void construct(Side side, AddonLoaderManager contentManager) {
    }

    @Override
    public void preInit(FMLPreInitializationEvent event, AddonLoaderManager contentManager) {
        System.out.println("Preinit and loading event bus");
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void init(FMLPostInitializationEvent event, AddonLoaderManager contentManager) {
        if (event.getSide() == Side.CLIENT) {
            /**
             * Init Melee Renderer
             */
            for (TypeEntry entry : ContentTypes.values) {
                if (entry.name.equalsIgnoreCase("melee")) {
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

    @SubscribeEvent
    public void onHandleKey(HandleKeyEvent event) {
        EntityPlayerSP entityPlayer = Minecraft.getMinecraft().player;
        if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemMelee) {
            final ItemStack itemStack = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            final MeleeType meleeType = ((ItemMelee) itemStack.getItem()).type;
            if(event.keyType == KeyType.ClientReload) {
                meleeType.enhancedModel.config = ModularWarfare.getRenderConfig(meleeType, MeleeRenderConfig.class);
            } else if (event.keyType == KeyType.Inspect) {
                RenderMelee.controller.INSPECT = 0;
            }
        }
    }

    /**
     * Generate the .render.json file of the melee weapons
     */
    @SubscribeEvent
    public void onGenerateJsonModels(GenerateJsonModelsEvent event) {
        System.out.println("Generate JSON Melee");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (ItemMelee itemMelee : meleeTypes.values()) {
            MeleeType type = itemMelee.type;
            if (type.contentPack == null)
                continue;

            File contentPackDir = new File(ModularWarfare.MOD_DIR, type.contentPack);
            if (contentPackDir.exists() && contentPackDir.isDirectory()) {
                if (ModularWarfare.DEV_ENV) {
                    final File dir = new File(contentPackDir, "/" + type.getAssetDir() + "/render");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    final File renderFile = new File(dir, type.internalName + ".render.json");
                    if (!renderFile.exists()) {
                        try {
                            FileWriter fileWriter = new FileWriter(renderFile, false);

                            MeleeRenderConfig renderConfig = new MeleeRenderConfig();
                            renderConfig.modelFileName = type.internalName.replaceAll(type.contentPack + ".", "");
                            renderConfig.modelFileName = renderConfig.modelFileName + ".glb";
                            gson.toJson(renderConfig, fileWriter);

                            fileWriter.flush();
                            fileWriter.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTickEvent(OnTickRenderEvent event) {
        if (RenderMelee.controller != null) {
            RenderMelee.controller.updateCurrentItem();
            RenderMelee.controller.onTickRender(event.smooth);
        }
    }

    @SubscribeEvent
    public void onAttack(PlayerInteractEvent event){
        if((event instanceof PlayerInteractEvent.LeftClickEmpty || event instanceof PlayerInteractEvent.LeftClickBlock)) {
            if (event.getItemStack().getItem() instanceof ItemMelee) {
                RenderMelee.controller.applyAttackAnim();
            }
        }
    }

}
