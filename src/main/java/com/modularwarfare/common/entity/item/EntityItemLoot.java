package com.modularwarfare.common.entity.item;

import com.modularwarfare.ModConfig;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.IFluidBlock;

public class EntityItemLoot extends EntityItemNew {

    EntityItem orig;
    long gameSeed;
    private int customAge;

    public EntityItemLoot(final World world) {
        super(world);
        this.orig = null;
        this.hoverStart = this.rand.nextFloat();
        this.setSize(0.65f, 0.65f);
        this.setCustomAge(0);
    }

    public EntityItemLoot(final EntityItem orig) {
        this(orig.world, orig.posX, orig.posY, orig.posZ, orig.getItem());
        final NBTTagCompound oldT = new NBTTagCompound();
        orig.writeEntityToNBT(oldT);
        this.readEntityFromNBT(oldT);
        final String thrower = orig.getThrower();
        final Entity entity = ((thrower == null) ? null : orig.world.getPlayerEntityByName(thrower));
        final double tossSpd = (entity != null && entity.isSprinting()) ? 2.0 : 1.0;
        if (entity != null) {
            this.motionX = orig.motionX * tossSpd;
            this.motionY = orig.motionY * tossSpd;
            this.motionZ = orig.motionZ * tossSpd;
        }
        this.setPickupDelay(0);
        this.setCustomAge(0);
    }

    public EntityItemLoot(final World world, final double x, final double y, final double z, final ItemStack stack) {
        super(world, x, y, z, stack);
        this.orig = null;
        this.hoverStart = this.rand.nextFloat();
        this.setSize(0.65f, 0.65f);
        this.setCustomAge(0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            this.setCustomAge(this.getCustomAge() + 1);
        }
        final float x = MathHelper.floor(this.posX);
        final float y = MathHelper.floor(this.posY);
        final float z = MathHelper.floor(this.posZ);
        final IBlockState bsHere = this.world.getBlockState(new BlockPos((double) x, (double) y, (double) z));
        final IBlockState bsAbove = this.world.getBlockState(new BlockPos((double) x, (double) (y + 1.0f), (double) z));
        final boolean liqHere = bsHere.getBlock() instanceof BlockLiquid || bsHere.getBlock() instanceof IFluidBlock;
        final boolean liqAbove = bsAbove.getBlock() instanceof BlockLiquid || bsAbove.getBlock() instanceof IFluidBlock;
        if (liqHere) {
            this.onGround = false;
            this.inWater = true;
            if (this.motionY < 0.05 && (liqAbove || this.posY % 1.0 < 0.8999999761581421)) {
                this.motionY += Math.min(0.075, 0.075 - this.motionY);
            }
            this.motionX = MathHelper.clamp(this.motionX, -0.05, 0.05);
            this.motionZ = MathHelper.clamp(this.motionZ, -0.05, 0.05);
        }
        if (this.getCustomAge() >= ModConfig.INSTANCE.drops.drops_despawn_time * 20) {
            this.setDead();
        }
    }

    @Override
    public void writeEntityToNBT(final NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setLong("GameSeed", this.gameSeed);
    }

    @Override
    public void readEntityFromNBT(final NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.gameSeed = compound.getLong("GameSeed");
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void onCollideWithPlayer(final EntityPlayer player) {
    }

    public int getCustomAge() {
        return this.customAge;
    }

    public void setCustomAge(final int customAge) {
        this.customAge = customAge;
    }

    public void pickup(final EntityPlayer player) {
        if (player.inventory.getFirstEmptyStack() != -1) {
            if (this.isDead || player.world.isRemote) {
                return;
            }
            final int i = this.getItem().getCount();
            this.onItemPickup(this, i, (EntityLivingBase) player);

            player.addItemStackToInventory(this.getItem());
            this.setDead();
        }
    }

    public void onItemPickup(final Entity entityIn, final int quantity, final EntityLivingBase player) {
        if (!entityIn.isDead && !this.world.isRemote) {
            final EntityTracker entitytracker = ((WorldServer) this.world).getEntityTracker();
            if (entityIn instanceof EntityItemNew || entityIn instanceof EntityArrow || entityIn instanceof EntityXPOrb) {
                entitytracker.sendToTracking(entityIn, new SPacketCollectItem(entityIn.getEntityId(), player.getEntityId(), quantity));
            }
        }
    }
}
