package com.modularwarfare.common.grenades;

import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.init.ModSounds;
import com.modularwarfare.common.type.BaseItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.function.Function;

public class ItemGrenade extends BaseItem {

    public static final Function<GrenadeType, ItemGrenade> factory = type -> {
        return new ItemGrenade(type);
    };
    public GrenadeType type;

    public ItemGrenade(GrenadeType type) {
        super(type);
        this.type = type;
        this.render3d = true;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        if (entityLiving instanceof EntityPlayer) {
            EntityPlayer playerIn = (EntityPlayer) entityLiving;
            World worldIn = playerIn.world;

            EntityGrenade grenade = new EntityGrenade(worldIn, playerIn, false, type);
            worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, ModSounds.GRENADE_THROW, SoundCategory.PLAYERS, 1.0f, 1.0f);

            if (!worldIn.isRemote) {
                worldIn.spawnEntity(grenade);

                if (!playerIn.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }

                if (worldIn.getDifficulty() == EnumDifficulty.PEACEFUL) {
                    playerIn.sendMessage(new TextComponentString(TextFormatting.RED + "Your difficulty is PEACEFUL, explosion won't do damage to players!"));
                }
            }
        }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        BlockPos pos = new BlockPos(playerIn.posX, playerIn.posY, playerIn.posZ);

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, ModSounds.GRENADE_THROW, SoundCategory.PLAYERS, 0.5f, 1.0f);

        if (!worldIn.isRemote) {
            EntityGrenade grenade = new EntityGrenade(worldIn, playerIn, true, type);
            worldIn.spawnEntity(grenade);

            if (!playerIn.capabilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }


    @Override
    public boolean getShareTag() {
        return true;
    }

}