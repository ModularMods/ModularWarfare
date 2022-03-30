package com.modularwarfare.client.fpp.enhanced.transforms;

public class DefaultTransform {

    public float[] translation = new float[3];
    public float[] rotation = new float[4];
    public float[] scale = new float[3];
    public float[] weight = new float[3];

    public DefaultTransform(float[] translation, float[] rotation, float[] scale, float[] weight){
        if(translation != null){
            this.translation = translation;
        }
        if(rotation != null){
            this.rotation = rotation;
        }
        if(scale != null){
            this.scale = scale;
        }
        if(weight != null){
            this.weight = weight;
        }
    }
}
