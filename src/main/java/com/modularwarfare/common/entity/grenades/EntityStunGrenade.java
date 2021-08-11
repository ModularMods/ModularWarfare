package com.modularwarfare.common.entity.grenades;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.grenades.GrenadeType;
import com.modularwarfare.common.init.ModSounds;
import com.modularwarfare.common.network.PacketFlashClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityStunGrenade extends EntityGrenade {

    private static final DataParameter GRENADE_NAME = EntityDataManager.createKey(EntityStunGrenade.class, DataSerializers.STRING);

    public EntityStunGrenade(World worldIn) {
        super(worldIn);
    }

    public EntityStunGrenade(World world, EntityLivingBase thrower, boolean isRightClick, GrenadeType grenadeType) {
        super(world, thrower, isRightClick, grenadeType);
        this.preventEntitySpawning = true;
        this.isImmuneToFire = true;
        this.setSize(0.35f, 0.35f);
        this.setEntityInvulnerable(false);
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
        return true;
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (!this.hasNoGravity()) {
            this.motionY -= 0.04D;
        }

        this.motionX *= 0.98D;
        this.motionY *= 0.98D;
        this.motionZ *= 0.98D;

        if (this.onGround) {
            this.motionX *= 0.8D;
            this.motionZ *= 0.8D;
            if (!playedSound) {
                world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.GRENADE_HIT, SoundCategory.BLOCKS, 0.50f, 1.0f);
                playedSound = true;
            }
        }

        if (Math.abs(motionX) < 0.1 && Math.abs(motionZ) < 0.1) {
            motionX = 0;
            motionZ = 0;
        }

        --this.fuse;

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

        if (this.fuse <= 0) {
            if (!exploded) {
                explode();
            }
        } else {
            this.handleWaterMovement();
            if (!this.isInWater()) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.2D, this.posZ, 0.0D, 0.0D, 0.0D);
            } else {
                this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, this.posY + 0.2D, this.posZ, 0.0D, 0.1D, 0.0D);
            }
        }
    }

    @Override
    public void explode(){
        if (!exploded) {
            world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.GRENADE_STUN, SoundCategory.BLOCKS, 2.0f, 1.0f);
            exploded = true;
            setDead();
            if (!this.world.isRemote) {
                for (EntityPlayer player : world.playerEntities) {
                    if (this.isInFieldOfVision(this, player)) {
                        if (player.getDistance(this) < 10) {
                            ModularWarfare.NETWORK.sendTo(new PacketFlashClient(255), (EntityPlayerMP) player);
                        } else if (player.getDistance(this) < 15) {
                            ModularWarfare.NETWORK.sendTo(new PacketFlashClient(180), (EntityPlayerMP) player);
                        } else if (player.getDistance(this) < 20) {
                            ModularWarfare.NETWORK.sendTo(new PacketFlashClient(100), (EntityPlayerMP) player);
                        } else if (player.getDistance(this) < 35) {
                            ModularWarfare.NETWORK.sendTo(new PacketFlashClient(60), (EntityPlayerMP) player);
                        }
                    }

                }
            }
        }
    }


    boolean isInFieldOfVision(Entity e1, EntityLivingBase e2) {
        float rotationYawPrime = e2.rotationYaw;
        float rotationPitchPrime = e2.rotationPitch;

        this.faceEntity(e2, e1, 360F, 360F);

        float f = e2.rotationYaw;
        float f2 = e2.rotationPitch;

        e2.rotationYaw = rotationYawPrime;
        e2.rotationPitch = rotationPitchPrime;

        rotationYawPrime = f;
        rotationPitchPrime = f2;

        float X = 60F;
        float Y = 60F;

        float yawFOVMin = e2.rotationYaw - X;
        float yawFOVMax = e2.rotationYaw + X;
        float pitchFOVMin = e2.rotationPitch - Y;
        float pitchFOVMax = e2.rotationPitch + Y;

        boolean flag1 = (rotationYawPrime < yawFOVMax && rotationYawPrime > yawFOVMin);

        boolean flag2 = (pitchFOVMin <= -180F && (rotationPitchPrime >= pitchFOVMin + 360F || rotationPitchPrime <= pitchFOVMax)) || (pitchFOVMax > 180F && (rotationPitchPrime <= pitchFOVMax - 360F || rotationPitchPrime >= pitchFOVMin)) || (pitchFOVMax < 180F && pitchFOVMin >= -180F && rotationPitchPrime <= pitchFOVMax && rotationPitchPrime >= pitchFOVMin);
        return flag1 && flag2 && e2.canEntityBeSeen(e1);
    }

    public void faceEntity(EntityLivingBase par1, Entity par1Entity, float par2, float par3) {
        double d0 = par1Entity.posX - par1.posX;
        double d1 = par1Entity.posZ - par1.posZ;
        double d2;

        if (par1Entity instanceof EntityLivingBase) {
            EntityLivingBase entitylivingbase = (EntityLivingBase) par1Entity;
            d2 = entitylivingbase.posY + (double) entitylivingbase.getEyeHeight() - (par1.posY + (double) par1.getEyeHeight());
        } else {
            d2 = (par1Entity.getEntityBoundingBox().minY + par1Entity.getEntityBoundingBox().maxY) / 2.0D - (par1.posY + (double) par1.getEyeHeight());
        }

        double d3 = MathHelper.sqrt(d0 * d0 + d1 * d1);
        float f2 = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) - 90.0F;
        float f3 = (float) (-(Math.atan2(d2, d3) * 180.0D / Math.PI));
        par1.rotationPitch = updateRotation(par1.rotationPitch, f3, par3);
        par1.rotationYaw = updateRotation(par1.rotationYaw, f2, par2);
    }

    private float updateRotation(float par1, float par2, float par3) {
        float f3 = MathHelper.wrapDegrees(par2 - par1);
        if (f3 > par3) {
            f3 = par3;
        }
        if (f3 < -par3) {
            f3 = -par3;
        }
        return par1 + f3;
    }


    public String getGrenadeName() {
        return (String) this.dataManager.get(GRENADE_NAME);
    }

    public void setGrenadeName(String grenadeName) {
        this.dataManager.set(GRENADE_NAME, grenadeName);
    }


    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(GRENADE_NAME, "");
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setDouble("posX", this.posX);
        compound.setDouble("posY", this.posY);
        compound.setDouble("posZ", this.posZ);
        compound.setDouble("motionX", this.motionX);
        compound.setDouble("motionY", this.motionY);
        compound.setDouble("motionZ", this.motionZ);
        compound.setFloat("fuse", this.fuse);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        posX = compound.getDouble("posX");
        posY = compound.getDouble("posY");
        posZ = compound.getDouble("posZ");
        motionX = compound.getDouble("motionX");
        motionY = compound.getDouble("motionY");
        motionZ = compound.getDouble("motionZ");
        fuse = compound.getFloat("fuse");
    }
}
