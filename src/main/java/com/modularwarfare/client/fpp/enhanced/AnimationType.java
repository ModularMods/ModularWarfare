package com.modularwarfare.client.fpp.enhanced;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

@JsonAdapter(AnimationType.AnimationTypeJsonAdapter.class)
public enum AnimationType {
    DEFAULT("default"),
    DRAW("draw"),
    AIM("aim"),
    INSPECT("inspect"),
    PRE_LOAD("preLoad"),
    LOAD("load"),
    POST_LOAD("postLoad"),
    PRE_UNLOAD("preUnload"),
    UNLOAD("unload"),
    POST_UNLOAD("postUnload"),
    PRE_RELOAD("preReload"),
    RELOAD_FIRST("reloadFirst"),
    RELOAD_SECOND("reloadSecond"),
    RELOAD_FIRST_QUICKLY("reloadFirstQuickly"),
    RELOAD_SECOND_QUICKLY("reloadSecondQuickly"),
    POST_RELOAD("postReload"),
    POST_RELOAD_EMPTY("postReloadEmpty"),
    PRE_FIRE("preFire"),
    FIRE("fire"),
    POST_FIRE("postFire"),
    MODE_CHANGE("modeChange"),
    SPRINT("sprint");

    public String serializedName;
    private AnimationType(String name) {
        serializedName=name;
    }
    
    public static class AnimationTypeJsonAdapter extends TypeAdapter<AnimationType>{

        @Override
        public AnimationType read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                throw new AnimationTypeException("wrong animation type format");
            }
            return fromString(in.nextString());
        }

        @Override
        public void write(JsonWriter out, AnimationType t) throws IOException {
            out.value(t.serializedName);
        }
        
        public static class AnimationTypeException extends RuntimeException{
            public AnimationTypeException(String str) {
                super(str);
            }
        }

        public static AnimationType fromString(String modeName) {
            for (AnimationType animationType : values()) {
                if (animationType.serializedName.equalsIgnoreCase(modeName)) {
                    return animationType;
                }
            }
            throw new AnimationTypeException("wrong animation type:"+modeName);
        }
        
    }
}
