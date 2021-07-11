package com.modularwarfare;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class ModConfig {

    public transient static ModConfig INSTANCE;

    public boolean enableHitmarker = true;
    public boolean enableModifiedInventory = true;
    public boolean enableDynamicCrosshair = true;
    public boolean enable3DModelsDrops = true;
    public boolean disableGunInteraction = true;

    public boolean UIshowAmmoCount = true;

    public boolean dropBulletCasing = true;

    public int despawnTimeShellCasing = 10;
    public int despawnTimeItemsDrops = 120;

    public boolean canShotBreakGlass = false;

    public boolean dropExtraSlotsOnDeath = true;
    public boolean kickIfModifiedContentPack = true;
    public boolean applyKnockback = false;

    public boolean enableWalkSounds = true;
    public float walkSoundsVolume = 0.3f;

    public boolean autoExtractContentpack = true;

    public KillFeed killFeed = new KillFeed();

    public boolean dev_mode = true;
    public String version = ModularWarfare.MOD_VERSION;

    public ModConfig(File configFile) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (configFile.exists()) {
                JsonReader jsonReader = new JsonReader(new FileReader(configFile));
                ModConfig config = gson.fromJson(jsonReader, ModConfig.class);
                System.out.println("Comparing version " + config.version + " to " + ModularWarfare.MOD_VERSION);
                if (config.version == null || !config.version.matches(ModularWarfare.MOD_VERSION)) {
                    try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile),"UTF-8")) {
                        gson.toJson(this, writer);
                    }
                    INSTANCE = this;
                } else {
                    INSTANCE = config;
                }
            } else {
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile),"UTF-8")) {
                    gson.toJson(this, writer);
                }
                INSTANCE = this;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class KillFeed {
        public boolean enableKillFeed = true;
        public boolean sendDefaultKillMessage = false;
        public int messageDuration = 10;
        public List<String> messageList = Arrays.asList("&a{killer} &7killed &c{victim}", "&a{killer} &fdestroyed &c{victim}", "&a{killer} &fshoted &c{victim}");
    }

}