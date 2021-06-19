package com.modularwarfare.common.capability.extraslots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * Player extraslots capability interface
 */
public interface IExtraItemHandler extends IItemHandlerModifiable {
    void setPlayer(EntityPlayer player);
}
