package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.killchat.KillFeedEntry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketClientKillFeedEntry extends PacketBase {

    /**
     * Default Constructor
     */
    public String text;
    public String weaponInternalName;
    public int timeLiving;


    public PacketClientKillFeedEntry() {
    }


    public PacketClientKillFeedEntry(String text, int timeLiving, String weaponInternalName) {
        this.text = text;
        this.timeLiving = timeLiving * 20;
        this.weaponInternalName = weaponInternalName;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        writeUTF(data, text);
        writeUTF(data, weaponInternalName);
        data.writeInt(timeLiving);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        text = readUTF(data);
        weaponInternalName = readUTF(data);
        timeLiving = data.readInt();
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {
        // This packet is client side only
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            ((ClientProxy) (ModularWarfare.PROXY)).getKillChatManager().add(new KillFeedEntry(text, timeLiving, weaponInternalName));
        });
    }
}
