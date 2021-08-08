package com.modularwarfare.common.hitbox;

import com.modularwarfare.common.hitbox.maths.EnumHitboxType;
import com.modularwarfare.common.hitbox.maths.RotatedAxes;
import com.modularwarfare.common.vector.Vector3f;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerHitbox {
    /**
     *
     */
    public EntityPlayer player;
    /**
     * The angles of this box
     */
    public RotatedAxes axes;
    /**
     * The origin of rotation for this box
     */
    public Vector3f rP;
    /**
     * The lower left corner of this box
     */
    public Vector3f o;
    /**
     * The dimensions of this box
     */
    public Vector3f d;
    /**
     * The type of hitbox
     */
    public EnumHitboxType type;

    public PlayerHitbox(EntityPlayer player, RotatedAxes axes, Vector3f rotationPoint, Vector3f origin, Vector3f dimensions, EnumHitboxType type) {
        this.player = player;
        this.axes = axes;
        this.o = origin;
        this.d = dimensions;
        this.type = type;
        this.rP = rotationPoint;
    }

    /*
    @SideOnly(Side.CLIENT)
    public void renderHitbox(World world, Vector3f pos) {

        Vector3f pointMin = new Vector3f(o.x, o.y, o.z);
        pointMin = axes.findLocalVectorGlobally(pointMin);

        Vector3f pointMax = new Vector3f(o.x + d.x * 2 / 2, o.y + d.y * 2 / 2, o.z + d.z * 2 / 2);
        pointMax = axes.findLocalVectorGlobally(pointMax);

        AxisAlignedBB hitbox = new AxisAlignedBB(player.posX + rP.x + pointMin.x, player.posY + rP.y + pointMin.y, player.posZ + rP.z + pointMin.z, player.posX + rP.x + pointMax.x, player.posY + rP.y + pointMax.y, player.posZ + rP.z + pointMax.z);
    }
     */


    public AxisAlignedBB getAxisAlignedBB(Vector3f pos) {
        Vector3f pointMin = new Vector3f(o.x, o.y, o.z);
        pointMin = axes.findLocalVectorGlobally(pointMin);

        Vector3f pointMax = new Vector3f(o.x + d.x, o.y + d.y, o.z + d.z);
        pointMax = axes.findLocalVectorGlobally(pointMax);

        AxisAlignedBB hitbox = new AxisAlignedBB(pos.x + rP.x + pointMin.x, pos.y + rP.y + pointMin.y, pos.z + rP.z + pointMin.z, pos.x + rP.x + pointMax.x, pos.y + rP.y + pointMax.y, pos.z + rP.z + pointMax.z);
        return hitbox;
    }


    public boolean raytrace(Vector3f origin, Vector3f motion) {
        //Move to local coords for this hitbox, but don't modify the original "origin" vector
        origin = Vector3f.sub(origin, rP, null);
        origin = axes.findGlobalVectorLocally(origin);
        motion = axes.findGlobalVectorLocally(motion);

        //We now have an AABB starting at o and with dimensions d and our ray in the same coordinate system
        //We are looking for a point at which the ray enters the box, so we need only consider faces that the ray can see. Partition the space into 3 areas in each axis

        //X - axis and faces x = o.x and x = o.x + d.x
        if (motion.x != 0F) {
            if (origin.x < o.x) //Check face x = o.x
            {
                float intersectTime = (o.x - origin.x) / motion.x;
                float intersectY = origin.y + motion.y * intersectTime;
                float intersectZ = origin.z + motion.z * intersectTime;
                if (intersectY >= o.y && intersectY <= o.y + d.y && intersectZ >= o.z && intersectZ <= o.z + d.z)
                    return true;
            } else if (origin.x > o.x + d.x) //Check face x = o.x + d.x
            {
                float intersectTime = (o.x + d.x - origin.x) / motion.x;
                float intersectY = origin.y + motion.y * intersectTime;
                float intersectZ = origin.z + motion.z * intersectTime;
                if (intersectY >= o.y && intersectY <= o.y + d.y && intersectZ >= o.z && intersectZ <= o.z + d.z)
                    return true;
            }
        }

        //Z - axis and faces z = o.z and z = o.z + d.z
        if (motion.z != 0F) {
            if (origin.z < o.z) //Check face z = o.z
            {
                float intersectTime = (o.z - origin.z) / motion.z;
                float intersectX = origin.x + motion.x * intersectTime;
                float intersectY = origin.y + motion.y * intersectTime;
                if (intersectX >= o.x && intersectX <= o.x + d.x && intersectY >= o.y && intersectY <= o.y + d.y)
                    return true;
            } else if (origin.z > o.z + d.z) //Check face z = o.z + d.z
            {
                float intersectTime = (o.z + d.z - origin.z) / motion.z;
                float intersectX = origin.x + motion.x * intersectTime;
                float intersectY = origin.y + motion.y * intersectTime;
                if (intersectX >= o.x && intersectX <= o.x + d.x && intersectY >= o.y && intersectY <= o.y + d.y)
                    return true;
            }
        }

        //Y - axis and faces y = o.y and y = o.y + d.y
        if (motion.y != 0F) {
            if (origin.y < o.y) //Check face y = o.y
            {
                float intersectTime = (o.y - origin.y) / motion.y;
                float intersectX = origin.x + motion.x * intersectTime;
                float intersectZ = origin.z + motion.z * intersectTime;
                if (intersectX >= o.x && intersectX <= o.x + d.x && intersectZ >= o.z && intersectZ <= o.z + d.z)
                    return true;
            } else if (origin.y > o.y + d.y) //Check face x = o.x + d.x
            {
                float intersectTime = (o.y + d.y - origin.y) / motion.y;
                float intersectX = origin.x + motion.x * intersectTime;
                float intersectZ = origin.z + motion.z * intersectTime;
                if (intersectX >= o.x && intersectX <= o.x + d.x && intersectZ >= o.z && intersectZ <= o.z + d.z)
                    return true;
            }
        }

        return false;
    }
}
