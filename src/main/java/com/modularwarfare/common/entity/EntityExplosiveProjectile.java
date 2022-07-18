package com.modularwarfare.common.entity;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.ItemBullet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityExplosiveProjectile extends EntityBullet implements IProjectile {

    public EntityExplosiveProjectile(World world) {
        super(world);
        setSize(0.2F, 0.2F);
    }

    public EntityExplosiveProjectile(World par1World, EntityPlayer par2EntityPlayer, float damage, float accuracy, float velocity, String bulletName) {
        super(par1World, par2EntityPlayer, damage, accuracy, velocity, bulletName);
    }

    public void onUpdate() {
        super.onUpdate();

        Vec3d vec3d1 = new Vec3d(this.posX, this.posY, this.posZ);
        Vec3d vec3d = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult raytraceresult = this.world.rayTraceBlocks(vec3d1, vec3d, false, true, false);

        ModularWarfare.PROXY.spawnRocketParticle(this.world, this.posX, this.posY, this.posZ);

        if (raytraceresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
            explode();
            this.setDead();
        }
    }

    public void explode(){
        if (!this.world.isRemote) {
            if (ModularWarfare.bulletTypes.containsKey(this.getBulletName())) {
                ItemBullet itemBullet = ModularWarfare.bulletTypes.get(this.getBulletName());
                Explosion explosion = new Explosion(this.world, this.player, posX, posY, posZ, itemBullet.type.explosionStrength, false, itemBullet.type.damageWorld);
                explosion.doExplosionA();
                explosion.doExplosionB(true);
                ModularWarfare.PROXY.spawnExplosionParticle(this.world, this.posX, this.posY, this.posZ);
            }
        }
        this.setDead();
    }
}