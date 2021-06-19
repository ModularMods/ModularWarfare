package com.modularwarfare.common.hitbox.playerdata;

import com.modularwarfare.common.hitbox.PlayerSnapshot;
import net.minecraft.entity.player.EntityPlayer;

public class PlayerData {
    /**
     * Their username
     */
    public String username;

    /**
     * Snapshots for bullet hit detection. Array size is set to number of snapshots required. When a new one is taken,
     * each snapshot is moved along one place and new one is added at the start, so that when the array fills up, the oldest one is lost
     */
    public PlayerSnapshot[] snapshots;

    public PlayerData(String name) {
        username = name;
        snapshots = new PlayerSnapshot[20];
    }

    public void tick(EntityPlayer player) {
        System.arraycopy(snapshots, 0, snapshots, 1, snapshots.length - 2 + 1);
        //Take new snapshot
        snapshots[0] = new PlayerSnapshot(player);
    }
}
