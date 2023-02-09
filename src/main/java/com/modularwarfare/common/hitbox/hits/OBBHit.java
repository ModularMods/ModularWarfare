package com.modularwarfare.common.hitbox.hits;

import com.modularwarfare.raycast.obb.OBBModelBox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;

public class OBBHit extends BulletHit {
    public EntityLivingBase entity;
    public OBBModelBox box;

    public OBBHit(EntityLivingBase entity,OBBModelBox box, RayTraceResult result) {
        super(result);
        this.box = box;
        this.entity=entity;
    }

}
