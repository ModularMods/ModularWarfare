package com.modularwarfare.common.armor;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.MWArmorType;
import com.modularwarfare.client.model.ModelCustomArmor;
import com.modularwarfare.common.init.ModSounds;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemMWArmor extends ItemArmor implements ISpecialArmor {
    public ArmorType type;
    public BaseType baseType;
    public String internalName;

    public ItemMWArmor(final ArmorType type, final MWArmorType armorSlot) {
        super(ItemArmor.ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.fromString(armorSlot.name().toLowerCase()));
        type.initializeArmor(armorSlot.name().toLowerCase());
        type.loadExtraValues();
        this.setUnlocalizedName(this.internalName = type.armorTypes.get(armorSlot).internalName);
        this.setRegistryName(this.internalName);
        setCreativeTab(ModularWarfare.MODS_TABS.get(type.contentPack));
        if (type.durability != null) {
            this.setMaxDamage(type.durability);
        }
        this.baseType = type;
        this.type = type;
    }

    public void setType(final BaseType type) {
        this.type = (ArmorType) type;
    }

    public void onUpdate(final ItemStack unused, final World world, final Entity holdingEntity, final int intI, final boolean flag) {
        if (holdingEntity instanceof EntityPlayer) {
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

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String armourType) {
        int skinId = stack.getTagCompound().getInteger("skinId");
        String path = skinId > 0 ? "skins/" + type.modelSkins[skinId].getSkin() : type.modelSkins[0].getSkin();
        return ModularWarfare.MOD_ID + ":skins/armor/" + path + ".png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    @Nullable
    public ModelBiped getArmorModel(EntityLivingBase living, ItemStack stack, EntityEquipmentSlot slot, ModelBiped defaultModel) {
        if(!type.simpleArmor) {
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ItemMWArmor) {
                    ArmorType armorType = ((ItemMWArmor) stack.getItem()).type;
                    ModelCustomArmor armorModel = (ModelCustomArmor) armorType.bipedModel;
                    if (slot != slot.MAINHAND && slot != slot.OFFHAND) {
                        armorModel.showChest(slot == EntityEquipmentSlot.CHEST);
                        armorModel.showFeet(slot == EntityEquipmentSlot.FEET);
                        armorModel.showHead(slot == EntityEquipmentSlot.HEAD);
                        armorModel.showLegs(slot == EntityEquipmentSlot.LEGS);
                    }

                    armorModel.setModelAttributes(defaultModel);

                    return armorModel;
                }
            }
        }
        return null;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World worldIn, final EntityPlayer playerIn, final EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemstack);
        ItemStack itemstack1 = playerIn.getItemStackFromSlot(entityequipmentslot);

        if (itemstack1.isEmpty()) {
            playerIn.setItemStackToSlot(entityequipmentslot, itemstack.copy());
            itemstack.setCount(0);
            worldIn.playSound(null, playerIn.getPosition(), ModSounds.EQUIP_EXTRA, SoundCategory.PLAYERS, 2.0f, 1.0f);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
        } else {
            return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);
        }
    }

    public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase player, final ItemStack armor, final DamageSource source, final double damage, final int slot) {
        return new ISpecialArmor.ArmorProperties(1, this.type.defense, Integer.MAX_VALUE);
    }

    public int getArmorDisplay(final EntityPlayer player, final ItemStack armor, final int slot) {
        return (int) (this.type.defense * 20.0);
    }

    public void damageArmor(final EntityLivingBase entity, final ItemStack stack, final DamageSource source, final int damage, final int slot) {
        if (this.type.durability != null) {
            stack.damageItem(damage, entity);
        }
    }
}
