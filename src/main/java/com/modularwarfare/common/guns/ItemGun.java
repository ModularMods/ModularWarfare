package com.modularwarfare.common.guns;

import com.google.common.collect.Multimap;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.handler.ClientTickHandler;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.common.entity.decals.EntityDecal;
import com.modularwarfare.common.guns.manager.ShotManager;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.network.*;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
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
        if (entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun && RenderParameters.GUN_CHANGE_Y == 0F && RenderParameters.collideFrontDistance <= 0.2f) {
            if (fireButtonHeld && Minecraft.getMinecraft().inGameHasFocus && gunType.getFireMode(heldStack) == WeaponFireMode.FULL) {
                ShotManager.fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
            } else if (fireButtonHeld & !lastFireButtonHeld && Minecraft.getMinecraft().inGameHasFocus && gunType.getFireMode(heldStack) == WeaponFireMode.SEMI) {
                ShotManager.fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
            } else if (gunType.getFireMode(heldStack) == WeaponFireMode.BURST) {
                NBTTagCompound tagCompound = heldStack.getTagCompound();
                boolean canFire = true;
                if (tagCompound.hasKey("shotsremaining") && tagCompound.getInteger("shotsremaining") > 0) {
                    ShotManager.fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
                    canFire = false;
                } else if (fireButtonHeld & !lastFireButtonHeld && Minecraft.getMinecraft().inGameHasFocus && canFire) {
                    ShotManager.fireClient(entityPlayer, world, heldStack, itemGun, gunType.getFireMode(heldStack));
                }
            }
            lastFireButtonHeld = fireButtonHeld;
            if(!fireButtonHeld) {
                ShotManager.defemptyclickLock=true;
            }
        }
    }

    public void onUpdateServer(EntityPlayer entityPlayer, World world, ItemStack heldStack, ItemGun itemGun, GunType gunType) {

    }

    public static void playImpactSound(World world, BlockPos pos, GunType gunType) {
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

    public static void doHit(double posX, double posY, double posZ,EnumFacing facing, EntityPlayer shooter) {
        doHit(new RayTraceResult(RayTraceResult.Type.BLOCK, new Vec3d(posX, posY, posZ), facing, new BlockPos(posX, posY, posZ)), shooter);
    }

    public static void doHit(RayTraceResult raytraceResultIn, EntityPlayer shooter) {
        if (raytraceResultIn.getBlockPos() != null) {
            BlockPos pos = raytraceResultIn.getBlockPos();

            EntityDecal.EnumDecalSide side = EntityDecal.EnumDecalSide.ALL;
            boolean shouldRender = true;
            double hitX = raytraceResultIn.hitVec.x;
            double hitY = raytraceResultIn.hitVec.y;
            double hitZ = raytraceResultIn.hitVec.z;
            switch (raytraceResultIn.sideHit) {
                case UP:
                    side= EntityDecal.EnumDecalSide.FLOOR;
                    break;
                case DOWN:
                    side= EntityDecal.EnumDecalSide.CEILING;
                    break;
                case EAST:
                    side= EntityDecal.EnumDecalSide.WEST;
                    break;
                case WEST:
                    side= EntityDecal.EnumDecalSide.EAST;
                    break;
                case SOUTH:
                    side= EntityDecal.EnumDecalSide.NORTH;
                    break;
                case NORTH:
                    side= EntityDecal.EnumDecalSide.SOUTH;
                    break;
                default:
                    shouldRender=false;
                    break;
            }
            if (shouldRender) {
                ModularWarfare.NETWORK.sendToAll(new PacketDecal(0, side, hitX, hitY + 0.095D, hitZ, false));
            }
        }
    }


    public static boolean canEntityGetHeadshot(Entity e) {
        return e instanceof EntityZombie || e instanceof EntitySkeleton || e instanceof EntityCreeper || e instanceof EntityWitch || e instanceof EntityPigZombie || e instanceof EntityEnderman || e instanceof EntityWitherSkeleton || e instanceof EntityPlayer || e instanceof EntityVillager || e instanceof EntityEvoker || e instanceof EntityStray || e instanceof EntityVindicator || e instanceof EntityIronGolem || e instanceof EntitySnowman || e.getName().contains("common");
    }

    public void onGunSwitchMode(EntityPlayer entityPlayer, World world, ItemStack gunStack, ItemGun itemGun, WeaponFireMode fireMode) {
        GunType.setFireMode(gunStack, fireMode);

        GunType gunType = itemGun.type;
        if (WeaponSoundType.ModeSwitch != null && !world.isRemote) {
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

            if(gunType.extraLore != null) {
                tooltip.add("" + TextFormatting.BLUE.toString() + "Lore:");
                tooltip.add(gunType.extraLore);
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