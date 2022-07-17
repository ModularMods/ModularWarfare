package com.modularwarfare;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;

import de.javagl.jgltf.model.MathUtils;

public class MainTest {
    public static void main(String[] args) {
        Quaternion quaternion = new Quaternion(0.313f, -0.003f, -0.901f, 0.300f);
        Matrix3f matrix3f = new Matrix3f();
        System.out.println(quaternion);
        matrix3f.m00 = 1 - 2 * quaternion.y * quaternion.y - 2 * quaternion.z * quaternion.z;
        matrix3f.m01 = 2 * quaternion.x * quaternion.y + 2 * quaternion.w * quaternion.z;
        matrix3f.m02 = 2 * quaternion.x * quaternion.z - 2 * quaternion.w * quaternion.y;

        matrix3f.m10 = 2 * quaternion.x * quaternion.y - 2 * quaternion.w * quaternion.z;
        matrix3f.m11 = 1 - 2 * quaternion.x * quaternion.x - 2 * quaternion.z * quaternion.z;
        matrix3f.m12 = 2 * quaternion.y * quaternion.z + 2 * quaternion.w * quaternion.x;

        matrix3f.m20 = 2 * quaternion.x * quaternion.z + 2 * quaternion.w * quaternion.y;
        matrix3f.m21 = 2 * quaternion.y * quaternion.z - 2 * quaternion.w * quaternion.x;
        matrix3f.m22 = 1 - 2 * quaternion.x * quaternion.x - 2 * quaternion.y * quaternion.y;
        
        Matrix3f scaleM=new Matrix3f();
        scaleM.m00=2;
        scaleM.m11=2;
        scaleM.m22=2;
        System.out.println("");
        System.out.println(matrix3f);
        matrix3f=Matrix3f.mul(matrix3f,scaleM , null);
        System.out.println("");
        System.out.println(matrix3f);
        
        Quaternion noz=new Quaternion();
        noz=noz.setFromMatrix(matrix3f).normalise(null);
        Matrix3f matrixO=matrix3f;
        matrix3f = new Matrix3f();
        System.out.println(quaternion);
        matrix3f.m00 = 1 - 2 * quaternion.y * quaternion.y - 2 * quaternion.z * quaternion.z;
        matrix3f.m01 = 2 * quaternion.x * quaternion.y + 2 * quaternion.w * quaternion.z;
        matrix3f.m02 = 2 * quaternion.x * quaternion.z - 2 * quaternion.w * quaternion.y;

        matrix3f.m10 = 2 * quaternion.x * quaternion.y - 2 * quaternion.w * quaternion.z;
        matrix3f.m11 = 1 - 2 * quaternion.x * quaternion.x - 2 * quaternion.z * quaternion.z;
        matrix3f.m12 = 2 * quaternion.y * quaternion.z + 2 * quaternion.w * quaternion.x;

        matrix3f.m20 = 2 * quaternion.x * quaternion.z + 2 * quaternion.w * quaternion.y;
        matrix3f.m21 = 2 * quaternion.y * quaternion.z - 2 * quaternion.w * quaternion.x;
        matrix3f.m22 = 1 - 2 * quaternion.x * quaternion.x - 2 * quaternion.y * quaternion.y;
        System.out.println("");
        System.out.println(matrix3f);
        
        System.out.println("");
        System.out.println(Matrix3f.mul(matrixO, Matrix3f.invert(matrix3f, null), null));
    }
}
