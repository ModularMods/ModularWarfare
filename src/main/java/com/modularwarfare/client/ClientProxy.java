package com.modularwarfare.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.WeaponAnimations;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.client.export.ItemModelExport;
import com.modularwarfare.client.fpp.basic.animations.anims.*;
import com.modularwarfare.client.fpp.basic.configs.*;
import com.modularwarfare.client.fpp.basic.renderers.*;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.fpp.enhanced.renderers.RenderGunEnhanced;
import com.modularwarfare.client.handler.*;
import com.modularwarfare.client.hud.AttachmentUI;
import com.modularwarfare.client.hud.FlashSystem;
import com.modularwarfare.client.hud.GunUI;
import com.modularwarfare.client.killchat.KillFeedManager;
import com.modularwarfare.client.killchat.KillFeedRender;
import com.modularwarfare.client.fpp.basic.models.ModelGun;
import com.modularwarfare.client.fpp.basic.models.layers.RenderLayerBackpack;
import com.modularwarfare.client.fpp.basic.models.layers.RenderLayerBody;
import com.modularwarfare.client.patch.customnpc.CustomNPCListener;
import com.modularwarfare.client.patch.galacticraft.GCCompatInterop;
import com.modularwarfare.client.patch.galacticraft.GCDummyInterop;
import com.modularwarfare.client.patch.obfuscate.ObfuscateCompatInterop;
import com.modularwarfare.client.scope.ScopeUtils;
import com.modularwarfare.common.CommonProxy;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ArmorType.ArmorInfo;
import com.modularwarfare.common.armor.ItemMWArmor;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.backpacks.BackpackType;
import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.entity.EntityBulletClient;
import com.modularwarfare.common.entity.decals.EntityBulletHole;
import com.modularwarfare.common.entity.decals.EntityShell;
import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.entity.grenades.EntitySmokeGrenade;
import com.modularwarfare.common.entity.grenades.EntityStunGrenade;
import com.modularwarfare.common.entity.item.EntityItemLoot;
import com.modularwarfare.common.extra.ItemLight;
import com.modularwarfare.common.grenades.GrenadeType;
import com.modularwarfare.common.grenades.ItemGrenade;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.init.ModSounds;
import com.modularwarfare.common.particle.EntityBloodFX;
import com.modularwarfare.common.particle.ParticleExplosion;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.common.type.ContentTypes;
import com.modularwarfare.common.type.TypeEntry;
import com.modularwarfare.objects.SoundEntry;
import com.modularwarfare.utility.MWResourcePack;
import com.modularwarfare.utility.MWSound;
import com.modularwarfare.utility.ModUtil;
import mchhui.modularmovements.tactical.client.ClientLitener;
import net.lingala.zip4j.core.ZipFile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.opengl.GL11;
import paulscode.sound.SoundSystemConfig;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.modularwarfare.ModularWarfare.contentPacks;

public class ClientProxy extends CommonProxy {

    public static String modelDir = "com.modularwarfare.client.fpp.basic.model.";

    //Renderes
    public static RenderGunStatic gunStaticRenderer;
    public static RenderGunEnhanced gunEnhancedRenderer;

    public static RenderAmmo ammoRenderer;
    public static RenderAttachment attachmentRenderer;
    public static RenderGrenade grenadeRenderer;

    public static HashMap<String, SoundEvent> modSounds = new HashMap<String, SoundEvent>();

    public static ScopeUtils scopeUtils;
    public static FlashSystem flashImage;

    public static ItemLight itemLight = new ItemLight("light");

    public static ClientRenderHooks renderHooks;

    public static AttachmentUI attachmentUI;
    public static GunUI gunUI;

    public static KillFeedManager killFeedManager;
    /**
     * Patches
     **/
    public static GCCompatInterop galacticraftInterop;
    public static ObfuscateCompatInterop obfuscateInterop;

    public KillFeedManager getKillChatManager() {
        return this.killFeedManager;
    }

