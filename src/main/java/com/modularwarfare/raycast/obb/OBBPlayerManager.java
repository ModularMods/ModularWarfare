package com.modularwarfare.raycast.obb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.vector.Vector3f;
import com.modularwarfare.raycast.obb.bbloader.BlockBenchOBBInfoLoader;

import mchhui.modularmovements.tactical.PlayerState;
import mchhui.modularmovements.tactical.client.ClientLitener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class OBBPlayerManager {
    public static HashMap<String, PlayerOBBModelObject> playerOBBObjectMap = new HashMap<String, PlayerOBBModelObject>();
    public static EntityPlayer entityPlayer;
    public static ModelPlayer modelPlayer;
    public static ArrayList<Line> lines = new ArrayList<OBBPlayerManager.Line>();

    public static class Line {
        public Vector3f start;
        public Vector3f end;
        public OBBModelBox box;
        public long aliveTime;

        public Line(Vector3f start, Vector3f end) {
            this.start = start;
            this.end = end;
            this.aliveTime = System.currentTimeMillis() + 5000;
        }

        public Line(OBBModelBox box) {
            this.box = box;
            this.aliveTime = System.currentTimeMillis() + 5000;
        }

        public void render() {
            if (aliveTime < System.currentTimeMillis()) {
                return;
            }
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableTexture2D();
            if (box != null) {
                box.axis.forEach((axi) -> {
                    Tessellator tessellator = Tessellator.getInstance();
                    tessellator.getBuffer().begin(3, DefaultVertexFormats.POSITION_COLOR);
                    tessellator.getBuffer().pos(box.center.x, box.center.y, box.center.z).color(255, 0, 0, 255)
                            .endVertex();
                    tessellator.getBuffer().pos(box.center.x + axi.x, box.center.y + axi.y, box.center.z + axi.z)
                            .color(0, 255, 0, 255).endVertex();
                    tessellator.draw();
                });
            } else {
                Tessellator tessellator = Tessellator.getInstance();
                tessellator.getBuffer().begin(3, DefaultVertexFormats.POSITION_COLOR);
                tessellator.getBuffer().pos(start.x, start.y, start.z).color(0, 0, 255, 255).endVertex();
                tessellator.getBuffer().pos(end.x, end.y, end.z).color(0, 0, 255, 255).endVertex();
                tessellator.draw();
            }
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
    }

    public static class PlayerOBBModelObject extends OBBModelObject {
        public long nextSyncTime = 0;
        public boolean isSyncing = false;

        public PlayerOBBModelObject() {
            boneUpdatePoseListeners.add((bone) -> {
                if (bone.name.equals("head")) {
                    bone.translation.set(0, 0, 0);
                    if (entityPlayer.isSneaking()) {
                        bone.translation.add(0, -5, 0);
                    }
                    bone.rotation.set(modelPlayer.bipedHead.rotateAngleX, modelPlayer.bipedHead.rotateAngleY,
                            modelPlayer.bipedHead.rotateAngleZ);
                }
                if (bone.name.equals("body")) {
                    bone.translation.set(0, 0, 0);
                    if (entityPlayer.isSneaking()) {
                        bone.translation.add(0, -5, 0);
                    }
                    bone.rotation.set(modelPlayer.bipedBody.rotateAngleX, modelPlayer.bipedBody.rotateAngleY,
                            modelPlayer.bipedBody.rotateAngleZ);
                }
                if (bone.name.equals("rightArm")) {
                    bone.translation.set(0, 0, 0);
                    if (entityPlayer.isSneaking()) {
                        bone.translation.add(0, -5, 0);
                    }
                    bone.rotation.set(modelPlayer.bipedRightArm.rotateAngleX, modelPlayer.bipedRightArm.rotateAngleY,
                            modelPlayer.bipedRightArm.rotateAngleZ);
                }
                if (bone.name.equals("leftArm")) {
                    bone.translation.set(0, 0, 0);
                    if (entityPlayer.isSneaking()) {
                        bone.translation.add(0, -5, 0);
                    }
                    bone.rotation.set(modelPlayer.bipedLeftArm.rotateAngleX, modelPlayer.bipedLeftArm.rotateAngleY,
                            modelPlayer.bipedLeftArm.rotateAngleZ);
                }
                if (bone.name.equals("rightLeg")) {
                    bone.translation.set(0, 0, 0);
                    if (entityPlayer.isSneaking()) {
                        bone.translation.add(0, 0, -4);
                    }
                    bone.rotation.set(modelPlayer.bipedRightLeg.rotateAngleX, modelPlayer.bipedRightLeg.rotateAngleY,
                            modelPlayer.bipedRightLeg.rotateAngleZ);
                }
                if (bone.name.equals("leftLeg")) {
                    bone.translation.set(0, 0, 0);
                    if (entityPlayer.isSneaking()) {
                        bone.translation.add(0, 0, -4);
                    }
                    bone.rotation.set(modelPlayer.bipedLeftLeg.rotateAngleX, modelPlayer.bipedLeftLeg.rotateAngleY,
                            modelPlayer.bipedLeftLeg.rotateAngleZ);
                }
            });
        }

        public List<OBBModelBox> calculateIntercept(OBBModelBox testBox) {
            List list = new ArrayList<OBBModelBox>();
            for (int i = 0; i < boxes.size(); i++) {
                OBBModelBox box = boxes.get(i);
                if (OBBModelBox.testCollisionOBBAndOBB(box, testBox)) {
                    list.add(box.copy());
                }
            }
            return list;
        }
    }

    public static PlayerOBBModelObject getPlayerOBBObject(String name) {
        PlayerOBBModelObject playerOBBObject = playerOBBObjectMap.get(name);
        if (playerOBBObject == null) {
            playerOBBObject = BlockBenchOBBInfoLoader.loadOBBInfo(PlayerOBBModelObject.class,
                    new ResourceLocation("modularwarfare:obb/player.obb.json"));
            playerOBBObjectMap.put(name, playerOBBObject);
        }
        return playerOBBObject;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerRender(RenderPlayerEvent.Pre event) {
        PlayerOBBModelObject playerOBBObject = playerOBBObjectMap.get(event.getEntityPlayer().getName());
        if (playerOBBObject == null) {
            playerOBBObject = BlockBenchOBBInfoLoader.loadOBBInfo(PlayerOBBModelObject.class,
                    new ResourceLocation("modularwarfare:obb/player.obb.json"));
            playerOBBObjectMap.put(event.getEntityPlayer().getName(), playerOBBObject);
        }
        if (!playerOBBObject.isSyncing && System.currentTimeMillis() >= playerOBBObject.nextSyncTime) {
            playerOBBObject.isSyncing = true;
            //playerOBBObject.nextSyncTime = System.currentTimeMillis() + 1000 / 60;
            entityPlayer = event.getEntityPlayer();
            modelPlayer = event.getRenderer().getMainModel();
            playerOBBObject.updatePose();
            PlayerOBBModelObject syncOBBObejct = playerOBBObject;
            syncOBBObejct.scene.resetMatrix();
            double lx = entityPlayer.lastTickPosX;
            double ly = entityPlayer.lastTickPosY;
            double lz = entityPlayer.lastTickPosZ;
            double x = entityPlayer.posX;
            double y = entityPlayer.posY;
            double z = entityPlayer.posZ;
            x = lx + (x - lx) * event.getPartialRenderTick();
            y = ly + (y - ly) * event.getPartialRenderTick();
            z = lz + (z - lz) * event.getPartialRenderTick();
            syncOBBObejct.scene.translate((float) x, (float) y, (float) z);
            float yaw = entityPlayer.renderYawOffset;
            if (entityPlayer.isRiding()) {
                yaw = entityPlayer.rotationYaw;
            }
            if (entityPlayer.isEntityAlive() && entityPlayer.isPlayerSleeping()) {
                syncOBBObejct.scene.rotate(entityPlayer.getBedOrientationInDegrees(), 0.0F, 1.0F, 0.0F);
                syncOBBObejct.scene.rotate(90, 0.0F, 0.0F, 1.0F);
                syncOBBObejct.scene.rotate(270.0F, 0.0F, 1.0F, 0.0F);
            } else if (entityPlayer.isElytraFlying()) {
                syncOBBObejct.scene.rotate(yaw / 180 * 3.14159f, 0, -1, 0);
                float f = (float) entityPlayer.getTicksElytraFlying() + event.getPartialRenderTick();
                float f1 = MathHelper.clamp(f * f / 100.0F, 0.0F, 1.0F);
                syncOBBObejct.scene.rotate(-f1 * (-90.0F - entityPlayer.rotationPitch) / 180 * 3.14159f, 1, 0, 0);
                Vec3d vec3d = entityPlayer.getLook(event.getPartialRenderTick());
                double d0 = entityPlayer.motionX * entityPlayer.motionX + entityPlayer.motionZ * entityPlayer.motionZ;
                double d1 = vec3d.x * vec3d.x + vec3d.z * vec3d.z;

                if (d0 > 0.0D && d1 > 0.0D) {
                    double d2 = (entityPlayer.motionX * vec3d.x + entityPlayer.motionZ * vec3d.z)
                            / (Math.sqrt(d0) * Math.sqrt(d1));
                    double d3 = entityPlayer.motionX * vec3d.z - entityPlayer.motionZ * vec3d.x;
                    syncOBBObejct.scene.rotate((float) (Math.signum(d3) * Math.acos(d2)), 0, 1, 0);
                }
            } else {
                boolean flag = false;
                if (ModularWarfare.isLoadedModularMovements) {
                    if (entityPlayer == (Minecraft.getMinecraft()).player && entityPlayer.isEntityAlive()) {
                        if (ClientLitener.clientPlayerState.isSitting) {
                            syncOBBObejct.scene.translate(0.0D, -0.5D, 0.0D);
                        }
                        if (ClientLitener.clientPlayerState.isCrawling) {
                            syncOBBObejct.scene.rotateDegree(entityPlayer.renderYawOffset, 0.0F, -1.0F, 0.0F);
                            syncOBBObejct.scene.rotateDegree(-90.0F, -1.0F, 0.0F, 0.0F);
                            syncOBBObejct.scene.translate(0.0D, -1.3D, -0.1D);
                            syncOBBObejct.scene.translate(ClientLitener.cameraProbeOffset * 0.4D, 0.0D, 0.0D);
                            flag = true;
                        }
                        if (ClientLitener.cameraProbeOffset != 0.0F) {
                            syncOBBObejct.scene.rotateDegree(180 - entityPlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
                            syncOBBObejct.scene.translate(ClientLitener.cameraProbeOffset * 0.1D, 0.0D, 0.0D);
                            syncOBBObejct.scene.rotateDegree(180 - entityPlayer.rotationYaw, 0.0F, -1.0F, 0.0F);
                            syncOBBObejct.scene.rotateDegree(entityPlayer.renderYawOffset, 0.0F, -1.0F, 0.0F);
                            syncOBBObejct.scene.rotateDegree(ClientLitener.cameraProbeOffset * -20.0F, 0.0F, 0.0F,
                                    -1.0F);
                            flag = true;
                        }
                    }
                    if (entityPlayer != (Minecraft.getMinecraft()).player && entityPlayer instanceof EntityPlayer
                            && entityPlayer.isEntityAlive() && ClientLitener.ohterPlayerStateMap
                                    .containsKey(Integer.valueOf(entityPlayer.getEntityId()))) {
                        PlayerState state = ClientLitener.ohterPlayerStateMap
                                .get(Integer.valueOf(entityPlayer.getEntityId()));
                        if (state.isSitting)
                            syncOBBObejct.scene.translate(0.0D, -0.5D, 0.0D);
                        if (state.isCrawling) {
                            syncOBBObejct.scene.rotateDegree(entityPlayer.renderYawOffset, 0.0F, -1.0F, 0.0F);
                            syncOBBObejct.scene.rotateDegree(-90.0F, -1.0F, 0.0F, 0.0F);
                            syncOBBObejct.scene.translate(0.0D, -1.3D, -0.1D);
                            syncOBBObejct.scene.translate(state.probeOffset * 0.4D, 0.0D, 0.0D);
                            flag = true;
                        }
                        state.updateOffset();
                        if (state.probeOffset != 0.0F) {
                            syncOBBObejct.scene.rotateDegree(180.0F - entityPlayer.rotationYaw, 0.0F, 1.0F, 0.0F);
                            syncOBBObejct.scene.translate(state.probeOffset * 0.1D, 0.0D, 0.0D);
                            syncOBBObejct.scene.rotateDegree(180.0F - entityPlayer.rotationYaw, 0.0F, -1.0F, 0.0F);
                            syncOBBObejct.scene.rotateDegree(entityPlayer.renderYawOffset, 0.0F, -1.0F, 0.0F);
                            syncOBBObejct.scene.rotateDegree(state.probeOffset * -20.0F, 0.0F, 0.0F, -1.0F);
                            flag = true;
                        }
                    }
                }
                if (!flag) {
                    syncOBBObejct.scene.rotate(yaw / 180 * 3.14159f, 0, -1, 0);
                }
            }
            syncOBBObejct.scene.scale(1 / 16f * 0.9375F, 1 / 16f * 0.9375F, 1 / 16f * 0.9375F);
            syncOBBObejct.computePose();
            syncOBBObejct.isSyncing = false;
        }
        if (Minecraft.getMinecraft().getRenderManager().isDebugBoundingBox()) {
            GlStateManager.pushMatrix();
            Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
            GlStateManager.translate(
                    -(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialRenderTick()),
                    -(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialRenderTick()),
                    -(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialRenderTick()));
            playerOBBObject.renderDebugBoxes();
            //playerOBBObject.renderDebugAixs();
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onrenderWorld(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().getRenderManager().isDebugBoundingBox()) {
            GlStateManager.pushMatrix();
            Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
            GlStateManager.translate(
                    -(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialTicks()),
                    -(entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialTicks()),
                    -(entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialTicks()));
            lines.forEach((line) -> {
                line.render();
            });
            GlStateManager.popMatrix();
        }
    }
}
