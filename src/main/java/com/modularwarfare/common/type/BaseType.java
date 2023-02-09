package com.modularwarfare.common.type;

import javax.annotation.Nullable;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.IMWModel;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.fpp.enhanced.models.EnhancedModel;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.SkinType;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.common.network.PacketPlaySound;
import com.modularwarfare.loader.MWModelBase;
import com.modularwarfare.loader.MWModelBipedBase;
import com.modularwarfare.loader.ObjModel;
import com.modularwarfare.objects.SoundEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class BaseType {

    /**
     * Basic model, only loaded for guns if WeaponAnimationType.BASIC
     */
    @SideOnly(value = Side.CLIENT)
    public transient MWModelBase model;

    @SideOnly(value = Side.CLIENT)
    public transient MWModelBipedBase bipedModel;

    /**
     * Enhanced model, only loaded for guns if WeaponAnimationType.ENHANCED
     * MWModelBase model will be ignored
     */
    @SideOnly(value = Side.CLIENT)
    public transient EnhancedModel enhancedModel;

    /**
     * Max stack size
     */
    public Integer maxStackSize;
    /**
     * Weapon staticModel skins/textures
     */
    public SkinType[] modelSkins;
    public String internalName;
    /**
     * Used to generate .lang files automatically
     */
    public String displayName;
    public String iconName;

    public transient int id;
    public transient String contentPack;
    public transient boolean isInDirectory;

    /**
     * SOUNDS
     */
    public HashMap<WeaponSoundType, ArrayList<SoundEntry>> weaponSoundMap;
    public boolean allowDefaultSounds = true;


    @SideOnly(value = Side.CLIENT)
    public static BaseType fromModel(ObjModel model) {
        return null;
    }



    /**
     * Method for sub types to use for loading extra values
     */
    public void loadExtraValues() {

    }

    public void loadBaseValues() {
        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
            reloadModel();

        if (modelSkins == null)
            modelSkins = new SkinType[]{SkinType.getDefaultSkin(this)};

        if (iconName == null)
            iconName = internalName;
    }

    public void postLoad(){

    }

    /**
     * Method for sub types to use for handling staticModel reloading
     */
    public void reloadModel() {

    }

    /**
     * Method for sub types to use for having models
     */
    public boolean hasModel() {
        return (model != null || bipedModel != null || enhancedModel != null);
    }
    
    @Nullable
    public IMWModel getModel() {
        if(enhancedModel!=null) {
            return enhancedModel;
        }
        if(model!=null) {
            return model;
        }
        return null;
    }

    /**
     * Returns internal name if not overridden by sub-type
     */
    @Override
    public String toString() {
        return internalName;
    }

    public String getAssetDir() {
        //System.out.println("called");
        return "";
    }


    /**
     * SOUND
     */

    public void playClientSound(EntityPlayer player, WeaponSoundType weaponSoundType) {
        if (weaponSoundType != null) {
            if(weaponSoundMap != null) {
                if (weaponSoundMap.containsKey(weaponSoundType)) {
                    for (SoundEntry soundEntry : weaponSoundMap.get(weaponSoundType)) {
                        Minecraft.getMinecraft().world.playSound(player, player.getPosition(), ClientProxy.modSounds.get(soundEntry.soundName), SoundCategory.PLAYERS, 1f, 1f);
                    }
                }else {
                    if (allowDefaultSounds && weaponSoundType.defaultSound != null) {
                        Minecraft.getMinecraft().world.playSound(player, player.getPosition(), ClientProxy.modSounds.get(weaponSoundType.defaultSound), SoundCategory.PLAYERS, 1f, 1f);
                    }
                }
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
                    float soundRange = soundEntry.soundRange != null ? soundEntry.soundRange : weaponSoundType.defaultRange;
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
                                float emptyPitch = 0.05F;
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
}
