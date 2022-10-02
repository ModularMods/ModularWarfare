package com.modularwarfare.melee.client.animation;

import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.enhanced.animation.EnhancedStateMachine;

import com.modularwarfare.melee.client.configs.AnimationMeleeType;
import com.modularwarfare.melee.client.configs.MeleeRenderConfig;
import com.modularwarfare.melee.common.melee.ItemMelee;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public class AnimationMeleeController {

    final EntityPlayerSP player;

    private MeleeRenderConfig config;

    private ActionPlaybackMelee playback;

    public static double DEFAULT;
    public static double DRAW;
    public static double INSPECT=1;
    public static double ATTACK;

    public static int oldCurrentItem;
    public static ItemStack oldItemstack;

    public static boolean nextResetDefault=false;

    public AnimationMeleeController(MeleeRenderConfig config){
        this.config = config;
        this.playback = new ActionPlaybackMelee(config);
        this.playback.action = AnimationMeleeType.DEFAULT;
        this.player = Minecraft.getMinecraft().player;
    }
    
    public void reset() {
        DEFAULT=0;
        DRAW=0;
        INSPECT=1;
        updateActionAndTime();
    }
    
    public void resetView() {
        INSPECT=1;
    }

    public void onTickRender(float partialTick) {
        /** DEFAULT **/
        double defaultSpeed = config.animations.get(AnimationMeleeType.DEFAULT).getSpeed(config.FPS) * partialTick;
        DEFAULT = Math.max(0F,DEFAULT + defaultSpeed);
        if(DEFAULT>1) {
            DEFAULT=0;
        }

        /** DRAW **/
        double drawSpeed = config.animations.get(AnimationMeleeType.DRAW).getSpeed(config.FPS) * partialTick;
        DRAW = Math.max(0, DRAW + drawSpeed);
        if(DRAW>1F) {
            DRAW=1F;
        }

        /** INSPECT **/
        if(!config.animations.containsKey (AnimationMeleeType.INSPECT)) {
            INSPECT=1;
        }else {
            double modeChangeVal = config.animations.get(AnimationMeleeType.INSPECT).getSpeed(config.FPS) * partialTick;
            INSPECT+=modeChangeVal;
            if(INSPECT>=1) {
                INSPECT=1;
            }
        }
        updateActionAndTime();
    }
    
    public AnimationMeleeType getPlayingAnimation() {
        return this.playback.action;
    }
    
    public void updateCurrentItem() {
        ItemStack stack = player.getHeldItemMainhand();
        if(oldCurrentItem != player.inventory.currentItem){
            reset();
            oldCurrentItem = player.inventory.currentItem;
        }
        if(oldItemstack != player.getHeldItemMainhand()) {
            if(oldItemstack==null||oldItemstack.isEmpty()) {
                reset();
            }
            oldItemstack=player.getHeldItemMainhand();
        }
    }
    
    public void updateAction() {
        boolean flag=nextResetDefault;
        nextResetDefault=false;
        if (DRAW < 1F) {
            this.playback.action = AnimationMeleeType.DRAW;
        } else if(ATTACK>0F) {
            resetView();
            this.playback.action = AnimationMeleeType.ATTACK;
        } else if (INSPECT  < 1) {
            this.playback.action = AnimationMeleeType.INSPECT;
        } else if (this.playback.hasPlayed || this.playback.action != AnimationMeleeType.DEFAULT) {
            if(flag) {
                this.playback.action = AnimationMeleeType.DEFAULT;
            }
            nextResetDefault=true;
        }
    }


    public void updateTime() {
        if(this.playback.action==null) {
            return;
        }
        switch (this.playback.action){
            case DEFAULT:
                this.playback.updateTime(DEFAULT);
                break;
            case DRAW:
                this.playback.updateTime(DRAW);
                break;
            case INSPECT:
                this.playback.updateTime(INSPECT);
                break;
            case ATTACK:
                this.playback.updateTime(ATTACK);
                break;
        default:
            break;
        }
    }
    
    public void updateActionAndTime() {
        updateAction();
        updateTime();
    }

    public float getTime(){
        return (float)playback.time;
    }


    public void setConfig(MeleeRenderConfig config){
        this.config = config;
    }

    public MeleeRenderConfig getConfig(){
        return this.config;
    }
    
    public boolean isDrawing() {
        Item item = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem();
        if (item instanceof ItemMelee) {
            return this.playback.action == AnimationMeleeType.DRAW;
        }
        return false;
    }

    public boolean isCouldAttack() {
        Item item = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem();
        if (item instanceof ItemMelee) {
            if (isDrawing()) {
                return false;
            }
        }
        return true;
    }
}
