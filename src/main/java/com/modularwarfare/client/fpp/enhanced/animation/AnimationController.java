package com.modularwarfare.client.fpp.enhanced.animation;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.GUN_CHANGE_Y;


public class AnimationController {

    final EntityPlayer player;

    private GunEnhancedRenderConfig config;

    private ActionPlayback playback;

    public static float DRAW;
    public static float ADS;
    public static float RELOAD;

    public static int oldCurrentItem;

    public AnimationController(GunEnhancedRenderConfig config){
        this.config = config;
        this.playback = new ActionPlayback(config);
        this.playback.action = AnimationType.DEFAULT;
        this.player = Minecraft.getMinecraft().player;
    }

    public void onTickRender(float partialTick) {
        EnhancedStateMachine stateMachine = ClientRenderHooks.getEnhancedAnimMachine(player);

        /** DRAW **/
        float drawSpeed = config.animations.get(AnimationType.DRAW).speed * partialTick;
        DRAW = Math.max(0, Math.min(1F, DRAW + drawSpeed));

        /** ADS **/
        float adsSpeed = config.animations.get(AnimationType.AIM_IN).speed * partialTick;
        float val = (Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(1)) ? ADS + adsSpeed : ADS - adsSpeed;
        ADS = Math.max(0, Math.min(1, val));

        if (DRAW > 0F && DRAW < 1F && (oldCurrentItem != player.inventory.currentItem)) {
            this.playback.action = AnimationType.DRAW;
        } else if (ADS > 0F && Mouse.isButtonDown(1)) {
            this.playback.action = AnimationType.AIM_IN;
        } else if (this.playback.action == AnimationType.AIM_IN && this.playback.hasPlayed && !Mouse.isButtonDown(1)) {
            this.playback.action = AnimationType.AIM_OUT;
        } else if (RELOAD > 0F) {
            this.playback.action = AnimationType.RELOAD;
        } else if (this.playback.hasPlayed) {
            this.playback.action = AnimationType.DEFAULT;
        }

        if(oldCurrentItem != player.inventory.currentItem){
            DRAW = 0;
            oldCurrentItem = player.inventory.currentItem;
        }
        updateTime();
    }


    public void updateTime() {
        switch (this.playback.action){
            case DEFAULT:
                this.playback.time = this.config.animations.get(AnimationType.DEFAULT).getStartTime();
                break;
            case DRAW:
                this.playback.updateTime(DRAW);
                if(this.playback.hasPlayed){
                    oldCurrentItem = player.inventory.currentItem;
                    DRAW = 0F;
                }
                break;
            case AIM_IN:
                this.playback.updateTime(ADS);
                break;
            case AIM_OUT:
                this.playback.updateTime(1F-ADS);
                break;
            case RELOAD:
                this.playback.updateTime(RELOAD);
                break;

        }
    }

    public float getTime(){
        return playback.time;
    }

    public void setConfig(GunEnhancedRenderConfig config){
        this.config = config;
    }

    public GunEnhancedRenderConfig getConfig(){
        return this.config;
    }

}
