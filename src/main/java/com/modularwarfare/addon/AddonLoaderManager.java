package com.modularwarfare.addon;

import com.modularwarfare.ModularWarfare;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonLoaderManager {

    public List<IContentAddon> addons = new ArrayList<>();
    public Map<String, List<Object>> eventClasses = new HashMap<>();
    public List<ICommand> registeredCommands = new ArrayList<>();
    /**
     * Loaded addons: modid
     */
    public List<String> loaded_addons = new ArrayList<>();

    /**
     * Registers a class containing events just like you would in a mod.
     * When unloading an addon, all event classes will be unregistered automatically
     *
     * @param eventClass - the class that contains events with {@link SubscribeEvent} annotation
     */
    public void registerEventClass(Object eventClass) {
        MinecraftForge.EVENT_BUS.register(eventClass);
        String currentAddon = addons.get(addons.size() - 1).getAddonID();

        if (this.eventClasses.containsKey(currentAddon))
            this.eventClasses.get(currentAddon).add(eventClass);

        else {
            List<Object> newList = new ArrayList<>();
            newList.add(eventClass);
            this.eventClasses.put(currentAddon, newList);
        }
    }

    /**
     * Registers a command to the internal map of <strong>server</strong>. Do not use it on clients playing on server
     *
     * @param cmd - The command to register
     */
    public void registerCommand(ICommand cmd) {
        CommandHandler ch = (CommandHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ch.registerCommand(cmd);
        this.registeredCommands.add(cmd);
    }

    public boolean isLoaded(String modid) {
        return loaded_addons.contains(modid);
    }

    public void constructAddons(File dirAddon, Side side) {
        for (File file : dirAddon.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                try {
                    ModularWarfare.LOGGER.info("ModularWarfare >> Trying to load addon: " + file.getName());
                    this.loadAddon(file, side);
                } catch (AddonLoadingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void constructDevAddons(File dirDevAddon, String mainClass, Side side) {
        ModularWarfare.LOGGER.info("ModularWarfare >> Trying to load addon: " + dirDevAddon.getName());
        try {
            this.loadDevAddon(dirDevAddon, mainClass, side);
        } catch (AddonLoadingException e) {
            e.printStackTrace();
        }
    }

    public void preInitAddons(FMLPreInitializationEvent event) {
        for (IContentAddon addon : addons) {
            ModularWarfare.LOGGER.info("Trying to preInit " + addon.getName());
            addon.preInit(event, ModularWarfare.loaderManager);
        }
    }

    public void initAddons(FMLPostInitializationEvent event) {
        for (IContentAddon addon : addons) {
            ModularWarfare.LOGGER.info("Trying to init " + addon.getName());
            addon.init(event, ModularWarfare.loaderManager);
        }
    }

    public void loadAddon(File file, Side side) throws AddonLoadingException {
        if (!file.exists())
            throw new AddonLoadingException("File '" + file.getAbsolutePath() + "' doesn't exist, aborting!");

        JarFile jar = null;
        InputStream inputStream = null;
        String mainClass = null;
        try {
            jar = new JarFile(file);
            JarEntry entry = jar.getJarEntry("main.mwf");
            if (entry == null) {
                throw new AddonLoadingException("Jar does not contain main.mwf");
            }
            inputStream = jar.getInputStream(entry);
            mainClass = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            ModularWarfare.LOGGER.warn("Could not load main class from main.mwf file in '" + file.getName() + "'\n More info: " + ExceptionUtils.getStackTrace(ex));
            throw new AddonLoadingException("Could not load main class from main.mwf file in '" + file.getName() + "'\n More info in console");
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                    ModularWarfare.LOGGER.warn("[Addon] Closed file !");
                } catch (IOException e) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        try {
            if (mainClass == null) {
                throw new AddonLoadingException("Could not load main class from main.mwf file in '" + file.getName() + "'");
            }
            ModularWarfare.LOADER.loadFile(file, mainClass, side);
        } catch (MalformedURLException mue) {
            ModularWarfare.LOGGER.warn("File path provided '" + file.getAbsolutePath() + "' threw an exception (MalformedURLException)\n More info: " + ExceptionUtils.getStackTrace(mue));
            throw new AddonLoadingException("File path provided threw an exception (MalformedURLException). More info in console.");
        }
    }

    public void loadDevAddon(File file, String mainClass, Side side) throws AddonLoadingException {
        if (!file.exists())
            throw new AddonLoadingException("File '" + file.getAbsolutePath() + "' doesn't exist, aborting!");

        try {
            if (mainClass == null) {
                throw new AddonLoadingException("Could not load main class from main.mwf file in '" + file.getName() + "'");
            }
            ModularWarfare.LOADER.loadFile(file, mainClass, side);
        } catch (MalformedURLException mue) {
            ModularWarfare.LOGGER.warn("File path provided '" + file.getAbsolutePath() + "' threw an exception (MalformedURLException)\n More info: " + ExceptionUtils.getStackTrace(mue));
            throw new AddonLoadingException("File path provided threw an exception (MalformedURLException). More info in console.");
        }
    }
}
