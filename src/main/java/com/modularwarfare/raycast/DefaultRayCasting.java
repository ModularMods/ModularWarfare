package com.modularwarfare.raycast;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.hitbox.PlayerHitbox;
import com.modularwarfare.common.hitbox.PlayerSnapshot;
import com.modularwarfare.common.hitbox.hits.BulletHit;
import com.modularwarfare.common.hitbox.hits.PlayerHit;
import com.modularwarfare.common.hitbox.playerdata.PlayerData;
import com.modularwarfare.common.network.PacketPlaySound;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

/**
 * This is the default ModularWarfare RayCaster
 * It can be overwritten by your own RayCasting
 */
public class DefaultRayCasting extends RayCasting {

    @Override
    public BulletHit computeDetection(World world, float x, float y, float z, float tx, float ty, float tz, float borderSize, HashSet<Entity> excluded, boolean collideablesOnly, int ping) {
        Vec3d startVec = new Vec3d(x, y, z);
        // Vec3d lookVec = new Vec3d(tx-x, ty-y, tz-z);
        Vec3d endVec = new Vec3d(tx, ty, tz);

        float minX = x < tx ? x : tx;
        float minY = y < ty ? y : ty;
        float minZ = z < tz ? z : tz;
        float maxX = x > tx ? x : tx;
        float maxY = y > ty ? y : ty;
        float maxZ = z > tz ? z : tz;
        AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(borderSize, borderSize, borderSize);
        List<Entity> allEntities = world.getEntitiesWithinAABBExcludingEntity(null, bb);
        RayTraceResult blockHit = rayTraceBlocks(world, startVec, endVec, true, true, false);

        startVec = new Vec3d(x, y, z);
        endVec = new Vec3d(tx, ty, tz);
        float maxDistance = (float) endVec.distanceTo(startVec);
        Vec3d realVecEnd;
        if (blockHit != null) {
            maxDistance = (float) blockHit.hitVec.distanceTo(startVec);
            realVecEnd = blockHit.hitVec;
        } else {
            realVecEnd = endVec;
        }


        //Iterate over all entities
        for (int i = 0; i < world.loadedEntityList.size(); i++) {
            Entity obj = world.loadedEntityList.get(i);

            if (((excluded != null && !excluded.contains(obj)) || excluded == null)) {
                if (obj instanceof EntityPlayer) {

                    PlayerData data = ModularWarfare.PLAYERHANDLER.getPlayerData((EntityPlayer) obj);

                    int snapshotToTry = ping / 50;
                    if (snapshotToTry >= data.snapshots.length)
                        snapshotToTry = data.snapshots.length - 1;

                    PlayerSnapshot snapshot = data.snapshots[snapshotToTry];

                    if (snapshot == null)
                        snapshot = data.snapshots[0];

                    if (snapshot != null && snapshot.hitboxes != null){
                        for (PlayerHitbox hitbox : snapshot.hitboxes) {
                            RayTraceResult intercept = hitbox.getAxisAlignedBB(snapshot.pos).calculateIntercept(startVec, realVecEnd);
                            if (intercept != null) {
                                intercept.entityHit = hitbox.player;

                                if (ModConfig.INSTANCE.debug_hits_message) {
                                    long currentTime = System.nanoTime();
                                    ModularWarfare.LOGGER.info("Shooter's ping: " + ping / 20 + "ms | " + ping + "ticks");
                                    ModularWarfare.LOGGER.info("Took the snapshot " + snapshotToTry + " Part: " + hitbox.type.toString());
                                    ModularWarfare.LOGGER.info("Delta (currentTime - snapshotTime) = " + (currentTime - snapshot.time) * 1e-6 + "ms");
                                }

                                return new PlayerHit(hitbox, intercept);
                            }
                        }
                }
                }
            }
        }

        Entity closestHitEntity = null;
        Vec3d hit = null;
        float closestHit = maxDistance;
        float currentHit = 0.f;
        AxisAlignedBB entityBb;// = ent.getBoundingBox();
        RayTraceResult intercept;
        for (Entity ent : allEntities) {
            if ((ent.canBeCollidedWith() || !collideablesOnly) && ((excluded != null && !excluded.contains(ent)) || excluded == null)) {
                if (ent instanceof EntityLivingBase && !(ent instanceof EntityPlayer)) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) ent;
                    if (!ent.isDead && entityLivingBase.getHealth() > 0.0F) {
                        float entBorder = ent.getCollisionBorderSize();
                        entityBb = ent.getEntityBoundingBox();
                        if (entityBb != null) {
                            entityBb = entityBb.grow(entBorder, entBorder, entBorder);
                            intercept = entityBb.calculateIntercept(startVec, endVec);
                            if (intercept != null) {
                                currentHit = (float) intercept.hitVec.distanceTo(startVec);
                                hit = intercept.hitVec;
                                if (currentHit < closestHit || currentHit == 0) {
                                    closestHit = currentHit;
                                    closestHitEntity = ent;
                                }
                            }
                        }
                    }
                } else if (ent instanceof EntityGrenade) {
                    float entBorder = ent.getCollisionBorderSize();
                    entityBb = ent.getEntityBoundingBox();
                    if (entityBb != null) {
                        entityBb = entityBb.grow(entBorder, entBorder, entBorder);
                        intercept = entityBb.calculateIntercept(startVec, endVec);
                        if (intercept != null) {
                            currentHit = (float) intercept.hitVec.distanceTo(startVec);
                            hit = intercept.hitVec;
                            if (currentHit < closestHit || currentHit == 0) {
                                closestHit = currentHit;
                                closestHitEntity = ent;
                            }
                        }
                    }
                }
            }
        }
        if (closestHitEntity != null && hit != null) {
            blockHit = new RayTraceResult(closestHitEntity, hit);
        }

        return new BulletHit(blockHit);
    }

    @Nullable
    public RayTraceResult rayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int i = MathHelper.floor(vec32.x);
                int j = MathHelper.floor(vec32.y);
                int k = MathHelper.floor(vec32.z);
                int l = MathHelper.floor(vec31.x);
                int i1 = MathHelper.floor(vec31.y);
                int j1 = MathHelper.floor(vec31.z);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
                    RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

                    if (raytraceresult != null) {
                        return raytraceresult;
                    }
                }

                RayTraceResult raytraceresult2 = null;
                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return returnLastUncollidableBlock ? raytraceresult2 : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double) l + 1.0D;
                    } else if (i < l) {
                        d0 = (double) l + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double) i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double) i1 + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double) j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double) j1 + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

                    if (flag2) {
                        d3 = (d0 - vec31.x) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - vec31.y) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - vec31.z) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    EnumFacing enumfacing;

                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState iblockstate1 = world.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();

                    if (ModConfig.INSTANCE.shots.shot_break_glass) {
                        if (block1 instanceof BlockGlass || block1 instanceof BlockStainedGlassPane || block1 instanceof BlockStainedGlass) {
                            world.setBlockToAir(blockpos);
                            ModularWarfare.NETWORK.sendToAllAround(new PacketPlaySound(blockpos, "impact.glass", 1f, 1f), new NetworkRegistry.TargetPoint(0, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 25));
                            continue;
                        }
                    }

                    if (block1 instanceof BlockPane) {
                        ModularWarfare.NETWORK.sendToAllAround(new PacketPlaySound(blockpos, "impact.iron", 1f, 1f), new NetworkRegistry.TargetPoint(0, blockpos.getX(), blockpos.getY(), blockpos.getZ(), 25));
                        continue;
                    }

                    if (block1 instanceof BlockDoor || block1 instanceof BlockLeaves) {
                        continue;
                    }

                    if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.BARRIER || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, blockpos, vec31, vec32);
                            if (raytraceresult1 != null) {
                                return raytraceresult1;
                            }
                        } else {
                            raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? raytraceresult2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
