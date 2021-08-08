package com.modularwarfare.common.hitbox.playerdata;

import com.modularwarfare.ModularWarfare;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;

public class PlayerDataHandler {

    public static Map<String, PlayerData> serverSideData = new HashMap<>();

    public static PlayerData getPlayerData(EntityPlayer player) {
        if (player == null)
            return null;
        return getPlayerData(player.getName(), player.world.isRemote ? Side.CLIENT : Side.SERVER);
    }

    public static PlayerData getPlayerData(String username) {
        return getPlayerData(username, Side.SERVER);
    }

    public static PlayerData getPlayerData(EntityPlayer player, Side side) {
        if (player == null)
            return null;
        return getPlayerData(player.getName(), side);
    }

    public static PlayerData getPlayerData(String username, Side side) {
        if (!serverSideData.containsKey(username))
            serverSideData.put(username, new PlayerData(username));
        return serverSideData.get(username);
    }

    public void serverTick() {
        if (FMLCommonHandler.instance().getMinecraftServerInstance() == null) {
            ModularWarfare.LOGGER.warn("Receiving server ticks when server is null");
            return;
        }
        for (WorldServer world : FMLCommonHandler.instance().getMinecraftServerInstance().worlds) {
            for (Object player : world.playerEntities) {
                getPlayerData((EntityPlayer) player).tick((EntityPlayer) player);
            }
        }
    }
}
