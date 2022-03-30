package com.modularwarfare.utility.maths;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import javax.vecmath.*;
import java.nio.FloatBuffer;

@SideOnly(Side.CLIENT)
public class MatrixUtils
{
    /**
     * Model view matrix buffer
     */
    public static final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    /**
     * Float array for transferring data from FloatBuffer to the matrix
     */
    public static final float[] floats = new float[16];

    /**
     * Model view matrix captured here
     */
    public static Matrix4f matrix;

    /**
     * Read OpenGL's model view matrix
     */
    public static Matrix4f readModelView(Matrix4f matrix4f)
    {
        buffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        buffer.get(floats);

        matrix4f.set(floats);
        matrix4f.transpose();

        return matrix4f;
    }

    public static Matrix4f readModelView()
    {
        return readModelView(new Matrix4f());
    }

    /**
     * Replace model view matrix with given matrix
     */
    public static void loadModelView(Matrix4f matrix4f)
    {
        matrixToFloat(floats, matrix4f);

        buffer.clear();
        buffer.put(floats);
        buffer.rewind();
        GL11.glLoadMatrix(buffer);
    }

    /**
     * Private method to fill the float array with values from the matrix
     */
    public static void matrixToFloat(float[] floats, Matrix4f matrix4f)
    {
        floats[0] = matrix4f.m00;
        floats[1] = matrix4f.m01;
        floats[2] = matrix4f.m02;
        floats[3] = matrix4f.m03;
        floats[4] = matrix4f.m10;
        floats[5] = matrix4f.m11;
        floats[6] = matrix4f.m12;
        floats[7] = matrix4f.m13;
        floats[8] = matrix4f.m20;
        floats[9] = matrix4f.m21;
        floats[10] = matrix4f.m22;
        floats[11] = matrix4f.m23;
        floats[12] = matrix4f.m30;
        floats[13] = matrix4f.m31;
        floats[14] = matrix4f.m32;
        floats[15] = matrix4f.m33;
    }

    public static boolean captureMatrix()
    {
        if (matrix == null)
        {
            matrix = MatrixUtils.readModelView(new Matrix4f());

            return true;
        }

        return false;
    }

    public static void releaseMatrix()
    {
        matrix = null;
    }

    public static Quat4d matrixToQuaternion(Matrix3f matrix)
    {
        double tr = matrix.m00 + matrix.m11 + matrix.m22;
        double qw = 0;
        double qx = 0;
        double qy = 0;
        double qz = 0;

        if (tr > 0)
        {
            double S = Math.sqrt(tr+1.0) * 2; // S=4*qw
            qw = 0.25 * S;
            qx = (matrix.m21 - matrix.m12) / S;
            qy = (matrix.m02 - matrix.m20) / S;
            qz = (matrix.m10 - matrix.m01) / S;
        }
        else if ((matrix.m00 > matrix.m11) & (matrix.m00 > matrix.m22))
        {
            double S = Math.sqrt(1.0 + matrix.m00 - matrix.m11 - matrix.m22) * 2; // S=4*qx
            qw = (matrix.m21 - matrix.m12) / S;
            qx = 0.25 * S;
            qy = (matrix.m01 + matrix.m10) / S;
            qz = (matrix.m02 + matrix.m20) / S;
        }
        else if (matrix.m11 > matrix.m22)
        {
            double S = Math.sqrt(1.0 + matrix.m11 - matrix.m00 - matrix.m22) * 2; // S=4*qy
            qw = (matrix.m02 - matrix.m20) / S;
            qx = (matrix.m01 + matrix.m10) / S;
            qy = 0.25 * S;
            qz = (matrix.m12 + matrix.m21) / S;
        }
        else
        {
            double S = Math.sqrt(1.0 + matrix.m22 - matrix.m00 - matrix.m11) * 2; // S=4*qz
            qw = (matrix.m10 - matrix.m01) / S;
            qx = (matrix.m02 + matrix.m20) / S;
            qy = (matrix.m12 + matrix.m21) / S;
            qz = 0.25 * S;
        }

        return new Quat4d(qw, qx, qy, qz);
    }

