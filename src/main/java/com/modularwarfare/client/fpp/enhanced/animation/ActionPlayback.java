package com.modularwarfare.client.fpp.enhanced.animation;

import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.utility.maths.Interpolation;

public class ActionPlayback {

    public AnimationType action;

    public float time;

    public boolean hasPlayed;

    private GunEnhancedRenderConfig config;

    public ActionPlayback(GunEnhancedRenderConfig config){
        this.config = config;
    }

    public void updateTime(float alpha){
        float startTime = config.animations.get(action).getStartTime();
        float endTime = config.animations.get(action).getEndTime();
        this.time = Interpolation.LINEAR.interpolate(startTime, endTime, alpha);
 
        if(this.time >= endTime){
            this.hasPlayed = true;
        } else {
            this.hasPlayed = false;
        }
    }
}
