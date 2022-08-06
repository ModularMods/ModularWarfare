package com.modularwarfare.mixin.client;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

    @Shadow
    public int currentItem;

    @Shadow
    @Final
    public NonNullList<ItemStack> mainInventory;

    @Shadow
    public EntityPlayer player;

    @Shadow
    public abstract ItemStack getStackInSlot(int index);

    /**
     * @author
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void changeCurrentItem(int direction) {
        if(ClientRenderHooks.getAnimMachine(player).reloading){
            return;
        }
        if (direction > 0)
        {
            direction = 1;
        }

        if (direction < 0)
        {
            direction = -1;
        }

        for (this.currentItem -= direction; this.currentItem < 0; this.currentItem += 9)
        {
            ;
        }

        while (this.currentItem >= 9)
        {
            this.currentItem -= 9;
        }
    }


}