    /**
     * This method calculates the angular velocity around the arbitrary axis.
     * The arbitrary axis and the angle around it can be obtained from this result.
     * @param rotation
     * @return the angular velocity vector.
     */
    public static Vector3f getAngularVelocity(Matrix3f rotation)
    {
        Matrix3f step = new Matrix3f(rotation);
        Matrix3f angularVelocity = new Matrix3f();
        Matrix3f i = new Matrix3f();

        i.setIdentity();
        angularVelocity.setIdentity();
        angularVelocity.mul(2);

        step.add(i);
        step.invert();
        step.mul(4);

        angularVelocity.sub(step);

        Vector3f angularV = new Vector3f(angularVelocity.m21,
                                    -angularVelocity.m20,
                                     angularVelocity.m10);

        return angularV;
    }

    public static Matrix3f getZYXrotationMatrix(float x, float y, float z)
    {
        Matrix3f rotation = new Matrix3f();
        Matrix3f rot = new Matrix3f();

        rotation.setIdentity();
        rot.rotZ(z);
        rotation.mul(rot);
        rot.rotY(y);
        rotation.mul(rot);
        rot.rotX(x);
        rotation.mul(rot);

        return rotation;
    }


    /**
     * This method extracts the rotation, translation and scale from the modelview matrix. It needs the view matrix to work properly
     * @author Christian F. (known as Chryfi)
     * @param cameraMatrix The cameraMatrix from rendering so you can extract modelView from OpenGL's matrix.
     * @param modelView The matrix containing the transformations that should be extracted.
     * @return Transformation contains translation, rotation and scale as 4x4 matrices. It also has getter methods for the 3x3 matrices.
     */
    public static Transformation extractTransformations(@Nullable Matrix4f cameraMatrix, Matrix4f modelView)
    {
        return extractTransformations(cameraMatrix, modelView, MatrixMajor.ROW);
    }

    public static Transformation extractTransformations(@Nullable Matrix4f cameraMatrix, Matrix4f modelView, MatrixMajor major)
    {
        Matrix4f parent = new Matrix4f(modelView);

        if (cameraMatrix != null)
        {
            parent.set(cameraMatrix);

            try
            {
                parent.invert();
            }
            catch (SingularMatrixException e)
            {
                Transformation transformation = new Transformation();
                transformation.creationException = e;

                return transformation;
            }

            parent.mul(modelView);
        }

        Matrix4f translation = new Matrix4f();
        Matrix4f scale = new Matrix4f();
        Matrix4f rotation = new Matrix4f();

        translation.setIdentity();
        rotation.setIdentity();
        scale.setIdentity();

        translation.m03 = parent.m03;
        translation.m13 = parent.m13;
        translation.m23 = parent.m23;

        Vector4f ax = new Vector4f(parent.m00, parent.m01, parent.m02, 0);
        Vector4f ay = new Vector4f(parent.m10, parent.m11, parent.m12, 0);
        Vector4f az = new Vector4f(parent.m20, parent.m21, parent.m22, 0);

        if (major == MatrixMajor.COLUMN)
        {
            ax = new Vector4f(parent.m00, parent.m10, parent.m20, 0.0F);
            ay = new Vector4f(parent.m01, parent.m11, parent.m21, 0.0F);
            az = new Vector4f(parent.m02, parent.m12, parent.m22, 0.0F);
        }

        ax.normalize();
        ay.normalize();
        az.normalize();
        rotation.setRow(0, ax);
        rotation.setRow(1, ay);
        rotation.setRow(2, az);

        if (major == MatrixMajor.COLUMN)
        {
            rotation.transpose();
        }

        scale.m00 = (float) Math.sqrt(parent.m00 * parent.m00 + parent.m01 * parent.m01 + parent.m02 * parent.m02);
        scale.m11 = (float) Math.sqrt(parent.m10 * parent.m10 + parent.m11 * parent.m11 + parent.m12 * parent.m12);
        scale.m22 = (float) Math.sqrt(parent.m20 * parent.m20 + parent.m21 * parent.m21 + parent.m22 * parent.m22);

        if (major == MatrixMajor.COLUMN)
        {
            scale.m00 = (float) Math.sqrt(parent.m00 * parent.m00 + parent.m10 * parent.m10 + parent.m20 * parent.m20);
            scale.m11 = (float) Math.sqrt(parent.m01 * parent.m01 + parent.m11 * parent.m11 + parent.m21 * parent.m21);
            scale.m22 = (float) Math.sqrt(parent.m02 * parent.m02 + parent.m12 * parent.m12 + parent.m22 * parent.m22);
        }

        return new Transformation(translation, rotation, scale);
    }

