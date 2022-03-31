package com.modularwarfare.common.guns;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.config.GunRenderConfig;
import com.modularwarfare.client.model.ModelGun;
import com.modularwarfare.common.network.PacketPlaySound;
import com.modularwarfare.common.textures.TextureEnumType;
import com.modularwarfare.common.textures.TextureType;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.objects.SoundEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GunType extends BaseType {

    /**
     * Weapon Classification for later use with default animations etc
     */
    public WeaponType weaponType;

    public WeaponScopeType scopeType = WeaponScopeType.DEFAULT;

    //Munition variables
    /**
     * Damage inflicted per bullet. Multiplied by the bullet damage value.
     */
    public float gunDamage = 0;

    public float moveSpeedModifier = 1F;

    /**
     * Damage inflicted per bullet. Multiplied by the bullet damage value.
     */
    public float gunDamageHeadshotBonus = 0;

    /**
     * Weapon block range
     */
    public int weaponMaxRange = 200;
    /**
     * Weapon effective max block range
     */
    public int weaponEffectiveRange = 50;
    /**
     * The number of bullet entities created by each shot
     */
    public int numBullets = 1;

    /**
     * The amount that bullets spread out when fired from this gun
     */
    public float bulletSpread;
    /**
     * The fire rate of the gun in RPM, 1200 = MAX
     */
    public int roundsPerMin = 1;
    /**
     * For when RPM is converted to ticks - Do not use
     */
    public transient int fireTickDelay = 0;
    /**
     * The number of bullets to fire per burst in burst mode
     */
    public int numBurstRounds = 3;


    public boolean isEnergyGun = false;

    //Recoil Variables
    /**
     * Base value for Upwards cursor/view recoil
     */
    public float recoilPitch = 10.0F;
    /**
     * Base value for Left/Right cursor/view recoil
     */
    public float recoilYaw = 1.0F;
    /**
     * Modifier for setting the maximum pitch divergence when randomizing recoil (Recoil 2 + rndRecoil 0.5 == 1.5-2.5 Recoil range)
     */

    /**
     * Factor of accuracy when sneaking
     */
    public float accuracySneakFactor = 0.75f;

    public float randomRecoilPitch = 0.5F;

    /**
     * Modifier for setting the maximum yaw divergence when randomizing recoil (Recoil 2 + rndRecoil 0.5 == 1.5-2.5 Recoil range)
     */
    public float randomRecoilYaw = 0.5F;

    /**
     * Modifier for setting the maximum yaw divergence when randomizing recoil (Recoil 2 + rndRecoil 0.5 == 1.5-2.5 Recoil range)
     */
    public float recoilAimReducer = 0.8F;

    /**
     * The firing modes of the gun. SEMI, FULL, BURST
     */
    public WeaponFireMode[] fireModes = new WeaponFireMode[]{WeaponFireMode.SEMI};

    /**
     * Attachment Types
     */
    public HashMap<AttachmentEnum, ArrayList<String>> acceptedAttachments;

    // Reload Variables
    /**
     * The time (in ticks) it takes to reload this gun
     */
    public int reloadTime = 40;
    /**
     * The time (in ticks) it takes to offhand reload this gun
     */
    public Integer offhandReloadTime;

    // Ammo Variables
    /**
     * Ammo types which can be used in the gun
     */
    public String[] acceptedAmmo;

    public boolean dropBulletCasing = true;

    @SideOnly(value = Side.CLIENT)
    public ModelBiped.ArmPose armPose;

    @SideOnly(value = Side.CLIENT)
    public ModelBiped.ArmPose armPoseAiming;

    /**
     * If true && != null, ammo staticModel will be set by ammo type used. Used built-in ammo staticModel by default
     */
    public boolean dynamicAmmo = false;

    // Bullet Variables
    public Integer internalAmmoStorage;
    public String[] acceptedBullets;
    // Misc Settings
    public boolean allowSprintFiring = true;
    public boolean allowDefaultSounds = true;

    /**
     * Custom flash textures
     */
    public String customFlashTexture;
    public transient TextureType flashType;

    /**
     * Extra Lore
     */
    public String extraLore;

    /**
     * Shell casing
     */
    public Vec3d shellEjectOffsetNormal = new Vec3d(-1.0f, 0.0f, 1.0f);
    public Vec3d shellEjectOffsetAiming = new Vec3d(0.0f, 0.12f, 1.0f);
    //Increases pitch incrementally over last 5 rounds, 0.05F recommended
    public float emptyPitch = 0.05F;
    public HashMap<WeaponSoundType, ArrayList<SoundEntry>> weaponSoundMap;
    //Sound Variables
    private SoundEntry[] weaponSounds;

    public static boolean isPackAPunched(ItemStack heldStack) {
        if (heldStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = heldStack.getTagCompound();
            return nbtTagCompound.hasKey("punched") ? nbtTagCompound.getBoolean("punched") : false;
        }
        return false;
    }

    public static void setPackAPunched(ItemStack heldStack, boolean bool) {
        if (heldStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = heldStack.getTagCompound();
            nbtTagCompound.setBoolean("punched", bool);
            heldStack.setTagCompound(nbtTagCompound);
        }
    }

    public static WeaponFireMode getFireMode(ItemStack heldStack) {
        if (heldStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = heldStack.getTagCompound();
            return nbtTagCompound.hasKey("firemode") ? WeaponFireMode.fromString(nbtTagCompound.getString("firemode")) : null;
        }
        return null;
    }

    public static void setFireMode(ItemStack heldStack, WeaponFireMode fireMode) {
        if (heldStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = heldStack.getTagCompound();
            nbtTagCompound.setString("firemode", fireMode.name().toLowerCase());
            heldStack.setTagCompound(nbtTagCompound);
        }
    }

    public static ItemStack getAttachment(ItemStack heldStack, AttachmentEnum type) {
        if (heldStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = heldStack.getTagCompound();
            return nbtTagCompound.hasKey("attachment_" + type.typeName) ? new ItemStack(nbtTagCompound.getCompoundTag("attachment_" + type.typeName)) : null;
        }
        return null;
    }

    public static void addAttachment(ItemStack heldStack, AttachmentEnum type, ItemStack attachment) {
        if (heldStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = heldStack.getTagCompound();
            nbtTagCompound.setTag("attachment_" + type.typeName, attachment.writeToNBT(new NBTTagCompound()));
        }
    }

    public static void removeAttachment(ItemStack heldStack, AttachmentEnum type) {
        if (heldStack.getTagCompound() != null) {
            NBTTagCompound nbtTagCompound = heldStack.getTagCompound();
            nbtTagCompound.removeTag("attachment_" + type.typeName);
        }
    }

    @Override
    public void loadExtraValues() {
        if (maxStackSize == null)
            maxStackSize = 1;

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            armPose = ModelBiped.ArmPose.BLOCK;
            armPoseAiming = ModelBiped.ArmPose.BOW_AND_ARROW;

            if(customFlashTexture != null){
                if(ModularWarfare.textureTypes.containsKey(customFlashTexture)){
                    flashType = ModularWarfare.textureTypes.get(customFlashTexture);
                } else {
                    flashType = new TextureType();
                    flashType.initDefaultTextures(TextureEnumType.Flash);
                }
            } else {
                flashType = new TextureType();
                flashType.initDefaultTextures(TextureEnumType.Flash);
            }
        }

        loadBaseValues();
        fireTickDelay = 1200 / roundsPerMin;
        try {
            for (ArrayList<SoundEntry> entryList : weaponSoundMap.values()) {
                for (SoundEntry soundEntry : entryList) {
                    if (soundEntry.soundName != null) {
                        ModularWarfare.PROXY.registerSound(soundEntry.soundName);
                        if (soundEntry.soundNameDistant != null)
                            ModularWarfare.PROXY.registerSound(soundEntry.soundNameDistant);
                    } else {
                        ModularWarfare.LOGGER.error(String.format("Sound entry event '%s' has null soundName for type '%s'", soundEntry.soundEvent, internalName));
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void reloadModel() {
        model = new ModelGun(ModularWarfare.getRenderConfig(this, GunRenderConfig.class), this);
    }

    public void playClientSound(EntityPlayer player, WeaponSoundType weaponSoundType) {
        if (weaponSoundMap.containsKey(weaponSoundType)) {
            for (SoundEntry soundEntry : weaponSoundMap.get(weaponSoundType)) {
                Minecraft.getMinecraft().world.playSound(player, player.getPosition(), ClientProxy.modSounds.get(soundEntry.soundName), SoundCategory.PLAYERS, 1f, 1f);
            }
        }
    }

    public void playSoundPos(BlockPos pos, World world, WeaponSoundType weaponSoundType) {
        playSoundPos(pos, world, weaponSoundType, null, 1f);
    }

    public void playSoundPos(BlockPos pos, World world, WeaponSoundType weaponSoundType, EntityPlayer excluded, float volume) {
        if (weaponSoundType != null) {
            if (weaponSoundMap.containsKey(weaponSoundType)) {
                Random random = new Random();
                for (SoundEntry soundEntry : weaponSoundMap.get(weaponSoundType)) {
                    int soundRange = soundEntry.soundRange != null ? soundEntry.soundRange : weaponSoundType.defaultRange;
                    for (EntityPlayer hearingPlayer : world.getEntities(EntityPlayer.class, e -> e.getPosition().getDistance(pos.getX(), pos.getY(), pos.getZ()) <= soundRange)) {
                        //Send sound packet for simple sounds (no distant sound effect)
                        if (!(hearingPlayer.equals(excluded))) {
                            ModularWarfare.NETWORK.sendTo(new PacketPlaySound(pos, soundEntry.soundName, (soundRange / 16) * soundEntry.soundVolumeMultiplier * volume, (random.nextFloat() / soundEntry.soundRandomPitch) + soundEntry.soundPitch), (EntityPlayerMP) hearingPlayer);
                        }
                    }
                }
            } else {
                if (allowDefaultSounds && weaponSoundType.defaultSound != null) {
                    Random random = new Random();
                    String soundName = weaponSoundType.defaultSound;
                    float soundRange = weaponSoundType.defaultRange;
                    for (EntityPlayer hearingPlayer : world.getEntities(EntityPlayer.class, e -> e.getPosition().getDistance(pos.getX(), pos.getY(), pos.getZ()) <= soundRange)) {
                        //Send sound packet for simple sounds (no distant sound effect)
                        if (!(hearingPlayer.equals(excluded))) {
                            ModularWarfare.NETWORK.sendTo(new PacketPlaySound(pos, soundName, (soundRange / 16) * 1f, (random.nextFloat() / 5) + 1), (EntityPlayerMP) hearingPlayer);
                        }
                    }
                }
            }
        }
    }

    public void playSound(EntityLivingBase entityPlayer, WeaponSoundType weaponSoundType, ItemStack gunStack) {
        playSound(entityPlayer, weaponSoundType, gunStack, null);
    }

    public void playSound(EntityLivingBase entityPlayer, WeaponSoundType weaponSoundType, ItemStack gunStack, @Nullable EntityPlayer excluded) {
        if (weaponSoundType != null) {
            if (weaponSoundMap.containsKey(weaponSoundType)) {
                BlockPos originPos = entityPlayer.getPosition();
                World world = entityPlayer.world;
                Random random = new Random();
                for (SoundEntry soundEntry : weaponSoundMap.get(weaponSoundType)) {
                    int soundRange = soundEntry.soundRange != null ? soundEntry.soundRange : weaponSoundType.defaultRange;
                    if (soundEntry.soundNameDistant != null && soundEntry.soundMaxRange != null) {
                        int maxSoundRange = soundEntry.soundMaxRange;
                        for (EntityPlayer hearingPlayer : world.getEntities(EntityPlayer.class, e -> e.getPosition().getDistance(originPos.getX(), originPos.getY(), originPos.getZ()) <= maxSoundRange)) {
                            if (!hearingPlayer.equals(excluded)) {
                                double distance = hearingPlayer.getPosition().getDistance(originPos.getX(), originPos.getY(), originPos.getZ());
                                float volume = 0f;
                                String soundName = "";

                                if (distance > soundRange & distance <= maxSoundRange) {
                                    // For distant sound range
                                    soundName = soundEntry.soundNameDistant;
                                    volume = (float) (((distance + maxSoundRange / 6) / 16) * soundEntry.soundFarVolumeMultiplier);
                                } else {
                                    // For non distant
                                    soundName = soundEntry.soundName;
                                    volume = (float) (((distance + maxSoundRange / 6) / 16) * soundEntry.soundVolumeMultiplier);
                                }
                                //Send sound packet for guns using advanced audio settings
                                //Increases pitch slighty towards end of mag if enabled

                                float customPitch = ((random.nextFloat() / soundEntry.soundRandomPitch) + soundEntry.soundPitch);
                                float modifyPitch = ItemGun.getMagazineBullets(gunStack) <= 5 && emptyPitch != 0F ? 0.30f - (emptyPitch * ItemGun.getMagazineBullets(gunStack)) : 0f;
                                customPitch += modifyPitch;
                                ModularWarfare.NETWORK.sendTo(new PacketPlaySound(originPos, soundName, volume, customPitch), (EntityPlayerMP) hearingPlayer);
                            }
                        }
                    } else {
                        for (EntityPlayer hearingPlayer : world.getEntities(EntityPlayer.class, e -> e.getPosition().getDistance(originPos.getX(), originPos.getY(), originPos.getZ()) <= soundRange)) {
                            if (!hearingPlayer.equals(excluded)) {
                                //Send sound packet for simple sounds (no distant sound effect)
                                ModularWarfare.NETWORK.sendTo(new PacketPlaySound(originPos, soundEntry.soundName, (soundRange / 16) * soundEntry.soundVolumeMultiplier, (random.nextFloat() / soundEntry.soundRandomPitch) + soundEntry.soundPitch), (EntityPlayerMP) hearingPlayer);
                            }
                        }
                    }
                }
            } else {
                if (allowDefaultSounds && weaponSoundType.defaultSound != null) {
                    BlockPos originPos = entityPlayer.getPosition();
                    World world = entityPlayer.world;
                    Random random = new Random();

                    String soundName = weaponSoundType.defaultSound;
                    float soundRange = weaponSoundType.defaultRange;

                    for (EntityPlayer hearingPlayer : world.getEntities(EntityPlayer.class, e -> e.getPosition().getDistance(originPos.getX(), originPos.getY(), originPos.getZ()) <= soundRange)) {
                        //Send sound packet for simple sounds (no distant sound effect)
                        ModularWarfare.NETWORK.sendTo(new PacketPlaySound(originPos, soundName, (soundRange / 16) * 1f, (random.nextFloat() / 5) + 1), (EntityPlayerMP) hearingPlayer);
                    }
                }
            }
        }
    }

    public boolean hasFireMode(WeaponFireMode fireMode) {
        if (fireModes != null) {
            for (int i = 0; i < fireModes.length; i++) {
                if (fireModes[i] == fireMode) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getAssetDir() {
        return "guns";
    }

}