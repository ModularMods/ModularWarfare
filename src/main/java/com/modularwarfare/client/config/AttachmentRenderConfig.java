package com.modularwarfare.client.config;


import com.modularwarfare.common.guns.WeaponDotColorType;
import org.lwjgl.util.vector.Vector3f;

public class AttachmentRenderConfig {

    public String modelFileName = "";

    public AttachmentRenderConfig.Extra extra = new AttachmentRenderConfig.Extra();

    public AttachmentRenderConfig.Sight sight = new AttachmentRenderConfig.Sight();
    public AttachmentRenderConfig.Grip grip = new AttachmentRenderConfig.Grip();

    public static class Extra {

        public float modelScale = 1.0f;
    }

    public static class Sight {
        public float fovZoom = 3.5f;
        public float mouseSensitivityFactor = 1.0f;
        public float rectileScale = 1.0f;
    }

    public static class Grip {
        public Vector3f leftArmOffset = new Vector3f(0F, 0F, 0F);
    }

}