    public static class Transformation
    {
        public Matrix4f translation = new Matrix4f();
        public Matrix4f rotation = new Matrix4f();
        public Matrix4f scale = new Matrix4f();
        /**
         * contains the exception that may have
         * occurred during calculation of transformation data
         */
        private Exception creationException = null;

        public Transformation(Matrix4f translation, Matrix4f rotation, Matrix4f scale)
        {
            this.translation.set(translation);
            this.rotation.set(rotation);
            this.scale.set(scale);
        }

        public Transformation()
        {
            this.translation.setIdentity();
            this.rotation.setIdentity();
            this.scale.setIdentity();
        }

        public Matrix3f getScale3f()
        {
            Matrix3f scale3f = new Matrix3f();

            scale3f.setIdentity();

            scale3f.m00 = this.scale.m00;
            scale3f.m11 = this.scale.m11;
            scale3f.m22 = this.scale.m22;

            return scale3f;
        }

        public Vector3f getTranslation3f()
        {
            Vector3f translation3f = new Vector3f();

            translation3f.set(this.translation.m03,this.translation.m13, this.translation.m23);

            return translation3f;
        }

        public Matrix3f getRotation3f()
        {
            Matrix3f rotation3f = new Matrix3f();

            rotation3f.setIdentity();
            rotation3f.setRow(0, this.rotation.m00, this.rotation.m01, this.rotation.m02);
            rotation3f.setRow(1, this.rotation.m10, this.rotation.m11, this.rotation.m12);
            rotation3f.setRow(2, this.rotation.m20, this.rotation.m21, this.rotation.m22);

            return rotation3f;
        }

        public Exception getCreationException()
        {
            return this.creationException;
        }

        public Vector3f getRotation(RotationOrder order)
        {
            return this.getRotation(order, null);
        }

        public Vector3f getRotation(RotationOrder order, Vector3f ref)
        {
            return this.getRotation(order, ref, 0);
        }

        public Vector3f getRotation(RotationOrder order, int invAxis)
        {
            return this.getRotation(order, null, invAxis);
        }

        /**
         * Get rotation values from matrix<br>
         * <b>It must be called first to determine if the value can be obtained
         * properly</b>
         * 
         * @param order   Rotation Order
         * @param ref     The vector used for reference. Null if nothing to refer. Make
         *                sure that the incoming matrix is not affected by negative
         *                scaling.
         * @param invAxis The axis to be inverted when the matrix is a left-handed
         *                coordinate system. 012 equals xyz
         * @return A vector includes rotation values with specific order, null if matrix
         *         is a illegal rotation matrix.<br>
         *         The returned vector will be as close to the reference vector as
         *         possible
         */
        public Vector3f getRotation(RotationOrder order, Vector3f ref, int invAxis)
        {
            Matrix3f mat = this.getRotation3f();
            float[] rotation = new float[3];
            float[] refFloats = null;

            if (ref != null)
            {
                refFloats = new float[3];
                ref.get(refFloats);
            }

            // DirectX -> OpenGL
            // If the scaling value has an odd number of negative values, this will cause it
            // to become a left-handed coordinate system.
            Vector3f x = new Vector3f(mat.m00, mat.m10, mat.m20);
            Vector3f y = new Vector3f(mat.m01, mat.m11, mat.m21);
            Vector3f z = new Vector3f(mat.m02, mat.m12, mat.m22);
            Vector3f crossY = new Vector3f();
            Vector3f originalY = new Vector3f();
            originalY.normalize(y);
            crossY.cross(z, x);
            crossY.normalize();

            if (crossY.dot(originalY) < 0)
            {
                mat.mul(getInvertAxisMatrix(invAxis));
            }

            Float angle = order.doTest(order.thirdIndex, mat);

            /* if the second rotation value is +/-90, here will be null. */
            if (angle != null)
            {
                if (refFloats != null)
                {
                    angle = refFloats[order.thirdIndex] + MathHelper.wrapDegrees(2F * (angle - refFloats[order.thirdIndex])) / 2F;
                }

                rotation[order.thirdIndex] = angle;
                mat.mul(getRotationMatrix(order.thirdIndex, -angle), mat);
            }
            else if (refFloats != null)
            {
                rotation[order.thirdIndex] = angle = refFloats[order.thirdIndex];
                mat.mul(getRotationMatrix(order.thirdIndex, -angle), mat);
            }

            angle = order.doTest(order.secondIndex, mat);

            if (angle == null)
            {
                // Illegal: Scale is zero, no rotation information here.
                return null;
            }

            if (refFloats != null)
            {
                angle = refFloats[order.secondIndex] + MathHelper.wrapDegrees(angle - refFloats[order.secondIndex]);
            }

            rotation[order.secondIndex] = angle;
            mat.mul(getRotationMatrix(order.secondIndex, -angle), mat);

            angle = order.doTest(order.firstIndex, mat);

            if (angle == null)
            {
                return null;
            }
            else if (refFloats != null)
            {
                angle = refFloats[order.firstIndex] + MathHelper.wrapDegrees(angle - refFloats[order.firstIndex]);
            }

            rotation[order.firstIndex] = angle;

            return new Vector3f(rotation);
        }

