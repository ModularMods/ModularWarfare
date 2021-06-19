package com.modularwarfare.loader.api;

import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.loader.ObjModel;
import com.modularwarfare.loader.ObjModelBuilder;
import net.minecraft.util.ResourceLocation;

public class ObjModelLoader {

    /**
     * Loads Obj Model. For now Builder supports only triangular and square faces, so before loading staticModel, be sure,
     * that you triangulated stuff like circles (for example in Blender).
     * <p>
     * If you want to rotate some parts of your staticModel, you'll need to set rotation points.
     * This can be done by creating .rp file which name is equal to obj file name (you can see an example in "resources" package)
     * The instruction that explains how to create .rp file you can see in wiki page on github (look at @see)
     *
     * @param resourceLocation resourceLocation of obj file.
     * @return built Obj Model which you can use in Entity Models or TESRs.
     */
    public static ObjModel load(String resourceLocation) {
        return new ObjModelBuilder(resourceLocation).loadModel();
    }

    public static ObjModel load(ResourceLocation resourceLocation) {
        return new ObjModelBuilder(resourceLocation).loadModelFromRL();
    }

    public static ObjModel load(BaseType type, String resourceLocation) {
        return new ObjModelBuilder(resourceLocation).loadModelFromZIP(type);
    }
}