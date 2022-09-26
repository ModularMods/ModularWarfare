package com.modularwarfare.client.fpp.basic.renderers;

import com.modularwarfare.api.WeaponAnimation;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.client.fpp.basic.animations.StateEntry;
import com.modularwarfare.client.fpp.basic.animations.StateType;
import com.modularwarfare.client.model.ModelGun;
import com.modularwarfare.utility.NumberHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.Optional;

public class RenderArms {

    /*
     * GL Calls are run from the bottom up
     * For ease of use the following order should be followed at all times;
     * glScale
     * glTranslate
     * glRotate
     * This will allow for translations without having to account for rotation
     */

    // right hand pump action animation
    public static void renderToFrom(ModelGun model, AnimStateMachine anim, float smoothing, Vector3f targetRot, Vector3f targetPos, Vector3f originRot, Vector3f originPos, boolean leftHand) {
        float progress = anim.getReloadState().isPresent() ? anim.getReloadState().get().currentValue : anim.getShootState().isPresent() ? anim.getShootState().get().currentValue : 1f;

        if (NumberHelper.subtractVector(targetPos, originPos) != null) {
            Vector3f offsetPosition = NumberHelper.multiplyVector(NumberHelper.subtractVector(targetPos, originPos), progress);
            float cancelOut = anim.getReloadState().isPresent() ? anim.getReloadState().get().stateType == StateType.ReturnHands ? 0f : 1f : anim.getShootState().isPresent() ? anim.getShootState().get().stateType == StateType.ReturnHands ? 0f : 1f : 1f;
            GL11.glTranslatef(originPos.x + offsetPosition.x + (cancelOut * (Math.abs(1f + (1f - 1f) * smoothing) * (model.config.bolt.chargeModifier.x * model.config.extra.modelScale))), 0F, 0F);
            GL11.glTranslatef(0F, originPos.y + offsetPosition.y + (cancelOut * (Math.abs(1f + (1f - 1f) * smoothing) * (model.config.bolt.chargeModifier.y * model.config.extra.modelScale))), 0F);
            GL11.glTranslatef(0F, 0F, originPos.z + offsetPosition.z + (cancelOut * (Math.abs(1f + (1f - 1f) * smoothing) * (model.config.bolt.chargeModifier.z * model.config.extra.modelScale))));

            //Rotation
            Vector3f offsetRotation = NumberHelper.multiplyVector(NumberHelper.subtractVector(targetRot, originRot), progress);
            if (leftHand) {
                GL11.glTranslatef(0.225F, 0.75F, 0);
                GL11.glRotatef(originRot.x + offsetRotation.x, 1F, 0F, 0F);
                GL11.glRotatef(originRot.y + offsetRotation.y, 0F, 1F, 0F);
                GL11.glRotatef(originRot.z + offsetRotation.z, 0F, 0F, 1F);
                GL11.glTranslatef(-0.225F, -0.75F, 0);
            } else {
                GL11.glTranslatef(-0.225F, 0.75F, 0);
                GL11.glRotatef(originRot.x + offsetRotation.x, 1F, 0F, 0F);
                GL11.glRotatef(originRot.y + offsetRotation.y, 0F, 1F, 0F);
                GL11.glRotatef(originRot.z + offsetRotation.z, 0F, 0F, 1F);
                GL11.glTranslatef(0.225F, -0.75F, 0);
            }
        }
    }

