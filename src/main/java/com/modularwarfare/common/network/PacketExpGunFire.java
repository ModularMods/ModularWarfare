package com.modularwarfare.common.network;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.guns.manager.ShotValidation;
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

public class PacketExpGunFire extends PacketBase {

    public int entityId;

    public String internalname;
    public String hitboxType;

    public int fireTickDelay;

    public float recoilPitch;
    public float recoilYaw;

    public float recoilAimReducer;

    public float bulletSpread;

    private double posX;
    private double posY;
    private double posZ;

    public PacketExpGunFire() {
    }

    public PacketExpGunFire(int entityId, String internalname, String hitboxType, int fireTickDelay, float recoilPitch, float recoilYaw, float recoilAimReducer, float bulletSpread, double x, double y, double z) {
        this.entityId = entityId;
        this.internalname = internalname;
        this.hitboxType = hitboxType;

        this.fireTickDelay = fireTickDelay;
        this.recoilPitch = recoilPitch;
        this.recoilYaw = recoilYaw;
        this.recoilAimReducer = recoilAimReducer;
        this.bulletSpread = bulletSpread;

        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        data.writeInt(this.entityId);
        ByteBufUtils.writeUTF8String(data, this.internalname);
        ByteBufUtils.writeUTF8String(data, this.hitboxType);

        data.writeInt(this.fireTickDelay);
        data.writeFloat(this.recoilPitch);
        data.writeFloat(this.recoilYaw);
        data.writeFloat(this.recoilAimReducer);
        data.writeFloat(this.bulletSpread);

        data.writeDouble(this.posX);
        data.writeDouble(this.posY);
        data.writeDouble(this.posZ);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
        this.entityId = data.readInt();
        this.internalname = ByteBufUtils.readUTF8String(data);
        this.hitboxType = ByteBufUtils.readUTF8String(data);

        this.fireTickDelay = data.readInt();
        this.recoilPitch = data.readFloat();
        this.recoilYaw = data.readFloat();
        this.recoilAimReducer = data.readFloat();
        this.bulletSpread = data.readFloat();

        this.posX = data.readDouble();
        this.posY = data.readDouble();
        this.posZ = data.readDouble();
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

                                if (ModularWarfare.gunTypes.get(internalname) != null) {
                                    ItemGun itemGun = ModularWarfare.gunTypes.get(internalname);

                                    if (entityId != -1) {
                                        Entity target = entityPlayer.world.getEntityByID(entityId);
                                        WeaponFireMode fireMode = GunType.getFireMode(entityPlayer.getHeldItemMainhand());
                                        if (fireMode == null)
                                            return;
                                        if (ShotValidation.verifShot(entityPlayer, entityPlayer.getHeldItemMainhand(), itemGun, fireMode, fireTickDelay, recoilPitch, recoilYaw, recoilAimReducer, bulletSpread)) {
                                            if (target != null) {
                                                float damage = itemGun.type.gunDamage;
                                                if (target instanceof EntityPlayer && hitboxType != null) {
                                                    if (hitboxType.contains("BODY")) {
                                                        EntityPlayer player = (EntityPlayer) target;
                                                        if (player.hasCapability(CapabilityExtra.CAPABILITY, null)) {
                                                            final IExtraItemHandler extraSlots = player.getCapability(CapabilityExtra.CAPABILITY, null);
                                                            final ItemStack plate = extraSlots.getStackInSlot(1);
                                                            if (plate != null) {
                                                                if (plate.getItem() instanceof ItemSpecialArmor) {
                                                                    ArmorType armorType = ((ItemSpecialArmor) plate.getItem()).type;
                                                                    damage = (float) (damage - (damage * armorType.defense));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!ModConfig.INSTANCE.shots.knockback_entity_damage) {
                                                    RayUtil.attackEntityWithoutKnockback(target, DamageSource.causePlayerDamage(entityPlayer).setProjectile(), damage);
                                                } else {
                                                    target.attackEntityFrom(DamageSource.causePlayerDamage(entityPlayer).setProjectile(), damage);
                                                }
                                                target.hurtResistantTime = 0;

                                                if (entityPlayer instanceof EntityPlayerMP) {
                                                    ModularWarfare.NETWORK.sendTo(new PacketPlayHitmarker(hitboxType.contains("HEAD")), entityPlayer);
                                                    ModularWarfare.NETWORK.sendTo(new PacketPlaySound(target.getPosition(), "flyby", 1f, 1f), (EntityPlayerMP) target);

                                                    if (ModConfig.INSTANCE.hud.snap_fade_hit) {
                                                        ModularWarfare.NETWORK.sendTo(new PacketPlayerHit(), (EntityPlayerMP) target);
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        BlockPos blockPos = new BlockPos(posX, posY, posZ);
                                        ItemGun.playImpactSound(entityPlayer.world, blockPos, itemGun.type);
                                        itemGun.type.playSoundPos(blockPos, entityPlayer.world, WeaponSoundType.Crack, entityPlayer, 1.0f);
                                        ItemGun.doHit(posX, posY, posZ, entityPlayer);
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
