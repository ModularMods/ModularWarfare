package com.modularwarfare.raycast.obb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.vecmath.Vector3d;

import com.modularwarfare.common.vector.Matrix4f;
import com.modularwarfare.common.vector.Vector3f;
import com.modularwarfare.loader.ObjModel;
import com.modularwarfare.loader.api.ObjModelLoader;
import com.modularwarfare.raycast.obb.OBBModelBox.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class OBBModelBox {
    public String name;
    public Vector3f anchor;
    public Vector3f rotation;
    public Vector3f size;
    public Vector3f center;
    public Axis axis = new Axis();
    public Axis axisNormal = new Axis();

    private transient static final ObjModel debugBoxModel = ObjModelLoader
            .load(new ResourceLocation("modularwarfare:obb/model.obj"));
    private transient static final ResourceLocation debugBoxTex = new ResourceLocation(
            "modularwarfare:obb/debugbox.png");

    public static class Axis implements Iterable<Vector3f> {
        public Vector3f x = new Vector3f(1, 0, 0);
        public Vector3f y = new Vector3f(0, 1, 0);
        public Vector3f z = new Vector3f(0, 0, 1);

        @Override
        public Iterator<Vector3f> iterator() {
            ArrayList<Vector3f> list = new ArrayList<Vector3f>();
            list.add(x);
            list.add(y);
            list.add(z);
            return list.iterator();
        }
        
        public Axis copy() {
            Axis axis=new Axis();
            axis.x=new Vector3f(this.x);
            axis.y=new Vector3f(this.y);
            axis.z=new Vector3f(this.z);
            return axis;
        }
    }
    
    public OBBModelBox copy() {
        OBBModelBox box=new OBBModelBox();
        box.name=this.name;
        box.anchor=new Vector3f(this.anchor);
        box.rotation=new Vector3f(this.rotation);
        box.size=new Vector3f(this.size);
        box.center=new Vector3f(this.center);
        box.axis=axis.copy();
        box.axisNormal=axisNormal.copy();
        return box;
    }

    public void compute(Matrix4f matrix) {
        center = Matrix4f.transform(matrix, anchor, null).add(matrix.m30, matrix.m31, matrix.m32);
        matrix = matrix.rotate(rotation.y, OBBModelBone.YAW).rotate(rotation.x, OBBModelBone.PITCH)
                .rotate(rotation.z, OBBModelBone.ROOL);
        axisNormal = new Axis();
        axisNormal.x = Matrix4f.transform(matrix, axisNormal.x, null);
        axisNormal.y = Matrix4f.transform(matrix, axisNormal.y, null);
        axisNormal.z = Matrix4f.transform(matrix, axisNormal.z, null);
        matrix=matrix.scale(size);
        axis = new Axis();
        axis.x = Matrix4f.transform(matrix, axis.x, null);
        axis.y = Matrix4f.transform(matrix, axis.y, null);
        axis.z = Matrix4f.transform(matrix, axis.z, null);
    }

    @SideOnly(Side.CLIENT)
    public void renderDebugBox() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(anchor.x, anchor.y, anchor.z);
        GlStateManager.scale(size.x * 2, size.y * 2, size.z * 2);
        GlStateManager.rotate((float) Math.toDegrees(rotation.y), 0, -1, 0);
        GlStateManager.rotate((float) Math.toDegrees(rotation.x), -1, 0, 0);
        GlStateManager.rotate((float) Math.toDegrees(rotation.z), 0, 0, 1);
        Minecraft.getMinecraft().renderEngine.bindTexture(debugBoxTex);
        debugBoxModel.renderAll(16);
        GlStateManager.popMatrix();
    }

    public static boolean testCollisionOBBAndOBB(OBBModelBox obb1, OBBModelBox obb2) {
        OBBModelBox[] obbs = new OBBModelBox[] { obb1, obb2 };
        Vector3f obb1VecX = obb1.axis.x;
        Vector3f obb1VecY = obb1.axis.y;
        Vector3f obb1VecZ = obb1.axis.z;
        Vector3f obb2VecX = obb2.axis.x;
        Vector3f obb2VecY = obb2.axis.y;
        Vector3f obb2VecZ = obb2.axis.z;
        Vector3f axiVec;
        double proj1;
        double proj2;
        double projAB;
        double tempProj;
        for (OBBModelBox obb : obbs) {
            for (Vector3f axi : obb.axisNormal) {
                axiVec = axi;
                proj1 = projectionFast(obb1VecX, axiVec);
                tempProj = projectionFast(obb1VecY, axiVec);
                if (tempProj > proj1) {
                    proj1 = tempProj;
                }
                tempProj = projectionFast(obb1VecZ, axiVec);
                if (tempProj > proj1) {
                    proj1 = tempProj;
                }
                proj2 = projectionFast(obb2VecX, axiVec);
                tempProj = projectionFast(obb2VecY, axiVec);
                if (tempProj > proj2) {
                    proj2 = tempProj;
                }
                tempProj = projectionFast(obb2VecZ, axiVec);
                if (tempProj > proj2) {
                    proj2 = tempProj;
                }
                projAB = projectionFast(new Vector3f(obb2.center.x - obb1.center.x, obb2.center.y - obb1.center.y,
                        obb2.center.z - obb1.center.z), axiVec);
                if (projAB > proj1 + proj2) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double projectionFast(Vector3f vec1, Vector3f vec2) {
        double delta = Vector3f.dotDouble(vec1, vec2);
        return Math.abs(delta);
    }
}
