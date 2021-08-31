package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.hud.FlashSystem;
import com.modularwarfare.common.init.ModSounds;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;

public class PacketFlashClient extends PacketBase {

    private int flashAmount;

    public PacketFlashClient() {
    }

    public PacketFlashClient(int givenFlashAmount) {

        this.flashAmount = givenFlashAmount;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(flashAmount);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.flashAmount = data.readInt();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

        FlashSystem.hasTookScreenshot = false;
        FlashSystem.flashValue += this.flashAmount;

        ((ClientProxy)(ModularWarfare.PROXY)).playFlashSound(entityPlayer);

        if (FlashSystem.flashValue > 255) {
            FlashSystem.flashValue = 255;
        }
    }
}
