package com.modularwarfare.client.animengine;

import com.google.gson.stream.JsonReader;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.utility.GSONUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;

public class AnimationLoader {

    public static Animation ak_reload;

    public static Animation loadAnimation(String file){
        try {
            File animFile = new File(ModularWarfare.MOD_DIR, file);
            JsonReader jsonReader = new JsonReader(new FileReader(animFile));

            Animation animation = GSONUtils.fromJson(ModularWarfare.gson, jsonReader, Animation.class);

            for(String bone : animation.bones.keySet()){
                ModularWarfare.LOGGER.info("Loading bone: "+bone);
                Collections.sort(animation.bones.get(bone).keyframes);
            }

            return animation;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
