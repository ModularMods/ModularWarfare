package com.modularwarfare.common.network;

import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketSyncExtraSlot extends PacketBase {

    int player;
    int slot;
    ItemStack itemStack;

    public PacketSyncExtraSlot() {
    }

    public PacketSyncExtraSlot(final EntityPlayer player, final int slot, final ItemStack backpack) {
        this.player = player.getEntityId();
        this.slot = slot;
        this.itemStack = backpack;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(this.player);
        data.writeInt(this.slot);
        ByteBufUtils.writeItemStack(data, this.itemStack);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.player = data.readInt();
        this.slot = data.readInt();
        this.itemStack = ByteBufUtils.readItemStack(data);
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Entity p = Minecraft.getMinecraft().world.getEntityByID(player);
            if (p != null && p instanceof EntityPlayer) {
                ((IExtraItemHandler) p.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).setStackInSlot(slot, itemStack);
            }
        });
    }
}
