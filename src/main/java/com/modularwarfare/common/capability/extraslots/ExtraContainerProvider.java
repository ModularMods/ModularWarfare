package com.modularwarfare.common.capability.extraslots;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ExtraContainerProvider implements INBTSerializable<NBTTagCompound>, ICapabilityProvider {
    private final ExtraContainer container;

    public ExtraContainerProvider(final ExtraContainer container) {
        this.container = container;
    }

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityExtra.CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityExtra.CAPABILITY) {
            return (T) this.container;
        }

        return null;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.container.serializeNBT();
    }

    @Override
    public void deserializeNBT(final NBTTagCompound nbt) {
        this.container.deserializeNBT(nbt);
    }
}
