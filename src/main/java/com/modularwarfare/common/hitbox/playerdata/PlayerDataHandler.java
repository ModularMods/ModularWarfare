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
    public static Map<String, PlayerData> clientSideData = new HashMap<>();

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
        if (side.isClient()) {
            if (!clientSideData.containsKey(username))
                clientSideData.put(username, new PlayerData(username));
        } else {
            if (!serverSideData.containsKey(username))
                serverSideData.put(username, new PlayerData(username));
        }
        return side.isClient() ? clientSideData.get(username) : serverSideData.get(username);
    }

    public void clientTick() {
        if(Minecraft.getMinecraft().world != null)
        {
            for(Object player : Minecraft.getMinecraft().world.playerEntities)
            {
                getPlayerData((EntityPlayer)player).tick((EntityPlayer)player);
            }
        }
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
