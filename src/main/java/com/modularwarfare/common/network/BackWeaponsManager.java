package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class BackWeaponsManager implements INBTSerializable<NBTTagCompound> {

    public static BackWeaponsManager INSTANCE = new BackWeaponsManager();

    private final HashMap<String, ItemStack> WEAPONS = new HashMap<>();

    public BackWeaponsManager collect() {
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            this.WEAPONS.put(player.getName(), getItemInBack(player));
        }
        return this;
    }

    public BackWeaponsManager sync() {
        ModularWarfare.NETWORK.sendToAll(new PacketSyncBackWeapons());
        return this;
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        WEAPONS.forEach((playerName, itemStack) -> {
            nbt.setTag(playerName, itemStack.serializeNBT());
        });
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.WEAPONS.clear();
        for (String s : nbt.getKeySet()) {
            NBTBase base = nbt.getTag(s);
            if (base instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) base;
                ItemStack stack = new ItemStack(compound);
                this.WEAPONS.put(s, stack);
            }
        }

    }

    private ItemStack getItemInBack(EntityPlayerMP mp) {
        ItemStack stack = ItemStack.EMPTY;
        ArrayList<ItemStack> stacks = getItemsInBack(mp);
        if (stacks.size() > 0) {
            stack = stacks.get(0);
        }
        return stack;
    }

    private ArrayList<ItemStack> getItemsInBack(EntityPlayerMP mp) {
        ArrayList<ItemStack> guns = new ArrayList<ItemStack>();
        for (int i = 0; i <= mp.inventory.getSizeInventory(); i++) {
            if (guns.size() < 2) {
                if (mp.inventory.getStackInSlot(i) != null && mp.inventory.getStackInSlot(i).getItem() instanceof ItemGun && mp.inventory.getStackInSlot(i) != mp.getHeldItemMainhand()) {
                    guns.add(mp.inventory.getStackInSlot(i));
                }
            }
        }
        return guns;
    }

    public ItemStack getItemToRender(AbstractClientPlayer player) {
        return this.WEAPONS.getOrDefault(player.getName(), ItemStack.EMPTY);
    }
}