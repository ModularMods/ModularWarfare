package com.modularwarfare.client.fpp.enhanced.animation;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.handler.ClientTickHandler;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponAnimationType;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.utility.maths.Interpolation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.GUN_CHANGE_Y;

public class AnimationController {

    final EntityPlayerSP player;

    private GunEnhancedRenderConfig config;

    private ActionPlayback playback;

    public static double DEFAULT;
    public static double DRAW;
    public static double ADS;
    public static double RELOAD;
    public static double SPRINT;
    public static double SPRINT_LOOP;
    public static double SPRINT_RANDOM;
    public static double INSPECT=1;
    public static double FIRE;
    public static double MODE_CHANGE;
    
    public static long sprintCoolTime=0;
    public static long sprintLoopCoolTime=0;

    public static int oldCurrentItem;
    public static ItemStack oldItemstack;
    public static boolean isJumping=false;
    
    public static boolean nextResetDefault=false;

    public static double SPRINT_BASIC;

    public boolean hasPlayedDrawSound = false;

    private static AnimationType[] RELOAD_TYPE=new AnimationType[] {
            AnimationType.PRE_LOAD,AnimationType.LOAD,AnimationType.POST_LOAD,
            AnimationType.PRE_UNLOAD,AnimationType.UNLOAD, AnimationType.POST_UNLOAD,
            AnimationType.PRE_RELOAD,AnimationType.RELOAD_FIRST,AnimationType.RELOAD_SECOND,
            AnimationType.RELOAD_FIRST_QUICKLY,AnimationType.RELOAD_SECOND_QUICKLY,
            AnimationType.POST_RELOAD,AnimationType.POST_RELOAD_EMPTY,
    };
    
    private static AnimationType[] FIRE_TYPE=new AnimationType[] {
            AnimationType.FIRE,
            AnimationType.PRE_FIRE, AnimationType.POST_FIRE, 
    };

    public AnimationController(GunEnhancedRenderConfig config){
        this.config = config;
        this.playback = new ActionPlayback(config);
        this.playback.action = AnimationType.DEFAULT;
        this.player = Minecraft.getMinecraft().player;
    }
    
    public void reset(boolean resetSprint) {
        DEFAULT=0;
        DRAW=0;
        hasPlayedDrawSound = false;
        ADS=0;
        RELOAD=0;
        if(resetSprint) {
            SPRINT=0;
        }
        SPRINT_LOOP=0;
        INSPECT=1;
        FIRE=0;
        MODE_CHANGE=1;
        updateActionAndTime();
    }
    
    public void resetView() {
        INSPECT=1;
        MODE_CHANGE=1;
    }

