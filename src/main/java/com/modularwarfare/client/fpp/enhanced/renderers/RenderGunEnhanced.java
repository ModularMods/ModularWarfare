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
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.util.Random;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.*;

public class RenderGunEnhanced extends CustomItemRenderer {

    private Timer timer;

    public AnimationController controller;

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

        float time = ((float) (Minecraft.getMinecraft().world.getTotalWorldTime() & 0xFFFFFF)) + this.timer.renderPartialTicks;
        /**
         * ACTION GUN MOTION
         */
        float gunRotX = RenderParameters.GUN_ROT_X_LAST + (RenderParameters.GUN_ROT_X - RenderParameters.GUN_ROT_X_LAST) * ClientProxy.renderHooks.partialTicks;
        float gunRotY = RenderParameters.GUN_ROT_Y_LAST + (RenderParameters.GUN_ROT_Y - RenderParameters.GUN_ROT_Y_LAST) * ClientProxy.renderHooks.partialTicks;
        GL11.glRotatef(gunRotX, 0, -1, 0);
        GL11.glRotatef(gunRotY, 0, 0, -1);

        /**
         * INITIAL BLENDER POSITION
         */
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-1.8f,1.3f,-1.399f);


        float adsModifier = 0.95f - AnimationController.ADS;

        /**
         * ACTION ADS
         */
        //GL11.glTranslatef(Interpolation.SINE_IN.interpolate(0F, -1f, AnimationController.ADS), 0,0);

        /**
         * ACTION FORWARD
         */
        float f1 = (player.distanceWalkedModified - player.prevDistanceWalkedModified);
        float f2 = -(player.distanceWalkedModified + f1 * partialTicks);
        float f3 = (player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks);
        float f4 = (player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks);

        GL11.glTranslatef(0, adsModifier * Interpolation.SINE_IN.interpolate(0F, (-0.2f * (1F - AnimationController.ADS)), GUN_BALANCING_Y),0);
        GL11.glTranslatef(0, adsModifier * ((float) (0.05f * (Math.sin(SMOOTH_SWING/10) * GUN_BALANCING_Y))),0);
        GL11.glRotatef(adsModifier * 0.1f * Interpolation.SINE_OUT.interpolate(-GUN_BALANCING_Y, GUN_BALANCING_Y, adsModifier * MathHelper.sin(f2 * (float) Math.PI)), 0f,1f, 0f);

        GlStateManager.translate(adsModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, adsModifier * -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F);
        GlStateManager.rotate(adsModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(adsModifier * Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(adsModifier * f4, 1.0F, 0.0F, 0.0F);

        /**
         * ACTION GUN BALANCING X / Y
         */
        GL11.glTranslatef((float) (0.1f*GUN_BALANCING_X*Math.cos(Math.PI * RenderParameters.SMOOTH_SWING / 50)) * (1F - AnimationController.ADS),0,0);
        GL11.glRotatef((GUN_BALANCING_X * 4F) + (float) (GUN_BALANCING_X * Math.sin(Math.PI * RenderParameters.SMOOTH_SWING / 35)), -1, 0, 0);
        GL11.glRotatef((float) Math.sin(Math.PI * GUN_BALANCING_X), -1, 0, 0);
        GL11.glRotatef((GUN_BALANCING_X) * 0.4F, -1, 0, 0);

        /**
         * ACTION GUN COLLIDE
         */
        float rotateZ = -(35F * collideFrontDistance);
        float translateY = -(0.7F * collideFrontDistance);
        GL11.glRotatef(rotateZ, 0, 0, 1);
        GL11.glTranslatef(translateY,0,0);

        /**
         * ACTION GUN SWAY
         */
        RenderParameters.VAL = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 100) * 8);
        RenderParameters.VAL2 = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 80) * 8);
        RenderParameters.VALROT = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 90) * 1.2f);
        GL11.glTranslatef(0f, ((VAL / 500) * (0.95f - AnimationController.ADS)),  ((VAL2 / 500 * (0.95f - AnimationController.ADS))));
        GL11.glRotatef(adsModifier * VALROT, 1F, 0F, 0F);

        /**
         * ACTION PROBE
         */
        if(Loader.isModLoaded("modularmovements")) {
            GL11.glRotatef(15F * ClientLitener.cameraProbeOffset, 1f, 0f, 0f);
        }

        /**
         * ACTION SPRINT
         */
        RenderParameters.VALSPRINT = (float) (Math.cos(RenderParameters.SMOOTH_SWING / 5) * 5) * gunType.moveSpeedModifier;
        float VALSPRINT2 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 5) * 5) * gunType.moveSpeedModifier;

        //OPTIONAL
        float VALSPRINT3 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 8) * 6) * gunType.moveSpeedModifier;
        float VALSPRINT4 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 9) * 7) * gunType.moveSpeedModifier;
        float VALSPRINT5 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 10) * 8) * gunType.moveSpeedModifier;
        //OPTIONAL

        GL11.glRotatef(adsModifier * VALSPRINT * AnimationController.SPRINT, 1, 1, -1);
        GL11.glRotatef(adsModifier * 0.2f * VALSPRINT2 * AnimationController.SPRINT, 0, 0, 1);

        //OPTIONAL
        GL11.glRotatef(adsModifier * 0.3f * VALSPRINT3 * AnimationController.SPRINT, 0, 1, 0);
        GL11.glRotatef(adsModifier * 0.4f * VALSPRINT4 * AnimationController.SPRINT, 0, 0, 1);
        GL11.glRotatef(adsModifier * 0.5f * VALSPRINT5 * AnimationController.SPRINT, -1, -1, 0);
        //OPTIONAL

        Vector3f customSprintRotation = new Vector3f((model.config.sprint.sprintRotate.x * AnimationController.SPRINT), (model.config.sprint.sprintRotate.y * AnimationController.SPRINT), (model.config.sprint.sprintRotate.z * AnimationController.SPRINT));
        Vector3f customSprintTranslate = new Vector3f((model.config.sprint.sprintTranslate.x * AnimationController.SPRINT), (model.config.sprint.sprintTranslate.y * AnimationController.SPRINT), (model.config.sprint.sprintTranslate.z * AnimationController.SPRINT));

        customSprintRotation.scale((1F - AnimationController.ADS));
        customSprintTranslate.scale((1F - AnimationController.ADS));

        /**
         * CUSTOM HIP POSITION
         */
        Vector3f customHipRotation = new Vector3f(model.config.aim.rotateHipPosition.x, model.config.aim.rotateHipPosition.y, model.config.aim.rotateHipPosition.z);
        Vector3f customHipTranslate = new Vector3f(model.config.aim.translateHipPosition.x, (model.config.aim.translateHipPosition.y), (model.config.aim.translateHipPosition.z));

        GL11.glRotatef(customHipRotation.x + customSprintRotation.x, 1f,0f,0f);
        GL11.glRotatef(customHipRotation.y + customSprintRotation.y, 0f,1f,0f);
        GL11.glRotatef(customHipRotation.z + customSprintRotation.z, 0f,0f,1f);
        GL11.glTranslatef(customHipTranslate.x + customSprintTranslate.x, customHipTranslate.y + customSprintTranslate.y, customHipTranslate.z + customSprintTranslate.z);

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

        GL11.glTranslatef(-(bounce) * model.config.extra.modelRecoilBackwards, 0F, 0F);
        GL11.glTranslatef(0F, (-(elastic) * model.config.extra.modelRecoilBackwards) * 0.05F, 0F);

        GL11.glTranslatef(0F, 0F, sin10 * anim.recoilSide * model.config.extra.modelRecoilUpwards);
        GL11.glRotatef(sin * anim.recoilSide * model.config.extra.modelRecoilUpwards, 0F, 0F, 1F);
        GL11.glRotatef(5F * sin10 * anim.recoilSide * model.config.extra.modelRecoilUpwards, 0F, 0F, 1F);


        GL11.glRotatef((bounce) * model.config.extra.modelRecoilUpwards, 0F, 0F, 1F);

        GL11.glRotatef(((-alpha) * randomShake * model.config.extra.modelRecoilShake), 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(((-alpha) * randomShake * model.config.extra.modelRecoilShake), 1.0f, 0.0f, 0.0f);


        model.render(controller.getTime());
    }

}