        public Vector3f getScale()
        {
            return this.getScale(0);
        }

        public Vector3f getScale(int invAxis)
        {
            Vector3f scale = new Vector3f(this.scale.m00, this.scale.m11, this.scale.m22);
            Vector3f x = new Vector3f(this.rotation.m00, this.rotation.m10, this.rotation.m20);
            Vector3f y = new Vector3f(this.rotation.m01, this.rotation.m11, this.rotation.m21);
            Vector3f z = new Vector3f(this.rotation.m02, this.rotation.m12, this.rotation.m22);
            Vector3f crossY = new Vector3f();
            Vector3f originalY = new Vector3f();

            originalY.normalize(y);
            crossY.cross(z, x);
            crossY.normalize();

            if (crossY.dot(originalY) < 0)
            {
                getInvertAxisMatrix(invAxis).transform(scale);
            }

            return scale;
        }

        public static Matrix3f getRotationMatrix(int axis, double degrees)
        {
            Matrix3f mat = new Matrix3f();

            switch (axis)
            {
                case 0:
                    mat.rotX((float) Math.toRadians(degrees));
                    break;
                case 1:
                    mat.rotY((float) Math.toRadians(degrees));
                    break;
                case 2:
                    mat.rotZ((float) Math.toRadians(degrees));
                    break;
            }

            return mat;
        }

        public static Matrix3f getInvertAxisMatrix(int axis)
        {
            Matrix3f mat = new Matrix3f();

            mat.setIdentity();

            switch (axis)
            {
                case 0:
                    mat.m00 = -1;
                    break;
                case 1:
                    mat.m11 = -1;
                    break;
                case 2:
                    mat.m22 = -1;
                    break;
            }

            return mat;
        }

        public static enum RotationOrder
        {
            XYZ, XZY, YXZ, YZX, ZXY, ZYX;

            public final int firstIndex;
            public final int secondIndex;
            public final int thirdIndex;

            private RotationOrder()
            {
                String order = this.name().toUpperCase();
                firstIndex = order.charAt(0) - 'X';
                secondIndex = order.charAt(1) - 'X';
                thirdIndex = order.charAt(2) - 'X';
            }

            public Float doTest(int index, Matrix3f test)
            {
                float[] buffer = new float[3];

                buffer[index == firstIndex ? secondIndex : firstIndex] = 1;

                Vector3f in = new Vector3f(buffer);
                Vector3f out = new Vector3f();

                test.transform(in, out);
                out.get(buffer);
                buffer[index] = 0;
                out.set(buffer);

                if (out.length() < 1E-07)
                {
                    return null;
                }

                out.normalize();

                float cos = in.dot(out);

                out.cross(in, out);
                out.get(buffer);

                float sin = out.length() * Math.signum(buffer[index]);

                return (float) Math.toDegrees(Math.atan2(sin, cos));
            }
        }
    }

    public enum MatrixMajor
    {
        ROW,
        COLUMN
    }
}