package com.modularwarfare.common.entity.item;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;

public class EntityItemNew extends Entity {
    private static final Logger LOGGER;
    private static final DataParameter<ItemStack> ITEM;

    static {
        LOGGER = LogManager.getLogger();
        ITEM = EntityDataManager.createKey((Class) EntityItemNew.class, DataSerializers.ITEM_STACK);
    }

    public float hoverStart;
    public int lifespan;
    private int age;
    private int delayBeforeCanPickup;
    private int health;
    private String thrower;
    private String owner;

    public EntityItemNew(final World worldIn, final double x, final double y, final double z) {
        super(worldIn);
        this.lifespan = 6000;
        this.health = 5;
        this.hoverStart = (float) (Math.random() * 3.141592653589793 * 2.0);
        this.setSize(0.65f, 0.65f);
        this.setPosition(x, y, z);
        this.rotationYaw = (float) (Math.random() * 360.0);
        this.motionX = (float) (Math.random() * 0.20000000298023224 - 0.10000000149011612) / 10.0f;
        this.motionY = 0.020000000298023225;
        this.motionZ = (float) (Math.random() * 0.20000000298023224 - 0.10000000149011612) / 10.0f;
    }

    public EntityItemNew(final World worldIn, final double x, final double y, final double z, final ItemStack stack) {
        this(worldIn, x, y, z);
        this.setItem(stack);
        this.lifespan = ((stack.getItem() == null) ? 6000 : stack.getItem().getEntityLifespan(stack, worldIn));
    }

    public EntityItemNew(final World worldIn) {
        super(worldIn);
        this.lifespan = 6000;
        this.health = 5;
        this.hoverStart = (float) (Math.random() * 3.141592653589793 * 2.0);
        this.setSize(0.25f, 0.25f);
        this.setItem(ItemStack.EMPTY);
    }

    public static void registerFixesItem(final DataFixer fixer) {
        fixer.registerWalker(FixTypes.ENTITY, (IDataWalker) new ItemStackData((Class) EntityItemNew.class, new String[]{"Item"}));
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    protected void entityInit() {
        this.getDataManager().register((DataParameter) EntityItemNew.ITEM, (Object) ItemStack.EMPTY);
    }

    public void onUpdate() {
        if (this.getItem().getItem().onEntityItemUpdate(new EntityItem(this.world, this.posX, this.posY, this.posZ, this.getItem()))) {
            return;
        }
        if (this.getItem().isEmpty()) {
            this.setDead();
        } else {
            super.onUpdate();
            if (this.delayBeforeCanPickup > 0 && this.delayBeforeCanPickup != 32767) {
                --this.delayBeforeCanPickup;
            }
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            final double d0 = this.motionX;
            final double d2 = this.motionY;
            final double d3 = this.motionZ;
            if (!this.hasNoGravity()) {
                this.motionY -= 0.03999999910593033;
            }
            if (this.world.isRemote) {
                this.noClip = false;
            } else {
                this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0, this.posZ);
            }
            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
            final boolean flag = (int) this.prevPosX != (int) this.posX || (int) this.prevPosY != (int) this.posY || (int) this.prevPosZ != (int) this.posZ;
            if (flag || this.ticksExisted % 25 == 0) {
                if (this.world.getBlockState(new BlockPos((Entity) this)).getMaterial() == Material.LAVA) {
                    this.motionY = 0.20000000298023224;
                    this.motionX = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2f;
                    this.motionZ = (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2f;
                    this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4f, 2.0f + this.rand.nextFloat() * 0.4f);
                }
                if (!this.world.isRemote) {
                    this.searchForOtherItemsNearby();
                }
            }
            float f = 0.98f;
            if (this.onGround) {
                f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.98f;
            }
            this.motionX *= f;
            this.motionY *= 0.9800000190734863;
            this.motionZ *= f;
            if (this.onGround) {
                this.motionY *= -0.5;
            }
            if (this.age != -32768) {
                ++this.age;
            }
            this.handleWaterMovement();
            if (!this.world.isRemote) {
                final double d4 = this.motionX - d0;
                final double d5 = this.motionY - d2;
                final double d6 = this.motionZ - d3;
                final double d7 = d4 * d4 + d5 * d5 + d6 * d6;
                if (d7 > 0.01) {
                    this.isAirBorne = true;
                }
            }
            final ItemStack item = this.getItem();
            if (this.world.isRemote || this.age >= this.lifespan) {
            }
            if (item.isEmpty()) {
                this.setDead();
            }
        }
    }

