package com.modularwarfare.common.container;

import com.modularwarfare.api.MWArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * Backpack slot in the players inventory
 */
public class SlotVest extends SlotItemHandler {
    public SlotVest(final IItemHandler inv, final int index, final int xPosition, final int yPosition) {
        super(inv, index, xPosition, yPosition);
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public boolean isItemValid(@Nonnull final ItemStack stack) {
        if (stack.getItem() instanceof ItemSpecialArmor) {
            ItemSpecialArmor armor = (ItemSpecialArmor) stack.getItem();
            return (armor.armorType == MWArmorType.Vest);
        }
        return false;
    }
}
