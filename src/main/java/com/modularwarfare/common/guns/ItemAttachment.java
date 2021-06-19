package com.modularwarfare.common.guns;

import com.modularwarfare.common.type.BaseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.function.Function;

public class ItemAttachment extends BaseItem {

    public static final Function<AttachmentType, ItemAttachment> factory = type -> {
        return new ItemAttachment(type);
    };
    public AttachmentType type;

    public ItemAttachment(AttachmentType type) {
        super(type);
        this.type = type;
        this.render3d = true;
    }

    @Override
    public void onUpdate(ItemStack unused, World world, Entity holdingEntity, int intI, boolean flag) {
        if (holdingEntity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) holdingEntity;

            if (unused != null && unused.getItem() instanceof ItemAttachment) {
                if (unused.getTagCompound() == null) {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    nbtTagCompound.setInteger("skinId", 1);
                    unused.setTagCompound(nbtTagCompound);
                }
            }
        }
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

}
