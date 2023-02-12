package com.modularwarfare.common.armor;

import com.modularwarfare.api.MWArmorType;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemSpecialArmor extends BaseItem {

    public ArmorType type;
    public MWArmorType armorType;
    public BaseType baseType;

    public ItemSpecialArmor(final ArmorType type, final MWArmorType armorType) {
        super(type);
        if (type.durability != null) {
            this.setMaxDamage(type.durability);
        }
        type.loadExtraValues();
        this.baseType = type;
        this.type = type;
        this.armorType = armorType;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, final EnumHand handIn) {
        final ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (playerIn.hasCapability(CapabilityExtra.CAPABILITY, null)) {
            final IExtraItemHandler backpack = playerIn.getCapability(CapabilityExtra.CAPABILITY, null);
            if (backpack.getStackInSlot(1).isEmpty()) {
                backpack.setStackInSlot(1, itemstack.copy());
                itemstack.setCount(0);
                return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
            }
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void setType(final BaseType type) {
        this.type = (ArmorType) type;
    }

    public void onUpdate(final ItemStack unused, final World world, final Entity holdingEntity, final int intI, final boolean flag) {
        if (holdingEntity instanceof EntityPlayer) {
            final EntityPlayer entityPlayer = (EntityPlayer) holdingEntity;
            if (unused != null && unused.getItem() instanceof ItemMWArmor && unused.getTagCompound() == null) {
                final NBTTagCompound nbtTagCompound = new NBTTagCompound();
                nbtTagCompound.setInteger("skinId", 0);
                unused.setTagCompound(nbtTagCompound);
            }
        }
    }

    public boolean getShareTag() {
        return true;
    }
}

