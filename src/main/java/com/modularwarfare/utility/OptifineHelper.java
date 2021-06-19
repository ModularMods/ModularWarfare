package com.modularwarfare.utility;

import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
public class OptifineHelper {
    private static Boolean loaded = null;
    private static Field shaderName;

    public static boolean isLoaded() {
        if (loaded == null) {
            try {
                Class.forName("optifine.Installer");
                loaded = true;
            } catch (ClassNotFoundException e) {
                loaded = false;
            }
        }
        return loaded;
    }

    public static boolean isShadersEnabled() {
        if (isLoaded()) {
            try {
                Class<?> clazz = Class.forName("net.optifine.shaders.Shaders");
                if (clazz != null && shaderName == null) {
                    shaderName = clazz.getDeclaredField("shaderPackLoaded");
                }
                if (shaderName != null) {
                    boolean name = (Boolean) shaderName.get(null);
                    return name;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}