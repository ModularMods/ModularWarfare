package com.modularwarfare.common.guns.manager;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.WeaponFireEvent;
import com.modularwarfare.api.WeaponHitEvent;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.handler.ClientTickHandler;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.entity.decals.EntityShell;
import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.hitbox.hits.BulletHit;
import com.modularwarfare.common.hitbox.hits.PlayerHit;
import com.modularwarfare.common.hitbox.maths.EnumHitboxType;
import com.modularwarfare.common.network.*;
import com.modularwarfare.utility.RayUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class ShotManager {

    public static void fireClient(EntityPlayer entityPlayer, World world, ItemStack gunStack, ItemGun itemGun, WeaponFireMode fireMode) {
        GunType gunType = itemGun.type;

        // Can fire checks
        if (ItemGun.isOnShootCooldown(entityPlayer.getUniqueID()) || ItemGun.isClientReloading(entityPlayer) || ClientRenderHooks.getAnimMachine(entityPlayer).attachmentMode || (!itemGun.type.allowSprintFiring && entityPlayer.isSprinting()) || !itemGun.type.hasFireMode(fireMode))
            return;

        int shotCount = fireMode == WeaponFireMode.BURST ? gunStack.getTagCompound().getInteger("shotsremaining") > 0 ? gunStack.getTagCompound().getInteger("shotsremaining") : gunType.numBurstRounds : 1;

        // Weapon pre fire event
        WeaponFireEvent.PreClient preFireEvent = new WeaponFireEvent.PreClient(entityPlayer, gunStack, itemGun, gunType.weaponMaxRange);
        MinecraftForge.EVENT_BUS.post(preFireEvent);
        if (preFireEvent.isCanceled())
            return;

        if (preFireEvent.getResult() == Event.Result.DEFAULT || preFireEvent.getResult() == Event.Result.ALLOW) {
            if (!ItemGun.hasNextShot(gunStack)) {
                if (ItemGun.canDryFire) {
                    gunType.playClientSound(entityPlayer, WeaponSoundType.DryFire);
                    gunType.playClientSound(entityPlayer, WeaponSoundType.FireLast);
                    ItemGun.canDryFire = false;
                }
                if (fireMode == WeaponFireMode.BURST) gunStack.getTagCompound().setInteger("shotsremaining", 0);
                return;
            }
        }

        ModularWarfare.PROXY.onShootAnimation(entityPlayer, gunType.internalName, gunType.fireTickDelay, itemGun.type.recoilPitch, itemGun.type.recoilYaw);

        ItemGun.canDryFire = true;

        // Sound
        if (GunType.getAttachment(gunStack, AttachmentEnum.Barrel) != null) {
            ItemAttachment barrelAttachment = (ItemAttachment) GunType.getAttachment(gunStack, AttachmentEnum.Barrel).getItem();
            if (barrelAttachment.type.barrel.isSuppressor) {
                gunType.playClientSound(entityPlayer, WeaponSoundType.FireSuppressed);
            } else {
                gunType.playClientSound(entityPlayer, WeaponSoundType.Fire);
            }
        } else if (GunType.isPackAPunched(gunStack)) {
            gunType.playClientSound(entityPlayer, WeaponSoundType.Punched);
            gunType.playClientSound(entityPlayer, WeaponSoundType.Fire);
        } else {
            gunType.playClientSound(entityPlayer, WeaponSoundType.Fire);
        }

        if (gunType.weaponType == WeaponType.BoltSniper || gunType.weaponType == WeaponType.Shotgun) {
            gunType.playClientSound(entityPlayer, WeaponSoundType.Pump);
        }

        // Burst Stuff
        if (fireMode == WeaponFireMode.BURST) {
            shotCount = shotCount - 1;
            gunStack.getTagCompound().setInteger("shotsremaining", shotCount);
        }

        ClientTickHandler.playerShootCooldown.put(entityPlayer.getUniqueID(), gunType.fireTickDelay);


        if (ModConfig.INSTANCE.casings_drops.drop_bullets_casings) {
            /**
             * Drop casing
             */
            int numBullets = gunType.numBullets;
            ItemBullet bulletItem = ItemGun.getUsedBullet(gunStack, gunType);
            if (bulletItem != null) {
                if (bulletItem.type.isSlug) {
                    numBullets = 1;
                }
            }

            EntityShell shell = new EntityShell(world, entityPlayer, itemGun, bulletItem);

            shell.setHeadingFromThrower(entityPlayer, entityPlayer.rotationPitch, entityPlayer.rotationYaw + 110, 0.0F, 0.2F, 5);
            world.spawnEntity(shell);
        }

        ItemGun.consumeShot(gunStack);

        /**
         * Hit Register
         */
        if (!ModConfig.INSTANCE.shots.client_sided_hit_registration) {
            ModularWarfare.NETWORK.sendToServer(new PacketGunFire(gunType.internalName, gunType.fireTickDelay, gunType.recoilPitch, gunType.recoilYaw, gunType.recoilAimReducer, gunType.bulletSpread, entityPlayer.rotationPitch, entityPlayer.rotationYaw));
        } else {
            fireClientSide(entityPlayer, itemGun);
        }
    }


    public static void fireServer(EntityPlayer entityPlayer, float rotationPitch, float rotationYaw, World world, ItemStack gunStack, ItemGun itemGun, WeaponFireMode fireMode, final int clientFireTickDelay, final float recoilPitch, final float recoilYaw, final float recoilAimReducer, final float bulletSpread) {
        GunType gunType = itemGun.type;
        // Can fire checks
        if (ShotValidation.verifShot(entityPlayer, gunStack, itemGun, fireMode, clientFireTickDelay, recoilPitch, recoilYaw, recoilAimReducer, bulletSpread)) {

            // Weapon pre fire event
            WeaponFireEvent.PreServer preFireEvent = new WeaponFireEvent.PreServer(entityPlayer, gunStack, itemGun, gunType.weaponMaxRange);
            MinecraftForge.EVENT_BUS.post(preFireEvent);
            if (preFireEvent.isCanceled())
                return;
            int shotCount = fireMode == WeaponFireMode.BURST ? gunStack.getTagCompound().getInteger("shotsremaining") > 0 ? gunStack.getTagCompound().getInteger("shotsremaining") : gunType.numBurstRounds : 1;

            if (preFireEvent.getResult() == Event.Result.DEFAULT || preFireEvent.getResult() == Event.Result.ALLOW) {
                if (!ItemGun.hasNextShot(gunStack)) {
                    if (ItemGun.canDryFire) {
                        gunType.playSound(entityPlayer, WeaponSoundType.DryFire, gunStack);
                        ItemGun.canDryFire = false;
                    }
                    if (fireMode == WeaponFireMode.BURST) gunStack.getTagCompound().setInteger("shotsremaining", 0);
                    return;
                }
            }

            // Sound
            if (GunType.getAttachment(gunStack, AttachmentEnum.Barrel) != null) {
                gunType.playSound(entityPlayer, WeaponSoundType.FireSuppressed, gunStack, entityPlayer);
            } else if (GunType.isPackAPunched(gunStack)) {
                gunType.playSound(entityPlayer, WeaponSoundType.Punched, gunStack, entityPlayer);
                gunType.playSound(entityPlayer, WeaponSoundType.Fire, gunStack, entityPlayer);
            } else {
                gunType.playSound(entityPlayer, WeaponSoundType.Fire, gunStack, entityPlayer);
            }
            List<Entity> entities = new ArrayList();
            int numBullets = gunType.numBullets;
            ItemBullet bulletItem = ItemGun.getUsedBullet(gunStack, gunType);
            if (bulletItem != null) {
                if (bulletItem.type.isSlug) {
                    numBullets = 1;
                }
            }
            ArrayList<BulletHit> rayTraceList = new ArrayList<BulletHit>();
            for (int i = 0; i < numBullets; i++) {
                BulletHit rayTrace = RayUtil.standardEntityRayTrace(Side.SERVER, world, rotationPitch, rotationYaw, entityPlayer, preFireEvent.getWeaponRange(), itemGun, GunType.isPackAPunched(gunStack));
                rayTraceList.add(rayTrace);
            }

            boolean headshot = false;
            for (BulletHit rayTrace : rayTraceList) {
                if (rayTrace instanceof PlayerHit) {
                    if (!world.isRemote) {
                        final EntityPlayer victim = ((PlayerHit) rayTrace).getEntity();
                        if (victim != null) {
                            if (!victim.isDead && victim.getHealth() > 0.0f) {
                                entities.add(victim);
                                gunType.playSoundPos(victim.getPosition(), world, WeaponSoundType.Penetration);
                                headshot = ((PlayerHit) rayTrace).hitbox.type.equals(EnumHitboxType.HEAD);
                                if (entityPlayer instanceof EntityPlayerMP) {
                                    ModularWarfare.NETWORK.sendTo(new PacketPlayHitmarker(headshot), (EntityPlayerMP) entityPlayer);
                                    ModularWarfare.NETWORK.sendTo(new PacketPlaySound(victim.getPosition(), "flyby", 1f, 1f), (EntityPlayerMP) victim);
                                    if (ModConfig.INSTANCE.hud.snap_fade_hit) {
                                        ModularWarfare.NETWORK.sendTo(new PacketPlayerHit(), (EntityPlayerMP) victim);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (!world.isRemote) {
                        if (rayTrace.rayTraceResult != null) {
                            if (rayTrace.rayTraceResult.entityHit instanceof EntityGrenade) {
                                ((EntityGrenade) rayTrace.rayTraceResult.entityHit).explode();
                            }
                            if (rayTrace.rayTraceResult.entityHit instanceof EntityLivingBase) {
                                final EntityLivingBase victim = (EntityLivingBase) ((BulletHit) rayTrace).rayTraceResult.entityHit;
                                if (victim != null) {
                                    entities.add(victim);
                                    gunType.playSoundPos(victim.getPosition(), world, WeaponSoundType.Penetration);
                                    headshot = ItemGun.canEntityGetHeadshot(victim) && rayTrace.rayTraceResult.hitVec.y >= victim.getPosition().getY() + victim.getEyeHeight() - 0.15f;
                                    if (entityPlayer instanceof EntityPlayerMP) {
                                        ModularWarfare.NETWORK.sendTo(new PacketPlayHitmarker(headshot), (EntityPlayerMP) entityPlayer);
                                    }
                                }
                            } else if (rayTrace.rayTraceResult.hitVec != null) {
                                BlockPos blockPos = rayTrace.rayTraceResult.getBlockPos();
                                ItemGun.playImpactSound(world, blockPos, gunType);
                                gunType.playSoundPos(blockPos, world, WeaponSoundType.Crack, entityPlayer, 1.0f);
                                ItemGun.doHit(rayTrace.rayTraceResult, entityPlayer);
                            }
                        }
                    }
                }
            }

            // Weapon post fire event
            WeaponFireEvent.Post postFireEvent = new WeaponFireEvent.Post(entityPlayer, gunStack, itemGun, entities);
            MinecraftForge.EVENT_BUS.post(postFireEvent);

            if (postFireEvent.getAffectedEntities() != null && !postFireEvent.getAffectedEntities().isEmpty()) {
                for (Entity target : postFireEvent.getAffectedEntities()) {
                    if (target != null) {
                        if (target != entityPlayer) {

                            // Weapon pre hit event
                            WeaponHitEvent.Pre preHitEvent = new WeaponHitEvent.Pre(entityPlayer, gunStack, itemGun, headshot, postFireEvent.getDamage(), target);
                            MinecraftForge.EVENT_BUS.post(preHitEvent);
                            if (preHitEvent.isCanceled())
                                return;

                            if (headshot) {
                                preHitEvent.setDamage(preHitEvent.getDamage() + gunType.gunDamageHeadshotBonus);
                            }

                            if (target instanceof EntityLivingBase) {
                                EntityLivingBase targetELB = (EntityLivingBase) target;
                                if (bulletItem != null) {
                                    if (bulletItem.type != null) {
                                        preHitEvent.setDamage(preHitEvent.getDamage() * bulletItem.type.bulletDamageFactor);
                                        if (bulletItem.type.bulletProperties != null) {
                                            if (!bulletItem.type.bulletProperties.isEmpty()) {
                                                BulletProperty bulletProperty = bulletItem.type.bulletProperties.get(targetELB.getName()) != null ? bulletItem.type.bulletProperties.get(targetELB.getName()) : bulletItem.type.bulletProperties.get("All");
                                                if (bulletProperty.potionEffects != null) {
                                                    for (PotionEntry potionEntry : bulletProperty.potionEffects) {
                                                        targetELB.addPotionEffect(new PotionEffect(potionEntry.potionEffect.getPotion(), potionEntry.duration, potionEntry.level));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (target instanceof EntityPlayer) {
                                if (((PlayerHit) rayTraceList.get(0)).hitbox.type.equals(EnumHitboxType.BODY)) {
                                    EntityPlayer player = (EntityPlayer) target;
                                    if (player.hasCapability(CapabilityExtra.CAPABILITY, null)) {
                                        final IExtraItemHandler extraSlots = player.getCapability(CapabilityExtra.CAPABILITY, null);
                                        final ItemStack plate = extraSlots.getStackInSlot(1);
                                        if (plate != null) {
                                            if (plate.getItem() instanceof ItemSpecialArmor) {
                                                ArmorType armorType = ((ItemSpecialArmor) plate.getItem()).type;
                                                float damage = preHitEvent.getDamage();
                                                preHitEvent.setDamage((float) (damage - (damage * armorType.defense)));
                                            }
                                        }
                                    }
                                }
                            }

                            if (!ModConfig.INSTANCE.shots.knockback_entity_damage) {
                                RayUtil.attackEntityWithoutKnockback(target, DamageSource.causePlayerDamage(preFireEvent.getWeaponUser()).setProjectile(), preHitEvent.getDamage());
                            } else {
                                target.attackEntityFrom(DamageSource.causePlayerDamage(preFireEvent.getWeaponUser()).setProjectile(), preHitEvent.getDamage());
                            }

                            target.hurtResistantTime = 0;

                            // Weapon pre hit event
                            WeaponHitEvent.Post postHitEvent = new WeaponHitEvent.Post(entityPlayer, gunStack, itemGun, postFireEvent.getAffectedEntities(), preHitEvent.getDamage());
                            MinecraftForge.EVENT_BUS.post(postHitEvent);
                        }
                    }
                }
            }

            // Burst Stuff
            if (fireMode == WeaponFireMode.BURST) {
                shotCount = shotCount - 1;
                gunStack.getTagCompound().setInteger("shotsremaining", shotCount);
            }

            if (preFireEvent.getResult() == Event.Result.DEFAULT || preFireEvent.getResult() == Event.Result.ALLOW) {
                itemGun.consumeShot(gunStack);
            }

            //Hands upwards when shooting
            if (ServerTickHandler.playerAimShootCooldown.get(entityPlayer.getName()) == null) {
                ModularWarfare.NETWORK.sendToAll(new PacketAimingReponse(entityPlayer.getName(), true));
            }
            ServerTickHandler.playerAimShootCooldown.put(entityPlayer.getName(), 60);
        } else {
            if (ModConfig.INSTANCE.general.modified_pack_server_kick) {
                ((EntityPlayerMP) entityPlayer).connection.disconnect(new TextComponentString("[ModularWarfare] Kicked for client-side modified content-pack. (Bad RPM/Recoil for the gun: " + itemGun.type.internalName + ") [RPM should be: " + itemGun.type.roundsPerMin + "]"));
            }
        }
    }


    public static void fireClientSide(EntityPlayer entityPlayer, ItemGun itemGun){
        if (entityPlayer.world.isRemote) {
            List<Entity> entities = new ArrayList();
            int numBullets = itemGun.type.numBullets;
            ItemBullet bulletItem = ItemGun.getUsedBullet(entityPlayer.getHeldItemMainhand(), itemGun.type);
            if (bulletItem != null) {
                if (bulletItem.type.isSlug) {
                    numBullets = 1;
                }
            }
            ArrayList<BulletHit> rayTraceList = new ArrayList<BulletHit>();
            for (int i = 0; i < numBullets; i++) {
                BulletHit rayTrace = RayUtil.standardEntityRayTrace(Side.CLIENT, entityPlayer.world, entityPlayer.rotationPitch, entityPlayer.rotationYaw, entityPlayer, itemGun.type.weaponMaxRange, itemGun, false);
                rayTraceList.add(rayTrace);
            }

            ModularWarfare.NETWORK.sendToServer(new PacketExpShot(entityPlayer.getEntityId(), itemGun.type.internalName));

            boolean headshot = false;
            for (BulletHit rayTrace : rayTraceList) {
                if (rayTrace instanceof PlayerHit) {
                    final EntityPlayer victim = ((PlayerHit) rayTrace).getEntity();
                    if (victim != null) {
                        if (!victim.isDead && victim.getHealth() > 0.0f) {
                            entities.add(victim);
                            //Send server player hit + hitbox
                            ModularWarfare.NETWORK.sendToServer(new PacketExpGunFire(victim.getEntityId(), itemGun.type.internalName, ((PlayerHit) rayTrace).hitbox.type.name(), itemGun.type.fireTickDelay, itemGun.type.recoilPitch, itemGun.type.recoilYaw, itemGun.type.recoilAimReducer, itemGun.type.bulletSpread, rayTrace.rayTraceResult.hitVec.x, rayTrace.rayTraceResult.hitVec.y, rayTrace.rayTraceResult.hitVec.z));
                        }
                    }
                } else {
                    if (rayTrace.rayTraceResult != null) {
                        if (rayTrace.rayTraceResult.hitVec != null) {
                            if(rayTrace.rayTraceResult.entityHit != null){
                                //Normal entity hit
                                ModularWarfare.NETWORK.sendToServer(new PacketExpGunFire(rayTrace.rayTraceResult.entityHit.getEntityId(), itemGun.type.internalName, "", itemGun.type.fireTickDelay, itemGun.type.recoilPitch, itemGun.type.recoilYaw, itemGun.type.recoilAimReducer, itemGun.type.bulletSpread, rayTrace.rayTraceResult.hitVec.x, rayTrace.rayTraceResult.hitVec.y, rayTrace.rayTraceResult.hitVec.z));
                            } else {
                                //Crack hit block packet
                                ModularWarfare.NETWORK.sendToServer(new PacketExpGunFire(-1, itemGun.type.internalName, "", itemGun.type.fireTickDelay, itemGun.type.recoilPitch, itemGun.type.recoilYaw, itemGun.type.recoilAimReducer, itemGun.type.bulletSpread, rayTrace.rayTraceResult.hitVec.x, rayTrace.rayTraceResult.hitVec.y, rayTrace.rayTraceResult.hitVec.z));
                            }
                        }
                    }
                }
            }
        }
    }

}
