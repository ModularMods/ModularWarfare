package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketClientAnimation extends PacketBase {

    public String wepType;
    /**
     * Shoot Animation
     */
    public int fireDelay;
    public float recoilPitch;
    public float recoilYaw;
    /**
     * Reload Animation
     */
    public int reloadTime;
    public int reloadCount;
    public int reloadType;
    /**
     * Default Constructor
     */
    private AnimationType animType;

    public PacketClientAnimation() {
    }
    public PacketClientAnimation(AnimationType animType, String wepType) {
        this.animType = animType;
        this.wepType = wepType;
    }
    public PacketClientAnimation(String wepType, int fireDelay, float recoilPitch, float recoilYaw) {
        this(AnimationType.Shoot, wepType);
        this.fireDelay = fireDelay;
        this.recoilPitch = recoilPitch;
        this.recoilYaw = recoilYaw;
    }

    public PacketClientAnimation(String wepType, int reloadTime, int reloadCount, int reloadType) {
        this(AnimationType.Reload, wepType);
        this.reloadTime = reloadTime;
        this.reloadCount = reloadCount;
        this.reloadType = reloadType;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeByte(animType.i);
        writeUTF(data, wepType);

        switch (animType) {
        case Reload: {
            data.writeInt(reloadTime);
            data.writeInt(reloadCount);
            data.writeInt(reloadType);
            break;
        }
        case Shoot: {
            data.writeInt(fireDelay);
            data.writeFloat(recoilPitch);
            data.writeFloat(recoilYaw);
            break;
        }
        default:
            break;
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        animType = AnimationType.getTypeFromInt(data.readByte());
        wepType = readUTF(data);

        switch (animType) {
        case Reload: {
            reloadTime = data.readInt();
            reloadCount = data.readInt();
            reloadType = data.readInt();
            break;
        }
        case Shoot: {
            fireDelay = data.readInt();
            recoilPitch = data.readFloat();
            recoilYaw = data.readFloat();
            break;
        }
        default:
            break;
        }
    }

    @Override
    public void handleServerSide(EntityPlayerMP playerEntity) {
        // This packet is client side only
    }

    @Override
    public void handleClientSide(EntityPlayer clientPlayer) {
        switch (animType) {
        case Reload: {
            ModularWarfare.PROXY.onReloadAnimation(clientPlayer, wepType, reloadTime, reloadCount, reloadType);
            break;
        }
        case Shoot: {
            ModularWarfare.PROXY.onShootAnimation(clientPlayer, wepType, fireDelay, recoilPitch, recoilYaw);
            break;
        }
        case ShootFailed:{
            ModularWarfare.PROXY.onShootFailedAnimation(clientPlayer, wepType);
            break;
        }
        case ModeChange:{
            ModularWarfare.PROXY.onModeChangeAnimation(clientPlayer, wepType);
        }
        default:
            break;
        }
    }

    public static enum AnimationType {
        Shoot(0), Reload(1),ShootFailed(2),ModeChange(3);

        public int i;

        AnimationType(int i) {
            this.i = i;
        }

        public static AnimationType getTypeFromInt(int i) {
            switch (i) {
            case 0:
                return Shoot;
            case 1:
                return Reload;
            case 2:
                return ShootFailed;
            case 3:
                return ModeChange;
            default:
                return null;
            }
        }
    }

}
