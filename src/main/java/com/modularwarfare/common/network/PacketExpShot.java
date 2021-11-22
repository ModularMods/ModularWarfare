package com.modularwarfare.common.network;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.guns.manager.ShotValidation;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.hitbox.maths.EnumHitboxType;
import com.modularwarfare.utility.RayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
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
                if(ModConfig.INSTANCE.enable_client_hit_reg) {
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
