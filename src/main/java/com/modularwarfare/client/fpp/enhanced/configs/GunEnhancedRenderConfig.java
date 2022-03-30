package com.modularwarfare.client.fpp.enhanced.configs;

import com.modularwarfare.client.fpp.enhanced.AnimationType;
import org.lwjgl.util.vector.Vector3f;
import java.util.HashMap;

public class GunEnhancedRenderConfig {

    public String modelFileName = "";

    public HashMap<AnimationType, Animation> animations = new HashMap<>();

    public GunEnhancedRenderConfig.Extra extra = new GunEnhancedRenderConfig.Extra();

    public static class Animation {
        public float startTime;
        public float endTime;
        public float speed;

        public float getStartTime() {
            return startTime * 0.041666667597f;
        }
        public float getEndTime() {
            return endTime * 0.041666667597f;
        }
    }

    public static class Extra {
        public Vector3f translateAll = new Vector3f(1F, -1.02F, -0.07F);
        //Allows you to modify the ADS speed per gun, adjust in small increments (+/- 0.01)
    }
}
