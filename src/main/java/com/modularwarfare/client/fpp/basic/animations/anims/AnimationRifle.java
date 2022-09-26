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
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;

public class AnimationRifle extends WeaponAnimation {

    public AnimationRifle() {
        ammoLoadOffset = new Vector3f(0, -1.5F, 0);
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
        GL11.glRotatef(20F * tiltProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(-10F * tiltProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(25F * tiltProgress, 0F, 0F, 1F);
    }

    @Override
    public void onAmmoAnimation(ModelGun gunModel, float ammoProgress, int reloadAmmoCount, AnimStateMachine animation) {
        //System.out.println(ammoProgress);
        float multiAmmoPosition = ammoProgress * 1;
        int bulletNum = MathHelper.floor(multiAmmoPosition);
        float bulletProgress = multiAmmoPosition - bulletNum;

        //System.out.println(bulletProgress);
        //Translate X - Forwards/Backwards
        GL11.glTranslatef(ammoProgress * -0.75F, 0F, 0F);
        //Translate Y - Up/Down
        GL11.glTranslatef(0F, ammoProgress * -8F, 0F);
        //Translate Z - Left/Right
        GL11.glTranslatef(0F, 0F, ammoProgress * 0F);
        //Rotate X axis - Rolls Left/Right
        GL11.glRotatef(30F * ammoProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(0F * ammoProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(-90F * ammoProgress, 0F, 0F, 1F);
    }

    @Override
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

}
