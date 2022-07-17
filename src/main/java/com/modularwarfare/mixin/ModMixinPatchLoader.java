package com.modularwarfare.mixin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import net.minecraft.launchwrapper.LaunchClassLoader;

public class ModMixinPatchLoader {
    public static void load() {
        if (ModMixinPatchLoader.class.getClassLoader() instanceof LaunchClassLoader) {
            LaunchClassLoader loader = (LaunchClassLoader) ModMixinPatchLoader.class.getClassLoader();
            File mcgltf = new File("./mods/mcgltf");
            if (!mcgltf.exists()) {
                mcgltf.mkdirs();
            }
            if (mcgltf.isDirectory()) {
                for (File file : mcgltf.listFiles()) {
                    if(file.getAbsolutePath().endsWith(".jar")) {
                        try {
                            loader.addURL(file.toURI().toURL());
                        } catch (MalformedURLException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
