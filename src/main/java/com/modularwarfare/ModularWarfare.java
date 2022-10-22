package com.modularwarfare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import com.modularwarfare.addon.AddonLoaderManager;
import com.modularwarfare.addon.LibClassLoader;
import com.modularwarfare.api.ItemRegisterEvent;
import com.modularwarfare.api.TypeRegisterEvent;
import com.modularwarfare.client.fpp.enhanced.AnimationType.AnimationTypeJsonAdapter.AnimationTypeException;
import com.modularwarfare.common.CommonProxy;
import com.modularwarfare.common.MWTab;
import com.modularwarfare.common.armor.ItemMWArmor;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.commands.CommandClear;
import com.modularwarfare.common.commands.CommandDebug;
import com.modularwarfare.common.commands.kits.CommandKit;
import com.modularwarfare.common.commands.CommandNBT;
import com.modularwarfare.common.entity.EntityExplosiveProjectile;
import com.modularwarfare.common.entity.decals.EntityBulletHole;
import com.modularwarfare.common.entity.decals.EntityShell;
import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.entity.grenades.EntitySmokeGrenade;
import com.modularwarfare.common.entity.grenades.EntityStunGrenade;
import com.modularwarfare.common.entity.item.EntityItemLoot;
import com.modularwarfare.common.extra.ItemLight;
import com.modularwarfare.common.grenades.ItemGrenade;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.handler.CommonEventHandler;
import com.modularwarfare.common.handler.GuiHandler;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.hitbox.playerdata.PlayerDataHandler;
import com.modularwarfare.common.network.NetworkHandler;
import com.modularwarfare.common.protector.ModularProtector;
import com.modularwarfare.common.textures.TextureType;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.common.type.ContentTypes;
import com.modularwarfare.common.type.TypeEntry;
import com.modularwarfare.raycast.DefaultRayCasting;
import com.modularwarfare.raycast.RayCasting;
import com.modularwarfare.utility.GSONUtils;
import com.modularwarfare.utility.ModUtil;
import com.modularwarfare.utility.ZipContentPack;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.modularwarfare.common.CommonProxy.zipJar;

@Mod(modid = ModularWarfare.MOD_ID, name = ModularWarfare.MOD_NAME, version = ModularWarfare.MOD_VERSION, dependencies = "required-client:mcgltf")
public class ModularWarfare {

    // Mod Info
    public static final String MOD_ID = "modularwarfare";
    public static final String MOD_NAME = "ModularWarfare";
    public static final String MOD_VERSION = "2.1.1f";
    public static final String MOD_PREFIX = TextFormatting.GRAY+"["+TextFormatting.RED+"ModularWarfare"+TextFormatting.GRAY+"]"+TextFormatting.GRAY;

    // Main instance
    @Instance(ModularWarfare.MOD_ID)
    public static ModularWarfare INSTANCE;
    // Proxy
    @SidedProxy(clientSide = "com.modularwarfare.client.ClientProxy", serverSide = "com.modularwarfare.common.CommonProxy")
    public static CommonProxy PROXY;
    // Development Environment
    public static boolean DEV_ENV = true;

    // Logger
    public static Logger LOGGER;
    // Network Handler
    public static NetworkHandler NETWORK;

    public static ModularProtector PROTECTOR;

    public static PlayerDataHandler PLAYERHANDLER = new PlayerDataHandler();

    public static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static HashMap<String, ZipContentPack> zipContentsPack = new HashMap<>();

    // The ModularWarfare directory
    public static File MOD_DIR;
    public static List<File> contentPacks = new ArrayList<File>();

    // Arrays for the varied types
    public static HashMap<String, ItemGun> gunTypes = new HashMap<String, ItemGun>();
    public static HashMap<String, ItemAmmo> ammoTypes = new HashMap<String, ItemAmmo>();
    public static HashMap<String, ItemAttachment> attachmentTypes = new HashMap<String, ItemAttachment>();
    public static LinkedHashMap<String, ItemMWArmor> armorTypes = new LinkedHashMap<String, ItemMWArmor>();
    public static LinkedHashMap<String, ItemSpecialArmor> specialArmorTypes = new LinkedHashMap<String, ItemSpecialArmor>();
    public static HashMap<String, ItemBullet> bulletTypes = new HashMap<String, ItemBullet>();
    public static HashMap<String, ItemSpray> sprayTypes = new HashMap<String, ItemSpray>();
    public static HashMap<String, ItemBackpack> backpackTypes = new HashMap<String, ItemBackpack>();
    public static HashMap<String, ItemGrenade> grenadeTypes = new HashMap<String, ItemGrenade>();
    public static HashMap<String, TextureType> textureTypes = new HashMap<String, TextureType>();

    public static ArrayList<BaseType> baseTypes = new ArrayList<BaseType>();

