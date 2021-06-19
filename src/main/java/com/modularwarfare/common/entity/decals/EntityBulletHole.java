package com.modularwarfare.common.entity.decals;

import com.modularwarfare.ModularWarfare;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityBulletHole extends EntityDecal {

    public EntityBulletHole(World worldIn) {
        super(worldIn);
        this.maxTimeAlive = 200;
    }

    public ResourceLocation getDecalTexture() {
        String location = ModularWarfare.MOD_ID + ":textures/entity/bullethole/bullethole" + this.getTextureNumber() + ".png";
        return new ResourceLocation(location);
    }

    public int getTextureCount() {
        return 1;
    }
}
