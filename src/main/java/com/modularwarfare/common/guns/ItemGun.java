package com.modularwarfare.common.guns;

import com.google.common.collect.Multimap;
import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.WeaponFireEvent;
import com.modularwarfare.api.WeaponHitEvent;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.anim.AnimStateMachine;
import com.modularwarfare.client.handler.ClientTickHandler;
import com.modularwarfare.client.model.renders.RenderParameters;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import com.modularwarfare.common.entity.decals.EntityDecal;
import com.modularwarfare.common.entity.decals.EntityShell;
import com.modularwarfare.common.entity.grenades.EntityGrenade;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.hitbox.hits.BulletHit;
import com.modularwarfare.common.hitbox.hits.PlayerHit;
import com.modularwarfare.common.hitbox.maths.EnumHitboxType;
import com.modularwarfare.common.network.*;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.utility.ModularDamageSource;
import com.modularwarfare.utility.RayUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class ItemGun extends BaseItem {

    public static final Function<GunType, ItemGun> factory = type -> {
        return new ItemGun((type));
    };
    protected static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("99999999-4180-4865-B01B-BCCE9785ACA3");
    public static boolean canDryFire = true;
    public static boolean fireButtonHeld = false;
    public static boolean lastFireButtonHeld = false;
    public GunType type;

    public ItemGun(GunType type) {
        super(type);
        this.type = type;
        this.setNoRepair();
    }

    /**
     * If the player is on a shoot cooldown
     *
     * @return shoot cooldown
     */
    public static boolean isOnShootCooldown(UUID uuid) {
        return ClientTickHandler.playerShootCooldown.containsKey(uuid);
    }

    /**
     * If the player is on a reload cooldown
     *
     * @param entityPlayer
     * @return reload cooldown
     */
    public static boolean isClientReloading(EntityPlayer entityPlayer) {
        return ClientTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID());
    }

    /**
     * If the player is on a reload cooldown
     *
     * @param entityPlayer
     * @return reload cooldown
     */
    public static boolean isServerReloading(EntityPlayer entityPlayer) {
        return ServerTickHandler.playerReloadCooldown.containsKey(entityPlayer.getUniqueID());
    }

    public static boolean hasAmmoLoaded(ItemStack gunStack) {
        return !gunStack.isEmpty() ? !(gunStack.getItem() instanceof ItemAir) ? gunStack.hasTagCompound() ? gunStack.getTagCompound().hasKey("ammo") ? gunStack.getTagCompound().getTag("ammo") != null : false : false : false : false;
    }

    public static int getMagazineBullets(ItemStack gunStack) {
        if (hasAmmoLoaded(gunStack)) {
            ItemStack ammoStack = new ItemStack(gunStack.getTagCompound().getCompoundTag("ammo"));
            if (ammoStack.getItem() instanceof ItemAmmo) {
                ItemAmmo itemAmmo = (ItemAmmo) ammoStack.getItem();
                if (ammoStack.getTagCompound() != null) {
                    String key = itemAmmo.type.magazineCount > 1 ? "ammocount" + ammoStack.getTagCompound().getInteger("magcount") : "ammocount";
                    int ammoCount = ammoStack.getTagCompound().getInteger(key);
                    return ammoCount;
                }
            }
        }
        return 0;
    }

    public static boolean hasNextShot(ItemStack gunStack) {
        if (hasAmmoLoaded(gunStack)) {
            ItemStack ammoStack = new ItemStack(gunStack.getTagCompound().getCompoundTag("ammo"));
            if (ammoStack != null) {
                if (ammoStack.getItem() instanceof ItemAmmo) {
                    ItemAmmo itemAmmo = (ItemAmmo) ammoStack.getItem();
                    if (ammoStack.getTagCompound() != null) {
                        String key = itemAmmo.type.magazineCount > 1 ? "ammocount" + ammoStack.getTagCompound().getInteger("magcount") : "ammocount";
                        int ammoCount = ammoStack.getTagCompound().getInteger(key) - 1;
                        return ammoCount >= 0;
                    }
                }
            }
        } else if (gunStack.getTagCompound() != null && gunStack.getTagCompound().hasKey("ammocount")) {
            return gunStack.getTagCompound().getInteger("ammocount") > 0;
        }
        return false;
    }

    public static void consumeShot(ItemStack gunStack) {
        if (hasAmmoLoaded(gunStack)) {
            ItemStack ammoStack = new ItemStack(gunStack.getTagCompound().getCompoundTag("ammo"));
            ItemAmmo itemAmmo = (ItemAmmo) ammoStack.getItem();
            if (ammoStack.getTagCompound() != null) {
                NBTTagCompound nbtTagCompound = ammoStack.getTagCompound();
                String key = itemAmmo.type.magazineCount > 1 ? "ammocount" + nbtTagCompound.getInteger("magcount") : "ammocount";
                nbtTagCompound.setInteger(key, nbtTagCompound.getInteger(key) - 1);
                gunStack.getTagCompound().setTag("ammo", ammoStack.writeToNBT(new NBTTagCompound()));
            }
        } else if (gunStack.getTagCompound() != null && gunStack.getTagCompound().hasKey("ammocount")) {
            int ammoCount = gunStack.getTagCompound().getInteger("ammocount");
            gunStack.getTagCompound().setInteger("ammocount", ammoCount - 1);
        }
    }

    public static ItemBullet getUsedBullet(ItemStack gunStack, GunType gunType) {
        if (gunType.acceptedAmmo != null)
            return ItemAmmo.getUsedBullet(gunStack);
        else if (gunType.acceptedBullets != null) {
            if (gunStack.hasTagCompound() && gunStack.getTagCompound().hasKey("bullet")) {
                ItemStack usedBullet = new ItemStack(gunStack.getTagCompound().getCompoundTag("bullet"));
                ItemBullet usedBulletItem = (ItemBullet) usedBullet.getItem();
                return usedBulletItem;
            }
        }
        return null;
    }

    public static boolean isIndoors(final EntityLivingBase givenEntity) {
        final BlockPos blockPos = givenEntity.world.getPrecipitationHeight(givenEntity.getPosition());
        if (blockPos != null) {
            if (blockPos.getY() > givenEntity.posY) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void setType(BaseType type) {
        this.type = (GunType) type;
    }

    @Override
    public void onUpdate(ItemStack unused, World world, Entity holdingEntity, int intI, boolean flag) {
        if (holdingEntity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) holdingEntity;

            if (entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
                ItemStack heldStack = entityPlayer.getHeldItemMainhand();
                ItemGun itemGun = (ItemGun) heldStack.getItem();
                GunType gunType = itemGun.type;

                if (world.isRemote)
                    onUpdateClient(entityPlayer, world, heldStack, itemGun, gunType);
                else
                    onUpdateServer(entityPlayer, world, heldStack, itemGun, gunType);

                if (heldStack.getTagCompound() == null) {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    nbtTagCompound.setString("firemode", gunType.fireModes[0].name().toLowerCase());
                    nbtTagCompound.setInteger("skinId", 0);
                    nbtTagCompound.setBoolean("punched", gunType.isEnergyGun);
                    heldStack.setTagCompound(nbtTagCompound);
                }
            }
        }
    }

    public void onUpdateClient(EntityPlayer entityPlayer, World world, ItemStack heldStack, ItemGun itemGun, GunType gunType) {
        if (RenderParameters.switchDelay > 0) {
            RenderParameters.switchDelay--;
        }
        if (entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun && RenderParameters.switchDelay == 0 && RenderParameters.collideFrontDistance <= 0.2f) {
            if (fireButtonHeld && Minecraft.getMinecraft().inGameHasFocus && gunType.getFireMode(heldStack) == WeaponFireMode.FULL) {
                fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
            } else if (fireButtonHeld & !lastFireButtonHeld && Minecraft.getMinecraft().inGameHasFocus && gunType.getFireMode(heldStack) == WeaponFireMode.SEMI) {
                fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
            } else if (gunType.getFireMode(heldStack) == WeaponFireMode.BURST) {
                NBTTagCompound tagCompound = heldStack.getTagCompound();
                boolean canFire = true;
                if (tagCompound.hasKey("shotsremaining") && tagCompound.getInteger("shotsremaining") > 0) {
                    fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
                    canFire = false;
                } else if (fireButtonHeld & !lastFireButtonHeld && Minecraft.getMinecraft().inGameHasFocus && canFire) {
                    fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
                }
            }
            lastFireButtonHeld = fireButtonHeld;
        }
    }

    public void onUpdateServer(EntityPlayer entityPlayer, World world, ItemStack heldStack, ItemGun itemGun, GunType gunType) {

    }

    public void fireClient(EntityPlayer entityPlayer, World world, ItemStack gunStack, ItemGun itemGun, WeaponFireMode fireMode) {
        GunType gunType = itemGun.type;

        // Can fire checks
        if (isOnShootCooldown(entityPlayer.getUniqueID()) || isClientReloading(entityPlayer) || ClientRenderHooks.getAnimMachine(entityPlayer).attachmentMode || (!type.allowSprintFiring && entityPlayer.isSprinting()) || !itemGun.type.hasFireMode(fireMode))
            return;

        int shotCount = fireMode == WeaponFireMode.BURST ? gunStack.getTagCompound().getInteger("shotsremaining") > 0 ? gunStack.getTagCompound().getInteger("shotsremaining") : gunType.numBurstRounds : 1;

        // Weapon pre fire event
        WeaponFireEvent.PreClient preFireEvent = new WeaponFireEvent.PreClient(entityPlayer, gunStack, itemGun, gunType.weaponMaxRange);
        MinecraftForge.EVENT_BUS.post(preFireEvent);
        if (preFireEvent.isCanceled())
            return;

        if (preFireEvent.getResult() == Event.Result.DEFAULT || preFireEvent.getResult() == Event.Result.ALLOW) {
            if (!hasNextShot(gunStack)) {
                if (canDryFire) {
                    gunType.playClientSound(entityPlayer, WeaponSoundType.DryFire);
                    gunType.playClientSound(entityPlayer, WeaponSoundType.FireLast);
                    canDryFire = false;
                }
                if (fireMode == WeaponFireMode.BURST) gunStack.getTagCompound().setInteger("shotsremaining", 0);
                return;
            }
        }

        if (gunStack.equals(entityPlayer.getHeldItem(EnumHand.MAIN_HAND))) {
            ModularWarfare.NETWORK.sendToServer(new PacketGunFire(gunType.internalName, gunType.fireTickDelay, gunType.recoilPitch, gunType.recoilYaw, gunType.recoilAimReducer, gunType.bulletSpread, entityPlayer.rotationPitch, entityPlayer.rotationYaw));
            consumeShot(gunStack);
        }

        ModularWarfare.PROXY.onShootAnimation(entityPlayer, gunType.internalName, gunType.fireTickDelay, type.recoilPitch, type.recoilYaw);

        canDryFire = true;

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


        if(ModConfig.INSTANCE.dropBulletCasing) {
            /**
             * Drop casing
             */
            int numBullets = gunType.numBullets;
            ItemBullet bulletItem = getUsedBullet(gunStack, gunType);
            if (bulletItem != null) {
                if (bulletItem.type.isSlug) {
                    numBullets = 1;
                }
            }

            EntityShell shell = new EntityShell(world, entityPlayer, itemGun, bulletItem);

            shell.setHeadingFromThrower(entityPlayer, entityPlayer.rotationPitch, entityPlayer.rotationYaw + 110, 0.0F, 0.2F, 5);
            world.spawnEntity(shell);
        }
    }

    public void fireServer(EntityPlayer entityPlayer, float rotationPitch, float rotationYaw, World world, ItemStack gunStack, ItemGun itemGun, WeaponFireMode fireMode, final int clientFireTickDelay, final float recoilPitch, final float recoilYaw, final float recoilAimReducer, final float bulletSpread) {
        GunType gunType = itemGun.type;

        // Can fire checks
        if (isValidShoot(clientFireTickDelay, recoilPitch, recoilYaw, recoilAimReducer, bulletSpread, type)) {

            if (isServerReloading(entityPlayer) || (!type.allowSprintFiring && entityPlayer.isSprinting()) || !itemGun.type.hasFireMode(fireMode))
                return;

            // Weapon pre fire event
            WeaponFireEvent.PreServer preFireEvent = new WeaponFireEvent.PreServer(entityPlayer, gunStack, itemGun, gunType.weaponMaxRange);
            MinecraftForge.EVENT_BUS.post(preFireEvent);
            if (preFireEvent.isCanceled())
                return;
            int shotCount = fireMode == WeaponFireMode.BURST ? gunStack.getTagCompound().getInteger("shotsremaining") > 0 ? gunStack.getTagCompound().getInteger("shotsremaining") : gunType.numBurstRounds : 1;

            if (preFireEvent.getResult() == Event.Result.DEFAULT || preFireEvent.getResult() == Event.Result.ALLOW) {
                if (!hasNextShot(gunStack)) {
                    if (canDryFire) {
                        gunType.playSound(entityPlayer, WeaponSoundType.DryFire, gunStack);
                        canDryFire = false;
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

            ItemBullet bulletItem = getUsedBullet(gunStack, gunType);
            if (bulletItem != null) {
                if (bulletItem.type.isSlug) {
                    numBullets = 1;
                }
            }

            ArrayList<BulletHit> rayTraceList = new ArrayList<BulletHit>();
            for (int i = 0; i < numBullets; i++) {
                BulletHit rayTrace = RayUtil.standardEntityRayTrace(world, rotationPitch, rotationYaw, entityPlayer, preFireEvent.getWeaponRange(), itemGun, GunType.isPackAPunched(gunStack));
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

                                    ModularWarfare.NETWORK.sendTo(new PacketPlayerHit(), (EntityPlayerMP) victim);
                                }
                            }
                        }
                    }
                } else {
                    if (!world.isRemote) {
                        if (rayTrace.rayTraceResult != null) {
                            if (rayTrace.rayTraceResult.entityHit instanceof EntityGrenade) {
                                ((EntityGrenade)rayTrace.rayTraceResult.entityHit).explode();
                            }
                            if (rayTrace.rayTraceResult.entityHit instanceof EntityLivingBase) {
                                final EntityLivingBase victim = (EntityLivingBase) ((BulletHit) rayTrace).rayTraceResult.entityHit;
                                if (victim != null) {
                                    entities.add(victim);
                                    gunType.playSoundPos(victim.getPosition(), world, WeaponSoundType.Penetration);
                                    headshot = canEntityGetHeadshot(victim) && rayTrace.rayTraceResult.hitVec.y >= victim.getPosition().getY() + victim.getEyeHeight() - 0.15f;
                                    if (entityPlayer instanceof EntityPlayerMP) {
                                        ModularWarfare.NETWORK.sendTo(new PacketPlayHitmarker(headshot), (EntityPlayerMP) entityPlayer);
                                    }
                                }
                            } else if (rayTrace.rayTraceResult.hitVec != null) {
                                BlockPos blockPos = rayTrace.rayTraceResult.getBlockPos();
                                playImpactSound(world, blockPos, gunType);
                                gunType.playSoundPos(blockPos, world, WeaponSoundType.Crack, entityPlayer, 1.0f);
                                doHit(rayTrace.rayTraceResult, entityPlayer);
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

                            if (!ModConfig.INSTANCE.applyKnockback) {
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
                consumeShot(gunStack);
            }

            //Hands upwards when shooting
            if (ServerTickHandler.playerAimShootCooldown.get(entityPlayer.getName()) == null) {
                ModularWarfare.NETWORK.sendToAll(new PacketAimingReponse(entityPlayer.getName(), true));
            }
            ServerTickHandler.playerAimShootCooldown.put(entityPlayer.getName(), 60);
        } else {
            if (ModConfig.INSTANCE.kickIfModifiedContentPack) {
                ((EntityPlayerMP) entityPlayer).connection.disconnect(new TextComponentString("[ModularWarfare] Kicked for client-side modified content-pack. (Bad RPM/Recoil for the gun: " + itemGun.type.internalName + ") [RPM should be: " + itemGun.type.roundsPerMin + "]"));
            }


        }
    }

    public boolean isValidShoot(final long clientFireTickDelay, final float recoilPitch, final float recoilYaw, final float recoilAimReducer, final float bulletSpread, GunType type) {
        return (clientFireTickDelay == type.fireTickDelay) && (type.recoilPitch == recoilPitch) && (type.recoilYaw == recoilYaw) && (type.recoilAimReducer == recoilAimReducer) && (type.bulletSpread == bulletSpread);
    }

    public void playImpactSound(World world, BlockPos pos, GunType gunType) {
        if (world.getBlockState(pos).getMaterial() == Material.ROCK) {
            gunType.playSoundPos(pos, world, WeaponSoundType.ImpactStone, null, 1f);
        } else if (world.getBlockState(pos).getMaterial() == Material.GRASS || world.getBlockState(pos).getMaterial() == Material.GROUND || world.getBlockState(pos).getMaterial() == Material.SAND) {
            gunType.playSoundPos(pos, world, WeaponSoundType.ImpactDirt, null, 1f);
        } else if (world.getBlockState(pos).getMaterial() == Material.WOOD) {
            gunType.playSoundPos(pos, world, WeaponSoundType.ImpactWood, null, 1f);
        } else if (world.getBlockState(pos).getMaterial() == Material.GLASS) {
            gunType.playSoundPos(pos, world, WeaponSoundType.ImpactGlass, null, 1f);
        } else if (world.getBlockState(pos).getMaterial() == Material.WATER) {
            gunType.playSoundPos(pos, world, WeaponSoundType.ImpactWater, null, 1f);
        } else if (world.getBlockState(pos).getMaterial() == Material.IRON) {
            gunType.playSoundPos(pos, world, WeaponSoundType.ImpactMetal, null, 1f);
        } else {
            gunType.playSoundPos(pos, world, WeaponSoundType.ImpactDirt, null, 1f);
        }
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "MovementSpeed", type.moveSpeedModifier - 1.0f, 2));
        }
        return multimap;
    }

    public void doHit(RayTraceResult raytraceResultIn, EntityPlayer shooter) {
        if (raytraceResultIn.getBlockPos() != null) {
            BlockPos pos = raytraceResultIn.getBlockPos();

            EntityDecal.EnumDecalSide side = EntityDecal.EnumDecalSide.ALL;
            boolean shouldRender = false;
            double hitX = raytraceResultIn.hitVec.x;
            double hitY = raytraceResultIn.hitVec.y;
            double hitZ = raytraceResultIn.hitVec.z;
            double milieuX = (double) pos.getX() + 0.5D;
            double milieuY = (double) pos.getY() + 0.5D;
            double milieuZ = (double) pos.getZ() + 0.5D;
            double differenceX = hitX - milieuX;
            double differenceY = hitY - milieuY;
            double differenceZ = hitZ - milieuZ;
            if (differenceX == 0.0D) {
                if (shooter.posX < hitX) {
                    hitX -= 0.5D;
                    differenceX -= 0.5D;
                } else {
                    hitX += 0.5D;
                    differenceX += 0.5D;
                }
            }

            if (differenceY == 0.0D) {
                hitY += 0.5D;
                differenceY += 0.5D;
            }

            if (differenceZ == 0.0D) {
                if (shooter.posZ < hitZ) {
                    hitZ -= 0.5D;
                    differenceZ -= 0.5D;
                } else {
                    hitZ += 0.5D;
                    differenceZ += 0.5D;
                }
            }

            if (differenceX == -0.5D) {
                side = EntityDecal.EnumDecalSide.EAST;
                shouldRender = true;
            }

            if (differenceX == 0.5D) {
                side = EntityDecal.EnumDecalSide.WEST;
                shouldRender = true;
            }

            if (differenceZ == -0.5D) {
                side = EntityDecal.EnumDecalSide.SOUTH;
                shouldRender = true;
            }

            if (differenceZ == 0.5D) {
                side = EntityDecal.EnumDecalSide.NORTH;
                shouldRender = true;
            }

            if (differenceY == 0.5D) {
                side = EntityDecal.EnumDecalSide.FLOOR;
                shouldRender = true;
            }

            if (shouldRender) {
                ModularWarfare.NETWORK.sendToAll(new PacketDecal(0, side, hitX, hitY + 0.095D, hitZ, false));
            }
        }
    }

    private boolean canEntityGetHeadshot(Entity e) {
        return e instanceof EntityZombie || e instanceof EntitySkeleton || e instanceof EntityCreeper || e instanceof EntityWitch || e instanceof EntityPigZombie || e instanceof EntityEnderman || e instanceof EntityWitherSkeleton || e instanceof EntityPlayer || e instanceof EntityVillager || e instanceof EntityEvoker || e instanceof EntityStray || e instanceof EntityVindicator || e instanceof EntityIronGolem || e instanceof EntitySnowman || e.getName().contains("common");
    }

    public void onGunSwitchMode(EntityPlayer entityPlayer, World world, ItemStack gunStack, ItemGun itemGun, WeaponFireMode fireMode) {
        GunType.setFireMode(gunStack, fireMode);

        GunType gunType = itemGun.type;
        if (WeaponSoundType.ModeSwitch != null) {
            gunType.playSound(entityPlayer, WeaponSoundType.ModeSwitch, gunStack);
        }
    }

    /**
     * Minecraft Overrides
     */
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        GunType gunType = ((ItemGun) stack.getItem()).type;

        if (gunType == null)
            return;


        if (hasAmmoLoaded(stack)) {
            ItemStack ammoStack = new ItemStack(stack.getTagCompound().getCompoundTag("ammo"));
            if (ammoStack.getItem() instanceof ItemAmmo) {
                ItemAmmo itemAmmo = (ItemAmmo) ammoStack.getItem();

                if (itemAmmo.type.magazineCount == 1) {
                    int currentAmmoCount = 0;
                    if (ammoStack.getTagCompound() != null) {
                        NBTTagCompound tag = ammoStack.getTagCompound();
                        currentAmmoCount = tag.hasKey("ammocount") ? tag.getInteger("ammocount") : 0;
                    }

                    tooltip.add(generateLoreLineAlt("Ammo", Integer.toString(currentAmmoCount), Integer.toString(itemAmmo.type.ammoCapacity)));
                } else {
                    if (stack.getTagCompound() != null) {
                        if (gunType.acceptedBullets != null) {
                            int ammoCount = stack.getTagCompound().hasKey("ammocount") ? stack.getTagCompound().getInteger("ammocount") : 0;
                            tooltip.add(generateLoreLineAlt("Ammo", Integer.toString(ammoCount), Integer.toString(gunType.internalAmmoStorage)));
                        }

                        String baseDisplayLine = "Ammo %s: %g%s%dg/%g%s";
                        baseDisplayLine = baseDisplayLine.replaceAll("%b", TextFormatting.BLUE.toString());
                        baseDisplayLine = baseDisplayLine.replaceAll("%dg", TextFormatting.DARK_GRAY.toString());

                        for (int i = 1; i < itemAmmo.type.magazineCount + 1; i++) {
                            NBTTagCompound tag = ammoStack.getTagCompound();
                            String displayLine = baseDisplayLine.replaceAll("%g", i == tag.getInteger("magcount") ? TextFormatting.YELLOW.toString() : TextFormatting.GRAY.toString());
                            tooltip.add(String.format(displayLine, i, tag.getInteger("ammocount" + i), itemAmmo.type.ammoCapacity));
                        }
                    }
                }
            }
        }

        if (stack.getTagCompound() != null) {
            if (gunType.acceptedBullets != null) {
                int ammoCount = stack.getTagCompound().hasKey("ammocount") ? stack.getTagCompound().getInteger("ammocount") : 0;
                tooltip.add(generateLoreLineAlt("Ammo", Integer.toString(ammoCount), Integer.toString(gunType.internalAmmoStorage)));
            }
        }

        if (ItemAmmo.getUsedBullet(stack) != null) {
            ItemBullet itemBullet = ItemAmmo.getUsedBullet(stack);
            tooltip.add(generateLoreLine("Bullet", itemBullet.type.displayName));
        }

        String baseDisplayLine = "%bFire Mode: %g%s";
        baseDisplayLine = baseDisplayLine.replaceAll("%b", TextFormatting.BLUE.toString());
        baseDisplayLine = baseDisplayLine.replaceAll("%g", TextFormatting.GRAY.toString());
        tooltip.add(String.format(baseDisplayLine, GunType.getFireMode(stack) != null ? GunType.getFireMode(stack) : gunType.fireModes[0]));


        if (GuiScreen.isShiftKeyDown()) {
            DecimalFormat decimalFormat = new DecimalFormat("#.#");

            String damageLine = "%bDamage: %g%s";
            damageLine = damageLine.replaceAll("%b", TextFormatting.BLUE.toString());
            damageLine = damageLine.replaceAll("%g", TextFormatting.RED.toString());
            if (gunType.numBullets > 1) {
                tooltip.add(String.format(damageLine, gunType.gunDamage + " x " + gunType.numBullets));
            } else {
                tooltip.add(String.format(damageLine, gunType.gunDamage));
            }


            String accuracyLine = "%bAccuracy: %g%s";
            accuracyLine = accuracyLine.replaceAll("%b", TextFormatting.BLUE.toString());
            accuracyLine = accuracyLine.replaceAll("%g", TextFormatting.RED.toString());

            tooltip.add(String.format(accuracyLine, decimalFormat.format((1 / gunType.bulletSpread) * 100) + "%"));

            if (gunType.acceptedAttachments != null) {
                if (!gunType.acceptedAttachments.isEmpty()) {
                    tooltip.add("" + TextFormatting.BLUE.toString() + "Accepted attachments:");
                    for (ArrayList<String> strings : gunType.acceptedAttachments.values()) {
                        for (int i = 0; i < strings.size(); i++) {
                            try {
                                final String attachment = ModularWarfare.attachmentTypes.get(strings.get(i)).type.displayName;
                                if (attachment != null) {
                                    tooltip.add("- " + attachment);
                                }
                            } catch (NullPointerException error) {
                            }
                        }
                    }
                }
            }

            if (gunType.acceptedAmmo != null) {
                tooltip.add("" + TextFormatting.BLUE.toString() + "Accepted mags:");
                if (gunType.acceptedAmmo.length > 0) {
                    for (String internalName : gunType.acceptedAmmo) {
                        if (ModularWarfare.ammoTypes.containsKey(internalName)) {
                            final String magName = ModularWarfare.ammoTypes.get(internalName).type.displayName;
                            if (magName != null) {
                                tooltip.add("- " + magName);
                            }
                        }
                    }
                }
            }

            if (gunType.acceptedBullets != null) {
                tooltip.add("" + TextFormatting.BLUE.toString() + "Accepted bullets:");

                if (gunType.acceptedBullets.length > 0) {
                    for (String internalName : gunType.acceptedBullets) {
                        if (ModularWarfare.bulletTypes.containsKey(internalName)) {
                            final String magName = ModularWarfare.bulletTypes.get(internalName).type.displayName;
                            if (magName != null) {
                                tooltip.add("- " + magName);
                            }
                        }
                    }
                }
            }
        } else {
            tooltip.add("\u00a7e" + "[Shift]");
        }
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack p_77626_1_) {
        return 0;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack p_77661_1_) {
        return EnumAction.NONE;
    }

    @Override
    public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack) {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        boolean result = !oldStack.equals(newStack);
        if (result) {
            // TODO: Requip animation
        }
        return result;
    }

    @Override
    public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
        World world = player.world;
        if (!world.isRemote) {
            // Client will still render block break if player is in creative so update block state
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
        return true;
    }


    public boolean doesSneakBypassUse(ItemStack stack, net.minecraft.world.IBlockAccess world, BlockPos pos, EntityPlayer player) {
        return false;
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canItemEditBlocks() {
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return true;
    }
}