    public static HashMap<String, MWTab> MODS_TABS = new HashMap<String, MWTab>();

    /**
     * Custom RayCasting
     */
    public RayCasting RAY_CASTING;

    public static final LibClassLoader LOADER = new LibClassLoader(ModularWarfare.class.getClassLoader());
    /**
     * ModularWarfare Addon System
     */
    public static File addonDir;
    public static AddonLoaderManager loaderManager;


    public static void loadContent() {
        Method method = null;
        try {
            method = (java.net.URLClassLoader.class).getDeclaredMethod("addURL", java.net.URL.class);
            method.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Failed to get class loader. All content loading will now fail.");
            e.printStackTrace();
        }
        for (File file : contentPacks) {
            if (!MODS_TABS.containsKey(file.getName())) {
                MODS_TABS.put(file.getName(), new MWTab(file.getName()));
            }
            if (zipJar.matcher(file.getName()).matches()) {
                if (!zipContentsPack.containsKey(file.getName())) {
                    try {
                        ZipFile zipFile = new ZipFile(file);

                        /** Set password */
                        if (zipFile.isEncrypted()) {
                            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                                ModularWarfare.PROTECTOR.applyPassword(zipFile, file.getName());
                            } else {
                                ModularWarfare.LOGGER.info("Can't use password protected content-pack on server-side.");
                            }
                        }
                        ZipContentPack zipContentPack = new ZipContentPack(file.getName(), zipFile.getFileHeaders(), zipFile);
                        zipContentsPack.put(file.getName(), zipContentPack);
                        ModularWarfare.LOGGER.info("Registered content pack");
                    } catch (ZipException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        getTypeFiles(contentPacks);
    }

    /**
     * Sorts all type files into their proper arraylist
     */
    public static void loadContentPacks(boolean reload) {

        loadContent();

        if (DEV_ENV) {
            PROXY.generateJsonModels(baseTypes);
        }

        for(TextureType type : textureTypes.values()){
            type.loadExtraValues();
        }

        for (BaseType baseType : baseTypes) {
            baseType.loadExtraValues();
            ContentTypes.values.get(baseType.id).typeAssignFunction.accept(baseType, reload);
        }

        if (DEV_ENV) {
            if (reload)
                return;
            //PROXY.generateJsonSounds(gunTypes.values(), DEV_ENV);
            PROXY.generateLangFiles(baseTypes, DEV_ENV);
        }
    }

    /**
     * Gets all the render config json for each gun
     */
    public static <T> T getRenderConfig(BaseType baseType, Class<T> typeClass) {
        if (baseType.isInDirectory) {
            try {
                File contentPackDir = new File(ModularWarfare.MOD_DIR, baseType.contentPack);
                if (contentPackDir.exists() && contentPackDir.isDirectory()) {
                    File renderConfig = new File(contentPackDir, "/" + baseType.getAssetDir() + "/render");
                    File typeRender = new File(renderConfig, baseType.internalName + ".render.json");
                    JsonReader jsonReader = new JsonReader(new FileReader(typeRender));
                    return GSONUtils.fromJson(gson, jsonReader, typeClass, baseType.internalName + ".render.json");
                }
            } catch (JsonParseException e){
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (AnimationTypeException err) {
                ModularWarfare.LOGGER.info(baseType.internalName + " was loaded. But something was wrong.");
                err.printStackTrace();
            }
        } else {
            if (zipContentsPack.containsKey(baseType.contentPack)) {
                String typeName = baseType.getAssetDir();

                FileHeader foundFile = zipContentsPack.get(baseType.contentPack).fileHeaders.stream().filter(fileHeader -> fileHeader.getFileName().startsWith(typeName + "/" + "render/") && fileHeader.getFileName().replace(typeName + "/render/", "").equalsIgnoreCase(baseType.internalName + ".render.json")).findFirst().orElse(null);
                if (foundFile != null) {
                    try {
                        ZipInputStream stream = zipContentsPack.get(baseType.contentPack).getZipFile().getInputStream(foundFile);
                        JsonReader jsonReader = new JsonReader(new InputStreamReader(stream));
                        return GSONUtils.fromJson(gson, jsonReader, typeClass, baseType.internalName + ".render.json");
                    } catch (JsonParseException e){
                        e.printStackTrace();
                    } catch (ZipException e) {
                        e.printStackTrace();
                    }catch (AnimationTypeException err) {
                        ModularWarfare.LOGGER.info(baseType.internalName + " was loaded. But something was wrong.");
                        err.printStackTrace();
                    }
                } else {
                    ModularWarfare.LOGGER.info(baseType.internalName + ".render.json not found. Aborting");
                }
            }
        }
        return null;
    }

    /**
     * Gets all type files from the content packs
     *
     * @param contentPacks
     */
    private static void getTypeFiles(List<File> contentPacks) {
        for (File file : contentPacks) {
            if (!file.getName().contains("cache")) {
                if (file.isDirectory()) {
                    for (TypeEntry type : ContentTypes.values) {
                        File subFolder = new File(file, "/" + type.name + "/");
                        if (subFolder.exists()) {
                            for (File typeFile : subFolder.listFiles()) {
                                try {
                                    if (typeFile.isFile()) {
                                        JsonReader jsonReader = new JsonReader(new FileReader(typeFile));
                                        BaseType parsedType = GSONUtils.fromJson(gson, jsonReader, type.typeClass, typeFile.getName());

                                        parsedType.id = type.id;
                                        parsedType.contentPack = file.getName();
                                        parsedType.isInDirectory = true;
                                        baseTypes.add(parsedType);

                                        if (parsedType instanceof TextureType) {
                                            textureTypes.put(parsedType.internalName, (TextureType) parsedType);
                                        }
                                    }
                                } catch (com.google.gson.JsonParseException ex) {
                                    ex.printStackTrace();
                                    continue;
                                } catch (FileNotFoundException exception) {
                                    exception.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    if (zipContentsPack.containsKey(file.getName())) {
                        for (FileHeader fileHeader : zipContentsPack.get(file.getName()).fileHeaders) {
                            for (TypeEntry type : ContentTypes.values) {
                                final String zipName = fileHeader.getFileName();
                                final String typeName = type.toString();
                                if (zipName.startsWith(typeName + "/") && zipName.split(typeName + "/").length > 1 && zipName.split(typeName + "/")[1].length() > 0 && !zipName.contains("render")) {
                                    ZipInputStream stream = null;
                                    try {
                                        stream = zipContentsPack.get(file.getName()).getZipFile().getInputStream(fileHeader);
                                        JsonReader jsonReader = new JsonReader(new InputStreamReader(stream));

                                        try {
                                            BaseType parsedType = (BaseType) GSONUtils.fromJson(gson, jsonReader, type.typeClass, fileHeader.getFileName());
                                            parsedType.id = type.id;
                                            parsedType.contentPack = file.getName();
                                            parsedType.isInDirectory = false;
                                            baseTypes.add(parsedType);

                                            if(parsedType instanceof TextureType){
                                                textureTypes.put(parsedType.internalName, (TextureType) parsedType);
                                            }
                                        } catch (com.google.gson.JsonParseException ex) {
                                            continue;
                                        }
                                    } catch (ZipException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Registers items, blocks, renders, etc
     *
     * @param event
     */
    @EventHandler
    public void onPreInitialization(FMLPreInitializationEvent event) {
        PROXY.preload();

        if (FMLCommonHandler.instance().getSide().isServer()) {
            // Creates directory if doesn't exist
            MOD_DIR = new File(event.getModConfigurationDirectory().getParentFile(), "ModularWarfare");
            if (!MOD_DIR.exists()) {
                MOD_DIR.mkdir();
                LOGGER.info("Created ModularWarfare folder, it's recommended to install content packs.");
                LOGGER.info("As the mod itself doesn't come with any content.");
            }
            loadConfig();
            DEV_ENV = true;

            contentPacks = PROXY.getContentList();
        }

        registerRayCasting(new DefaultRayCasting());
        this.loaderManager.preInitAddons(event);

        // Loads Content Packs
        ContentTypes.registerTypes();
        loadContentPacks(false);

        // Client side loading
        //PROXY.forceReload();

        PROXY.registerEventHandlers();

        MinecraftForge.EVENT_BUS.register(new CommonEventHandler());
        MinecraftForge.EVENT_BUS.register(this);

    }
    
    public static void loadConfig() {
        new ModConfig(new File(MOD_DIR, "mod_config.json"));
    }

    /**
     * Register events, imc, and world stuff
     *
     * @param event
     */
    @EventHandler
    public void onInitialization(FMLInitializationEvent event) {
        new ServerTickHandler();

        PROXY.load();

        NETWORK = new NetworkHandler();
        NETWORK.initialise();
        NetworkRegistry.INSTANCE.registerGuiHandler(ModularWarfare.INSTANCE, new GuiHandler());
    }

    /**
     * Last loading things
     *
     * @param event
     */
    @EventHandler
    public void onPostInitialization(FMLPostInitializationEvent event) {
        NETWORK.postInitialise();
        PROXY.init();

        this.loaderManager.initAddons(event);
    }

    /**
     * Registers commands and server sided regions
     *
     * @param event
     */
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandClear());
        event.registerServerCommand(new CommandNBT());
        event.registerServerCommand(new CommandDebug());
        event.registerServerCommand(new CommandKit());
    }

    /**
     * Registers protected content-pack before preInit, to allow making a custom ResourcePackLoader allowing protected .zip
     *
     * @param event
     */
    @Mod.EventHandler
    public void constructionEvent(FMLConstructionEvent event) {
        LOGGER = LogManager.getLogger(ModularWarfare.MOD_ID);

        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
            PROTECTOR = new ModularProtector();
        }
        /**
         * Create & Check Addon System
         */

        this.addonDir = new File(ModUtil.getGameFolder() + "/addons");

        if (!this.addonDir.exists())
            this.addonDir.mkdirs();
        this.loaderManager = new AddonLoaderManager();
        this.loaderManager.constructAddons(this.addonDir, event.getSide());

        /**
         * Load the addon from the gradle project compilation (.class folder) instead of final .jar
         * in order to allow HotSwap changes
         */
        if(ModUtil.isIDE()) {
            File file = new File(ModUtil.getGameFolder()).getParentFile().getParentFile();
            String folder = file.toString().replace("\\", "/");
            this.loaderManager.constructDevAddons(new File(folder + "/melee-addon/build/classes/java/main"), "com.modularwarfare.melee.ModularWarfareMelee", event.getSide());
        }

        PROXY.construction(event);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        for (File file : contentPacks) {
            List<Item> tabOrder = new ArrayList<Item>();
            for (ItemGun itemGun : gunTypes.values()) {
                if (itemGun.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemGun);
                    tabOrder.add(itemGun);
                }
            }
            for (ItemAmmo itemAmmo : ammoTypes.values()) {
                if (itemAmmo.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemAmmo);
                    tabOrder.add(itemAmmo);
                }
            }
            for (ItemBullet itemBullet : bulletTypes.values()) {
                if (itemBullet.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemBullet);
                    tabOrder.add(itemBullet);
                }
            }
            for (ItemMWArmor itemArmor : armorTypes.values()) {
                if (itemArmor.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemArmor);
                    tabOrder.add(itemArmor);
                }
            }
            for (ItemAttachment itemAttachment : attachmentTypes.values()) {
                if (itemAttachment.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemAttachment);
                    tabOrder.add(itemAttachment);
                }
            }

            for (ItemSpecialArmor itemSpecialArmor : specialArmorTypes.values()) {
                if (itemSpecialArmor.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemSpecialArmor);
                    tabOrder.add(itemSpecialArmor);
                }
            }

            for (ItemSpray itemSpray : sprayTypes.values()) {
                if (itemSpray.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemSpray);
                    tabOrder.add(itemSpray);
                }
            }

            for (ItemBackpack itemBackpack : backpackTypes.values()) {
                if (itemBackpack.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemBackpack);
                    tabOrder.add(itemBackpack);
                }
            }

            for (ItemGrenade itemGrenade : grenadeTypes.values()) {
                if (itemGrenade.type.contentPack.equals(file.getName())) {
                    event.getRegistry().register(itemGrenade);
                    tabOrder.add(itemGrenade);
                }
            }

            ItemRegisterEvent itemRegisterEvent = new ItemRegisterEvent(event.getRegistry(), tabOrder);
            MinecraftForge.EVENT_BUS.post(itemRegisterEvent);

            itemRegisterEvent.tabOrder.forEach((item)->{
                if(item instanceof ItemGun){
                    for(SkinType skin: ((ItemGun) item).type.modelSkins) {
                        PROXY.preloadSkinTypes.put(skin, ((ItemGun) item).type);
                    }
                }
                if(item instanceof ItemMWArmor) {
                    for(SkinType skin: ((ItemMWArmor) item).type.modelSkins) {
                        PROXY.preloadSkinTypes.put(skin, ((ItemMWArmor) item).type);
                    }
                }

            });

            MODS_TABS.get(file.getName()).preInitialize(tabOrder);
        }

        event.getRegistry().register(new ItemLight("light"));
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {
        EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "bullethole"), EntityBulletHole.class, "bullethole", 3, this, 80, 10, false);
        EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "shell"), EntityShell.class, "shell", 4, this, 64, 1, false);
        EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "itemloot"), EntityItemLoot.class, "itemloot", 6, this, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "grenade"), EntityGrenade.class, "grenade", 7, this, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "smoke_grenade"), EntitySmokeGrenade.class, "smoke_grenade", 8, this, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "stun_grenade"), EntityStunGrenade.class, "stun_grenade", 9, this, 64, 1, true);

        //EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "bullet"), EntityBullet.class, "bullet", 15, this, 64, 1, true);
        EntityRegistry.registerModEntity(new ResourceLocation(ModularWarfare.MOD_ID, "explosive_projectile"), EntityExplosiveProjectile.class, "explosive_projectile", 15, this, 80, 1, true);
    }

    public static void registerRayCasting(RayCasting rayCasting){
        INSTANCE.RAY_CASTING = rayCasting;
    }

}