    public static void renderArmPump(ModelGun model, AnimStateMachine anim, float smoothing, Vector3f reloadRot, Vector3f reloadPos, boolean leftHand) {
        Optional<StateEntry> currentShootState = anim.getShootState();
        float pumpCurrent = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().currentValue : 1f : 1f;
        float pumpLast = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().lastValue : 1f : 1f;

        if (leftHand) {
            GL11.glTranslatef((model.config.arms.leftArm.armPos.x - (1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)) * model.config.bolt.pumpHandleDistance), model.config.arms.leftArm.armPos.y, model.config.arms.leftArm.armPos.z);
            handleRotateLeft(reloadRot);
        } else {
            GL11.glTranslatef((model.config.arms.rightArm.armPos.x - (1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)) * model.config.bolt.pumpHandleDistance), model.config.arms.rightArm.armPos.y, model.config.arms.rightArm.armPos.z);
            handleRotateRight(reloadRot);
        }
    }

    // This moves the right hand if leftHandAmmo & handCharge are true (For left
    // hand reload with right hand charge)
    public static void renderArmCharge(ModelGun model, AnimStateMachine anim, float smoothing, Vector3f reloadRot, Vector3f reloadPos, Vector3f defaultRot, Vector3f defaultPos, boolean leftHand) {
        Vector3f offsetPosition = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadPos, defaultPos), 1f);
        Optional<StateEntry> currentReloadState = anim.getReloadState();
        float chargeCurrent = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().currentValue : 1f : 1f;
        float chargeLast = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().lastValue : 1f : 1f;

        GL11.glTranslatef(defaultPos.x + offsetPosition.x + Math.abs(chargeLast + (chargeCurrent - chargeLast) * smoothing) * (model.config.extra.chargeHandleDistance * model.config.extra.modelScale), 0F, 0F);

        GL11.glTranslatef(0F, defaultPos.y + offsetPosition.y, 0F);
        GL11.glTranslatef(0F, 0F, defaultPos.z + offsetPosition.z);

        //Rotation
        Vector3f offsetRotation = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadRot, defaultRot), 1f);
        if (leftHand) {
            GL11.glTranslatef(0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(-0.225F, -0.75F, 0);
        } else {
            GL11.glTranslatef(-0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(0.225F, -0.75F, 0);
        }
    }

    // This moves the right hand if leftHandAmmo & handBolt are true (For left hand
    // reload with right hand bolt action)
    public static void renderArmBolt(ModelGun model, AnimStateMachine anim, float smoothing, Vector3f reloadRot, Vector3f reloadPos, boolean leftHand) {
        Optional<StateEntry> currentShootState = anim.getShootState();
        float pumpCurrent = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.Charge || currentShootState.get().stateType == StateType.Uncharge) ? currentShootState.get().currentValue : 1f : 1f;
        float pumpLast = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.Charge || currentShootState.get().stateType == StateType.Uncharge) ? currentShootState.get().lastValue : 1f : 1f;

        if (anim.isReloadState(StateType.Charge) || anim.isReloadState(StateType.Uncharge)) {
            StateEntry boltState = anim.getReloadState().get();
            pumpCurrent = boltState.currentValue;
            pumpLast = boltState.lastValue;
        }

        GL11.glTranslatef((reloadPos.x - (1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)) * model.config.bolt.chargeModifier.x), 0F, 0F);
        GL11.glTranslatef(0F, (reloadPos.y - (1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)) * model.config.bolt.chargeModifier.y), 0F);
        GL11.glTranslatef(0F, 0F, (reloadPos.z - (1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)) * model.config.bolt.chargeModifier.z));

        if (leftHand)
            handleRotateLeft(reloadRot);
        else
            handleRotateRight(reloadRot);
    }

    public static void renderArmDefault(ModelGun model, AnimStateMachine anim, float smoothing, Vector3f reloadRot, Vector3f reloadPos, boolean firingHand, boolean leftHand) {
        GL11.glTranslatef(reloadPos.x - (firingHand ? RenderParameters.triggerPullSwitch : 0f), reloadPos.y, reloadPos.z);
        if (leftHand)
            handleRotateLeft(reloadRot);
        else
            handleRotateRight(reloadRot);
    }

    public static void renderArmReload(ModelGun model, AnimStateMachine anim, WeaponAnimation animation, float smoothing, float tiltProgress, Vector3f reloadRot, Vector3f reloadPos, Vector3f defaultRot, Vector3f defaultPos, boolean leftHand) {
        //Translation
        Vector3f offsetPosition = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadPos, defaultPos), tiltProgress);

        Optional<StateEntry> currentState = anim.getReloadState();
        Vector3f ammoLoadOffset = anim.isReloadType(ReloadType.Load) && currentState.isPresent() && currentState.get().stateType != StateType.Load && currentState.get().stateType != StateType.Untilt ? animation.ammoLoadOffset != null ? animation.ammoLoadOffset : new Vector3f(0f, 0f, 0f) : new Vector3f(0f, 0f, 0f);
        //System.out.println(tiltProgress);
        //System.out.println(anim.isLoadOnly());
        GL11.glTranslatef(defaultPos.x + offsetPosition.x + (ammoLoadOffset.x * tiltProgress), 0F, 0F);
        GL11.glTranslatef(0F, defaultPos.y + offsetPosition.y + (ammoLoadOffset.y * tiltProgress), 0F);
        GL11.glTranslatef(0F, 0F, defaultPos.z + offsetPosition.z + (ammoLoadOffset.z * tiltProgress));

        //Rotation
        Vector3f offsetRotation = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadRot, defaultRot), tiltProgress);
        if (leftHand) {
            GL11.glTranslatef(0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(-0.225F, -0.75F, 0);
        } else {
            GL11.glTranslatef(-0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(0.225F, -0.75F, 0);
        }
    }

    public static void renderStaticArmReload(ModelGun model, AnimStateMachine anim, float smoothing, float tiltProgress, Vector3f reloadRot, Vector3f reloadPos, Vector3f defaultRot, Vector3f defaultPos, boolean leftHand) {
        //Translation
        Vector3f offsetPosition = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadPos, defaultPos), tiltProgress);
        GL11.glTranslatef(defaultPos.x + offsetPosition.x, defaultPos.y + offsetPosition.y, defaultPos.z + offsetPosition.z);

        //Rotation
        Vector3f offsetRotation = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadRot, defaultRot), tiltProgress);
        if (leftHand) {
            GL11.glTranslatef(0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(-0.225F, -0.75F, 0);
        } else {
            GL11.glTranslatef(-0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(0.225F, -0.75F, 0);
        }

    }

    public static void renderArmLoad(ModelGun model, AnimStateMachine anim, WeaponAnimation animation, float smoothing, float tiltProgress, Vector3f reloadRot, Vector3f reloadPos, Vector3f defaultRot, Vector3f defaultPos, boolean leftHand) {
        //Translation
        Vector3f offsetPosition = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadPos, defaultPos), tiltProgress);
        Optional<StateEntry> currentState = anim.getReloadState();
        Vector3f ammoLoadOffset = anim.isReloadType(ReloadType.Load) && currentState.isPresent() && currentState.get().stateType != StateType.Load ? animation.ammoLoadOffset != null ? animation.ammoLoadOffset : new Vector3f(0f, 0f, 0f) : new Vector3f(0f, 0f, 0f);
        //System.out.println(ammoLoadOffset);
        //System.out.println(anim.isLoadOnly());

        //tiltProgress = currentState.get().currentValue;
        //ModularWarfare.LOGGER.info(tiltProgress);

        GL11.glTranslatef(defaultPos.x + offsetPosition.x + (ammoLoadOffset.x * tiltProgress), 0F, 0F);
        GL11.glTranslatef(0F, defaultPos.y + offsetPosition.y + (ammoLoadOffset.y * tiltProgress), 0F);
        GL11.glTranslatef(0F, 0F, defaultPos.z + offsetPosition.z + (ammoLoadOffset.z * tiltProgress));
        //Rotation
        Vector3f offsetRotation = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadRot, defaultRot), tiltProgress);
        if (leftHand) {
            GL11.glTranslatef(0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(-0.225F, -0.75F, 0);
        } else {
            GL11.glTranslatef(-0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(0.225F, -0.75F, 0);
        }
    }

    public static void renderArmUnload(ModelGun model, AnimStateMachine anim, WeaponAnimation animation, float smoothing, float tiltProgress, Vector3f reloadRot, Vector3f reloadPos, Vector3f defaultRot, Vector3f defaultPos, boolean leftHand) {
        //Translation
        Vector3f offsetPosition = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadPos, defaultPos), tiltProgress);
        Vector3f ammoLoadOffset = anim.isReloadType(ReloadType.Load) ? animation.ammoLoadOffset != null ? animation.ammoLoadOffset : new Vector3f(0f, 0f, 0f) : new Vector3f(0f, 0f, 0f);
        GL11.glTranslatef(defaultPos.x + offsetPosition.x + (ammoLoadOffset.x * tiltProgress), 0F, 0F);
        GL11.glTranslatef(0F, defaultPos.y + offsetPosition.y + (ammoLoadOffset.y * tiltProgress), 0F);
        GL11.glTranslatef(0F, 0F, defaultPos.z + offsetPosition.z + (ammoLoadOffset.z * tiltProgress));
        //Rotation
        Vector3f offsetRotation = NumberHelper.multiplyVector(NumberHelper.subtractVector(reloadRot, defaultRot), tiltProgress);

        if (leftHand) {
            GL11.glTranslatef(0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(-0.225F, -0.75F, 0);
        } else {
            GL11.glTranslatef(-0.225F, 0.75F, 0);
            GL11.glRotatef(defaultRot.x + offsetRotation.x, 1F, 0F, 0F);
            GL11.glRotatef(defaultRot.y + offsetRotation.y, 0F, 1F, 0F);
            GL11.glRotatef(defaultRot.z + offsetRotation.z, 0F, 0F, 1F);
            GL11.glTranslatef(0.225F, -0.75F, 0);
        }
    }

    private static void handleRotateLeft(Vector3f reloadRot) {
        GL11.glTranslatef(0.225F, 0.75F, 0);
        GL11.glRotatef(reloadRot.x, 1F, 0F, 0F);
        GL11.glRotatef(reloadRot.y, 0F, 1F, 0F);
        GL11.glRotatef(reloadRot.z, 0F, 0F, 1F);
        GL11.glTranslatef(-0.225F, -0.75F, 0);

    }

    private static void handleRotateRight(Vector3f reloadRot) {
        GL11.glTranslatef(-0.225F, 0.75F, 0);
        GL11.glRotatef(reloadRot.x, 1F, 0F, 0F);
        GL11.glRotatef(reloadRot.y, 0F, 1F, 0F);
        GL11.glRotatef(reloadRot.z, 0F, 0F, 1F);
        GL11.glTranslatef(0.225F, -0.75F, 0);

    }

}
