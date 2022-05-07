package com.modularwarfare.client.fpp.enhanced.configs;

import com.modularwarfare.client.fpp.basic.configs.GunRenderConfig;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import org.lwjgl.util.vector.Vector3f;
import java.util.HashMap;

public class GunEnhancedRenderConfig {

    public String modelFileName = "";

    public HashMap<AnimationType, Animation> animations = new HashMap<>();

    public GunEnhancedRenderConfig.Sprint sprint = new GunEnhancedRenderConfig.Sprint();
    public GunEnhancedRenderConfig.Aim aim = new GunEnhancedRenderConfig.Aim();
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

    public static class Sprint {
        public Vector3f sprintRotate = new Vector3f(-20.0F, 30.0F, -0.0F);
        public Vector3f sprintTranslate = new Vector3f(0.5F, -0.10F, -0.65F);
    }

    public static class Aim {

        //Advanced configuration - Allows you to change how the gun is held without effecting the sight alignment
        public Vector3f rotateHipPosition = new Vector3f(0F, 0F, 0F);
        //Advanced configuration - Allows you to change how the gun is held without effecting the sight alignment
        public Vector3f translateHipPosition = new Vector3f(0F, 0F, 0F);
        //Advanced configuration - Allows you to change how the gun is held while aiming
        public Vector3f rotateAimPosition = new Vector3f(0F, 0F, 0F);
        //Advanced configuration - Allows you to change how the gun is held while aiming
        public Vector3f translateAimPosition = new Vector3f(0F, 0F, 0F);
    }

    public static class Extra {
        public Vector3f translateAll = new Vector3f(1F, -1.02F, -0.07F);
        //Allows you to modify the ADS speed per gun, adjust in small increments (+/- 0.01)
    }
}
