package com.modularwarfare.common.handler;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.handler.data.DataGunReloadEnhancedTask;
import com.modularwarfare.common.network.BackWeaponsManager;
import com.modularwarfare.common.network.PacketAimingReponse;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerTickHandler extends ForgeEvent {

    public static ConcurrentHashMap<UUID, Integer> playerReloadCooldown = new ConcurrentHashMap<UUID, Integer>();
    public static ConcurrentHashMap<UUID, ItemStack> playerReloadItemStack = new ConcurrentHashMap<UUID, ItemStack>();

    public static ConcurrentHashMap<String, Integer> playerAimShootCooldown = new ConcurrentHashMap<String, Integer>();
    public static ConcurrentHashMap<String, Boolean> playerAimInstant = new ConcurrentHashMap<String, Boolean>();
    public static ConcurrentHashMap<UUID, DataGunReloadEnhancedTask> reloadEnhancedTask = new ConcurrentHashMap<UUID, DataGunReloadEnhancedTask>();

    int i = 0;

    private long lastBackWeaponsSync = -1;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        {
            long currentTime = System.currentTimeMillis();
            if (lastBackWeaponsSync == -1 || currentTime - this.lastBackWeaponsSync > 1000) {
                this.lastBackWeaponsSync = currentTime;
                //BackWeaponsManager.INSTANCE.collect().sync();
            }
        }
        switch (event.phase) {

            case START:
                ModularWarfare.NETWORK.handleServerPackets();
                // Player shoot aim cooldown
                for (String playername : playerAimShootCooldown.keySet()) {
                    i += 1;

                    int value = playerAimShootCooldown.get(playername) - 1;

                    if (value <= 0) {
                        playerAimShootCooldown.remove(playername);
                        ModularWarfare.NETWORK.sendToAll(new PacketAimingReponse(playername, false));
                    } else {
                        playerAimShootCooldown.replace(playername, value);
                    }

                }

                // Player reload cooldown
                for (UUID uuid : playerReloadCooldown.keySet()) {
                    i += 1;
                    int value = playerReloadCooldown.get(uuid) - 1;
                    if (value <= 0) {
                        playerReloadCooldown.remove(uuid);

                        playerReloadItemStack.get(uuid);
                    } else {
                        playerReloadCooldown.replace(uuid, value);
                    }
                }
                break;
            case END:
                ModularWarfare.PLAYERHANDLER.serverTick();
                break;
        }
    }

}