    public void onTickRender(float partialTick) {
        long time=System.currentTimeMillis();
        EnhancedStateMachine anim = ClientRenderHooks.getEnhancedAnimMachine(player);
        float moveDistance=player.distanceWalkedModified-player.prevDistanceWalkedModified;
        /** DEFAULT **/
        double defaultSpeed = config.animations.get(AnimationType.DEFAULT).getSpeed(config.FPS) * partialTick;
        if(DEFAULT==0) {
            if (player.getHeldItemMainhand().getItem() instanceof ItemGun) {
                GunType type=((ItemGun)player.getHeldItemMainhand().getItem()).type;
                type.playClientSound(player, WeaponSoundType.Idle);
            }
        }
        DEFAULT = Math.max(0F,DEFAULT + defaultSpeed);
        if(DEFAULT>1) {
            DEFAULT=0;
        }
        
        /** DRAW **/
        double drawSpeed = config.animations.get(AnimationType.DRAW).getSpeed(config.FPS) * partialTick;
        DRAW = Math.max(0, DRAW + drawSpeed);
        if(DRAW>1F) {
            DRAW=1F;
        }

        /** INSPECT **/
        if(!config.animations.containsKey (AnimationType.INSPECT)) {
            INSPECT=1;
        }else {
            double modeChangeVal = config.animations.get(AnimationType.INSPECT).getSpeed(config.FPS) * partialTick;
            INSPECT+=modeChangeVal;
            if(INSPECT>=1) {
                INSPECT=1;
            }
        }

        /** ADS **/
        boolean aimChargeMisc = ClientRenderHooks.getEnhancedAnimMachine(player).reloading;
        double adsSpeed = config.animations.get(AnimationType.AIM).getSpeed(config.FPS) * partialTick;
        double val = 0;
        if(Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(1) && !aimChargeMisc && INSPECT == 1F) {
            val = ADS + adsSpeed * (2 - ADS);
        } else {
            val = ADS - adsSpeed * (1 + ADS);
        }  
        
        if(!isDrawing()) {
            ADS = Math.max(0, Math.min(1, val));
        }else {
            ADS = 0;
        }
        
        if(!anim.shooting) {
            FIRE=0;
        }
        
        if(!anim.reloading) {
            RELOAD=0;
        }

        /**
         * Sprinting
         */
        if(!config.sprint.basicSprint) {
            double sprintSpeed = 0.15f * partialTick;
            double sprintValue = 0;

            if(player.movementInput.jump) {
                isJumping=true;
            }else if(player.onGround) {
                isJumping=false;
            }

            boolean flag=(player.onGround||player.fallDistance<2f)&&!isJumping;

            if (player.isSprinting() && moveDistance > 0.05 && flag) {
                if (time > sprintCoolTime) {
                    sprintValue = SPRINT + sprintSpeed;
                }
            } else {
                sprintCoolTime = time + 100;
                sprintValue = SPRINT - sprintSpeed;
            }
            if (anim.gunRecoil > 0.1F || ADS > 0.8 || RELOAD > 0) {
                sprintValue = SPRINT - sprintSpeed * 2.5f;
            }

            SPRINT = Math.max(0, Math.min(1, sprintValue));

            /** SPRINT_LOOP **/
            if (!config.animations.containsKey(AnimationType.SPRINT)) {
                SPRINT_LOOP = 0;
                SPRINT_RANDOM = 0;
            } else {
                double sprintLoopSpeed = config.animations.get(AnimationType.SPRINT).getSpeed(config.FPS) * partialTick
                        * (moveDistance / 0.15f);
                boolean flagSprintRand = false;
                if (flag) {
                    if (time > sprintLoopCoolTime) {
                        if (player.isSprinting()) {
                            SPRINT_LOOP += sprintLoopSpeed;
                            SPRINT_RANDOM += sprintLoopSpeed;
                            flagSprintRand = true;
                        }
                    }
                } else {
                    sprintLoopCoolTime = time + 100;
                }
                if (!flagSprintRand) {
                    SPRINT_RANDOM -= config.animations.get(AnimationType.SPRINT).getSpeed(config.FPS) * 3 * partialTick;
                }
                if (SPRINT_LOOP > 1) {
                    SPRINT_LOOP = 0;
                }
                if (SPRINT_RANDOM > 1) {
                    SPRINT_RANDOM = 0;
                }
                if (SPRINT_RANDOM < 0) {
                    SPRINT_RANDOM = 0;
                }
                if (Double.isNaN(SPRINT_RANDOM)) {
                    SPRINT_RANDOM = 0;
                }
            }
        } else {
            /** SPRINT **/
            float sprintSpeed = 0.15f * partialTick;
            float sprintValue = (float) ((player.isSprinting()) ? SPRINT_BASIC + sprintSpeed : SPRINT_BASIC - sprintSpeed);
            if(anim.gunRecoil > 0.1F){
                sprintValue = (float) (SPRINT_BASIC - sprintSpeed*3f);
            }
            SPRINT_BASIC = Math.max(0, Math.min(1, sprintValue));
        }
        
        /** MODE CHANGE **/
        if(!config.animations.containsKey (AnimationType.MODE_CHANGE)) {
            MODE_CHANGE=1;
        }else {
            double modeChangeVal = config.animations.get(AnimationType.MODE_CHANGE).getSpeed(config.FPS) * partialTick;
            MODE_CHANGE+=modeChangeVal;
            if(MODE_CHANGE>=1) {
                MODE_CHANGE=1;
            }
        }
        
        updateActionAndTime();
    }
    
    public AnimationType getPlayingAnimation() {
        return this.playback.action;
    }
    
    public void updateCurrentItem() {
        ItemStack stack = player.getHeldItemMainhand();
        Item item = stack.getItem();
        if (item instanceof ItemGun) {
            GunType type = ((ItemGun) item).type;
            if (!type.allowAimingSprint && ADS > 0.2f) {
                player.setSprinting(false);
            }
            if (!type.allowReloadingSprint && RELOAD > 0f) {
                player.setSprinting(false);
            }
            if (!type.allowFiringSprint && FIRE > 0f) {
                player.setSprinting(false);
            }
        }
        if(oldCurrentItem != player.inventory.currentItem){
            reset(true);
            oldCurrentItem = player.inventory.currentItem;
        }
        if(oldItemstack != player.getHeldItemMainhand()) {
            if(oldItemstack==null||oldItemstack.isEmpty()) {
                reset(true);
            }
            oldItemstack=player.getHeldItemMainhand();
        }
    }
    
    public void updateAction() {
        EnhancedStateMachine anim = ClientRenderHooks.getEnhancedAnimMachine(player);
        boolean flag=nextResetDefault;
        nextResetDefault=false;
        if (DRAW < 1F) {
            if(!hasPlayedDrawSound){
                Item item = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem();
                if (item instanceof ItemGun) {
                    ((ItemGun) item).type.playClientSound(player, WeaponSoundType.Draw);
                    hasPlayedDrawSound = true;
                }
            }
            this.playback.action = AnimationType.DRAW;
        }else if (RELOAD > 0F) {
            resetView();
            this.playback.action = anim.getReloadAnimationType();
        }else if(FIRE>0F) {
            resetView();
            this.playback.action = anim.getShootingAnimationType();
        } else if (INSPECT  < 1) {
            this.playback.action = AnimationType.INSPECT;
        } else if (MODE_CHANGE  < 1) {
            this.playback.action = AnimationType.MODE_CHANGE;
        } else if (this.playback.hasPlayed||this.playback.action != AnimationType.DEFAULT) {
            if(flag) {
                this.playback.action = AnimationType.DEFAULT;
            }
            nextResetDefault=true;
        }
    }


