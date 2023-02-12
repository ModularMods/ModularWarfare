package com.modularwarfare.common.capability.extraslots;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.network.PacketSyncExtraSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.ItemStackHandler;

public class ExtraContainer extends ItemStackHandler implements IExtraItemHandler {
    private EntityPlayer player;

    public ExtraContainer(final EntityPlayer player) {
        super(5);
        this.player = player;
    }

    public ExtraContainer() {
        super(5);
    }

    public void setPlayer(final EntityPlayer player) {
        this.player = player;
    }

    protected void onContentsChanged(final int slot) {
        if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) {
            for (int i = 0; i < this.getSlots(); i++) {
                ModularWarfare.NETWORK.sendToAllTracking(new PacketSyncExtraSlot(this.player, i, this.getStackInSlot(i)), this.player);
            }  
        }
    }
}
