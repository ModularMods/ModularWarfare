package com.modularwarfare.common;

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
import net.minecraftforge.fml.common.event.FMLConstructionEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;


public class CommonProxy extends ForgeEvent {

    public static Pattern zipJar = Pattern.compile("(.+).(zip|jar)$");


    public void construction(FMLConstructionEvent event) {

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

}
