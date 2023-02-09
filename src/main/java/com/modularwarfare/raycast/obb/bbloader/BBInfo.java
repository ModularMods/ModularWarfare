package com.modularwarfare.raycast.obb.bbloader;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

public class BBInfo {
    @SerializedName("groups")
    public ArrayList<Group> groups;
    @SerializedName("cubes")
    public ArrayList<Cube> cubes;
}
