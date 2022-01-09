package com.modularwarfare.common.network;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.handler.ServerTickHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketExpShot extends PacketBase {

    public int entityId;
    public String internalname;

    public PacketExpShot() {
    }

    public PacketExpShot(int entityId, String internalname) {
        this.entityId = entityId;
        this.internalname = internalname;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(data, this.internalname);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.entityId = data.readInt();
        this.internalname = ByteBufUtils.readUTF8String(data);
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        IThreadListener mainThread = (WorldServer) entityPlayer.world;
        mainThread.addScheduledTask(new Runnable() {
            public void run() {
                if(ModConfig.INSTANCE.shots.client_sided_hit_registration) {
                    if (entityPlayer.ping > 100 * 20) {
                        entityPlayer.sendMessage(new TextComponentString(TextFormatting.GRAY + "[" + TextFormatting.RED + "ModularWarfare" + TextFormatting.GRAY + "] Your ping is too high, shot not registered."));
                        return;
                    }
                    if (entityPlayer != null) {
                        if (entityPlayer.getHeldItemMainhand() != null) {
                            if (entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {

                                if (ServerTickHandler.playerAimShootCooldown.get(entityPlayer.getName()) == null) {
                                    ModularWarfare.NETWORK.sendToAll(new PacketAimingReponse(entityPlayer.getName(), true));
                                }
                                ServerTickHandler.playerAimShootCooldown.put(entityPlayer.getName(), 60);

                                if (ModularWarfare.gunTypes.get(internalname) != null) {
                                    ItemGun itemGun = ModularWarfare.gunTypes.get(internalname);
                                    WeaponFireMode fireMode = itemGun.type.getFireMode(entityPlayer.getHeldItemMainhand());
                                    int shotCount = fireMode == WeaponFireMode.BURST ? entityPlayer.getHeldItemMainhand().getTagCompound().getInteger("shotsremaining") > 0 ? entityPlayer.getHeldItemMainhand().getTagCompound().getInteger("shotsremaining") : itemGun.type.numBurstRounds : 1;

                                    // Burst Stuff
                                    if (fireMode == WeaponFireMode.BURST) {
                                        shotCount = shotCount - 1;
                                        entityPlayer.getHeldItemMainhand().getTagCompound().setInteger("shotsremaining", shotCount);
                                    }

                                    itemGun.consumeShot(entityPlayer.getHeldItemMainhand());

                                    // Sound
                                    if (GunType.getAttachment(entityPlayer.getHeldItemMainhand(), AttachmentEnum.Barrel) != null) {
                                        itemGun.type.playSound(entityPlayer, WeaponSoundType.FireSuppressed, entityPlayer.getHeldItemMainhand(), entityPlayer);
                                    } else {
                                        itemGun.type.playSound(entityPlayer, WeaponSoundType.Fire, entityPlayer.getHeldItemMainhand(), entityPlayer);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void handleClientSide(EntityPlayer entityPlayer) {

    }

}
