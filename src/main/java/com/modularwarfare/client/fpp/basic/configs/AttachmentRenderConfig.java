package com.modularwarfare.client.fpp.basic.configs;


import org.lwjgl.util.vector.Vector3f;

import net.minecraft.util.ResourceLocation;

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
        
        public float factorCrossScale = 0.2f;
        public String maskTexture="default_mask";
        public float maskSize=0.75f;
        public float uniformMaskRange=0.1f;
        public float uniformDrawRange=245f/1600;
        public float uniformStrength=3f;
        public float uniformScaleRangeY=1f;
        public float uniformScaleStrengthY=1f;
        public float uniformVerticality=0f;
    }

    public static class Grip {
        public Vector3f leftArmOffset = new Vector3f(0F, 0F, 0F);
    }

    
    public void init() {
        //sight.maskTextureLocation=new ResourceLocation(sight.maskTexture);
    }
}