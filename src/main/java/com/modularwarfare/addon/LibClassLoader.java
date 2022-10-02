package com.modularwarfare.addon;

import com.modularwarfare.ModularWarfare;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class LibClassLoader extends URLClassLoader {

    private LaunchClassLoader parentCL;

    public LibClassLoader(ClassLoader parentLoader) {
        super(new URL[0], parentLoader); // this is how forge loads mods, I have no idea why this specific approach of loading jar files works
        parentCL = (LaunchClassLoader) parentLoader;
    }

    public void loadFile(File libFile, String main, Side side) throws MalformedURLException, AddonLoadingException {
        Class<?> mainClass;
        IContentAddon addon = null;
        parentCL.addURL(libFile.toURI().toURL());

        try {
            mainClass = Class.forName(main, true, parentCL);

            try {
                addon = (IContentAddon) mainClass.newInstance();

                if (ModularWarfare.loaderManager.addons.contains(addon.getAddonID())) {
                    throw new AddonLoadingException("Addon '" + addon.getAddonID() + "' is already loaded!");
                }
                ModularWarfare.LOGGER.log(Level.INFO, "ModularWarfare >> Loading " + addon.getName());
                ModularWarfare.loaderManager.addons.add(addon); //add before loading to prevent half loading without noticing while catching an exception
                ModularWarfare.loaderManager.loaded_addons.add(addon.getAddonID());
                if (side.isClient()) {
                    ModularWarfare.LOGGER.info("Attempting to load container mod for " + addon.getAddonID());
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("modid", addon.getAddonID());
                    map.put("name", addon.getName());
                    map.put("version", "1");
                    FMLModContainer container = new FMLModContainer("com.modularwarfare", new ModCandidate(libFile, libFile, libFile.isDirectory() ? ContainerType.DIR : ContainerType.JAR), map);
                    container.bindMetadata(MetadataCollection.from(null, ""));
                    FMLClientHandler.instance().addModAsResource(container);
                }
                addon.construct(side, ModularWarfare.loaderManager);
            } catch (ClassCastException cce) {
                ModularWarfare.LOGGER.warn("Main class '" + mainClass + "' in lib '" + libFile.getName() + "' does not implement required IContentAddon class!");
            } catch (InstantiationException ie) {
                ModularWarfare.LOGGER.warn("Main class '" + mainClass + "' in lib '" + libFile.getName() + "' could not be instatiated!");
            } catch (IllegalAccessException iae) {
                ModularWarfare.LOGGER.warn("Main class '" + mainClass + "' in lib '" + libFile.getName() + "' is inaccessible!");
            } catch (AddonLoadingException ale) {
                throw ale;
            } catch (Exception e) {
                ModularWarfare.LOGGER.warn("Unknow exception has been caught while loading '\" + libFile.getName() + \"'. More info: " + ExceptionUtils.getStackTrace(e));
                throw new AddonLoadingException("Unknow exception has been caught while loading '" + libFile.getName() + "'. More info in console");
            }

        } catch (ClassNotFoundException e) {
            ModularWarfare.LOGGER.warn("No main class '" + main + "' defined in main.mwf found in lib '" + libFile.getName() + "'!");
        }
    }
}
