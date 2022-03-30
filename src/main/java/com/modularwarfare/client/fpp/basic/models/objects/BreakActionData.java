package com.modularwarfare.client.fpp.basic.models.objects;

import org.lwjgl.util.vector.Vector3f;

public class BreakActionData {

    public String modelName;
    public Vector3f breakPoint;
    public float angle;
    public boolean scopePart;

    public BreakActionData(String modelName, Vector3f breakPoint, float angle, boolean scopePart) {
        this.modelName = modelName;
        this.breakPoint = breakPoint;
        this.angle = angle;
        this.scopePart = scopePart;
    }

}
