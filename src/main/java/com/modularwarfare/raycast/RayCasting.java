package com.modularwarfare.raycast;

import com.modularwarfare.common.hitbox.hits.BulletHit;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashSet;

public abstract class RayCasting {

    /**
     * Method for ray tracing entities (the useless default method doesn't work, despite EnumHitType having an ENTITY
     * field...) You can also use this for seeking.
     *
     * @param world
     * @param x          startX
     * @param y          startY
     * @param z          startZ
     * @param tx         endX
     * @param ty         endY
     * @param tz         endZ
     * @param borderSize extra area to examine around line for entities
     * @param excluded   any excluded entities (the player, etc)
     * @return a RayTraceResult of either the block hit (no entity hit), the entity hit (hit an entity), or null for
     * nothing hit
     */
    public abstract BulletHit computeDetection(World world, float x, float y, float z, float tx, float ty, float tz, float borderSize, HashSet<Entity> excluded, boolean collideablesOnly, int ping);

    public abstract RayTraceResult rayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock);
}