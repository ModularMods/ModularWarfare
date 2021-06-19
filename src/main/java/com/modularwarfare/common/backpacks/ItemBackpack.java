package com.modularwarfare.common.backpacks;

import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.init.ModSounds;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.function.Function;

public class ItemBackpack extends BaseItem {

    public static final Function<BackpackType, ItemBackpack> factory = type -> {
        return new ItemBackpack(type);
    };
    public BackpackType type;

    public ItemBackpack(BackpackType type) {
        super(type);
        this.maxStackSize = 1;
        this.type = type;
        this.render3d = false;
    }

    @Override
    public void setType(BaseType type) {
        this.type = (BackpackType) type;
    }


    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        return new BackpackType.Provider(this.type);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, final EnumHand handIn) {
        final ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (playerIn.hasCapability(CapabilityExtra.CAPABILITY, null)) {
            final IExtraItemHandler backpack = playerIn.getCapability(CapabilityExtra.CAPABILITY, null);
            if (backpack.getStackInSlot(0).isEmpty()) {
                backpack.setStackInSlot(0, itemstack.copy());
                itemstack.setCount(0);
                worldIn.playSound(null, playerIn.getPosition(), ModSounds.EQUIP_EXTRA, SoundCategory.PLAYERS, 2.0f, 1.0f);
                return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
            }
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Nullable
    @Override
    public NBTTagCompound getNBTShareTag(final ItemStack stack) {
        NBTTagCompound tags = super.getNBTShareTag(stack);

        // Add extraslots information to NBT tag that is sent to the client
        if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            if (tags == null) {
                tags = new NBTTagCompound();
            }

            final IItemHandler items = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            tags.setTag("_items", CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.writeNBT(items, null));
        }

        return tags;
    }

    @Override
    public void readNBTShareTag(final ItemStack stack, @Nullable final NBTTagCompound nbt) {
        super.readNBTShareTag(stack, nbt);

        if (stack.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null) && (nbt != null)) {
            final IItemHandler items = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            final NBTBase itemTags = nbt.getTag("_items");
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.readNBT(items, null, itemTags);
        }
    }

    @Override
    public void onUpdate(final ItemStack stack, final World worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

}
