package com.modularwarfare.common.network;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponFireMode;
import com.modularwarfare.common.guns.manager.ShotManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class PacketGunFire extends PacketBase {

    public String internalname;
    public int fireTickDelay;

    public float recoilPitch;
    public float recoilYaw;

    public float recoilAimReducer;

    public float bulletSpread;

    public float rotationPitch;
    public float rotationYaw;

    public PacketGunFire() {
    }

    public PacketGunFire(String internalname, int fireTickDelay, float recoilPitch, float recoilYaw, float recoilAimReducer, float bulletSpread, float rotationPitch, float rotationYaw) {
        this.internalname = internalname;
        this.fireTickDelay = fireTickDelay;
        this.recoilPitch = recoilPitch;
        this.recoilYaw = recoilYaw;
        this.recoilAimReducer = recoilAimReducer;
        this.bulletSpread = bulletSpread;

        this.rotationPitch = rotationPitch;
        this.rotationYaw = rotationYaw;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        ByteBufUtils.writeUTF8String(data, this.internalname);
        data.writeInt(this.fireTickDelay);
        data.writeFloat(this.recoilPitch);
        data.writeFloat(this.recoilYaw);
        data.writeFloat(this.recoilAimReducer);
        data.writeFloat(this.bulletSpread);

        data.writeFloat(this.rotationPitch);
        data.writeFloat(this.rotationYaw);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.internalname = ByteBufUtils.readUTF8String(data);
        this.fireTickDelay = data.readInt();
        this.recoilPitch = data.readFloat();
        this.recoilYaw = data.readFloat();
        this.recoilAimReducer = data.readFloat();
        this.bulletSpread = data.readFloat();

        this.rotationPitch = data.readFloat();
        this.rotationYaw = data.readFloat();
    }

    @Override
    public void handleServerSide(EntityPlayerMP entityPlayer) {
        IThreadListener mainThread = (WorldServer) entityPlayer.world;
        mainThread.addScheduledTask(new Runnable() {
            public void run() {
                if(!ModConfig.INSTANCE.shots.client_sided_hit_registration) {
                    if (entityPlayer != null) {
                        if (ModularWarfare.gunTypes.get(internalname) != null) {
                            ItemGun itemGun = ModularWarfare.gunTypes.get(internalname);
                            WeaponFireMode fireMode = GunType.getFireMode(entityPlayer.getHeldItemMainhand());
                            if (fireMode == null)
                                return;
                            ShotManager.fireServer(entityPlayer, rotationPitch, rotationYaw, entityPlayer.world, entityPlayer.getHeldItemMainhand(), itemGun, fireMode, fireTickDelay, recoilPitch, recoilYaw, recoilAimReducer, bulletSpread);
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
