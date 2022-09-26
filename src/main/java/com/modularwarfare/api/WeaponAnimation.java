package com.modularwarfare.api;

import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.client.fpp.basic.animations.StateEntry;
import com.modularwarfare.client.fpp.basic.animations.StateEntry.MathType;
import com.modularwarfare.client.fpp.basic.animations.StateType;
import com.modularwarfare.client.model.ModelGun;
import com.modularwarfare.common.guns.GunType;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class WeaponAnimation {

    public Vector3f ammoLoadOffset;

    public void onGunAnimation(float reloadRotate, AnimStateMachine animation) {

    }

    public void onAmmoAnimation(ModelGun gunModel, float ammoPosition, int reloadAmmoCount, AnimStateMachine animation) {

    }

    public ArrayList<StateEntry> getReloadStates(ReloadType reloadType, int reloadCount) {
        ArrayList<StateEntry> states = new ArrayList<StateEntry>();
        states.add(new StateEntry(StateType.Tilt, 0.15f, 0f, MathType.Add));
        if (reloadType == ReloadType.Unload || reloadType == ReloadType.Full)
            states.add(new StateEntry(StateType.Unload, 0.35f, 0f, MathType.Add));
        if (reloadType == ReloadType.Load || reloadType == ReloadType.Full)
            states.add(new StateEntry(StateType.Load, 0.35f, 1f, MathType.Sub, reloadCount));
        states.add(new StateEntry(StateType.Untilt, 0.15f, 1f, MathType.Sub));
        return states;
    }

    public ArrayList<StateEntry> getShootStates(ModelGun gunModel, GunType gunType) {
        ArrayList<StateEntry> states = new ArrayList<StateEntry>();

        if (gunModel.staticModel != null) {
            if (gunModel.staticModel.getPart("pumpModel") != null) {
                states.add(new StateEntry(StateType.PumpOut, 0.5f, 1f, MathType.Sub));
                states.add(new StateEntry(StateType.PumpIn, 0.5f, 0f, MathType.Add));
            }
        }
        return states;
    }

}
