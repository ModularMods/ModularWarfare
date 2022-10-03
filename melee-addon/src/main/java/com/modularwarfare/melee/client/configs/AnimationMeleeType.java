package com.modularwarfare.melee.client.configs;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

@JsonAdapter(AnimationMeleeType.AnimationTypeJsonAdapter.class)
public enum AnimationMeleeType {
    DEFAULT("default"),
    DRAW("draw"),
    INSPECT("inspect"),
    ATTACK("attack"),
    SPRINT("sprint");

    public String serializedName;

    private AnimationMeleeType(String name) {
        serializedName = name;
    }

    public static class AnimationTypeJsonAdapter extends TypeAdapter<AnimationMeleeType> {

        public static AnimationMeleeType fromString(String modeName) {
            for (AnimationMeleeType animationType : values()) {
                if (animationType.serializedName.equalsIgnoreCase(modeName)) {
                    return animationType;
                }
            }
            throw new AnimationTypeException("wrong animation type:" + modeName);
        }

        @Override
        public AnimationMeleeType read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                throw new AnimationTypeException("wrong animation type format");
            }
            return fromString(in.nextString());
        }

        @Override
        public void write(JsonWriter out, AnimationMeleeType t) throws IOException {
            out.value(t.serializedName);
        }

        public static class AnimationTypeException extends RuntimeException {
            public AnimationTypeException(String str) {
                super(str);
            }
        }

    }
}
