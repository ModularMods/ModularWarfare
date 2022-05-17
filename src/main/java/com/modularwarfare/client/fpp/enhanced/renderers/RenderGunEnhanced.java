package com.modularwarfare.client.fpp.enhanced.renderers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.animation.AnimationController;
import com.modularwarfare.client.fpp.enhanced.animation.EnhancedStateMachine;
import com.modularwarfare.client.fpp.enhanced.models.EnhancedModel;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.utility.maths.Interpolation;
import mchhui.modularmovements.tactical.client.ClientLitener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Timer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.Random;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.*;

public class RenderGunEnhanced extends CustomItemRenderer {

    public static final float PI = 3.14159265f;

    private Timer timer;

    public AnimationController controller;

    public FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);

    public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data) {
        if (!(item.getItem() instanceof ItemGun))
            return;

        GunType gunType = ((ItemGun) item.getItem()).type;
        if (gunType == null)
            return;

        EnhancedModel model = gunType.enhancedModel;

        if (model == null)
            return;

        if(this.controller == null || this.controller.getConfig() != model.config){
            this.controller = new AnimationController(model.config);
        }


        if (this.timer == null) {
            this.timer = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
        }

        float partialTicks = this.timer.renderPartialTicks;

        EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().getRenderViewEntity();

        EnhancedStateMachine anim = ClientRenderHooks.getEnhancedAnimMachine(player);

        Matrix4f mat = new Matrix4f();
        /**
         * ACTION GUN MOTION
         */
        float gunRotX = RenderParameters.GUN_ROT_X_LAST + (RenderParameters.GUN_ROT_X - RenderParameters.GUN_ROT_X_LAST) * ClientProxy.renderHooks.partialTicks;
        float gunRotY = RenderParameters.GUN_ROT_Y_LAST + (RenderParameters.GUN_ROT_Y - RenderParameters.GUN_ROT_Y_LAST) * ClientProxy.renderHooks.partialTicks;
        mat.rotate(toRadians(gunRotX), new Vector3f(0,-1,0));
        mat.rotate(toRadians(gunRotY), new Vector3f(0,0,-1));

        /**
         * INITIAL BLENDER POSITION
         */
        mat.rotate(toRadians(45.0F), new Vector3f(0,1,0));
        mat.translate(new Vector3f(-1.8f,1.3f,-1.399f));

        float adsModifier = 0.95f - AnimationController.ADS;

        /**
         * ACTION FORWARD
         */
        float f1 = (player.distanceWalkedModified - player.prevDistanceWalkedModified);
        float f2 = -(player.distanceWalkedModified + f1 * partialTicks);
        float f3 = (player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks);
        float f4 = (player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks);

        mat.translate(new Vector3f(0, adsModifier * Interpolation.SINE_IN.interpolate(0F, (-0.2f * (1F - AnimationController.ADS)), GUN_BALANCING_Y),0));
        mat.translate(new Vector3f(0, adsModifier * ((float) (0.05f * (Math.sin(SMOOTH_SWING/10) * GUN_BALANCING_Y))),0));

        mat.rotate(toRadians(adsModifier * 0.1f * Interpolation.SINE_OUT.interpolate(-GUN_BALANCING_Y, GUN_BALANCING_Y, adsModifier * MathHelper.sin(f2 * (float) Math.PI))), new Vector3f(0f,1f, 0f));

        mat.translate(new Vector3f(adsModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, adsModifier * -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F));
        mat.rotate(toRadians(adsModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F), new Vector3f(0.0F, 0.0F, 1.0F));
        mat.rotate(toRadians(adsModifier * Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F), new Vector3f(1.0F, 0.0F, 0.0F));
        mat.rotate(toRadians(adsModifier * f4), new Vector3f(1.0F, 0.0F, 0.0F));

        /**
         * ACTION GUN BALANCING X / Y
         */
        mat.translate(new Vector3f((float) (0.1f*GUN_BALANCING_X*Math.cos(Math.PI * RenderParameters.SMOOTH_SWING / 50)) * (1F - AnimationController.ADS),0,0));
        mat.rotate(toRadians((GUN_BALANCING_X * 4F) + (float) (GUN_BALANCING_X * Math.sin(Math.PI * RenderParameters.SMOOTH_SWING / 35))), new Vector3f(-1, 0, 0));
        mat.rotate(toRadians((float) Math.sin(Math.PI * GUN_BALANCING_X)), new Vector3f(-1, 0, 0));
        mat.rotate(toRadians((GUN_BALANCING_X) * 0.4F), new Vector3f(-1, 0, 0));

        /**
         * ACTION GUN COLLIDE
         */
        float rotateZ = -(35F * collideFrontDistance);
        float translateY = -(0.7F * collideFrontDistance);
        mat.rotate(toRadians(rotateZ), new Vector3f(0, 0, 1));
        mat.translate(new Vector3f(translateY,0,0));

        /**
         * ACTION GUN SWAY
         */
        RenderParameters.VAL = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 100) * 8);
        RenderParameters.VAL2 = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 80) * 8);
        RenderParameters.VALROT = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 90) * 1.2f);
        mat.translate(new Vector3f(0f, ((VAL / 500) * (0.95f - AnimationController.ADS)),  ((VAL2 / 500 * (0.95f - AnimationController.ADS)))));
        mat.rotate(toRadians(adsModifier * VALROT), new Vector3f(1F, 0F, 0F));

        /**
         * ACTION PROBE
         */
        if(Loader.isModLoaded("modularmovements")) {
            mat.rotate(toRadians(15F * ClientLitener.cameraProbeOffset),  new Vector3f(1f, 0f, 0f));
        }

        /**
         * ACTION SPRINT
         */
        RenderParameters.VALSPRINT = (float) (Math.cos(RenderParameters.SMOOTH_SWING / 5) * 5) * gunType.moveSpeedModifier;
        float VALSPRINT2 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 5) * 5) * gunType.moveSpeedModifier;

        /*
        OPTIONAL
        float VALSPRINT3 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 8) * 6) * gunType.moveSpeedModifier;
        float VALSPRINT4 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 9) * 7) * gunType.moveSpeedModifier;
        float VALSPRINT5 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 10) * 8) * gunType.moveSpeedModifier;
        */

        mat.rotate(toRadians(adsModifier * VALSPRINT * AnimationController.SPRINT), new Vector3f(1, 1, -1));
        mat.rotate(toRadians(adsModifier * 0.2f * VALSPRINT2 * AnimationController.SPRINT), new Vector3f(0, 0, 1));

        /*
        OPTIONAL
        GL11.glRotatef(adsModifier * 0.3f * VALSPRINT3 * AnimationController.SPRINT, 0, 1, 0);
        GL11.glRotatef(adsModifier * 0.4f * VALSPRINT4 * AnimationController.SPRINT, 0, 0, 1);
        GL11.glRotatef(adsModifier * 0.5f * VALSPRINT5 * AnimationController.SPRINT, -1, -1, 0);
         */

        Vector3f customSprintRotation = new Vector3f((model.config.sprint.sprintRotate.x * AnimationController.SPRINT), (model.config.sprint.sprintRotate.y * AnimationController.SPRINT), (model.config.sprint.sprintRotate.z * AnimationController.SPRINT));
        Vector3f customSprintTranslate = new Vector3f((model.config.sprint.sprintTranslate.x * AnimationController.SPRINT), (model.config.sprint.sprintTranslate.y * AnimationController.SPRINT), (model.config.sprint.sprintTranslate.z * AnimationController.SPRINT));

        customSprintRotation.scale((1F - AnimationController.ADS));
        customSprintTranslate.scale((1F - AnimationController.ADS));

        /**
         * CUSTOM HIP POSITION
         */
        Vector3f customHipRotation = new Vector3f(model.config.aim.rotateHipPosition.x, model.config.aim.rotateHipPosition.y, model.config.aim.rotateHipPosition.z);
        Vector3f customHipTranslate = new Vector3f(model.config.aim.translateHipPosition.x, (model.config.aim.translateHipPosition.y), (model.config.aim.translateHipPosition.z));

        mat.rotate(toRadians(customHipRotation.x + customSprintRotation.x), new Vector3f(1f,0f,0f));
        mat.rotate(toRadians(customHipRotation.y + customSprintRotation.y), new Vector3f(0f,1f,0f));
        mat.rotate(toRadians(customHipRotation.z + customSprintRotation.z), new Vector3f(0f,0f,1f));
        mat.translate(new Vector3f(customHipTranslate.x + customSprintTranslate.x, customHipTranslate.y + customSprintTranslate.y, customHipTranslate.z + customSprintTranslate.z));

        /**
         * RECOIL
         */
        /** Random Shake */
        float min = -1.5f;
        float max = 1.5f;
        float randomNum = new Random().nextFloat();
        float randomShake = min + (randomNum * (max - min));

        final float alpha = anim.lastGunRecoil + (anim.gunRecoil - anim.lastGunRecoil) * partialTicks;
        float bounce = Interpolation.BOUNCE_INOUT.interpolate(0F, 1F, alpha);
        float elastic = Interpolation.ELASTIC_OUT.interpolate(0F, 1F, alpha);

        float sin = MathHelper.sin((float) (2 * Math.PI * alpha));

        float sin10 = MathHelper.sin((float) (2 * Math.PI * alpha)) * 0.05f;

        mat.translate(new Vector3f(-(bounce) * model.config.extra.modelRecoilBackwards, 0F, 0F));
        mat.translate(new Vector3f(0F, (-(elastic) * model.config.extra.modelRecoilBackwards) * 0.05F, 0F));

        mat.translate(new Vector3f(0F, 0F, sin10 * anim.recoilSide * model.config.extra.modelRecoilUpwards));
        mat.rotate(toRadians(sin * anim.recoilSide * model.config.extra.modelRecoilUpwards), new Vector3f(0F, 0F, 1F));
        mat.rotate(toRadians(5F * sin10 * anim.recoilSide * model.config.extra.modelRecoilUpwards), new Vector3f(0F, 0F, 1F));

        mat.rotate(toRadians((bounce) * model.config.extra.modelRecoilUpwards), new Vector3f(0F, 0F, 1F));

        mat.rotate(toRadians(((-alpha) * randomShake * model.config.extra.modelRecoilShake)), new Vector3f(0.0f, 1.0f, 0.0f));
        mat.rotate(toRadians(((-alpha) * randomShake * model.config.extra.modelRecoilShake)), new Vector3f(1.0f, 0.0f, 0.0f));

        floatBuffer.clear();
        mat.store(floatBuffer);
        floatBuffer.rewind();

        GL11.glMultMatrix(floatBuffer);

        model.render(controller.getTime());
    }


    public static float toRadians(float angdeg) {
        return angdeg / 180.0f * PI;
    }
}
