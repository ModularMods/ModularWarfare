package com.modularwarfare.client.fpp.basic.configs;

public class BulletRenderConfig {

    public String modelFileName = "";

    public Extra extra = new Extra();

    public static class Extra {

        /**
         * Number of bullets to render when loading
         */
        public Integer bulletsToRender;

        /**
         * This models overall scale for rendering
         */
        public float modelScale = 1.0F;
    }
}