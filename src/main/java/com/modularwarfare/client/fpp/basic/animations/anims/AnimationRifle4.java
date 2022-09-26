package com.modularwarfare.client.fpp.basic.animations.anims;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.WeaponAnimation;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.model.ModelGun;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class AnimationRifle4 extends WeaponAnimation {

    public AnimationRifle4() {
        //ammoLoadOffset = new Vector3f(-2, 0, 0);
    }

    @Override
    public void onGunAnimation(float tiltProgress, AnimStateMachine animation) {
        ModularWarfare.LOGGER.info(tiltProgress);
        //Translate X - Forwards/Backwards
        GL11.glTranslatef(0.15F * tiltProgress, 0F, 0F);
        //Translate Y - Up/Down
        GL11.glTranslatef(0F, 0.25F * tiltProgress, 0F);
        //Translate Z - Left/Right
        GL11.glTranslatef(0F, 0F, -0.15F * tiltProgress);
        //Rotate X axis - Rolls Left/Right
        GL11.glRotatef(-60F * tiltProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(-10F * tiltProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(70F * tiltProgress, 0F, 0F, 1F);
    }

    @Override
    public void onAmmoAnimation(ModelGun gunModel, float ammoProgress, int reloadAmmoCount, AnimStateMachine animation) {
        float multiAmmoPosition = ammoProgress * 1;
        int bulletNum = MathHelper.floor(multiAmmoPosition);
        float bulletProgress = multiAmmoPosition - bulletNum;

        //Translate X - Forwards/Backwards
        GL11.glTranslatef(ammoProgress * -12.75F, 0F, 0F);
        //Translate Y - Up/Down
        GL11.glTranslatef(0F, ammoProgress * -5F, 0F);
        //Translate Z - Left/Right
        GL11.glTranslatef(0F, 0F, ammoProgress * -3F);
        //Rotate X axis - Rolls Left/Right
        GL11.glRotatef(0F * ammoProgress, 1F, 0F, 0F);
        //Rotate Y axis - Angle Left/Right
        GL11.glRotatef(-50F * ammoProgress, 0F, 1F, 0F);
        //Rotate Z axis - Angle Up/Down
        GL11.glRotatef(-150F * ammoProgress, 0F, 0F, 1F);

    }

}
