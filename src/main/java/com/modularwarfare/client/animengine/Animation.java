package com.modularwarfare.client.animengine;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.List;

public class Animation {

    public HashMap<String, Keyframes> bones;
    public float length;


    public class Keyframes {
        public List<Keyframe> keyframes;
    }

    public class Keyframe implements Comparable<Keyframe> {
        public Integer frame;
        public Vector3f position = new Vector3f(0.0F, 0.0F, 0.0F);
        public Vector3f rotation = new Vector3f(0.0F, 0.0F, 0.0F);
        public Vector3f scale = new Vector3f(0.0F, 0.0F, 0.0F);

        @Override
        public int compareTo(Keyframe o) {
            return this.frame.compareTo(o.frame);
        }
    }

    public static final class PairKeyframe {

        private final Keyframe first;
        private final Keyframe second;

        public PairKeyframe(Keyframe first, Keyframe second) {
            this.first = first;
            this.second = second;
        }

        public Keyframe getPrev() {
            return first;
        }

        public Keyframe getNext() {
            return second;
        }
    }
}
