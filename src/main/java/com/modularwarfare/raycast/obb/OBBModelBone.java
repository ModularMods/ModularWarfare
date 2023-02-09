package com.modularwarfare.raycast.obb;

import java.util.ArrayList;

import com.modularwarfare.common.vector.Matrix4f;
import com.modularwarfare.common.vector.Vector3f;

public class OBBModelBone {
    public String name;
    public OBBModelBone parent;
    public Vector3f oirign = new Vector3f();
    public Vector3f translation = new Vector3f(0, 0, 0);
    public Vector3f rotation = new Vector3f();
    public ArrayList<OBBModelBone> children = new ArrayList<OBBModelBone>();
    public Matrix4f currentPose = new Matrix4f();
    public static final Vector3f YAW = new Vector3f(0, -1, 0);
    public static final Vector3f PITCH = new Vector3f(1, 0, 0);
    public static final Vector3f ROOL = new Vector3f(0, 0, -1);

    public void updatePose(OBBModelObject obbModelObject) {
        obbModelObject.onBoneUpdatePose(this);
    }
    
    public void computePose(OBBModelObject obbModelObject, Matrix4f matrix) {
        matrix = matrix.translate(translation).translate(oirign).rotate(rotation.y, YAW).rotate(rotation.x, PITCH)
                .rotate(rotation.z, ROOL).translate(oirign.negate(null));
        /*
        if(name.equals("head")) {
            System.out.println("\n name:"+name);
            System.out.println(currentPose.m00+" , "+currentPose.m01+" , "+currentPose.m02+" , "+currentPose.m03+"\n");
            System.out.println(currentPose.m10+" , "+currentPose.m11+" , "+currentPose.m12+" , "+currentPose.m13+"\n");
            System.out.println(currentPose.m20+" , "+currentPose.m21+" , "+currentPose.m22+" , "+currentPose.m23+"\n");
            System.out.println(currentPose.m30+" , "+currentPose.m31+" , "+currentPose.m32+" , "+currentPose.m33+"\n");
            System.out.println("\n");
        }
        */
        currentPose = new Matrix4f(matrix);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).computePose(obbModelObject, new Matrix4f(matrix));
        }
    }
}
