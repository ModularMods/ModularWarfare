package com.modularwarfare.api;

import com.modularwarfare.ModularWarfare;
import org.apache.logging.log4j.Level;

import java.util.HashMap;

public class WeaponAnimations {

    // Prefabs
    public static String RIFLE = "rifle";
    public static String RIFLE2 = "rifle2";
    public static String RIFLE3 = "rifle3";
    public static String RIFLE4 = "rifle4";
    public static String PISTOL = "pistol";
    public static String SHOTGUN = "shotgun";
    public static String SNIPER = "sniper";
    public static String SNIPER_TOP = "sniper_top";
    public static String SIDE_CLIP = "sideclip";
    public static String TOP_RIFLE = "toprifle";
    private static HashMap<String, WeaponAnimation> animationMap = new HashMap<String, WeaponAnimation>();

    public static String registerAnimation(String internalName, WeaponAnimation animation) {
        animationMap.put(internalName, animation);
        return internalName;
    }

    public static WeaponAnimation getAnimation(String internalName) {
        WeaponAnimation weaponAnimation = animationMap.get(internalName);
        if (weaponAnimation == null)
            ModularWarfare.LOGGER.log(Level.ERROR, String.format("Animation named '%s' does not exist in animation registry.", internalName));
        return animationMap.get(internalName);
    }


}
