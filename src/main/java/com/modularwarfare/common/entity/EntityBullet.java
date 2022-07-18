package com.modularwarfare.common.entity;

import com.modularwarfare.common.entity.decals.EntityShell;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityBullet extends EntityArrow implements IProjectile {

    public EntityPlayer player;
    public int ticksInAir = 0;
    public int ticks = 0;
    private int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private int inTile = 0;
    private int inData = 0;
    private boolean inGround = false;
    private float damage;
    private int liveTime = 600;

    private float velocity;

    private static final DataParameter BULLET_NAME = EntityDataManager.createKey(EntityShell.class, DataSerializers.STRING);

    public EntityBullet(World world) {
        super(world);
        setSize(0.2F, 0.2F);
    }

    public EntityBullet(World par1World, EntityPlayer par2EntityPlayer, float damage, float accuracy, float velocity, String bulletName) {
        super(par1World);
        this.setBulletType(bulletName);
        this.player = par2EntityPlayer;
        this.shootingEntity = par2EntityPlayer;
        setSize(0.2F, 0.2F);
        setLocationAndAngles(par2EntityPlayer.posX, par2EntityPlayer.posY + par2EntityPlayer.getEyeHeight(), par2EntityPlayer.posZ, par2EntityPlayer.rotationYaw, par2EntityPlayer.rotationPitch);
        this.posX -= MathHelper.cos(this.rotationYaw / 180.0F * 3.141593F) * 0.16F;
        this.posY -= 0.0D;
        this.posZ -= MathHelper.sin(this.rotationYaw / 180.0F * 3.141593F) * 0.16F;
        setPosition(par2EntityPlayer.posX, par2EntityPlayer.posY + par2EntityPlayer.getEyeHeight(), par2EntityPlayer.posZ);
        this.motionX = (-MathHelper.sin(this.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.141593F));
        this.motionZ = (MathHelper.cos(this.rotationYaw / 180.0F * 3.141593F) * MathHelper.cos(this.rotationPitch / 180.0F * 3.141593F));
        this.motionY = (-MathHelper.sin(this.rotationPitch / 180.0F * 3.141593F));
        this.damage = damage;
        shoot(this.motionX, this.motionY, this.motionZ, velocity, accuracy);
    }

    public void onUpdate() {
        super.onEntityUpdate();
        this.ticks += 1;
        this.liveTime -= 1;
        if ((this.posY > 300.0D) || (this.liveTime <= 0)) {
            setDead();
        }
        if ((this.prevRotationPitch == 0.0F) && (this.prevRotationYaw == 0.0F)) {
            float f = (float) Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.prevRotationYaw = (this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.141592653589793D));
            this.prevRotationPitch = (this.rotationPitch = (float) (Math.atan2(this.motionY, f) * 180.0D / 3.141592653589793D));
        }
        if (this.inGround) {
            setDead();
        } else {
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;

            float f2 = (float) Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            this.rotationYaw = ((float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / 3.141592653589793D));

            for (this.rotationPitch = ((float) (Math.atan2(this.motionY, f2) * 180.0D / 3.141592653589793D)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
                ;

            while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = (this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F);
            this.rotationYaw = (this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F);

            float f4 = 1F;
            if (isInWater()) {
                for (int j1 = 0; j1 < 4; j1++) {
                    float f3 = 0.25F;
                    float f5 = 0.75F;
                    this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f3, this.posY - 0.25 + this.motionY * f5, this.posZ - this.motionZ * f3, this.motionX, this.motionY, this.motionZ);
                }
                f4 = 0.8F;
            }

            this.motionX *= f4;
            this.motionY *= f4;
            this.motionZ *= f4;
            setPosition(this.posX, this.posY, this.posZ);
            if (this.inGround) {
                setDead();
            }
        }

    }

    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        par1NBTTagCompound.setShort("xTile", (short) this.xTile);
        par1NBTTagCompound.setShort("yTile", (short) this.yTile);
        par1NBTTagCompound.setShort("zTile", (short) this.zTile);
        par1NBTTagCompound.setDouble("motX", this.motionX);
        par1NBTTagCompound.setDouble("motY", this.motionY);
        par1NBTTagCompound.setDouble("motZ", this.motionZ);
        par1NBTTagCompound.setByte("inTile", (byte) this.inTile);
        par1NBTTagCompound.setByte("inData", (byte) this.inData);
        par1NBTTagCompound.setByte("inGround", (byte) (this.inGround ? 1 : 0));
        par1NBTTagCompound.setFloat("damage", this.damage);
        par1NBTTagCompound.setFloat("velocity", this.velocity);
    }

    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        this.xTile = par1NBTTagCompound.getShort("xTile");
        this.yTile = par1NBTTagCompound.getShort("yTile");
        this.zTile = par1NBTTagCompound.getShort("zTile");
        this.motionX = par1NBTTagCompound.getDouble("motX");
        this.motionY = par1NBTTagCompound.getDouble("motY");
        this.motionZ = par1NBTTagCompound.getDouble("motZ");
        this.inTile = (par1NBTTagCompound.getByte("inTile") & 0xFF);
        this.inData = (par1NBTTagCompound.getByte("inData") & 0xFF);
        this.inGround = (par1NBTTagCompound.getByte("inGround") == 1);
        if (par1NBTTagCompound.hasKey("damage")) {
            this.damage = par1NBTTagCompound.getFloat("damage");
        }
        this.velocity = par1NBTTagCompound.getFloat("velocity");
    }

    @Override
    protected ItemStack getArrowStack() {
        return null;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(BULLET_NAME, "");
    }

    public String getBulletName() {
        return (String) this.dataManager.get(BULLET_NAME);
    }

    public void setBulletType(String bulletType) {
        this.dataManager.set(BULLET_NAME, bulletType);
    }
}