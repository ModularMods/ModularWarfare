package com.modularwarfare.common.entity.decals;

import com.modularwarfare.ModularWarfare;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityBulletHole extends EntityDecal {
    public static int timeAliveSeconds = 10;

    public EntityBulletHole(World worldIn) {
        super(worldIn);
        this.maxTimeAlive = timeAliveSeconds*20;
    }

    public ResourceLocation getDecalTexture() {
        String location = ModularWarfare.MOD_ID + ":textures/entity/bullethole/bullethole" + this.getTextureNumber() + ".png";
        return new ResourceLocation(location);
    }

    public int getTextureCount() {
        return 1;
    }
}
