package com.modularwarfare.client.fpp.basic.configs;

public class ArmorRenderConfig {

    public String modelFileName = "";

    public Extra extra = new Extra();

    public static class Extra {
        /**
         * This models overall scale for rendering
         */
        public float modelScale = 1.0F;
        public boolean slimSupported = false;
        public boolean isSuit = false;
        public boolean hidePlayerModel = false;
        public boolean hideAllPlayerWearModel = false;
    }
}