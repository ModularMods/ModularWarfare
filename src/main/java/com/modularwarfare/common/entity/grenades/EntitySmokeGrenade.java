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
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;


public class EntitySmokeGrenade extends EntityGrenade {

    private static final DataParameter GRENADE_NAME = EntityDataManager.createKey(EntitySmokeGrenade.class, DataSerializers.STRING);

    public float smokeTime = 12 * 20;

    public EntitySmokeGrenade(World worldIn) {
        super(worldIn);
    }

    public EntitySmokeGrenade(World world, EntityLivingBase thrower, boolean isRightClick, GrenadeType grenadeType) {
        super(world, thrower, isRightClick, grenadeType);
        this.smokeTime = grenadeType.smokeTime * 20;
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
            explode();
        } else {
            this.handleWaterMovement();
            if (!this.isInWater()) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.2D, this.posZ, 0.0D, 0.0D, 0.0D);
            } else {
                this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX, this.posY + 0.2D, this.posZ, 0.0D, 0.1D, 0.0D);
            }
        }

        if(this.exploded){
            --this.smokeTime;
            if(this.smokeTime <= 0){
                setDead();
            }
        }
    }

    @Override
    public void explode(){
        if (!this.exploded) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.GRENADE_SMOKE, SoundCategory.BLOCKS, 2.0f, 1.0f);
            this.exploded = true;
            this.fuse = 0;
            this.smokeTime = 220;
        }
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
        compound.setFloat("smokeTime", this.smokeTime);
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
        smokeTime = compound.getFloat("smokeTime");
    }
}
