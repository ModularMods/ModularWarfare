package com.modularwarfare.common.entity.decals;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class EntityDecal extends Entity {

    private static final DataParameter PERMANENT = EntityDataManager.createKey(EntityDecal.class, DataSerializers.BOOLEAN);
    private static final DataParameter AGE = EntityDataManager.createKey(EntityDecal.class, DataSerializers.VARINT);
    private static final DataParameter SEED = EntityDataManager.createKey(EntityDecal.class, DataSerializers.VARINT);
    private static final DataParameter SIDE_ID = EntityDataManager.createKey(EntityDecal.class, DataSerializers.VARINT);
    private static final DataParameter TEXTURE_NUMBER = EntityDataManager.createKey(EntityDecal.class, DataSerializers.VARINT);
    protected int maxTimeAlive;
    private EnumDecalSide side;

    public EntityDecal(World var1) {
        super(var1);
        this.isImmuneToFire = true;
        this.setSide(EnumDecalSide.ALL);
        this.setSize(0.0f, 0.0F);
        this.maxTimeAlive = 900;
    }

    public abstract ResourceLocation getDecalTexture();

    public abstract int getTextureCount();

    protected void entityInit() {
        this.dataManager.register(PERMANENT, false);
        this.dataManager.register(AGE, 0);
        this.dataManager.register(SEED, 0);
        this.dataManager.register(SIDE_ID, 0);
        this.dataManager.register(TEXTURE_NUMBER, this.world.rand.nextInt(this.getTextureCount()));
    }

    public void onKillCommand() {
    }

    public EnumDecalSide getSide() {
        return this.side;
    }

    public void setSide(EnumDecalSide side) {
        this.side = side;
        this.setSideID(side.getId());
    }

    public float getAgeRatio() {
        return Math.max(0.0F, (float) (this.getAge() + this.maxTimeAlive) / (float) this.maxTimeAlive);
    }

    public boolean isPermanent() {
        return (Boolean) this.dataManager.get(PERMANENT);
    }

    public void setPermanent(boolean bool) {
        this.dataManager.set(PERMANENT, bool);
    }

    public int getAge() {
        return (Integer) this.dataManager.get(AGE);
    }

    public void setAge(int num) {
        this.dataManager.set(AGE, num);
    }

    public int getSeed() {
        return (Integer) this.dataManager.get(SEED);
    }

    public void setSeed(int num) {
        this.dataManager.set(SEED, num);
    }

    public int getSideID() {
        return (Integer) this.dataManager.get(SIDE_ID);
    }

    public void setSideID(int num) {
        this.dataManager.set(SIDE_ID, num);
    }

    public int getTextureNumber() {
        return (Integer) this.dataManager.get(TEXTURE_NUMBER);
    }

    public void setTextureNumber(int textureNumber) {
        this.dataManager.set(TEXTURE_NUMBER, textureNumber);
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    public void setDead() {
        super.setDead();
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double var1) {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    public void onUpdate() {
        this.noClip = true;
        if (!this.isPermanent()) {
            if (this.getAge() > -this.maxTimeAlive) {
                this.setAge(this.getAge() - 1);
            } else {
                this.setDead();
            }
        }

        this.noClip = false;
        this.setNoGravity(true);

    }

    public boolean isEntityInsideOpaqueBlock() {
        return false;
    }

    public void writeEntityToNBT(NBTTagCompound var1) {
        var1.setBoolean("Permanent", this.isPermanent());
        var1.setInteger("Age", this.getAge());
        var1.setInteger("Seed", this.getSeed());
        var1.setInteger("SideID", this.getSideID());
        var1.setInteger("TextureNumber", this.getTextureNumber());
    }

    public void readEntityFromNBT(NBTTagCompound var1) {
        this.setPermanent(var1.getBoolean("Permanent"));
        this.setAge(var1.getInteger("Age"));
        this.setSeed(var1.getInteger("Seed"));
        this.setSideID(var1.getInteger("SideID"));
        if (var1.hasKey("TextureNumber")) {
            this.setTextureNumber(var1.getInteger("TextureNumber"));
        }

    }

    public static enum EnumDecalSide {
        ALL(0),
        WALLS(1),
        FLOOR(2),
        NORTH(3),
        EAST(4),
        SOUTH(5),
        WEST(6),
        CEILING(7);

        private final int id;

        private EnumDecalSide(int id) {
            this.id = id;
        }

        public static EnumDecalSide getEnumFromId(int id) {
            return values()[id];
        }

        public int getId() {
            return this.id;
        }
    }
}
