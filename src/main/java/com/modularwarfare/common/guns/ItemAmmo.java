package com.modularwarfare.common.guns;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.network.PacketGunReload;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ItemAmmo extends BaseItem {

    public static final Function<AmmoType, ItemAmmo> factory = type -> {
        return new ItemAmmo(type);
    };
    public AmmoType type;

    public ItemAmmo(AmmoType type) {
        super(type);
        this.type = type;
        if (type.maxStackSize == null)
            type.maxStackSize = 4;
        this.setMaxStackSize(type.maxStackSize);
        this.render3d = false;
        this.setMaxDamage(type.ammoCapacity);
    }

    public static boolean hasAmmo(ItemStack ammoStack) {
        if (ammoStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = ammoStack.getTagCompound();
            if (nbtTagCompound.hasKey("magcount")) {
                ItemAmmo itemAmmo = ((ItemAmmo) ammoStack.getItem());
                for (int i = 0; i < itemAmmo.type.magazineCount; i++) {
                    if (nbtTagCompound.getInteger("ammocount" + i) > 0)
                        return true;
                }
            } else {
                return nbtTagCompound.getInteger("ammocount") > 0;
            }
        }
        return false;
    }

    public static ItemBullet getUsedBullet(ItemStack gunStack) {
        if (ItemGun.hasAmmoLoaded(gunStack)) {
            ItemStack ammoStack = new ItemStack(gunStack.getTagCompound().getCompoundTag("ammo"));
            if (ammoStack.hasTagCompound() && ammoStack.getTagCompound().hasKey("bullet")) {
                ItemStack usedBullet = new ItemStack(ammoStack.getTagCompound().getCompoundTag("bullet"));
                ItemBullet usedBulletItem = (ItemBullet) usedBullet.getItem();
                return usedBulletItem;
            } else {
                GunType gunType = ((ItemGun) gunStack.getItem()).type;
                if (gunType.acceptedAmmo != null) {
                    if (((ItemAmmo) ammoStack.getItem()).type.subAmmo != null) {
                        return ModularWarfare.bulletTypes.get(((ItemAmmo) ammoStack.getItem()).type.subAmmo[0]);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void setType(BaseType type) {
        this.type = (AmmoType) type;
    }

    @Override
    public void onUpdate(ItemStack heldStack, World world, Entity holdingEntity, int intI, boolean flag) {
        if (heldStack.getTagCompound() == null && !world.isRemote) {
            ItemAmmo itemAmmo = (ItemAmmo) heldStack.getItem();
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            nbtTagCompound.setInteger("ammocount", itemAmmo.type.ammoCapacity);
            nbtTagCompound.setInteger("skinId", 0);
            this.setMaxDamage(itemAmmo.type.ammoCapacity);
            if (itemAmmo.type.magazineCount > 1) {
                nbtTagCompound.setInteger("magcount", 1);
                for (int i = 1; i < itemAmmo.type.magazineCount + 1; i++) {
                    nbtTagCompound.setInteger("ammocount" + i, itemAmmo.type.ammoCapacity);
                }
            }
            if (itemAmmo.type.subAmmo != null) {
                if (itemAmmo.type.subAmmo[0] != null) {
                    ItemBullet bullet = ModularWarfare.bulletTypes.get(itemAmmo.type.subAmmo[0]);
                    if (bullet != null) {
                        ItemStack bulletStack = new ItemStack(bullet);
                        nbtTagCompound.setTag("bullet", bulletStack.writeToNBT(new NBTTagCompound()));
                    }
                }
            }
            heldStack.setTagCompound(nbtTagCompound);
        }
        if (heldStack.getTagCompound() != null) {
            ItemAmmo itemAmmo = (ItemAmmo) heldStack.getItem();
            NBTTagCompound tag = heldStack.getTagCompound();
            this.setDamage(heldStack,itemAmmo.type.ammoCapacity - tag.getInteger("ammocount"));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        ItemAmmo itemAmmo = (ItemAmmo) stack.getItem();
        AmmoType ammoType = itemAmmo.type;
        if (ammoType.subAmmo != null) {
            ModularWarfare.NETWORK.sendToServer(new PacketGunReload());
            return new ActionResult(EnumActionResult.SUCCESS, stack);
        } else {
            return new ActionResult(EnumActionResult.FAIL, stack);
        }
    }

    /**
     * Minecraft Overrides
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack ammoStack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (type.magazineCount == 1) {
            int currentAmmoCount = 0;

            if (ammoStack.getTagCompound() != null) {
                NBTTagCompound tag = ammoStack.getTagCompound();
                currentAmmoCount = tag.hasKey("ammocount") ? tag.getInteger("ammocount") : 0;
            } else {
                currentAmmoCount = type.ammoCapacity;
            }

            tooltip.add(generateLoreLineAlt("Ammo", Integer.toString(currentAmmoCount), Integer.toString(type.ammoCapacity)));
        } else {
            if (ammoStack.getTagCompound() != null) {
                String baseDisplayLine = "%bMag Ammo %s: %g%s%dg/%g%s";
                baseDisplayLine = baseDisplayLine.replaceAll("%b", TextFormatting.BLUE.toString());
                baseDisplayLine = baseDisplayLine.replaceAll("%g", TextFormatting.GRAY.toString());
                baseDisplayLine = baseDisplayLine.replaceAll("%dg", TextFormatting.DARK_GRAY.toString());

                for (int i = 1; i < type.magazineCount + 1; i++) {
                    NBTTagCompound tag = ammoStack.getTagCompound();
                    tooltip.add(String.format(baseDisplayLine, i, tag.getInteger("ammocount" + i), type.ammoCapacity));
                }
            }
        }

        if (ammoStack.getTagCompound() != null) {
            if (ammoStack.getTagCompound().hasKey("bullet")) {
                ItemStack usedBullet = new ItemStack(ammoStack.getTagCompound().getCompoundTag("bullet"));
                ItemBullet usedBulletItem = (ItemBullet) usedBullet.getItem();
                tooltip.add(generateLoreLine("Bullet", usedBulletItem.type.displayName));
            }
        }

        tooltip.add("" + TextFormatting.BLUE.toString() + "Accepted bullets:");
        if (((ItemAmmo) ammoStack.getItem()).type.subAmmo != null) {
            if (((ItemAmmo) ammoStack.getItem()).type.subAmmo.length > 0) {
                for (String internalName : ((ItemAmmo) ammoStack.getItem()).type.subAmmo) {
                    if (ModularWarfare.bulletTypes.containsKey(internalName)) {
                        final String bulletName = ModularWarfare.bulletTypes.get(internalName).type.displayName;
                        if (bulletName != null) {
                            tooltip.add("- " + bulletName);
                        }
                    }
                }
            }
        }
        tooltip.add("\u00a7e" + "[R] to reload");
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

}