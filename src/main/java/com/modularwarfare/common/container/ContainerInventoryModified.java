package com.modularwarfare.common.container;

import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.utility.ModUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/***
 * Modified copy of Vanilla's Player inventory
 */
public class ContainerInventoryModified extends Container {

    private static final EntityEquipmentSlot[] EQUIPMENT_SLOTS = new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
    public final InventoryCrafting craftMatrix = new InventoryCrafting(this, 2, 2);
    public final InventoryCraftResult craftResult = new InventoryCraftResult();
    private final EntityPlayer thePlayer;
    public IExtraItemHandler extra;
    public boolean isLocalWorld;

    public ContainerInventoryModified(final InventoryPlayer playerInv, final boolean isLocalWorld, final EntityPlayer player) {
        this.isLocalWorld = isLocalWorld;
        this.thePlayer = player;
        this.onCraftMatrixChanged(this.craftMatrix);

        this.addSlots(playerInv, player);
    }

    public void addSlots(final InventoryPlayer playerInv, final EntityPlayer player) {
        this.inventorySlots.clear();
        this.inventoryItemStacks.clear();

        this.extra = player.getCapability(CapabilityExtra.CAPABILITY, null);

        this.addSlotToContainer(new SlotCrafting(playerInv.player, this.craftMatrix, this.craftResult, 0, 154, 28));

        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                this.addSlotToContainer(new Slot(this.craftMatrix, j + (i * 2), 116 + (j * 18), 18 + (i * 18)));
            }
        }

        for (int k = 0; k < 4; k++) {
            final EntityEquipmentSlot slot = EQUIPMENT_SLOTS[k];
            this.addSlotToContainer(new Slot(playerInv, 36 + (3 - k), 8, 8 + (k * 18)) {
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }

                @Override
                public boolean isItemValid(final ItemStack stack) {
                    return stack.getItem().isValidArmor(stack, slot, player);
                }

                @Override
                public boolean canTakeStack(final EntityPlayer playerIn) {
                    final ItemStack itemstack = this.getStack();
                    return !itemstack.isEmpty() && !playerIn.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false
                            : super.canTakeStack(playerIn);
                }

                @Override
                public String getSlotTexture() {
                    return ItemArmor.EMPTY_SLOT_NAMES[slot.getIndex()];
                }
            });
        }

        // Second light gray slots bar
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInv, j + ((i + 1) * 9), 8 + (j * 18), 102 - 12 + (i * 18)));
            }
        }

        // First light gray slots bar
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + (i * 18), 166 - 12));
        }

        // This is for the OFFHAND MouseHover
        this.addSlotToContainer(new Slot(playerInv, 40, 76, 62) {
            @Override
            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });

        this.addSlotToContainer(new SlotBackpack(this.extra, 0, ModUtil.BACKPACK_SLOT_OFFSET_X, ModUtil.BACKPACK_SLOT_OFFSET_Y + 1) {
            @Override
            public void onSlotChanged() {
                ContainerInventoryModified.this.updateBackpack();
                ContainerInventoryModified.this.addSlots(playerInv, player);
                super.onSlotChanged();
            }
        });


        this.addSlotToContainer(new SlotVest(this.extra, 1, ModUtil.BACKPACK_SLOT_OFFSET_X, ModUtil.BACKPACK_SLOT_OFFSET_Y + 1 + 18) {
            @Override
            public void onSlotChanged() {
                ContainerInventoryModified.this.addSlots(playerInv, player);
                super.onSlotChanged();
            }
        });

        this.updateBackpack();
    }


    private void updateBackpack() {
        if (this.extra.getStackInSlot(0).hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            final IItemHandler backpackInvent = this.extra.getStackInSlot(0).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

            int xP = 0;
            int yP = 0;
            final int x = 1 + ModUtil.BACKPACK_CONTENT_OFFSET_X;
            final int y = 1 + ModUtil.BACKPACK_CONTENT_OFFSET_Y;

            for (int i = 0; i < backpackInvent.getSlots(); i++) {
                this.addSlotToContainer(
                        new SlotItemHandler(backpackInvent, i, x + (xP * ModUtil.INVENTORY_SLOT_SIZE_PIXELS),
                                -1 + y + (yP * ModUtil.INVENTORY_SLOT_SIZE_PIXELS)) {
                            // Don't allow nesting backpacks if they are bigger (or have the same size) as the current extraslots
                            @Override
                            public boolean isItemValid(@Nonnull final ItemStack stack) {
                                if (stack.getItem() instanceof ItemBackpack) {
                                    ItemBackpack itemBackpack = ((ItemBackpack) extra.getStackInSlot(0).getItem());
                                    if (itemBackpack.type.allowSmallerBackpackStorage) {
                                        final int otherBackpackSize = ((ItemBackpack) stack.getItem()).type.size;
                                        final int thisBackpackSize = backpackInvent.getSlots();
                                        if (otherBackpackSize <= thisBackpackSize) {
                                            return true;
                                        }
                                        return false;
                                    } else {
                                        return false;
                                    }
                                }
                                if (stack.getItem() instanceof ItemGun) {
                                    ItemBackpack itemBackpack = ((ItemBackpack) extra.getStackInSlot(0).getItem());
                                    if (itemBackpack.type.maxWeaponStorage != null) {
                                        if (this.getNumberOfGuns(backpackInvent) >= itemBackpack.type.maxWeaponStorage) {
                                            return false;
                                        }
                                    }
                                }
                                return super.isItemValid(stack);
                            }

                            private int getNumberOfGuns(IItemHandler backpackInvent) {
                                int numGuns = 0;
                                for (int i = 0; i < backpackInvent.getSlots(); i++) {
                                    if (backpackInvent.getStackInSlot(i) != null) {
                                        if (backpackInvent.getStackInSlot(i).getItem() instanceof ItemGun) {
                                            numGuns++;
                                        }
                                    }
                                }
                                return numGuns;
                            }
                        });
                xP++;

                if ((xP % 4) == 0) {
                    xP = 0;
                    yP++;
                }
            }
        }
    }


    @Override
    public void onCraftMatrixChanged(final IInventory par1IInventory) {
        this.slotChangedCraftingGrid(this.thePlayer.getEntityWorld(), this.thePlayer, this.craftMatrix, this.craftResult);
    }

    @Override
    public void onContainerClosed(final EntityPlayer player) {
        super.onContainerClosed(player);
        this.craftResult.clear();

        if (!player.world.isRemote) {
            this.clearContainer(player, player.world, this.craftMatrix);
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer par1EntityPlayer) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);

        if ((slot != null) && slot.getHasStack()) {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            final EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);


            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            final ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);

            if (index == 0) {
                playerIn.dropItem(itemstack2, false);
            }
        }

        return itemstack;
    }

    @Override
    public boolean canMergeSlot(final ItemStack stack, final Slot slot) {
        return (slot.inventory != this.craftResult) && super.canMergeSlot(stack, slot);
    }
}
