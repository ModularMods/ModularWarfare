package com.modularwarfare.common.hitbox.hits;

import net.minecraft.util.math.RayTraceResult;

public class BulletHit {

    public RayTraceResult rayTraceResult;

    public BulletHit(RayTraceResult result) {
        this.rayTraceResult = result;
    }

}