    @Override
    public void construction(FMLConstructionEvent event) {
        super.construction(event);

        for (File file : modularWarfareDir.listFiles()) {
            if (!file.getName().contains("cache") && !file.getName().contains("officialmw") && !file.getName().contains("highres")) {
                if (zipJar.matcher(file.getName()).matches()) {
                    try {
                        ZipFile zipFile = new ZipFile(file);
                        if (zipFile.isEncrypted()) {
                            /** Check if the zipFile is encrypted by a password or not */

                            ModularWarfare.PROTECTOR.requestPassword(file.getName());
                            ModularWarfare.PROTECTOR.applyPassword(zipFile, file.getName());

                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("modid", ModularWarfare.MOD_ID);
                            map.put("name", ModularWarfare.MOD_NAME + " : " + file.getName());
                            map.put("version", "1");

                            FMLModContainer container = new MWResourcePack.Container("com.modularwarfare.ModularWarfare", new ModCandidate(file, file, ContainerType.JAR), map, zipFile, ModularWarfare.MOD_NAME + " : " + file.getName());
                            container.bindMetadata(MetadataCollection.from(null, ""));
                            FMLClientHandler.instance().addModAsResource(container);
                            contentPacks.add(file);
                        } else {
                            try {
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                map.put("modid", ModularWarfare.MOD_ID);
                                map.put("name", ModularWarfare.MOD_NAME + " : " + file.getName());
                                map.put("version", "1");
                                FMLModContainer container = new FMLModContainer("com.modularwarfare.ModularWarfare", new ModCandidate(file, file, file.isDirectory() ? ContainerType.DIR : ContainerType.JAR), map);
                                container.bindMetadata(MetadataCollection.from(null, ""));
                                FMLClientHandler.instance().addModAsResource(container);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            contentPacks.add(file);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    try {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("modid", ModularWarfare.MOD_ID);
                        map.put("name", ModularWarfare.MOD_NAME + " : " + file.getName());
                        map.put("version", "1");
                        FMLModContainer container = new FMLModContainer("com.modularwarfare.ModularWarfare", new ModCandidate(file, file, file.isDirectory() ? ContainerType.DIR : ContainerType.JAR), map);
                        container.bindMetadata(MetadataCollection.from(null, ""));
                        FMLClientHandler.instance().addModAsResource(container);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    contentPacks.add(file);
                }
            }
        }
    }


    @Override
    public void preload() {
        //Smooth Swing Ticker Runnable
        SmoothSwingTicker smoothSwingTicker = new SmoothSwingTicker();
        Thread smoothTickThread = new Thread(smoothSwingTicker, "SmoothSwingThread");
        smoothTickThread.start();

        MinecraftForge.EVENT_BUS.register(this);
        startPatches();
        Minecraft.getMinecraft().gameSettings.useVbo = false;
    }

    public void startPatches() {
        if (Loader.isModLoaded("customnpcs")) {
            CustomNPCListener customNPCListener = new CustomNPCListener();
            MinecraftForge.EVENT_BUS.register(customNPCListener);
        }
        if (Loader.isModLoaded("galacticraftcore")) {
            try {
                ClientProxy.galacticraftInterop = (GCCompatInterop) Class.forName("com.modularwarfare.client.patch.galacticraft.GCInteropImpl").asSubclass(GCCompatInterop.class).newInstance();
                ModularWarfare.LOGGER.info("Galatic Craft has been detected! Will attempt to patch.");
                ClientProxy.galacticraftInterop.applyFix();
            } catch (Exception e) {
                e.printStackTrace();
                ClientProxy.galacticraftInterop = new GCDummyInterop();
            }
        } else {
            ClientProxy.galacticraftInterop = new GCDummyInterop();
        }
    }

    @Override
    public void load() {

        SoundSystemConfig.setNumberNormalChannels(1024);
        SoundSystemConfig.setNumberStreamingChannels(32);

        new KeyInputHandler();
        new ClientTickHandler();
        new ClientGunHandler();
        new RenderGuiHandler();

        this.renderHooks = new ClientRenderHooks();
        MinecraftForge.EVENT_BUS.register(this.renderHooks);

        this.scopeUtils = new ScopeUtils();
        MinecraftForge.EVENT_BUS.register(this.scopeUtils);

        this.flashImage = new FlashSystem();
        MinecraftForge.EVENT_BUS.register(this.flashImage);

        this.attachmentUI = new AttachmentUI();
        MinecraftForge.EVENT_BUS.register(this.attachmentUI);

        this.gunUI = new GunUI();
        MinecraftForge.EVENT_BUS.register(this.gunUI);

        this.killFeedManager = new KillFeedManager();
        MinecraftForge.EVENT_BUS.register(new KillFeedRender(this.killFeedManager));

        WeaponAnimations.registerAnimation("rifle", new AnimationRifle());
        WeaponAnimations.registerAnimation("rifle2", new AnimationRifle2());
        WeaponAnimations.registerAnimation("rifle3", new AnimationRifle3());
        WeaponAnimations.registerAnimation("rifle4", new AnimationRifle4());
        WeaponAnimations.registerAnimation("pistol", new AnimationPistol());
        WeaponAnimations.registerAnimation("revolver", new AnimationRevolver());
        WeaponAnimations.registerAnimation("shotgun", new AnimationShotgun());
        WeaponAnimations.registerAnimation("sniper", new AnimationSniperBottom());
        WeaponAnimations.registerAnimation("sniper_top", new AnimationSniperTop());
        WeaponAnimations.registerAnimation("sideclip", new AnimationSideClip());
        WeaponAnimations.registerAnimation("toprifle", new AnimationTopRifle());

        final Map<String, RenderPlayer> skinMap = Minecraft.getMinecraft().getRenderManager().getSkinMap();
        for (final RenderPlayer renderer : skinMap.values()) {
            setupLayers(renderer);
        }
    }

    public void setupLayers(RenderPlayer renderer) {
        renderer.addLayer(new RenderLayerBackpack(renderer, renderer.getMainModel().bipedBodyWear));
        renderer.addLayer(new RenderLayerBody(renderer, renderer.getMainModel().bipedBodyWear));
        // Disabled for animation third person test
        // renderer.addLayer(new RenderLayerHeldGun(renderer));
    }

    @Override
    public void init() {
        //Disable VAO on Mac computer (not compatibility)
        if(ModUtil.isMac()){
            ModConfig.INSTANCE.model_optimization = false;
        }
        if(!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
            Minecraft.getMinecraft().getFramebuffer().enableStencil();
        }
    }

    public void loadTextures() {
        ModularWarfare.LOGGER.info("Preloading textures");
        long time = System.currentTimeMillis();
        preloadSkinTypes.forEach((skin, type) -> {
            for (int i = 0; i < skin.textures.length; i++) {
                ResourceLocation resource = new ResourceLocation(ModularWarfare.MOD_ID,
                        String.format(skin.textures[i].format, type.getAssetDir(), skin.getSkin()));
                Minecraft.getMinecraft().getTextureManager().bindTexture(resource);
                if (skin.sampling.equals(SkinType.Sampling.LINEAR)) {
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                }
            }
        });
        ModularWarfare.LOGGER.info("All textures are ready(" + (System.currentTimeMillis() - time) + "ms)");
    }

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {

        for (ItemGun itemGun : ModularWarfare.gunTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemGun, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemGun.type.internalName));
        }

        for (ItemAmmo itemAmmo : ModularWarfare.ammoTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemAmmo, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemAmmo.type.internalName));
        }

        for (ItemAttachment itemAttachment : ModularWarfare.attachmentTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemAttachment, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemAttachment.type.internalName));
        }

        for (ItemBullet itemBullet : ModularWarfare.bulletTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemBullet, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemBullet.type.internalName));
        }

        for (ItemMWArmor itemArmor : ModularWarfare.armorTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemArmor, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemArmor.internalName));
        }

        for (ItemSpecialArmor itemArmor : ModularWarfare.specialArmorTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemArmor, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemArmor.type.internalName));
        }

        for (ItemSpray itemSpray : ModularWarfare.sprayTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemSpray, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemSpray.type.internalName));
        }

        for (ItemBackpack itemBackpack : ModularWarfare.backpackTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemBackpack, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemBackpack.type.internalName));
        }

        for (ItemGrenade itemGrenade : ModularWarfare.grenadeTypes.values()) {
            ModelLoader.setCustomModelResourceLocation(itemGrenade, 0, new ModelResourceLocation(ModularWarfare.MOD_ID + ":" + itemGrenade.type.internalName));
        }

        ModelLoader.setCustomModelResourceLocation(itemLight, 0, new ModelResourceLocation(itemLight.getRegistryName(), "inventory"));

    }

    @Override
    public void forceReload() {
        FMLClientHandler.instance().refreshResources(new IResourceType[0]);
    }

    /**
     * Helper method that sorts out packages with staticModel name input
     * For example, the staticModel class "com.modularwarfare.client.staticModel.mw.ModelMP5"
     * is referenced in the type file by the string "mw.MP5"
     */
    private String getModelName(String in) {
        //Split about dots
        String[] split = in.split("\\.");
        //If there is no dot, our staticModel class is in the default staticModel package
        if (split.length == 1)
            return in;
            //Otherwise, we need to slightly rearrange the wording of the string for it to make sense
        else if (split.length > 1) {
            String out = split[split.length - 1];
            for (int i = split.length - 2; i >= 0; i--) {
                out = split[i] + "." + out;
            }
            return out;
        }
        return in;
    }

    /**
     * Generic staticModel loader method for getting staticModel classes and casting them to the required class type
     */
    @Override
    public <T> T loadModel(String s, String shortName, Class<T> typeClass) {
        if (s == null || shortName == null)
            return null;
        try {
            return typeClass.cast(Class.forName(modelDir + getModelName(s)).getConstructor().newInstance());
        } catch (Exception e) {
            ModularWarfare.LOGGER.error("Failed to load staticModel : " + shortName + " (" + s + ")");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void reloadModels(boolean reloadSkins) {
        for (BaseType baseType : ModularWarfare.baseTypes) {
            if (baseType.hasModel()) {
                baseType.reloadModel();
            }
        }
        if (reloadSkins)
            forceReload();
    }

    @Override
    public void generateJsonModels(ArrayList<BaseType> types) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (BaseType type : types) {
            if (type.contentPack == null)
                continue;

            File contentPackDir = new File(ModularWarfare.MOD_DIR, type.contentPack);
            if (contentPackDir.exists() && contentPackDir.isDirectory()) {

                File itemModelsDir = new File(contentPackDir, "/assets/modularwarfare/models/item");
                if (!itemModelsDir.exists())
                    itemModelsDir.mkdirs();
                File typeModel = new File(itemModelsDir, type.internalName + ".json");

                if (ModularWarfare.DEV_ENV) {
                    if (type instanceof ArmorType) {
                        ArmorType armorType = (ArmorType) type;
                        for (ArmorInfo armorInfo : armorType.armorTypes.values()) {
                            String internalName = armorInfo.internalName != null ? armorInfo.internalName : armorType.internalName;
                            typeModel = new File(itemModelsDir, internalName + ".json");
                            try {
                                FileWriter fileWriter = new FileWriter(typeModel, false);
                                gson.toJson(createJson(type, internalName), fileWriter);
                                fileWriter.flush();
                                fileWriter.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            FileWriter fileWriter = new FileWriter(typeModel, false);
                            gson.toJson(createJson(type), fileWriter);
                            fileWriter.flush();
                            fileWriter.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                /**
                 * Create directories & files for .render.json if they don't exist
                 */
                if (ModularWarfare.DEV_ENV) {
                    final File dir = new File(contentPackDir, "/" + type.getAssetDir() + "/render");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    final File renderFile = new File(dir, type.internalName + ".render.json");
                    if (!renderFile.exists()) {
                        try {
                            FileWriter fileWriter = new FileWriter(renderFile, true);
                            if (type instanceof GunType) {
                                if (((GunType) type).animationType.equals(WeaponAnimationType.ENHANCED)) {
                                    GunEnhancedRenderConfig renderConfig = new GunEnhancedRenderConfig();
                                    renderConfig.modelFileName = type.internalName.replaceAll(type.contentPack + ".", "");
                                    renderConfig.modelFileName = renderConfig.modelFileName + ".glb";
                                    gson.toJson(renderConfig, fileWriter);
                                } else {
                                    GunRenderConfig renderConfig = new GunRenderConfig();
                                    renderConfig.modelFileName = type.internalName.replaceAll(type.contentPack + ".", "");
                                    renderConfig.modelFileName = renderConfig.modelFileName + ".obj";
                                    gson.toJson(renderConfig, fileWriter);
                                }
                                fileWriter.flush();
                                fileWriter.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void generateJsonSounds(Collection<ItemGun> types, boolean replace) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, ArrayList<String>> cpSounds = new HashMap<String, ArrayList<String>>();

        for (ItemGun itemGun : types) {
            GunType type = itemGun.type;
            if (type.contentPack == null)
                continue;

            String contentPack = type.contentPack;

            if (!cpSounds.containsKey(contentPack))
                cpSounds.put(contentPack, new ArrayList<String>());

            for (WeaponSoundType weaponSoundType : type.weaponSoundMap.keySet()) {
                ArrayList<SoundEntry> soundEntries = type.weaponSoundMap.get(weaponSoundType);
                for (SoundEntry soundEntry : soundEntries) {
                    if (soundEntry.soundName != null && !cpSounds.get(contentPack).contains(soundEntry.soundName))
                        cpSounds.get(contentPack).add(soundEntry.soundName);

                    if (soundEntry.soundNameDistant != null && !cpSounds.get(contentPack).contains(soundEntry.soundNameDistant))
                        cpSounds.get(contentPack).add(soundEntry.soundNameDistant);
                }
            }
        }

        for (String contentPack : cpSounds.keySet()) {
            try {
                File contentPackDir = new File(ModularWarfare.MOD_DIR, contentPack);
                if (contentPackDir.exists() && contentPackDir.isDirectory()) {
                    ArrayList<String> soundEntries = cpSounds.get(contentPack);
                    if (soundEntries != null && !soundEntries.isEmpty()) {
                        Path assetsDir = Paths.get(ModularWarfare.MOD_DIR.getAbsolutePath() + "/" + contentPack + "/assets/modularwarfare/");
                        if (!Files.exists(assetsDir))
                            Files.createDirectories(assetsDir);
                        Path soundsFile = Paths.get(assetsDir + "/sounds.json");

                        boolean soundsExists = Files.exists(soundsFile);
                        boolean shouldCreate = soundsExists ? replace : true;
                        if (shouldCreate) {
                            if (!soundsExists)
                                Files.createFile(soundsFile);

                            ArrayList<String> jsonEntries = new ArrayList<String>();
                            String format = "\"%s\":{\"category\": \"player\",\"subtitle\": \"MW Sound\",\"sounds\": [\"modularwarfare:%s\"]}";
                            jsonEntries.add("{");
                            for (int i = 0; i < soundEntries.size(); i++) {
                                if (i + 1 < soundEntries.size()) {
                                    // add comma
                                    jsonEntries.add(format.replaceAll("%s", soundEntries.get(i)) + ",");
                                } else {
                                    // no comma
                                    jsonEntries.add(format.replaceAll("%s", soundEntries.get(i)));
                                }
                            }
                            jsonEntries.add("}");
                            Files.write(soundsFile, jsonEntries, Charset.forName("UTF-8"));
                        }
                    }
                }
            } catch (Exception exception) {
                if (ModularWarfare.DEV_ENV) {
                    exception.printStackTrace();
                } else {
                    ModularWarfare.LOGGER.error(String.format("Failed to create sounds.json for content pack '%s'", contentPack));
                }
            }
        }
    }

    @Override
    public void generateLangFiles(ArrayList<BaseType> types, boolean replace) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        HashMap<String, ArrayList<BaseType>> langEntryMap = new HashMap<String, ArrayList<BaseType>>();

        for (BaseType baseType : types) {
            if (baseType.contentPack == null)
                continue;

            String contentPack = baseType.contentPack;

            if (!langEntryMap.containsKey(contentPack))
                langEntryMap.put(contentPack, new ArrayList<BaseType>());

            if (baseType.displayName != null && !langEntryMap.get(contentPack).contains(baseType))
                langEntryMap.get(contentPack).add(baseType);

            if (baseType instanceof ArmorType)
                langEntryMap.get(contentPack).add(baseType);
        }

        for (String contentPack : langEntryMap.keySet()) {
            try {
                File contentPackDir = new File(ModularWarfare.MOD_DIR, contentPack);
                if (contentPackDir.exists() && contentPackDir.isDirectory()) {
                    ArrayList<BaseType> langEntries = langEntryMap.get(contentPack);
                    if (langEntries != null && !langEntries.isEmpty()) {
                        Path langDir = Paths.get(ModularWarfare.MOD_DIR.getAbsolutePath() + "/" + contentPack + "/assets/modularwarfare/lang/");
                        if (!Files.exists(langDir))
                            Files.createDirectories(langDir);
                        Path langPath = Paths.get(langDir + "/en_US.lang");

                        boolean soundsExists = Files.exists(langPath);
                        boolean shouldCreate = soundsExists ? replace : true;
                        if (shouldCreate) {
                            if (!soundsExists)
                                Files.createFile(langPath);

                            ArrayList<String> jsonEntries = new ArrayList<String>();
                            String format = "item.%s.name=%s";
                            for (int i = 0; i < langEntries.size(); i++) {
                                BaseType type = langEntries.get(i);
                                if (type instanceof ArmorType) {
                                    ArmorType armorType = (ArmorType) type;
                                    for (ArmorInfo armorInfo : armorType.armorTypes.values()) {
                                        String internalName = armorInfo.internalName != null ? armorInfo.internalName : armorType.internalName;
                                        jsonEntries.add(String.format(format, internalName, armorInfo.displayName));
                                    }
                                } else {
                                    jsonEntries.add(String.format(format, type.internalName, type.displayName));
                                }
                            }
                            Files.write(langPath, jsonEntries, Charset.forName("UTF-8"));
                        }
                    }
                }
            } catch (Exception exception) {
                if (ModularWarfare.DEV_ENV) {
                    exception.printStackTrace();
                } else {
                    ModularWarfare.LOGGER.error(String.format("Failed to create sounds.json for content pack '%s'", contentPack));
                }
            }
        }
    }

    private ItemModelExport createJson(BaseType type) {
        ItemModelExport exportedModel = new ItemModelExport();

        if (!(type instanceof GunType) && !(type instanceof GrenadeType)) {
            exportedModel.display.thirdperson_lefthand.scale[0] = 0.4f;
            exportedModel.display.thirdperson_lefthand.scale[1] = 0.4f;
            exportedModel.display.thirdperson_lefthand.scale[2] = 0.4f;

            exportedModel.display.thirdperson_righthand.scale[0] = 0.4f;
            exportedModel.display.thirdperson_righthand.scale[1] = 0.4f;
            exportedModel.display.thirdperson_righthand.scale[2] = 0.4f;
        } else {
            exportedModel.display.thirdperson_lefthand.scale[0] = 0.0f;
            exportedModel.display.thirdperson_lefthand.scale[1] = 0.0f;
            exportedModel.display.thirdperson_lefthand.scale[2] = 0.0f;

            exportedModel.display.thirdperson_righthand.scale[0] = 0.0f;
            exportedModel.display.thirdperson_righthand.scale[1] = 0.0f;
            exportedModel.display.thirdperson_righthand.scale[2] = 0.0f;
        }
        exportedModel.setBaseLayer(type.getAssetDir() + "/" + (type.iconName != null ? type.iconName : type.internalName));
        return exportedModel;
    }

    private ItemModelExport createJson(BaseType type, String iconName) {
        ItemModelExport exportedModel = new ItemModelExport();

        exportedModel.display.thirdperson_lefthand.scale[0] = 0.4f;
        exportedModel.display.thirdperson_lefthand.scale[1] = 0.4f;
        exportedModel.display.thirdperson_lefthand.scale[2] = 0.4f;

        exportedModel.display.thirdperson_righthand.scale[0] = 0.4f;
        exportedModel.display.thirdperson_righthand.scale[1] = 0.4f;
        exportedModel.display.thirdperson_righthand.scale[2] = 0.4f;

        exportedModel.setBaseLayer(type.getAssetDir() + "/" + iconName);
        return exportedModel;
    }

    @Override
    public void playSound(MWSound sound) {
        SoundEvent soundEvent = modSounds.get(sound.soundName);
        if (soundEvent == null) {
            ModularWarfare.LOGGER.error(String.format("The sound named '%s' does not exist. Skipping playSound", sound.soundName));
            return;
        }

        Minecraft.getMinecraft().world.playSound(Minecraft.getMinecraft().player, sound.blockPos, soundEvent, SoundCategory.PLAYERS, sound.volume, sound.pitch);
    }

    @Override
    public void registerSound(String soundName) {
        ResourceLocation resourceLocation = new ResourceLocation(ModularWarfare.MOD_ID, soundName);
        modSounds.put(soundName, new SoundEvent(resourceLocation).setRegistryName(resourceLocation));
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
        IForgeRegistry<SoundEvent> registry = event.getRegistry();
        for (SoundEvent soundEvent : modSounds.values()) {
            registry.register(soundEvent);
        }

        for (WeaponSoundType weaponSoundType : WeaponSoundType.values()) {
            if (weaponSoundType.defaultSound != null) {
                registerSound(weaponSoundType.defaultSound);
                registry.register(modSounds.get(weaponSoundType.defaultSound));
            }
        }
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityEntry> event) {

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {

            //BULLET HOLE
            RenderingRegistry.registerEntityRenderingHandler(EntityBulletHole.class, RenderDecal.FACTORY);

            //RENDER SHELL EJECTION
            RenderingRegistry.registerEntityRenderingHandler(EntityShell.class, RenderShell.FACTORY);

            //RENDER GRENADES
            RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, RenderGrenadeEntity.FACTORY);
            RenderingRegistry.registerEntityRenderingHandler(EntitySmokeGrenade.class, RenderGrenadeEntity.FACTORY);
            RenderingRegistry.registerEntityRenderingHandler(EntityStunGrenade.class, RenderGrenadeEntity.FACTORY);

            RenderingRegistry.registerEntityRenderingHandler(EntityItemLoot.class, RenderItemLoot.FACTORY);

            RenderingRegistry.registerEntityRenderingHandler(EntityBulletClient.class, RenderBullet.FACTORY);

        }

    }

    @Override
    public void onShootAnimation(EntityPlayer player, String wepType, int fireTickDelay, float recoilPitch, float recoilYaw) {
        GunType gunType = ModularWarfare.gunTypes.get(wepType).type;
        if (gunType != null) {
            ClientRenderHooks.getAnimMachine(player).triggerShoot((ModelGun) gunType.model, gunType, fireTickDelay);

            RenderParameters.rate = Math.min(RenderParameters.rate + 0.07f, 1f);

            float recoilPitchGripFactor = 1.0f;
            float recoilYawGripFactor = 1.0f;

            float recoilPitchBarrelFactor = 1.0f;
            float recoilYawBarrelFactor = 1.0f;

            if (GunType.getAttachment(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Grip) != null) {
                ItemAttachment gripAttachment = (ItemAttachment) GunType.getAttachment(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Grip).getItem();
                recoilPitchGripFactor = gripAttachment.type.grip.recoilPitchFactor;
                recoilYawGripFactor = gripAttachment.type.grip.recoilYawFactor;
            }

            if (GunType.getAttachment(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Barrel) != null) {
                ItemAttachment barrelAttachment = (ItemAttachment) GunType.getAttachment(player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentEnum.Barrel).getItem();
                recoilPitchBarrelFactor = barrelAttachment.type.barrel.recoilPitchFactor;
                recoilYawBarrelFactor = barrelAttachment.type.barrel.recoilYawFactor;
            }

            boolean isCrawling = false;
            if(Loader.isModLoaded("modularmovements")){
                if(ClientLitener.clientPlayerState.isCrawling){
                    isCrawling = true;
                }
            }
            if (!(ClientRenderHooks.isAiming || ClientRenderHooks.isAimingScope)) {
                RenderParameters.playerRecoilPitch += (gunType.recoilPitch + (1.0f * (gunType.randomRecoilPitch * 2) - gunType.randomRecoilPitch)) * (recoilPitchGripFactor * recoilPitchBarrelFactor);

                RenderParameters.playerRecoilYaw += RenderParameters.rate * (isCrawling ? 0.2f : 1.0f) * (RenderParameters.phase ? 1 : -1 * gunType.recoilYaw + (new Random().nextFloat() * (gunType.randomRecoilYaw * 2) - gunType.randomRecoilYaw)) * (recoilYawGripFactor * recoilYawBarrelFactor);
            } else {
                RenderParameters.playerRecoilPitch += ((gunType.recoilPitch + (1.0f * (gunType.randomRecoilPitch * 2) - gunType.randomRecoilPitch)) * gunType.recoilAimReducer) * (recoilPitchGripFactor * recoilPitchBarrelFactor);

                RenderParameters.playerRecoilYaw += RenderParameters.rate * (isCrawling ? 0.2f : 1.0f) * ((RenderParameters.phase ? 1 : -1 * gunType.recoilYaw + (new Random().nextFloat() * (gunType.randomRecoilYaw * 2) - gunType.randomRecoilYaw)) * gunType.recoilAimReducer) * (recoilYawGripFactor * recoilYawBarrelFactor);
            }
            RenderParameters.phase = !RenderParameters.phase;
        }
    }

    @Override
    public void onReloadAnimation(EntityPlayer player, String wepType, int reloadTime, int reloadCount, int reloadType) {
        ClientTickHandler.playerReloadCooldown.put(player.getUniqueID(), reloadTime);
        ItemGun gunType = ModularWarfare.gunTypes.get(wepType);
        if (gunType != null) {
            ClientRenderHooks.getAnimMachine(player).triggerReload(reloadTime, reloadCount, (ModelGun) gunType.type.model, ReloadType.getTypeFromInt(reloadType), player.isSprinting());
        }
    }

    @Override
    public World getClientWorld() {
        return FMLClientHandler.instance().getClient().world;
    }

    @Override
    public void registerEventHandlers() {
        super.registerEventHandlers();
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
    }


    @Override
    public void addBlood(final EntityLivingBase living, final int amount, final boolean onhit) {
        if (onhit) {
            this.addBlood(living, amount);
        }
    }

    @Override
    public void playHitmarker(boolean headshot) {
        if (ModConfig.INSTANCE.hud.hitmarkers) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getRecord(ClientProxy.modSounds.get("hitmarker"), 1f, 4f));
            ClientProxy.gunUI.addHitMarker(headshot);
        }
    }

    @Override
    public void addBlood(final EntityLivingBase living, final int amount) {
        for (int k = 0; k < amount; ++k) {
            float attenuator = 0.3f;
            double mX = -MathHelper.sin(living.rotationYaw / 180.0f * 3.1415927f) * MathHelper.cos(living.rotationPitch / 180.0f * 3.1415927f) * attenuator;
            double mZ = MathHelper.cos(living.rotationYaw / 180.0f * 3.1415927f) * MathHelper.cos(living.rotationPitch / 180.0f * 3.1415927f) * attenuator;
            double mY = -MathHelper.sin(living.rotationPitch / 180.0f * 3.1415927f) * attenuator + 0.1f;
            attenuator = 0.02f;
            final float var5 = living.getRNG().nextFloat() * 3.1415927f * 2.0f;
            attenuator *= living.getRNG().nextFloat();
            mX += Math.cos(var5) * attenuator;
            mY += (living.getRNG().nextFloat() - living.getRNG().nextFloat()) * 0.1f;
            mZ += Math.sin(var5) * attenuator;
            final Particle blood = new EntityBloodFX(living.getEntityWorld(), living.posX, living.posY + 0.5 + living.getRNG().nextDouble() * 0.7, living.posZ, living.motionX * 2.0 + mX, living.motionY + mY, living.motionZ * 2.0 + mZ, 0.0);
            Minecraft.getMinecraft().effectRenderer.addEffect(blood);
        }
    }

    @Override
    public void resetSens() {
        ClientRenderHooks.isAimingScope = false;
        ClientRenderHooks.isAiming = false;
    }

    @Override
    public void spawnExplosionParticle(World world, double x, double y, double z) {
        final Particle explosionParticle = new ParticleExplosion(world, x, y, z);
        Minecraft.getMinecraft().effectRenderer.addEffect(explosionParticle);
    }

    @Override
    public void playFlashSound(EntityPlayer entityPlayer) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(ModSounds.FLASHED, SoundCategory.PLAYERS, (float) FlashSystem.flashValue / 1000, 1, (float) entityPlayer.posX, (float) entityPlayer.posY, (float) entityPlayer.posZ));
        Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(ModSounds.FLASHED, SoundCategory.PLAYERS, 5.0f, 0.2f, (float) entityPlayer.posX, (float) entityPlayer.posY, (float) entityPlayer.posZ));
        Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(ModSounds.FLASHED, SoundCategory.PLAYERS, 5.0f, 0.1f, (float) entityPlayer.posX, (float) entityPlayer.posY, (float) entityPlayer.posZ));
    }
}
