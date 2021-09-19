package com.modularwarfare.common.hitbox;

import com.modularwarfare.api.AnimationUtils;
import com.modularwarfare.api.PlayerSnapshotCreateEvent;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.handler.ServerTickHandler;
import com.modularwarfare.common.hitbox.maths.EnumHitboxType;
import com.modularwarfare.common.hitbox.maths.RotatedAxes;
import com.modularwarfare.common.vector.Vector3f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;


/**
 * This class takes a snapshot of the player's position rotation and held items at a certain point in time.
 * It is used to handle bullet detection. The server will store a second or two of snapshots so that it
 * can work out where the player thought they were shooting accounting for packet lag
 */
public class PlayerSnapshot {
    /**
     * The player this snapshot is for
     */
    public EntityPlayer player;
    /**
     * The player's position at the point the snapshot was taken
     */
    public Vector3f pos;
    /**
     * The hitboxes for this player
     */
    public ArrayList<PlayerHitbox> hitboxes;
    /**
     * The time at which this snapshot was taken
     */
    public long time;

    public PlayerSnapshot(EntityPlayer p) {
        player = p;
        pos = new Vector3f(p.posX, p.posY, p.posZ);

        PlayerSnapshotCreateEvent.Pre event=new PlayerSnapshotCreateEvent.Pre(p, pos);
        MinecraftForge.EVENT_BUS.post(event);
        pos=event.pos;
        hitboxes = new ArrayList<>();

        RotatedAxes bodyAxes = new RotatedAxes(p.renderYawOffset, 0F, 0F);
        RotatedAxes headAxes = new RotatedAxes(p.rotationYawHead - p.renderYawOffset, p.rotationPitch, 0F);

        if (p.isSneaking()) {
            hitboxes.add(new PlayerHitbox(player, bodyAxes, new Vector3f(0F, 0F, 0F), new Vector3f(-0.25F, 0F, -0.15F), new Vector3f(0.5F, 1F, 0.3F), EnumHitboxType.BODY));
            hitboxes.add(new PlayerHitbox(player, bodyAxes.findLocalAxesGlobally(headAxes), new Vector3f(0.0F, 1F, 0F), new Vector3f(-0.25F, 0F, -0.25F), new Vector3f(0.5F, 0.5F, 0.5F), EnumHitboxType.HEAD));
        } else {
            hitboxes.add(new PlayerHitbox(player, bodyAxes, new Vector3f(0F, 0F, 0F), new Vector3f(-0.25F, 0F, -0.15F), new Vector3f(0.5F, 1.4F, 0.3F), EnumHitboxType.BODY));
            hitboxes.add(new PlayerHitbox(player, bodyAxes.findLocalAxesGlobally(headAxes), new Vector3f(0.0F, 1.4F, 0F), new Vector3f(-0.25F, 0F, -0.25F), new Vector3f(0.5F, 0.5F, 0.5F), EnumHitboxType.HEAD));
        }

        //Calculate rotation of arms using modified code from ModelBiped
        float yHead = (p.rotationYawHead - p.renderYawOffset) / (180F / (float) Math.PI);
        float xHead = p.rotationPitch / (180F / (float) Math.PI);


        float zRight = 0.0F;
        float zLeft = 0.0F;
        float yRight = 0.0F;
        float yLeft = 0.0F;
        float xRight = 0.0F;
        float xLeft = 0.0F;

        if (p.getHeldItemMainhand() != null) {
            if (p.getHeldItemMainhand().getItem() instanceof ItemGun) {
                if (p.world.isRemote) {
                    if (AnimationUtils.isAiming.get(player.getDisplayNameString()) != null) {
                        yRight = -0.1F + yHead - ((float) Math.PI / 2F);
                        yLeft = 0.1F + yHead + 0.4F - ((float) Math.PI / 2F);
                        xRight = -((float) Math.PI / 2F) + xHead;
                        xLeft = -((float) Math.PI / 2F) + xHead;
                    } else {
                        yRight = -0.1F - 0.17f - ((float) Math.PI / 2F);
                        yLeft = 0.1F - 0.17f + 0.4F - ((float) Math.PI / 2F);
                        xRight = -((float) Math.PI / 2F) + 0.6990046f;
                        xLeft = -((float) Math.PI / 2F) + 0.6990046f;
                    }
                } else {
                    if (ServerTickHandler.playerAimShootCooldown.get(p.getDisplayNameString()) != null) {
                        yRight = -0.1F + yHead - ((float) Math.PI / 2F);
                        yLeft = 0.1F + yHead + 0.4F - ((float) Math.PI / 2F);
                        xRight = -((float) Math.PI / 2F) + xHead;
                        xLeft = -((float) Math.PI / 2F) + xHead;
                    } else {
                        yRight = -0.1F - 0.17f - ((float) Math.PI / 2F);
                        yLeft = 0.1F - 0.17f + 0.4F - ((float) Math.PI / 2F);
                        xRight = -((float) Math.PI / 2F) + 0.6990046f;
                        xLeft = -((float) Math.PI / 2F) + 0.6990046f;
                    }
                }
            }
        }

        RotatedAxes leftArmAxes = (new RotatedAxes()).rotateGlobalPitchInRads(xLeft).rotateGlobalYawInRads((float) Math.PI + yLeft).rotateGlobalRollInRads(-zLeft);
        RotatedAxes rightArmAxes = (new RotatedAxes()).rotateGlobalPitchInRads(xRight).rotateGlobalYawInRads((float) Math.PI + yRight).rotateGlobalRollInRads(-zRight);


        float originZRight = MathHelper.sin(-p.renderYawOffset * 3.14159265F / 180F) * 5.0F / 16F;
        float originXRight = -MathHelper.cos(-p.renderYawOffset * 3.14159265F / 180F) * 5.0F / 16F;

        float originZLeft = -MathHelper.sin(-p.renderYawOffset * 3.14159265F / 180F) * 5.0F / 16F;
        float originXLeft = MathHelper.cos(-p.renderYawOffset * 3.14159265F / 180F) * 5.0F / 16F;


        if (p.isSneaking()) {
            hitboxes.add(new PlayerHitbox(player, bodyAxes.findLocalAxesGlobally(leftArmAxes), new Vector3f(originXLeft, 0.9F, originZLeft), new Vector3f(-2F / 16F, -0.6F, -2F / 16F), new Vector3f(0.25F, 0.7F, 0.25F), EnumHitboxType.LEFTARM));
            hitboxes.add(new PlayerHitbox(player, bodyAxes.findLocalAxesGlobally(rightArmAxes), new Vector3f(originXRight, 0.9F, originZRight), new Vector3f(-2F / 16F, -0.6F, -2F / 16F), new Vector3f(0.25F, 0.7F, 0.25F), EnumHitboxType.RIGHTARM));
        } else {
            hitboxes.add(new PlayerHitbox(player, bodyAxes.findLocalAxesGlobally(leftArmAxes), new Vector3f(originXLeft, 1.3F, originZLeft), new Vector3f(-2F / 16F, -0.6F, -2F / 16F), new Vector3f(0.25F, 0.7F, 0.25F), EnumHitboxType.LEFTARM));
            hitboxes.add(new PlayerHitbox(player, bodyAxes.findLocalAxesGlobally(rightArmAxes), new Vector3f(originXRight, 1.3F, originZRight), new Vector3f(-2F / 16F, -0.6F, -2F / 16F), new Vector3f(0.25F, 0.7F, 0.25F), EnumHitboxType.RIGHTARM));
        }
        MinecraftForge.EVENT_BUS.post(new PlayerSnapshotCreateEvent.Post(p, pos, hitboxes));

        this.time = System.nanoTime();
    }

    /*
    public ArrayList<BulletHit> raytrace(Vector3f origin, Vector3f motion)
    {
        //Get the bullet raytrace vector into local coordinates
        Vector3f localOrigin = Vector3f.sub(origin, pos, null);
        //Prepare a list for the hits
        ArrayList<BulletHit> hits = new ArrayList<>();

        //Check each hitbox for a hit
        for(PlayerHitbox hitbox : hitboxes)
        {
            PlayerBulletHit hit = hitbox.raytrace(localOrigin, motion);
            if(hit != null && hit.intersectTime >= 0F && hit.intersectTime <= 1F)
            {
                hits.add(hit);
            }
        }

        return hits;
    }
    */

    @SideOnly(Side.CLIENT)
    public void renderSnapshot() {
        for (PlayerHitbox hitbox : hitboxes) {
            //hitbox.renderHitbox(player.world, pos);
        }
    }

    public PlayerHitbox GetHitbox(EnumHitboxType type) {
        for (PlayerHitbox hitbox : hitboxes) {
            if (hitbox.type == type) {
                return hitbox;
            }
        }
        return null;
    }
}