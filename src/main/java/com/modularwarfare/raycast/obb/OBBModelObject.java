package com.modularwarfare.raycast.obb;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.modularwarfare.common.vector.Matrix4f;

import io.netty.buffer.Unpooled;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OBBModelObject {
    public static final FloatBuffer FLOAT_BUFFER = BufferUtils.createFloatBuffer(16);
    public OBBModelScene scene;
    public ArrayList<OBBModelBox> boxes = new ArrayList<OBBModelBox>();
    public HashMap<OBBModelBox, OBBModelBone> boneBinding = new HashMap<>();
    public ArrayList<IBoneUpdatePoseListener> boneUpdatePoseListeners = new ArrayList<OBBModelObject.IBoneUpdatePoseListener>();
    
    public static interface IBoneUpdatePoseListener {
        public void onBoneUpdatePose(OBBModelBone bone);
    }

    public void updatePose() {
        scene.updatePose(this);
    }
    
    public void computePose() {
        scene.computePose(this);
        boneBinding.forEach((box,bone)->{
            box.compute(new Matrix4f(bone.currentPose).translate(bone.oirign));
        });
        return;
    }

    public void onBoneUpdatePose(OBBModelBone bone) {
        for (int i = 0; i < boneUpdatePoseListeners.size(); i++) {
            boneUpdatePoseListeners.get(i).onBoneUpdatePose(bone);
        }
    }

    @SideOnly(Side.CLIENT)
    public void renderDebugBoxes() {
        boneBinding.forEach((box, bone) -> {
            GlStateManager.pushMatrix();
            FLOAT_BUFFER.rewind();

            FLOAT_BUFFER.put(bone.currentPose.m00);
            FLOAT_BUFFER.put(bone.currentPose.m01);
            FLOAT_BUFFER.put(bone.currentPose.m02);
            FLOAT_BUFFER.put(bone.currentPose.m03);
            FLOAT_BUFFER.put(bone.currentPose.m10);
            FLOAT_BUFFER.put(bone.currentPose.m11);
            FLOAT_BUFFER.put(bone.currentPose.m12);
            FLOAT_BUFFER.put(bone.currentPose.m13);
            FLOAT_BUFFER.put(bone.currentPose.m20);
            FLOAT_BUFFER.put(bone.currentPose.m21);
            FLOAT_BUFFER.put(bone.currentPose.m22);
            FLOAT_BUFFER.put(bone.currentPose.m23);
            FLOAT_BUFFER.put(bone.currentPose.m30);
            FLOAT_BUFFER.put(bone.currentPose.m31);
            FLOAT_BUFFER.put(bone.currentPose.m32);
            FLOAT_BUFFER.put(bone.currentPose.m33);

            FLOAT_BUFFER.flip();
            GL11.glPushMatrix();
            GlStateManager.multMatrix(FLOAT_BUFFER);
            GL11.glTranslatef(bone.oirign.getX(), bone.oirign.getY(), bone.oirign.getZ());
            box.renderDebugBox();
            GL11.glPopMatrix();
            GlStateManager.popMatrix();
        });
    }
    
    @SideOnly(Side.CLIENT)
    public void renderDebugAixs() {
        GlStateManager.color(1, 1, 1,1);
        GlStateManager.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        boxes.forEach((box)->{
            box.axis.forEach((axi)->{
                Tessellator tessellator=Tessellator.getInstance();
                tessellator.getBuffer().begin(3, DefaultVertexFormats.POSITION_COLOR);
                tessellator.getBuffer().pos(box.center.x, box.center.y, box.center.z).color(255, 0, 0,255).endVertex();
                tessellator.getBuffer().pos(box.center.x+axi.x, box.center.y+axi.y, box.center.z+axi.z).color(255, 0, 0,255).endVertex();
                tessellator.draw();
            });
        });
        GlStateManager.enableTexture2D();
    }
}
