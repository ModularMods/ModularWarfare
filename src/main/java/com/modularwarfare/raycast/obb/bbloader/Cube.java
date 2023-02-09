package com.modularwarfare.raycast.obb.bbloader;

import com.google.gson.annotations.SerializedName;

public class Cube {
    @SerializedName("name")
    public String name;
    @SerializedName("parent")
    public String parent;
    @SerializedName("from")
    public float[] from;
    @SerializedName("to")
    public float[] to;
}
