package com.modularwarfare.client.fpp.basic.models.objects;

import org.lwjgl.util.vector.Vector3f;

public class RenderVariables {

    public Vector3f offset;
    public Vector3f scale;
    public Float angle;
    public Vector3f rotation;

    public RenderVariables(Vector3f offset, Vector3f scale) {
        this.offset = offset;
        this.scale = scale;
    }

    public RenderVariables(Vector3f offset) {
        this.offset = offset;
    }

    public RenderVariables(Vector3f offset, float scale) {
        this(offset, new Vector3f(scale, scale, scale));
    }

    public RenderVariables(Vector3f offset, float angle, Vector3f rotation) {
        this.offset = offset;
        this.angle = angle;
        this.rotation = rotation;
    }

}
