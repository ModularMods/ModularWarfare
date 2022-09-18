package net.optifine.shaders;

import java.nio.IntBuffer;

/**
 * @author Hueihuea
 * 高清修复:我是小脑袋瓜子想烂了都想不到有一天我会被hack
 * */
public class MWFOptifineShadesHelper {
    public static FlipTextures getFlipTextures() {
        return Shaders.dfbColorTexturesFlip;
    }
    
    public static int getDFB() {
        return Shaders.dfb;
    }
    
    public static IntBuffer getDFBDrawBuffers() {
        return Shaders.dfbDrawBuffers;
    }
    
    public static int getUsedColorBuffers() {
        return Shaders.usedColorBuffers;
    }
    
    public static IntBuffer getDFBDepthTextures() {
        return Shaders.dfbDepthTextures;
    }
}
