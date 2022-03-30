package com.modularwarfare.client.fpp.basic.configs;

import org.lwjgl.util.vector.Vector3f;

public class GrenadeRenderConfig {

    public String modelFileName = "";

    public Arms arms = new Arms();

    public Extra extra = new Extra();


    public static class Arms {

        public Arms.RightArm rightArm = new Arms.RightArm();

        public enum EnumArm {
            Left, Right;
        }

        public enum EnumAction {
            Bolt, Pump, Charge
        }

        public class RightArm {
            public Vector3f armScale = new Vector3f(0.8F, 0.8F, 0.8F);

            public Vector3f armPos = new Vector3f(0.26F, -0.65F, 0.0F);
            public Vector3f armRot = new Vector3f(0.0F, 0.0F, -90.0F);

            public Vector3f armChargePos = new Vector3f(0.47F, -0.39F, 0.14F);
            public Vector3f armChargeRot = new Vector3f(0.0F, 0.0F, -90.0F);
        }

    }

    public static class Extra {
        /**
         * This models overall scale for rendering
         */
        public float modelScale = 1.0F;
        public Vector3f thirdPersonOffset = new Vector3f(0, 0F, 0F);
        public Vector3f translateAll = new Vector3f(0, 0F, 0F);
    }
}