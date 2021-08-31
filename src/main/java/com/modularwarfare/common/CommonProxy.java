package com.modularwarfare.common;

import com.google.common.collect.ImmutableList;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.utility.MWSound;
import com.modularwarfare.utility.event.ForgeEvent;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.relauncher.CoreModManager;
import com.modularwarfare.ModConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.modularwarfare.ModularWarfare.MOD_VERSION;


public class CommonProxy extends ForgeEvent {

    public static Pattern zipJar = Pattern.compile("(.+).(zip|jar)$");


    public void construction(FMLConstructionEvent event) {
        //Production-environment
        File myDir = new File("ModularWarfare");
        File modFile = null;

        if (!myDir.exists()) {
            if (myDir.getParentFile() != null) {
                //Dev-environment
                myDir = new File(myDir.getParentFile().getParentFile(), "ModularWarfare");
                if (!myDir.exists()) {
                    //first-run of the mod, in production environment
                    myDir = new File("ModularWarfare");
                    myDir.mkdirs();
                }
            } else //First run, in production environment
                myDir.mkdirs();
        }


        // Creates directory if doesn't exist
        ModularWarfare.MOD_DIR = myDir;
        if (!ModularWarfare.MOD_DIR.exists()) {
            ModularWarfare.MOD_DIR.mkdir();
            ModularWarfare.DEV_ENV = ModConfig.INSTANCE.dev_mode;
        }
        new ModConfig(new File(ModularWarfare.MOD_DIR, "mod_config.json"));

        List<String> knownLibraries = ImmutableList.<String>builder()
                // skip default libs
                .addAll(event.getModClassLoader().getDefaultLibraries())
                // skip loaded coremods
                .addAll(CoreModManager.getIgnoredMods())
                // skip reparse coremods here
                .addAll(CoreModManager.getReparseableCoremods())
                .build();

        File[] minecraftSources = event.getModClassLoader().getParentSources();
        if (minecraftSources.length == 1 && minecraftSources[0].isFile()) {
            FMLLog.log.debug("Minecraft is a file at {}, loading", minecraftSources[0].getAbsolutePath());
        } else {
            int i = 0;
            for (File source : minecraftSources) {
                if (source.isFile()) {
                    if (knownLibraries.contains(source.getName()) || event.getModClassLoader().isDefaultLibrary(source)) {
                        FMLLog.log.trace("Skipping known library file {}", source.getAbsolutePath());
                    } else {
                        FMLLog.log.debug("Found a minecraft related file at {}, examining for mod candidates", source.getAbsolutePath());
                        if (source.getAbsolutePath().contains("modularwarfare")) {
                            modFile = source;
                        }
                    }
                } else if (minecraftSources[i].isDirectory()) {
                    FMLLog.log.debug("Found a minecraft related directory at {}, examining for mod candidates", source.getAbsolutePath());
                }
                i++;
            }
        }

        boolean needPrototypeExtract = ModConfig.INSTANCE.autoExtractContentpack;
        for (File file : myDir.listFiles()) {
            if (file.getName().matches("prototype-" + MOD_VERSION + "-contentpack.zip")) {
                needPrototypeExtract = false;
            } else if (file.getName().contains("prototype") && !file.getName().contains(MOD_VERSION) && file.getName().contains(".zip") && !file.getName().endsWith(".bak")) {
                file.renameTo(new File(file.getAbsolutePath() + ".bak"));
            }
        }
        if (needPrototypeExtract) {
            try {
                ZipFile zipFile = new ZipFile(modFile);
                if (zipFile.isValidZipFile()) {
                    zipFile.extractFile("prototype-" + MOD_VERSION + "-contentpack.zip", myDir.getAbsolutePath());
                }
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }

    }

    public void preload() {

    }

    public void load() {

    }

    public void init() {
    }

    public void forceReload() {
    }

    public List<File> getContentList() {
        List<File> contentPacks = new ArrayList<File>();
        for (File file : ModularWarfare.MOD_DIR.listFiles()) {
            if (!file.getName().contains("cache") && !file.getName().contains("officialmw") && !file.getName().contains("highres")) {
                if (file.isDirectory()) {
                    contentPacks.add(file);
                } else if (zipJar.matcher(file.getName()).matches()) {
                    try {
                        ZipFile zipFile = new ZipFile(file);
                        if (!zipFile.isEncrypted()) {
                            contentPacks.add(file);
                        } else {
                            ModularWarfare.LOGGER.info("[WARNING] ModularWarfare can't load encrypted content-packs in server-side (" + file.getName() + ") !");
                        }
                    } catch (ZipException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ModularWarfare.LOGGER.info("Loaded content pack list server side.");
        return contentPacks;
    }

    public <T> T loadModel(String s, String shortName, Class<T> typeClass) {
        return null;
    }

    public void spawnExplosionParticle(World par1World, double par2, double par4, double par6) {
    }


    public void reloadModels(boolean reloadSkins) {
    }

    public void generateJsonModels(ArrayList<BaseType> types) {
    }

    public void generateJsonSounds(Collection<ItemGun> types, boolean replace) {
    }

    public void generateLangFiles(ArrayList<BaseType> types, boolean replace) {
    }

    public void playSound(MWSound sound) {
    }

    public void playHitmarker(boolean headshot) {
    }

    public void registerSound(String soundName) {
    }

    public void onShootAnimation(EntityPlayer player, String wepType, int fireTickDelay, float recoilPitch, float recoilYaw) {
    }

    public void onReloadAnimation(EntityPlayer player, String wepType, int reloadTime, int reloadCount, int reloadType) {
    }

    public World getClientWorld() {
        return null;
    }

    public void addBlood(final EntityLivingBase living, final int amount) {
    }

    public void addBlood(final EntityLivingBase living, final int amount, final boolean onhit) {
    }

    public void registerEventHandlers() {
    }

    public void resetSens() {
    }

    public void playFlashSound(EntityPlayer player) {
    }

}