    private void searchForOtherItemsNearby() {
        for (final EntityItemNew entityitem : this.world.getEntitiesWithinAABB(EntityItemNew.class, this.getEntityBoundingBox().grow(0.5, 0.0, 0.5))) {
            this.combineItems(entityitem);
        }
    }

    private boolean combineItems(final EntityItemNew other) {
        if (other == this) {
            return false;
        }
        if (!other.isEntityAlive() || !this.isEntityAlive()) {
            return false;
        }
        final ItemStack itemstack = this.getItem();
        final ItemStack itemstack2 = other.getItem();
        if (this.delayBeforeCanPickup == 32767 || other.delayBeforeCanPickup == 32767) {
            return false;
        }
        if (this.age == -32768 || other.age == -32768) {
            return false;
        }
        if (itemstack2.getItem() != itemstack.getItem()) {
            return false;
        }
        if (itemstack2.hasTagCompound() ^ itemstack.hasTagCompound()) {
            return false;
        }
        if (itemstack2.hasTagCompound() && !itemstack2.getTagCompound().equals((Object) itemstack.getTagCompound())) {
            return false;
        }
        if (itemstack2.getItem() == null) {
            return false;
        }
        if (itemstack2.getItem().getHasSubtypes() && itemstack2.getMetadata() != itemstack.getMetadata()) {
            return false;
        }
        if (itemstack2.getCount() < itemstack.getCount()) {
            return other.combineItems(this);
        }
        if (itemstack2.getCount() + itemstack.getCount() > itemstack2.getMaxStackSize()) {
            return false;
        }
        if (!itemstack.areCapsCompatible(itemstack2)) {
            return false;
        }
        itemstack2.grow(itemstack.getCount());
        other.delayBeforeCanPickup = Math.max(other.delayBeforeCanPickup, this.delayBeforeCanPickup);
        other.age = Math.min(other.age, this.age);
        other.setItem(itemstack2);
        this.setDead();
        return true;
    }

    public void setAgeToCreativeDespawnTime() {
        this.age = 4800;
    }

