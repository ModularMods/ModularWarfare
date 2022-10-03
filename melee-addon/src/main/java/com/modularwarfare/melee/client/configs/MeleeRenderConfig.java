package com.modularwarfare.melee.client.configs;

import com.modularwarfare.client.fpp.enhanced.configs.EnhancedRenderConfig;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class MeleeRenderConfig extends EnhancedRenderConfig {

    public HashMap<AnimationMeleeType, ArrayList<Animation>> animations = new HashMap<>();

    public MeleeRenderConfig.Global global = new MeleeRenderConfig.Global();


    public static class Global {
        public Vector3f globalTranslate = new Vector3f(0, 0, 0);
        public Vector3f globalScale = new Vector3f(1, 1, 1);
        public Vector3f globalRotate = new Vector3f(0, 0, 0);
    }

    public static class Animation {
        public double startTime = 0;
        public double endTime = 1;
        public double speed = 1;

        public double getStartTime(double FPS) {
            return startTime * 1 / FPS;
        }

        public double getEndTime(double FPS) {
            return endTime * 1 / FPS;
        }

        public double getSpeed(double FPS) {
            double a = (getEndTime(FPS) - getStartTime(FPS));
            if (a <= 0) {
                a = 1;
            }
            return speed / a;
        }
    }

}
