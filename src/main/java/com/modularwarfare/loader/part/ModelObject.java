package com.modularwarfare.loader.part;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ModelObject {
    public String name;
    public ArrayList<Face> faces = new ArrayList<>();
    public int glDrawingMode;

    public ModelObject() {
        this("");
    }

    public ModelObject(String name) {
        this(name, -1);
    }

    public ModelObject(String name, int glDrawingMode) {
        this.name = name;
        this.glDrawingMode = glDrawingMode;
    }

    @SideOnly(Side.CLIENT)
    public void render(BufferBuilder renderer, float scale) {
        if (faces.size() > 0) {
            for (Face face : faces) {
                face.render(glDrawingMode, renderer, scale);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public int renderByVAO(List<Float> renderer, float scale) {
        int flag = 0;
        if (faces.size() > 0) {
            for (Face face : faces) {
                flag = face.renderByVAO(glDrawingMode, renderer, scale);
            }
        }
        return flag;
    }
}