    public boolean handleWaterMovement() {
        if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, (Entity) this)) {
            if (!this.inWater && !this.firstUpdate) {
                this.doWaterSplashEffect();
            }
            this.inWater = true;
        } else {
            this.inWater = false;
        }
        return this.inWater;
    }

    protected void dealFireDamage(final int amount) {
        this.attackEntityFrom(DamageSource.IN_FIRE, amount);
    }

    public boolean attackEntityFrom(final DamageSource source, final float amount) {
        if (this.world.isRemote || this.isDead) {
            return false;
        }
        if (this.isEntityInvulnerable(source)) {
            return false;
        }
        if (!this.getItem().isEmpty() && this.getItem().getItem() == Items.NETHER_STAR && source.isExplosion()) {
            return false;
        }
        this.markVelocityChanged();
        this.health -= (int) amount;
        if (this.health <= 0) {
            this.setDead();
        }
        return false;
    }

    public void writeEntityToNBT(final NBTTagCompound compound) {
        compound.setShort("Health", (short) this.health);
        compound.setShort("Age", (short) this.age);
        compound.setShort("PickupDelay", (short) this.delayBeforeCanPickup);
        compound.setInteger("Lifespan", this.lifespan);
        if (this.getThrower() != null) {
            compound.setString("Thrower", this.thrower);
        }
        if (this.getOwner() != null) {
            compound.setString("Owner", this.owner);
        }
        if (!this.getItem().isEmpty()) {
            compound.setTag("Item", (NBTBase) this.getItem().writeToNBT(new NBTTagCompound()));
        }
    }

    public void readEntityFromNBT(final NBTTagCompound compound) {
        this.health = compound.getShort("Health");
        this.age = compound.getShort("Age");
        if (compound.hasKey("PickupDelay")) {
            this.delayBeforeCanPickup = compound.getShort("PickupDelay");
        }
        if (compound.hasKey("Owner")) {
            this.owner = compound.getString("Owner");
        }
        if (compound.hasKey("Thrower")) {
            this.thrower = compound.getString("Thrower");
        }
        final NBTTagCompound nbttagcompound = compound.getCompoundTag("Item");
        this.setItem(new ItemStack(nbttagcompound));
        if (this.getItem().isEmpty()) {
            this.setDead();
        }
        if (compound.hasKey("Lifespan")) {
            this.lifespan = compound.getInteger("Lifespan");
        }
    }

    public void onCollideWithPlayer(final EntityPlayer entityIn) {
        if (!this.world.isRemote) {
            if (this.delayBeforeCanPickup > 0) {
                return;
            }
            final ItemStack itemstack = this.getItem();
            final Item item = itemstack.getItem();
            final int i = itemstack.getCount();
            final int hook = ForgeEventFactory.onItemPickup(new EntityItem(this.world, this.posX, this.posY, this.posZ, this.getItem()), entityIn);
            if (hook < 0) {
                return;
            }
            if (this.delayBeforeCanPickup <= 0 && (this.owner == null || this.lifespan - this.age <= 200 || this.owner.equals(entityIn.getName())) && (hook == 1 || i <= 0 || entityIn.inventory.addItemStackToInventory(itemstack))) {
                FMLCommonHandler.instance().firePlayerItemPickupEvent(entityIn, new EntityItem(this.world, this.posX, this.posY, this.posZ, this.getItem()), this.getItem());
                entityIn.onItemPickup((Entity) new EntityItem(this.world, this.posX, this.posY, this.posZ, this.getItem()), i);
                if (itemstack.isEmpty()) {
                    this.setDead();
                    itemstack.setCount(i);
                }
                entityIn.addStat(StatList.getObjectsPickedUpStats(item), i);
            }
        }
    }

    public String getName() {
        return this.hasCustomName() ? this.getCustomNameTag() : I18n.translateToLocal("item." + this.getItem().getUnlocalizedName());
    }

    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Nullable
    public Entity changeDimension(final int dimensionIn) {
        final Entity entity = super.changeDimension(dimensionIn);
        if (!this.world.isRemote && entity instanceof EntityItemNew) {
            ((EntityItemNew) entity).searchForOtherItemsNearby();
        }
        return entity;
    }

    public ItemStack getItem() {
        return (ItemStack) this.getDataManager().get((DataParameter) EntityItemNew.ITEM);
    }

    public void setItem(final ItemStack stack) {
        this.getDataManager().set((DataParameter) EntityItemNew.ITEM, (Object) stack);
        this.getDataManager().setDirty((DataParameter) EntityItemNew.ITEM);
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public String getThrower() {
        return this.thrower;
    }

    public void setThrower(final String thrower) {
        this.thrower = thrower;
    }

    @SideOnly(Side.CLIENT)
    public int getAge() {
        return this.age;
    }

    public void setDefaultPickupDelay() {
        this.delayBeforeCanPickup = 10;
    }

    public void setNoPickupDelay() {
        this.delayBeforeCanPickup = 0;
    }

    public void setInfinitePickupDelay() {
        this.delayBeforeCanPickup = 32767;
    }

    public void setPickupDelay(final int ticks) {
        this.delayBeforeCanPickup = ticks;
    }

    public boolean cannotPickup() {
        return this.delayBeforeCanPickup > 0;
    }

    public void setNoDespawn() {
        this.age = -6000;
    }

    public void makeFakeItem() {
        this.setInfinitePickupDelay();
        this.age = this.getItem().getItem().getEntityLifespan(this.getItem(), this.world) - 1;
    }
}
