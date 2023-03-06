package com.modularwarfare.raycast;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.hitbox.PlayerHitbox;
import com.modularwarfare.common.hitbox.PlayerSnapshot;
import com.modularwarfare.common.hitbox.hits.BulletHit;
import com.modularwarfare.common.hitbox.hits.OBBHit;
import com.modularwarfare.common.hitbox.hits.PlayerHit;
import com.modularwarfare.common.hitbox.maths.EnumHitboxType;
import com.modularwarfare.common.hitbox.playerdata.PlayerData;
import com.modularwarfare.common.network.PacketPlaySound;
import com.modularwarfare.common.vector.Matrix4f;
import com.modularwarfare.common.vector.Vector3f;
import com.modularwarfare.raycast.obb.OBBModelBox;
import com.modularwarfare.raycast.obb.OBBPlayerManager;
import com.modularwarfare.raycast.obb.OBBPlayerManager.Line;
import com.modularwarfare.raycast.obb.OBBPlayerManager.PlayerOBBModelObject;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
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

    //在未来应当考虑穿透
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
        if (blockHit != null) {
            maxDistance = (float) blockHit.hitVec.distanceTo(startVec);
            endVec = blockHit.hitVec;
        }

        Vector3f rayVec=new Vector3f(endVec.x-startVec.x, endVec.y-startVec.y, endVec.z-startVec.z);
        float len=rayVec.length();
        Vector3f normlVec=rayVec.normalise(null);
        OBBModelBox ray=new OBBModelBox();
        float pitch=(float) Math.asin(normlVec.y);
        normlVec.y=0;
        normlVec=normlVec.normalise(null);
        float yaw=(float)Math.asin(normlVec.x);
        if(normlVec.z<0) {
            yaw=(float) (Math.PI-yaw);
        }
        Matrix4f matrix=new Matrix4f();
        matrix.rotate(yaw, new Vector3f(0, 1, 0));
        matrix.rotate(pitch, new Vector3f(-1, 0, 0));
        ray.center=new Vector3f((startVec.x+endVec.x)/2, (startVec.y+endVec.y)/2, (startVec.z+endVec.z)/2);
        ray.axis.x=new Vector3f(0, 0, 0);
        ray.axis.y=new Vector3f(0, 0, 0);
        ray.axis.z=Matrix4f.transform(matrix, new Vector3f(0, 0, len/2), null);
        ray.axisNormal.x=Matrix4f.transform(matrix, new Vector3f(1, 0, 0), null);
        ray.axisNormal.y=Matrix4f.transform(matrix, new Vector3f(0, 1, 0), null);
        ray.axisNormal.z=Matrix4f.transform(matrix, new Vector3f(0, 0, 1), null);
        

        OBBPlayerManager.lines.add(new Line(ray));
        OBBPlayerManager.lines.add(new Line(new Vector3f(startVec), new Vector3f(endVec)));
        //Iterate over all entities
        for (int i = 0; i < world.loadedEntityList.size(); i++) {
            Entity obj = world.loadedEntityList.get(i);

            if (((excluded != null && !excluded.contains(obj)) || excluded == null)) {
                if (obj instanceof EntityPlayer) {
                    //to delete in the future
                    
                    /*
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
                    */
                    //Minecraft.getMinecraft().player.sendMessage(new TextComponentString("test:"+startVec+" "+endVec));
                    PlayerOBBModelObject obbModelObject = OBBPlayerManager.getPlayerOBBObject(obj.getName());
                    OBBModelBox finalBox=null;
                    boolean isHeadShot=false;
                    boolean isBodyShot=false;
                    List<OBBModelBox> boxes = obbModelObject.calculateIntercept(ray);
                    if (boxes.size() > 0) {
                        double distanceSq = Double.MAX_VALUE;
                        for (OBBModelBox obb : boxes) {
                            if (obb.name.equals("obb_head")) {
                                finalBox=obb;
                                isHeadShot=true;
                                break;
                            } else if (!isHeadShot && obb.name.equals("obb_body")) {
                                finalBox=obb;
                                isBodyShot=true;
                            } else if (!isHeadShot && !isBodyShot) {
                                double d = startVec
                                        .squareDistanceTo(new Vec3d(obb.center.x, obb.center.y, obb.center.z));
                                if (d < distanceSq) {
                                    distanceSq = d;
                                    finalBox=obb;
                                }
                            }
                        }
                        PlayerData data = ModularWarfare.PLAYERHANDLER.getPlayerData((EntityPlayer) obj);
                        RayTraceResult intercept = new RayTraceResult(obj, new Vec3d(finalBox.center.x, finalBox.center.y, finalBox.center.z));
                        return new OBBHit((EntityPlayer)obj,finalBox.copy(), intercept);
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
