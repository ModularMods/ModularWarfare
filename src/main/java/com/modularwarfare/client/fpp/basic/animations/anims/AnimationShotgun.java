package com.modularwarfare.client.fpp.basic.animations.anims;

import com.modularwarfare.api.WeaponAnimation;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.client.fpp.basic.animations.StateEntry;
import com.modularwarfare.client.fpp.basic.animations.StateEntry.MathType;
import com.modularwarfare.client.fpp.basic.animations.StateType;
import com.modularwarfare.client.model.ModelGun;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class AnimationShotgun extends WeaponAnimation {

    public AnimationShotgun() {
        //ammoLoadOffset = new Vector3f(0, -0.5F, 0);
    }

    @Override
    public void onGunAnimation(float tiltProgress, AnimStateMachine animation) {
        //Translate X - Forwards/Backwards
        GL11.glTranslatef(0.0F * tiltProgress, 0F, 0F);
        //Translate Y - Up/Down
        GL11.glTranslatef(0F, 0.0F * tiltProgress, 0F);
        //Translate Z - Left/Right
        GL11.glTranslatef(0F, 0F, -0.2F * tiltProgress);
        //Rotate X axis - Rolls Left/Right
        GL11.glRotatef(10F * tiltProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(-10F * tiltProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(15F * tiltProgress, 0F, 0F, 1F);
    }

    @Override
    public void onAmmoAnimation(ModelGun gunModel, float ammoPosition, int reloadAmmoCount, AnimStateMachine animation) {
        float multiAmmoPosition = ammoPosition * reloadAmmoCount;
        int bulletNum = MathHelper.floor(multiAmmoPosition);
        float bulletProgress = multiAmmoPosition - bulletNum;

        //Translate X - Forwards/Backwards
        GL11.glTranslatef(bulletProgress * (-2F / 16F) / gunModel.config.extra.modelScale, 0F, 0F);
        //Translate Y - Up/Down
        GL11.glTranslatef(0F, bulletProgress * (-8F / 16F) / gunModel.config.extra.modelScale, 0F);
        //Translate Z - Left/Right
        GL11.glTranslatef(0F, 0F, bulletProgress * (-1F / 16F) / gunModel.config.extra.modelScale);
        //Rotate X axis - Rolls Left/Right
        GL11.glRotatef(1F * bulletProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(1F * bulletProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(20F * bulletProgress, 0F, 0F, 1F);
    }

    @Override
    public ArrayList<StateEntry> getReloadStates(ReloadType reloadType, int reloadCount) {
        ArrayList<StateEntry> states = new ArrayList<StateEntry>();
        states.add(new StateEntry(StateType.Tilt, 0.15f, 0f, MathType.Add));

        if (reloadCount >= 6) {
            states.add(new StateEntry(StateType.Load, 0.35f, 1f, MathType.Sub, reloadCount));
        } else if (reloadCount >= 3 && reloadCount <= 5) {
            states.add(new StateEntry(StateType.Load, 0.15f, 1f, MathType.Sub, reloadCount));
        } else if (reloadCount >= 0 && reloadCount <= 2) {
            states.add(new StateEntry(StateType.Load, 0.1f, 1f, MathType.Sub, reloadCount));
        }

        states.add(new StateEntry(StateType.Untilt, 0.15f, 1f, MathType.Sub));
        return states;
    }

}