    public void updateTime() {
        EnhancedStateMachine anim = ClientRenderHooks.getEnhancedAnimMachine(player);
        if(this.playback.action==null) {
            return;
        }
        switch (this.playback.action){
            case DEFAULT:
                this.playback.updateTime(DEFAULT);
                //this.playback.time = this.config.animations.get(AnimationType.DEFAULT).getStartTime();
                break;
            case DRAW:
                this.playback.updateTime(DRAW);
                break;
            case INSPECT:
                this.playback.updateTime(INSPECT);
                break;
            case MODE_CHANGE:
                this.playback.updateTime(MODE_CHANGE);
                break;
        default:
            break;
        }
        for(AnimationType reloadType:RELOAD_TYPE) {
            if(this.playback.action==reloadType) {
                this.playback.updateTime(RELOAD);
                break;
            }  
        }
        for(AnimationType fireType:FIRE_TYPE) {
            if(this.playback.action==fireType) {
                this.playback.updateTime(FIRE);
                break;
            }  
        }
    }
    
    public void updateActionAndTime() {
        updateAction();
        updateTime();
    }

    public float getTime(){
        //return (280+(330-280)*(System.currentTimeMillis()%5000/5000f))/24f;
        return (float)playback.time;
    }
    
    public float getSprintTime(){
        if(config.animations.get(AnimationType.SPRINT)==null) {
            return 0;
        }
        double startTime = config.animations.get(AnimationType.SPRINT).getStartTime(config.FPS);
        double endTime = config.animations.get(AnimationType.SPRINT).getEndTime(config.FPS);
        double result=Interpolation.LINEAR.interpolate(startTime, endTime, SPRINT_LOOP);
        if(Double.isNaN(result)) {
            return 0;
        }
        return(float) result;
    }

    public void setConfig(GunEnhancedRenderConfig config){
        this.config = config;
    }

    public GunEnhancedRenderConfig getConfig(){
        return this.config;
    }
    
    public boolean isDrawing() {
        Item item = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem();
        if (item instanceof ItemGun) {
            if (((ItemGun) item).type.animationType.equals(WeaponAnimationType.ENHANCED)) {
                return this.playback.action == AnimationType.DRAW;
            }
        }
        return false;
    }
    
    public boolean isCouldReload() {
        Item item = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem();
        if (item instanceof ItemGun) {
            if (((ItemGun) item).type.animationType.equals(WeaponAnimationType.ENHANCED)) {
                if (isDrawing()) {
                    return false;
                }
                if(ClientRenderHooks.getEnhancedAnimMachine(player).reloading) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean isCouldShoot() {
        Item item = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem();
        if (item instanceof ItemGun) {
            if (((ItemGun) item).type.animationType.equals(WeaponAnimationType.ENHANCED)) {
                if (isDrawing()) {
                    return false;
                }
                if(ClientRenderHooks.getEnhancedAnimMachine(player).reloading) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public ItemStack getRenderAmmo(ItemStack ammo) {
        EnhancedStateMachine anim = ClientRenderHooks.getEnhancedAnimMachine(player);
        if(anim.reloading) {
            AnimationType reloadAni=anim.getReloadAnimationType();
            if (anim.getReloadType() == ReloadType.Full && (reloadAni == AnimationType.PRE_RELOAD
                    || reloadAni == AnimationType.RELOAD_FIRST || reloadAni == AnimationType.RELOAD_FIRST_QUICKLY)) {
                return ammo;
            }
            if (reloadAni == AnimationType.PRE_UNLOAD || reloadAni == AnimationType.UNLOAD|| reloadAni == AnimationType.POST_UNLOAD) {
                return ammo;
            }  
        }
        if (ClientTickHandler.reloadEnhancedPrognosisAmmoRendering != null
                && !ClientTickHandler.reloadEnhancedPrognosisAmmoRendering.isEmpty()) {
            return ClientTickHandler.reloadEnhancedPrognosisAmmoRendering;
        }
        return ammo;
    }
    
    public boolean shouldRenderAmmo() {
        EnhancedStateMachine anim = ClientRenderHooks.getEnhancedAnimMachine(player);
        if(anim.reloading) {
            if(anim.getReloadAnimationType()==AnimationType.POST_UNLOAD) {
                return false;
            }
            return true;
        }
        if (ClientTickHandler.reloadEnhancedPrognosisAmmoRendering != null
                && !ClientTickHandler.reloadEnhancedPrognosisAmmoRendering.isEmpty()) {
            return true;
        }
        return ItemGun.hasAmmoLoaded(Minecraft.getMinecraft().player.getHeldItemMainhand());
    }

}
