package com.modularwarfare.api;

import java.util.ArrayList;

import com.modularwarfare.common.hitbox.PlayerHitbox;
import com.modularwarfare.common.vector.Vector3f;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerSnapshotCreateEvent extends Event {
    public static class Pre extends PlayerSnapshotCreateEvent {
        public final EntityPlayer player;
        public Vector3f pos;

        public Pre(EntityPlayer player, Vector3f pos) {
            this.player = player;
            this.pos = pos;
        }
    }

    public static class Post extends PlayerSnapshotCreateEvent {
        public final ArrayList<PlayerHitbox> hitboxes;
        public final EntityPlayer player;
        public Vector3f pos;

        public Post(EntityPlayer player, Vector3f pos, ArrayList<PlayerHitbox> hitboxes) {
            this.player = player;
            this.pos = pos;
            this.hitboxes = hitboxes;
        }
    }
}
