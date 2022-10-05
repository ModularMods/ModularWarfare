package com.modularwarfare.api;

import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderHeldItemLayerEvent extends Event {

    public ItemStack stack;
    public LayerHeldItem layerHeldItem;
    public EntityLivingBase entitylivingbaseIn;
    public float partialTicks;

    public RenderHeldItemLayerEvent(ItemStack stack, LayerHeldItem layerHeldItem, EntityLivingBase entitylivingbaseIn, float partialTicks) {
        this.stack = stack;
        this.layerHeldItem = layerHeldItem;
        this.entitylivingbaseIn = entitylivingbaseIn;
        this.partialTicks = partialTicks;
    }

}
