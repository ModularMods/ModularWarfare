package com.modularwarfare.client.model;


import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.vector.Vector3f;
import com.modularwarfare.utility.RayUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Random;

public class InstantBulletRenderer {
    private static TextureManager textureManager;
    private static ArrayList<InstantShotTrail> trails = new ArrayList<>();

    public static void AddTrail(InstantShotTrail trail) {
        trails.add(trail);
    }

    public static void RenderAllTrails(float partialTicks) {
        for (InstantShotTrail trail : trails) {
            trail.Render(partialTicks);
        }
    }

    public static void UpdateAllTrails() {
        for (int i = trails.size() - 1; i >= 0; i--) {
            if (trails.get(i).Update()) {
                trails.remove(i);
            }
        }
    }

    public static class InstantShotTrail {
        private Vector3f origin;
        private Vector3f hitPos;
        private float width;
        private float length;
        private float distanceToTarget;
        private float bulletSpeed;
        private int ticksExisted;

        private ResourceLocation texture;

        public InstantShotTrail(Vector3f origin, Vector3f hitPos, float bulletSpeed, boolean isPunched) {
            this.ticksExisted = 0;
            this.bulletSpeed = bulletSpeed;
            this.origin = origin;
            this.hitPos = hitPos;
            this.length = 15.0f * new Random().nextFloat();
            if (!isPunched) {
                this.texture = new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/" + "defaultbullettrail.png");
                this.width = 0.3f;
            } else {
                this.texture = new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/" + "punchedbullettrail.png");
                this.width = 0.1f;
            }

            Vector3f dPos = Vector3f.sub(hitPos, origin, null);

            RayTraceResult result = ModularWarfare.INSTANCE.RAY_CASTING.rayTraceBlocks(Minecraft.getMinecraft().world, origin.toVec3(), hitPos.toVec3(), true, true, false);
            if (result != null) {
                if (result.hitVec != null) {
                    dPos = Vector3f.sub(new Vector3f(result.hitVec), origin, null);
                }
            }
            this.distanceToTarget = dPos.length();

            if (Math.abs(distanceToTarget) > 300.0f) {
                distanceToTarget = 300.0f;
            }
        }

        // Return true if this needs deleting
        public boolean Update() {
            ticksExisted++;
            if (length > 0F) {
                length -= 0.05F;
            }
            return (ticksExisted) * bulletSpeed >= distanceToTarget - length / 4;
        }

        public void Render(float partialTicks) {
            float x_ = OpenGlHelper.lastBrightnessX;
            float y_ = OpenGlHelper.lastBrightnessY;


            //Make sure we actually have the renderEngine
            if (textureManager == null)
                textureManager = Minecraft.getMinecraft().renderEngine;

            textureManager.bindTexture(texture);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            GlStateManager.pushMatrix();

            //Get the camera frustrum for clipping
            Entity camera = Minecraft.getMinecraft().getRenderViewEntity();
            double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partialTicks;
            double y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partialTicks;
            double z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partialTicks;

            GL11.glTranslatef(-(float) x, -(float) y + (0.1F), -(float) z);

            float parametric = ((float) (ticksExisted) + partialTicks) * bulletSpeed;

            Vector3f dPos = Vector3f.sub(hitPos, origin, null);
            dPos.normalise();

            float startParametric = parametric - length * 0.5f;
            Vector3f startPos = new Vector3f(origin.x + dPos.x * startParametric, origin.y + dPos.y * startParametric, origin.z + dPos.z * startParametric);
            float endParametric = parametric + length * 0.5f;
            Vector3f endPos = new Vector3f(origin.x + dPos.x * endParametric, origin.y + dPos.y * endParametric, origin.z + dPos.z * endParametric);

            dPos.normalise();

            EntityPlayer player = Minecraft.getMinecraft().player;
            Vector3f vectorToPlayer = new Vector3f(player.posX - hitPos.x, player.posY - hitPos.y, player.posZ - hitPos.z);

            vectorToPlayer.normalise();

            Vector3f trailTangent = Vector3f.cross(dPos, vectorToPlayer, null);
            trailTangent.normalise();
            trailTangent.scale(-width * 0.5f);

            Vector3f normal = Vector3f.cross(trailTangent, dPos, null);
            normal.normalise();

            GlStateManager.enableRescaleNormal();
            GL11.glNormal3f(normal.x, normal.y, normal.z);

            GL11.glEnable(3042);
            GL11.glEnable(2832);
            GL11.glHint(3153, 4353);

            Tessellator tessellator = Tessellator.getInstance();
            tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);

            tessellator.getBuffer().pos(startPos.x + trailTangent.x, startPos.y + trailTangent.y, startPos.z + trailTangent.z).tex(0.0f, 0.0f).endVertex();
            tessellator.getBuffer().pos(startPos.x - trailTangent.x, startPos.y - trailTangent.y, startPos.z - trailTangent.z).tex(0.0f, 1.0f).endVertex();
            tessellator.getBuffer().pos(endPos.x - trailTangent.x, endPos.y - trailTangent.y, endPos.z - trailTangent.z).tex(1.0f, 1.0f).endVertex();
            tessellator.getBuffer().pos(endPos.x + trailTangent.x, endPos.y + trailTangent.y, endPos.z + trailTangent.z).tex(1.0f, 0.0f).endVertex();

            tessellator.draw();


            GL11.glDisable(3042);
            GL11.glDisable(2832);

            GlStateManager.disableRescaleNormal();

            GlStateManager.popMatrix();
        }
    }
}