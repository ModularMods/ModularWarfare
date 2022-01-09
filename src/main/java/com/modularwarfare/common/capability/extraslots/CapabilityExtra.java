package com.modularwarfare.common.capability.extraslots;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.common.network.PacketSyncExtraSlot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

@Mod.EventBusSubscriber(modid = ModularWarfare.MOD_ID)
public class CapabilityExtra {

    @CapabilityInject(IExtraItemHandler.class)
    public static final Capability<IExtraItemHandler> CAPABILITY;

    static {
        CAPABILITY = null;
    }

    @SubscribeEvent
    public static void attachCapabilities(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(new ResourceLocation(ModularWarfare.MOD_ID, "extraslots"), new ExtraContainerProvider(new ExtraContainer((EntityPlayer) event.getObject())));
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(final EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        final EntityPlayer target = (EntityPlayer) event.getEntity();

        if (ModularWarfare.PROXY instanceof ClientProxy) {
            return;
        }
        sync(target, Collections.singletonList(target));
    }

    @SubscribeEvent
    public static void onStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getTarget().world.isRemote) {
            return;
        }
        if (!(event.getTarget() instanceof EntityPlayer)) {
            return;
        }
        if (ModularWarfare.PROXY instanceof ClientProxy) {
            return;
        }
        sync((EntityPlayer) event.getTarget(), Collections.singletonList(event.getEntityPlayer()));
    }

    @SubscribeEvent
    public static void playerDeath(final LivingDeathEvent event) {
        if(event.getEntity() instanceof EntityPlayer) {
            final EntityPlayer player = (EntityPlayer) event.getEntity();
            final World world = player.world;
            if (world.isRemote || world.getGameRules().getBoolean("keepInventory") || !ModConfig.INSTANCE.general.drop_extra_slots_on_death) {
                return;
            }

            for (int i = 0; i < ((IExtraItemHandler) player.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getSlots(); i++) {
                final ItemStack extra = ((IExtraItemHandler) player.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getStackInSlot(i);
                if (extra.isEmpty()) {
                    return;
                }
                final EntityItem item = new EntityItem(world, player.posX, player.posY + player.getEyeHeight(), player.posZ, extra);
                item.setPickupDelay(40);
                final float f1 = world.rand.nextFloat() * 0.5f;
                final float f2 = world.rand.nextFloat() * 3.1415927f * 2.0f;
                item.motionX = -MathHelper.sin(f2) * f1;
                item.motionZ = MathHelper.cos(f2) * f1;
                item.motionY = 0.20000000298023224;
                world.spawnEntity(item);
                ((IExtraItemHandler) player.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    public static void sync(final EntityPlayer entity, final Collection<? extends EntityPlayer> receivers) {
        if (entity.hasCapability(CapabilityExtra.CAPABILITY, null)) {
            for (int i = 0; i < ((IExtraItemHandler) entity.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getSlots(); i++) {
                final PacketSyncExtraSlot msg = new PacketSyncExtraSlot(entity, i, ((IExtraItemHandler) entity.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getStackInSlot(i));
                receivers.forEach(p -> {
                    ModularWarfare.NETWORK.sendTo(msg, (EntityPlayerMP) p);
                });
            }
        }
    }

    public static class Storage implements Capability.IStorage<IExtraItemHandler> {
        @Nullable
        public NBTBase writeNBT(final Capability<IExtraItemHandler> capability, final IExtraItemHandler instance, final EnumFacing side) {
            return null;
        }

        public void readNBT(final Capability<IExtraItemHandler> capability, final IExtraItemHandler instance, final EnumFacing side, final NBTBase nbt) {
        }
    }
}