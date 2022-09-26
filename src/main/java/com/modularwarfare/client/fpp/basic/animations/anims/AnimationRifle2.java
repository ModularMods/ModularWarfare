package com.modularwarfare.client.fpp.basic.animations.anims;

import com.modularwarfare.api.WeaponAnimation;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.model.ModelGun;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class AnimationRifle2 extends WeaponAnimation {

    public AnimationRifle2() {
        //ammoLoadOffset = new Vector3f(0, -0.5F, 0);
    }

    @Override
    public void onGunAnimation(float tiltProgress, AnimStateMachine animation) {
        //Translate X - Forwards/Backwards
        GL11.glTranslatef(0.0F * tiltProgress, 0F, 0F);
        //Translate Y - Up/Down
        GL11.glTranslatef(0F, 0.0F * tiltProgress, 0F);
        //Translate Z - Left/Right
        GL11.glTranslatef(0F, 0F, 0.0F * tiltProgress);
        //Rotate X axis - Rolls Left/Right
        GL11.glRotatef(10F * tiltProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(-15F * tiltProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(25F * tiltProgress, 0F, 0F, 1F);
    }

    @Override
    public void onAmmoAnimation(ModelGun gunModel, float ammoProgress, int reloadAmmoCount, AnimStateMachine animation) {
        float multiAmmoPosition = ammoProgress * 1;
        int bulletNum = MathHelper.floor(multiAmmoPosition);
        float bulletProgress = multiAmmoPosition - bulletNum;

        //System.out.println("bp" + bulletProgress);
        //System.out.println("cp" + ammoPosition);
        //Translate X - Forwards/Backwards
        GL11.glTranslatef(ammoProgress * -2.75F, 0F, 0F);
        //Translate Y - Up/Down
        GL11.glTranslatef(0F, ammoProgress * -2F, 0F);
        //Translate Z - Left/Right
        GL11.glTranslatef(0F, 0F, ammoProgress * 0F);
        //Rotate X axis - Rolls Left/Right
        GL11.glRotatef(30F * ammoProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(0F * ammoProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(-150F * ammoProgress, 0F, 0F, 1F);

    }

}
