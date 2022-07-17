package com.modularwarfare.common.network;

import com.modularwarfare.client.handler.ClientTickHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketGunReloadEnhancedTask extends PacketBase {
    public ItemStack prognosisAmmo;
    public boolean isQuickly=false;

    public PacketGunReloadEnhancedTask() {
        // TODO Auto-generated constructor stub
    }
    

    public PacketGunReloadEnhancedTask(ItemStack prognosisAmmo) {
        this.prognosisAmmo = prognosisAmmo;
    }
    
    public PacketGunReloadEnhancedTask(ItemStack prognosisAmmo,boolean isQuickly) {
        this.prognosisAmmo = prognosisAmmo;
        this.isQuickly=isQuickly;
    }


    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        ByteBufUtils.writeUTF8String(data, prognosisAmmo.serializeNBT().toString());
        data.writeBoolean(isQuickly);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        try {
            prognosisAmmo=new ItemStack(JsonToNBT.getTagFromJson(ByteBufUtils.readUTF8String(data)));
            prognosisAmmo.setCount(1);
            isQuickly=data.readBoolean();
        } catch (NBTException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {

    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        ClientTickHandler.reloadEnhancedPrognosisAmmo=prognosisAmmo;
        ClientTickHandler.reloadEnhancedIsQuickly=isQuickly;
    }

}
