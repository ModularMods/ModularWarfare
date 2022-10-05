package com.modularwarfare.melee.client.animation;

import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.melee.client.configs.AnimationMeleeType;
import com.modularwarfare.melee.client.configs.MeleeRenderConfig;
import com.modularwarfare.melee.common.melee.ItemMelee;
import com.modularwarfare.melee.common.melee.MeleeType;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Random;


public class AnimationMeleeController {

    public static double DEFAULT;

    public static double DRAW;

    public static double INSPECT = 1;

    public static double ATTACK = 1;

    HashMap<AnimationMeleeType, Integer> currentRandomAnim = new HashMap<>();

    public static int oldCurrentItem;
    public static ItemStack oldItemstack;
    public static boolean nextResetDefault = false;
    final EntityPlayerSP player;
    private MeleeRenderConfig config;
    private ActionPlaybackMelee playback;

    public AnimationMeleeController(MeleeRenderConfig config) {
        this.config = config;
        this.playback = new ActionPlaybackMelee(config);
        this.playback.action = AnimationMeleeType.DEFAULT;
        this.player = Minecraft.getMinecraft().player;

        currentRandomAnim.put(AnimationMeleeType.DRAW, 0);
        currentRandomAnim.put(AnimationMeleeType.INSPECT, 0);
        currentRandomAnim.put(AnimationMeleeType.ATTACK, 0);
    }

    public void reset() {
        DEFAULT = 0;
        DRAW = 0;
        INSPECT = 1;
        ATTACK = 1;

        currentRandomAnim.put(AnimationMeleeType.DRAW, 0);
        currentRandomAnim.put(AnimationMeleeType.INSPECT, 0);
        currentRandomAnim.put(AnimationMeleeType.ATTACK, 0);
        updateActionAndTime();
    }

    public void resetView() {
        INSPECT = 1;
        DRAW = 1;
    }

    public void onTickRender(float partialTick) {
        /** DEFAULT **/
        double defaultSpeed = config.animations.get(AnimationMeleeType.DEFAULT).get(0).getSpeed(config.FPS) * partialTick;
        DEFAULT = Math.max(0F, DEFAULT + defaultSpeed);
        if (DEFAULT > 1) {
            DEFAULT = 0;
        }

        /** DRAW **/
        double drawSpeed = config.animations.get(AnimationMeleeType.DRAW).get(currentRandomAnim.get(AnimationMeleeType.DRAW)).getSpeed(config.FPS) * partialTick;
        DRAW = Math.max(0, DRAW + drawSpeed);
        if (DRAW > 1F) {
            DRAW = 1F;
        }

        /** INSPECT **/
        if (!config.animations.containsKey(AnimationMeleeType.INSPECT)) {
            INSPECT = 1;
        } else {
            double modeChangeVal = config.animations.get(AnimationMeleeType.INSPECT).get(currentRandomAnim.get(AnimationMeleeType.INSPECT)).getSpeed(config.FPS) * partialTick;
            INSPECT += modeChangeVal;
            if (INSPECT >= 1) {
                INSPECT = 1;
            }
        }
        /** ATTACK **/
        if (config.animations.isEmpty()) {
            ATTACK = 1;
        } else {
            double modeChangeVal = config.animations.get(AnimationMeleeType.ATTACK).get(currentRandomAnim.get(AnimationMeleeType.ATTACK)).getSpeed(config.FPS) * partialTick;
            ATTACK += modeChangeVal;
            if (ATTACK >= 1) {
                ATTACK = 1;
            }
        }
        updateActionAndTime();
    }

    public AnimationMeleeType getPlayingAnimation() {
        return this.playback.action;
    }

    public void updateCurrentItem() {
        ItemStack stack = player.getHeldItemMainhand();
        if (oldCurrentItem != player.inventory.currentItem) {
            reset();
            oldCurrentItem = player.inventory.currentItem;
            if (stack.getItem() instanceof ItemMelee) {
                ((ItemMelee)stack.getItem()).type.playClientSound(player, WeaponSoundType.MeleeDraw);
            }
        }
        if (oldItemstack != player.getHeldItemMainhand()) {
            if (oldItemstack == null || oldItemstack.isEmpty()) {
                reset();
            }
            oldItemstack = player.getHeldItemMainhand();
        }
    }

    public void updateAction() {
        boolean flag = nextResetDefault;
        nextResetDefault = false;
        if (ATTACK < 1) {
            this.playback.action = AnimationMeleeType.ATTACK;
            resetView();
        } else if (DRAW < 1F) {
            this.playback.action = AnimationMeleeType.DRAW;
        } else if (INSPECT < 1) {
            this.playback.action = AnimationMeleeType.INSPECT;
        } else if (this.playback.hasPlayed || this.playback.action != AnimationMeleeType.DEFAULT) {
            if (flag) {
                this.playback.action = AnimationMeleeType.DEFAULT;
            }
            nextResetDefault = true;
        }
    }


    public void updateTime() {
        if (this.playback.action == null) {
            return;
        }
        switch (this.playback.action) {
            case DEFAULT:
                this.playback.updateTime(0, DEFAULT);
                break;
            case DRAW:
                this.playback.updateTime(0, DRAW);
                break;
            case INSPECT:
                this.playback.updateTime(0, INSPECT);
                break;
            case ATTACK:
                this.playback.updateTime(currentRandomAnim.get(AnimationMeleeType.ATTACK), ATTACK);
                break;
            default:
                break;
        }
    }

    public void updateActionAndTime() {
        updateAction();
        updateTime();
    }

    public float getTime() {
        return (float) playback.time;
    }

    public MeleeRenderConfig getConfig() {
        return this.config;
    }

    public void setConfig(MeleeRenderConfig config) {
        this.config = config;
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

    public void applyAnim(AnimationMeleeType type){
        Item item = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem();
        if (item instanceof ItemMelee) {
            MeleeType meleeType = ((ItemMelee) item).type;
            switch (type){
                case ATTACK:
                    if(meleeType.resetAttackOnClick) {
                        applyRandomAnim(AnimationMeleeType.ATTACK);
                        ATTACK = 0;
                        ((ItemMelee) item).type.playClientSound(player, WeaponSoundType.MeleeAttack);
                    } else if(ATTACK == 1F){
                        applyRandomAnim(AnimationMeleeType.ATTACK);
                        ATTACK = 0;
                        ((ItemMelee) item).type.playClientSound(player, WeaponSoundType.MeleeAttack);
                    }
                    break;
                case INSPECT:
                    if (INSPECT == 1F) {
                        applyRandomAnim(AnimationMeleeType.INSPECT);
                        INSPECT = 0;
                        ((ItemMelee) item).type.playClientSound(player, WeaponSoundType.MeleeInspect);
                    }
                    break;
            }
        }
    }

    public void applyRandomAnim(AnimationMeleeType meleeType) {
        Random rand = new Random();
        currentRandomAnim.put(meleeType, rand.nextInt(config.animations.get(meleeType).size()));
    }
}
