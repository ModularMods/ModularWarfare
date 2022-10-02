package com.modularwarfare.api;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public class ItemRegisterEvent extends Event {

    public IForgeRegistry<Item> registry;
    public List<Item> tabOrder;

    public ItemRegisterEvent(IForgeRegistry<Item> registry, List<Item> tabOrder) {
        this.registry = registry;
        this.tabOrder = tabOrder;
    }
}
