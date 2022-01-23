package com.modularwarfare.client.animengine;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import java.util.List;

public class AnimationManager {


    public static void applyAnimation(Animation anim, String bone, double tickTime){
        double time = tickTime;
        float localTime = (float)(time % anim.length);
        float worldScale = 1F/16F;

        final Animation.PairKeyframe val = getPrevAndNext(localTime, anim.bones.get(bone).keyframes);

        Vector3f interpTr = interpolateVec3(localTime, EnumAction.TRANSLATION, val.getPrev(), val.getNext());
        Vector3f interpRot = interpolateVec3(localTime, EnumAction.ROTATION, val.getPrev(), val.getNext());

        /**
         * Ne pas toucher
         */
        GL11.glTranslatef(interpTr.x * worldScale, interpTr.z * worldScale, -interpTr.y * worldScale);

        GL11.glRotatef(-90, 1,0,0);
        GL11.glRotatef(interpRot.x, 1,0,0);
        GL11.glRotatef(interpRot.z, 0,1,0);
        GL11.glRotatef(-interpRot.y, 0,0,1);
    }

    public static Vector3f interpolateVec3(float time, EnumAction action, Animation.Keyframe prev, Animation.Keyframe next){

        float size = next.frame - prev.frame;
        float step = (time - prev.frame) / size;

        switch (action){
            case TRANSLATION:
                Vector3f prevVecTr = prev.position;
                Vector3f nextVecTr = next.position;
                if(next.frame == prev.frame){
                    return next.position;
                }
                return new Vector3f(prevVecTr.x + (nextVecTr.x - prevVecTr.x) * step, prevVecTr.y + (nextVecTr.y - prevVecTr.y) * step, prevVecTr.z + (nextVecTr.z - prevVecTr.z) * step);
            case ROTATION:
                Vector3f prevVecRt = prev.rotation;
                Vector3f nextVecRt = next.rotation;
                if(next.frame == prev.frame){
                    return next.rotation;
                }
                return new Vector3f(prevVecRt.x + (nextVecRt.x - prevVecRt.x) * step, prevVecRt.y + (nextVecRt.y - prevVecRt.y) * step, prevVecRt.z + (nextVecRt.z - prevVecRt.z) * step);
            default:
                return new Vector3f(0,0,0);
        }
    }

    public static Animation.PairKeyframe getPrevAndNext(float time, List<Animation.Keyframe> keyframes) {
        Animation.Keyframe prev = keyframes.get(0);
        Animation.Keyframe next = keyframes.get(keyframes.size() - 1);

        for (Animation.Keyframe keyframe : keyframes) {
            if (keyframe.frame <= time && keyframe.frame > prev.frame) {
                prev = keyframe;
            }
            if (keyframe.frame > time && keyframe.frame < next.frame) {
                next = keyframe;
            }
        }
        return new Animation.PairKeyframe(prev, next);
    }

    public enum EnumAction {
        TRANSLATION, ROTATION, SCALE
    }

}
