package com.modularwarfare.client.fpp.enhanced.animation;

import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.utility.maths.Interpolation;

import java.util.Random;

public class ActionPlayback {

    public AnimationType action;

    public double time;

    public boolean hasPlayed;

    private GunEnhancedRenderConfig config;

    public ActionPlayback(GunEnhancedRenderConfig config){
        this.config = config;
    }

    public void updateTime(double alpha){
        if(config.animations.get(action)==null) {
            return;
        }
        double startTime = config.animations.get(action).get(AnimationController.currentRandomAnim.get(action)).getStartTime(config.FPS);
        double endTime = config.animations.get(action).get(AnimationController.currentRandomAnim.get(action)).getEndTime(config.FPS);
        this.time = Interpolation.LINEAR.interpolate(startTime, endTime, alpha);
        checkPlayed();
        //System.out.println(this.time-endTime);
    }
    
    public boolean checkPlayed() {
        double endTime = config.animations.get(action).get(AnimationController.currentRandomAnim.get(action)).getEndTime(config.FPS);
        if(this.time >= endTime){
            this.hasPlayed = true;
        } else {
            this.hasPlayed = false;
        }
        return hasPlayed;
    }

    public void setAction(AnimationType type){
        if(this.action != type){
            applyRandomAnim(type);
        }
        this.action = type;
    }

    public void applyRandomAnim(AnimationType type) {
        Random rand = new Random();
        AnimationController.currentRandomAnim.put(type, rand.nextInt(config.animations.get(type).size()));
    }
}
