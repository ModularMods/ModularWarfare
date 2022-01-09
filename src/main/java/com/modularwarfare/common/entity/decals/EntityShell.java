package com.modularwarfare.common.entity.decals;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.ItemBullet;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.network.PacketPlaySound;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityShell extends Entity implements IProjectile {

    private static final DataParameter AGE = EntityDataManager.createKey(EntityShell.class, DataSerializers.VARINT);
    private static final DataParameter BULLET_NAME = EntityDataManager.createKey(EntityShell.class, DataSerializers.STRING);
    public boolean playedSound;
    protected int ticksInGround;
    protected int maxTimeAlive;


    public EntityShell(World worldIn) {
        super(worldIn);
        this.setSize(0.1F, 0.1F);
        maxTimeAlive = 20 * ModConfig.INSTANCE.casings_drops.despawn_time;
        playedSound = false;
    }

    public EntityShell(World worldIn, EntityPlayer throwerIn, ItemGun gun, ItemBullet bullet) {
        super(worldIn);

        this.setBulletType(bullet.type.internalName);

        Vec3d rotateYaw = new Vec3d(0, 0, 0);

        if (ServerTickHandler.playerAimInstant.get(throwerIn.getDisplayNameString()) != null || ServerTickHandler.playerReloadCooldown.get(throwerIn.getDisplayNameString()) != null) {
            if (ServerTickHandler.playerAimInstant.get(throwerIn.getDisplayNameString())) {
                rotateYaw = gun.type.shellEjectOffsetAiming.rotatePitch(-(float) Math.toRadians(throwerIn.rotationPitch)).rotateYaw(-(float) Math.toRadians(throwerIn.rotationYaw));
            } else {
                rotateYaw = gun.type.shellEjectOffsetNormal.rotatePitch(-(float) Math.toRadians(throwerIn.rotationPitch)).rotateYaw(-(float) Math.toRadians(throwerIn.rotationYaw));
            }
        }

        Vec3d source = new Vec3d(throwerIn.posX + throwerIn.motionX + rotateYaw.x, throwerIn.posY + (double) throwerIn.getEyeHeight() - 0.10000000149011612D + throwerIn.motionY + rotateYaw.y, throwerIn.posZ + throwerIn.motionZ + rotateYaw.z);
        this.setPosition(source.x, source.y, source.z);
        this.setSize(0.25F, 0.25F);
        maxTimeAlive = 20 * ModConfig.INSTANCE.casings_drops.despawn_time;
    }


    @Override
    protected void entityInit() {
        this.dataManager.register(AGE, 0);
        this.dataManager.register(BULLET_NAME, "");
    }

    public int getAge() {
        return (Integer) this.dataManager.get(AGE);
    }

    public void setAge(int num) {
        this.dataManager.set(AGE, num);
    }

    public String getBulletName() {
        return (String) this.dataManager.get(BULLET_NAME);
    }

    public void setBulletType(String bulletType) {
        this.dataManager.set(BULLET_NAME, bulletType);
    }


    public void onUpdate() {
        super.onUpdate();
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (!this.hasNoGravity()) {
            this.motionY -= 0.03999999910593033D;
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
            this.motionY *= -0.5D;
            if (!playedSound) {
                if (ModularWarfare.bulletTypes.containsKey(getBulletName())) {
                    ItemBullet itemBullet = ModularWarfare.bulletTypes.get(getBulletName());
                    ModularWarfare.NETWORK.sendToAllAround(new PacketPlaySound(this.getPosition(), itemBullet.type.shellSound, 0.8f, 1f), new NetworkRegistry.TargetPoint(dimension, this.posX, this.posY, this.posZ, 3f));
                    playedSound = true;
                }
            }
        }

        {
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            float f = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);

            for (this.rotationPitch = (float) (MathHelper.atan2(this.motionY, (double) f) * (180D / Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
                ;
            }

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * this.rand.nextFloat();
            this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * this.rand.nextFloat();
        }
        if (this.getAge() > -this.maxTimeAlive) {
            this.setAge(this.getAge() - 1);
        } else {
            this.setDead();
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {

    }

    public void setHeadingFromThrower(Entity entityThrower, float rotationPitchIn, float rotationYawIn, float pitchOffset, float velocity, float inaccuracy) {

        float sideOffset = 0f;
        float forwardOffset = 0.1f;

        this.posX -= (MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) * sideOffset) + (
                MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) *
                        MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * forwardOffset);


        this.posY += (-MathHelper.sin(this.rotationPitch / 180.0F * 3.1415927F) * forwardOffset);


        this.posZ -= (MathHelper.sin(this.rotationYaw / 180.0F * 3.1415927F) * sideOffset) - (
                MathHelper.cos(this.rotationYaw / 180.0F * 3.1415927F) *
                        MathHelper.cos(this.rotationPitch / 180.0F * 3.1415927F) * forwardOffset);
        setPosition(this.posX, this.posY, this.posZ);


        float f = -MathHelper.sin(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        float f1 = -MathHelper.sin((rotationPitchIn + pitchOffset) * 0.017453292F);
        float f2 = MathHelper.cos(rotationYawIn * 0.017453292F) * MathHelper.cos(rotationPitchIn * 0.017453292F);
        this.setThrowableHeading((double) f, (double) f1, (double) f2, velocity * this.rand.nextFloat(), inaccuracy);
        this.motionX += entityThrower.motionX;
        this.motionZ += entityThrower.motionZ;

        if (!entityThrower.onGround) {
            this.motionY += entityThrower.motionY;
        }
    }

    public void setThrowableHeading(double x, double y, double z, float velocity, float inaccuracy) {
        float f = MathHelper.sqrt(x * x + y * y + z * z);
        x = x / (double) f;
        y = y / (double) f;
        z = z / (double) f;
        x = x + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        y = y + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        z = z + this.rand.nextGaussian() * 0.007499999832361937D * (double) inaccuracy;
        x = x * (double) velocity;
        y = y * (double) velocity;
        z = z * (double) velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        float f1 = MathHelper.sqrt(x * x + z * z);
        this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
        this.rotationPitch = (float) (MathHelper.atan2(y, (double) f1) * (180D / Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.ticksInGround = 0;
    }

    /**
     * Updates the velocity of the entity to a new value.
     */
    @SideOnly(Side.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationYaw = (float) (MathHelper.atan2(x, z) * (180D / Math.PI));
            this.rotationPitch = (float) (MathHelper.atan2(y, (double) f) * (180D / Math.PI));
            this.prevRotationYaw = this.rotationYaw;
            this.prevRotationPitch = this.rotationPitch;
        }
    }
}