package com.modularwarfare.utility.maths;

import static java.lang.Math.PI;

public class MathUtils
{
    static public final float degreesToRadians = (float) (PI / 180);

    public static int clamp(int x, int min, int max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static float clamp(float x, float min, float max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static double clamp(double x, double min, double max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static long clamp(long x, long min, long max)
    {
        return x < min ? min : (x > max ? max : x);
    }

    public static int cycler(int x, int min, int max)
    {
        return x < min ? max : (x > max ? min : x);
    }

    public static float cycler(float x, float min, float max)
    {
        return x < min ? max : (x > max ? min : x);
    }

    public static double cycler(double x, double min, double max)
    {
        return x < min ? max : (x > max ? min : x);
    }

    public static int gridIndex(int x, int y, int size, int width)
    {
        x = x / size;
        y = y / size;

        return x + y * width / size;
    }

    public static int gridRows(int count, int size, int width)
    {
        double x = count * size / (double) width;

        return count <= 0 ? 1 : (int) Math.ceil(x);
    }

    /** Sets the quaternion to the given euler angles in degrees.
     * @param yaw the rotation around the y axis in degrees
     * @param pitch the rotation around the x axis in degrees
     * @param roll the rotation around the z axis degrees
     * @return this quaternion */
    public static float[] getQuat(float yaw, float pitch, float roll) {
        return setEulerAnglesRad(yaw * MathUtils.degreesToRadians, pitch * MathUtils.degreesToRadians,
                roll * MathUtils.degreesToRadians);
    }

    /** Sets the quaternion to the given euler angles in radians.
     * @param yaw the rotation around the y axis in radians
     * @param pitch the rotation around the x axis in radians
     * @param roll the rotation around the z axis in radians
     * @return this quaternion */
    public static float[] setEulerAnglesRad (float yaw, float pitch, float roll) {
        final float hr = roll * 0.5f;
        final float shr = (float)Math.sin(hr);
        final float chr = (float)Math.cos(hr);
        final float hp = pitch * 0.5f;
        final float shp = (float)Math.sin(hp);
        final float chp = (float)Math.cos(hp);
        final float hy = yaw * 0.5f;
        final float shy = (float)Math.sin(hy);
        final float chy = (float)Math.cos(hy);
        final float chy_shp = chy * shp;
        final float shy_chp = shy * chp;
        final float chy_chp = chy * chp;
        final float shy_shp = shy * shp;

        float[] quat = new float[4];
        quat[0] = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        quat[1] = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        quat[2] = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        quat[3] = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
        return quat;
    }
}