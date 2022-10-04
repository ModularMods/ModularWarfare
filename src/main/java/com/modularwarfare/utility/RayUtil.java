package com.modularwarfare.utility;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemBullet;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.hitbox.hits.BulletHit;
import com.modularwarfare.common.network.PacketGunTrail;
import com.modularwarfare.common.network.PacketGunTrailAskServer;
import mchhui.modularmovements.coremod.ModularMovementsHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Random;

public class RayUtil {

    public static Vec3d getGunAccuracy(float pitch, float yaw, final float accuracy, final Random rand) {
        final float randAccPitch = rand.nextFloat() * accuracy;
        final float randAccYaw = rand.nextFloat() * accuracy;
        pitch += (rand.nextBoolean() ? randAccPitch : (-randAccPitch));
        yaw += (rand.nextBoolean() ? randAccYaw : (-randAccYaw));
        final float f = MathHelper.cos(-yaw * 0.017453292f - 3.1415927f);
        final float f2 = MathHelper.sin(-yaw * 0.017453292f - 3.1415927f);
        final float f3 = -MathHelper.cos(-pitch * 0.017453292f);
        final float f4 = MathHelper.sin(-pitch * 0.017453292f);
        return new Vec3d((f2 * f3), f4, (f * f3));
    }

    public static float calculateAccuracyServer(final ItemGun item, final EntityLivingBase player) {
        final GunType gun = item.type;
        float acc = gun.bulletSpread;
        if (player.posX != player.lastTickPosX || player.posZ != player.lastTickPosZ) {
            acc += 0.15f;
        }
        if (!player.onGround) {
            acc += 0.3f;
        }
        if (player.isSprinting()) {
            acc += 0.4f;
        }
        if (player.isSneaking()) {
            acc *= gun.accuracySneakFactor;
        }

        /** Bullet Accuracy **/
        if (player.getHeldItemMainhand() != null) {
            if (player.getHeldItemMainhand().getItem() instanceof ItemGun) {
                ItemBullet bullet = ItemGun.getUsedBullet(player.getHeldItemMainhand(), ((ItemGun) player.getHeldItemMainhand().getItem()).type);
                if (bullet != null) {
                    if (bullet.type != null) {
                        acc *= bullet.type.bulletAccuracyFactor;
                    }
                }
            }
        }
        return acc;
    }

    public static float calculateAccuracyClient(final ItemGun item, final EntityPlayer player) {
        final GunType gun = item.type;
        float acc = gun.bulletSpread;
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        if (settings.keyBindForward.isKeyDown() || settings.keyBindLeft.isKeyDown() || settings.keyBindBack.isKeyDown() || settings.keyBindRight.isKeyDown()) {
            acc += 0.75f;
        }
        if (!player.onGround) {
            acc += 1.5f;
        }
        if (player.isSprinting()) {
            acc += 0.25f;
        }
        if (player.isSneaking()) {
            acc *= gun.accuracySneakFactor;
        }
        /** Bullet Accuracy **/
        if (player.getHeldItemMainhand() != null) {
            if (player.getHeldItemMainhand().getItem() instanceof ItemGun) {
                ItemBullet bullet = ItemGun.getUsedBullet(player.getHeldItemMainhand(), ((ItemGun) player.getHeldItemMainhand().getItem()).type);
                if (bullet != null) {
                    if (bullet.type != null) {
                        acc *= bullet.type.bulletAccuracyFactor;
                    }
                }
            }
        }
        return acc;
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public static RayTraceResult rayTrace(Entity entity, double blockReachDistance, float partialTicks)
    {
        Vec3d vec3d = entity.getPositionEyes(partialTicks);
        Vec3d vec3d1 = entity.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);

        if(Loader.isModLoaded("modularmovements")) {
            if (entity instanceof EntityPlayer) {
                vec3d = ModularMovementsHooks.onGetPositionEyes((EntityPlayer) entity, partialTicks);
            }
        }

        return entity.world.rayTraceBlocks(vec3d, vec3d2, false, true, false);
    }

    /**
     * Attacks the given entity with the given damage source and amount, but
     * preserving the entity's original velocity instead of applying knockback, as
     * would happen with
     * {@link EntityLivingBase#attackEntityFrom(DamageSource, float)} <i>(More
     * accurately, calls that method as normal and then resets the entity's velocity
     * to what it was before).</i> Handy for when you need to damage an entity
     * repeatedly in a short space of time.
     *
     * @param entity The entity to attack
     * @param source The source of the damage
     * @param amount The amount of damage to apply
     * @return True if the attack succeeded, false if not.
     */
    public static boolean attackEntityWithoutKnockback(Entity entity, DamageSource source, float amount) {
        double vx = entity.motionX;
        double vy = entity.motionY;
        double vz = entity.motionZ;
        boolean succeeded = entity.attackEntityFrom(source, amount);
        entity.motionX = vx;
        entity.motionY = vy;
        entity.motionZ = vz;
        return succeeded;
    }

    /**
     * Helper method which does a rayTrace for entities from an entity's eye level in the direction they are looking
     * with a specified range, using the tracePath method. Tidies up the code a bit. Border size defaults to 1.
     *
     * @param world
     * @param range
     * @return
     */
    @Nullable
    public static BulletHit standardEntityRayTrace(Side side, World world, float rotationPitch, float rotationYaw, EntityLivingBase player, double range, ItemGun item, boolean isPunched) {

        HashSet<Entity> hashset = new HashSet<Entity>(1);
        hashset.add(player);

        final float accuracy = calculateAccuracyServer(item, player);

        Vec3d dir = getGunAccuracy(rotationPitch, rotationYaw, accuracy, player.world.rand);

        double dx = dir.x * range;
        double dy = dir.y * range;
        double dz = dir.z * range;

        if(side.isServer()) {
            ModularWarfare.NETWORK.sendToDimension(new PacketGunTrail(player.posX, player.getEntityBoundingBox().minY + player.getEyeHeight() - 0.10000000149011612, player.posZ, player.motionX, player.motionZ, dir.x, dir.y, dir.z, range, 10, isPunched), player.world.provider.getDimension());
        } else {
            ModularWarfare.NETWORK.sendToServer(new PacketGunTrailAskServer(player.posX, player.getEntityBoundingBox().minY + player.getEyeHeight() - 0.10000000149011612, player.posZ, player.motionX, player.motionZ, dir.x, dir.y, dir.z, range, 10, isPunched));
        }

        int ping = 0;
        if (player instanceof EntityPlayerMP) {
            final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
            ping = entityPlayerMP.ping;
        }

        Vec3d offsetVec = player.getPositionEyes(1.0f);
        if(Loader.isModLoaded("modularmovements")) {
            if (player instanceof EntityPlayer) {
                offsetVec = ModularMovementsHooks.onGetPositionEyes((EntityPlayer) player, 1.0f);
            }
        }

        return ModularWarfare.INSTANCE.RAY_CASTING.computeDetection(world, (float) offsetVec.x, (float) offsetVec.y, (float) offsetVec.z, (float) (player.posX + dx + player.motionX), (float) (player.posY + dy + player.motionY), (float) (player.posZ + dz + player.motionZ), 0.001f, hashset, false, ping);
    }
}