package com.modularwarfare.raycast.obb.bbloader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.modularwarfare.common.vector.Vector3f;
import com.modularwarfare.raycast.obb.OBBModelBone;
import com.modularwarfare.raycast.obb.OBBModelBox;
import com.modularwarfare.raycast.obb.OBBModelObject;
import com.modularwarfare.raycast.obb.OBBModelScene;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class BlockBenchOBBInfoLoader {
    public static HashMap<ResourceLocation, BBInfo> infoCache=new HashMap<ResourceLocation, BBInfo>();
    
    public static <T extends OBBModelObject> T loadOBBInfo(Class<T> clazz, ResourceLocation loc){
        Gson gson = new Gson();
        try {
            BBInfo info =null;
            if(infoCache.containsKey(loc)) {
                info=infoCache.get(loc);
            }else {
                InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
                info= gson.fromJson(new InputStreamReader(stream), BBInfo.class);
                stream.close();
                infoCache.put(loc, info);
            }
            OBBModelObject object;
            try {
                object = clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
            OBBModelScene scene = new OBBModelScene();

            object.scene = scene;

            HashMap<String, OBBModelBone> bones = new HashMap<String, OBBModelBone>();
            info.groups.forEach((g) -> {
                OBBModelBone bone = new OBBModelBone();
                bone.name = g.name;
                bone.oirign = new Vector3f();
                bone.oirign.x = g.origin[0];
                bone.oirign.y = g.origin[1];
                bone.oirign.z = g.origin[2];
                bones.put(g.name, bone);
            });
            info.groups.forEach((g) -> {
                if (!g.parent.equals("undefined")) {
                    OBBModelBone parent = bones.get(g.parent);
                    parent.children.add(bones.get(g.name));
                    bones.get(g.name).parent = parent;
                } else {
                    scene.rootBones.add(bones.get(g.name));
                }
            });

            info.cubes.forEach((c) -> {
                OBBModelBox box = new OBBModelBox();
                Vector3f from = new Vector3f(c.from[0], c.from[1], c.from[2]);
                Vector3f to = new Vector3f(c.to[0], c.to[1], c.to[2]);
                Vector3f size = new Vector3f((to.x - from.x) / 2, (to.y - from.y) / 2, (to.z - from.z) / 2);
                Vector3f center = new Vector3f(from.x + size.x, from.y + size.y, from.z + size.z);
                OBBModelBone bone = bones.get(c.parent);
                if (bone == null) {
                    throw new RuntimeException();
                }
                box.name = c.name;
                box.center = center;
                box.anchor = new Vector3f(box.center.x - bone.oirign.x, box.center.y - bone.oirign.y,
                        box.center.z - bone.oirign.z);
                box.size = size;
                box.rotation = new Vector3f(0, 0, 0);
                object.boxes.add(box);
                object.boneBinding.put(box, bone);
            });
            return (T) object;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
