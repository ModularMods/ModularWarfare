package com.modularwarfare.client.fpp.enhanced.renderers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.animation.AnimationController;
import com.modularwarfare.client.fpp.enhanced.models.EnhancedModel;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.utility.maths.Interpolation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Timer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

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

        this.controller.onTickRender(partialTicks);

        EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().getRenderViewEntity();

        float time = ((float) (Minecraft.getMinecraft().world.getTotalWorldTime() & 0xFFFFFF)) + this.timer.renderPartialTicks;

        /**
         * INITIAL BLENDER POSITION
         */

        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-1.8f,1.3f,-1.399f);

        float bobModifier = 0.8f - (adsSwitch*0.6f);


        /**
         * ACTION ADS
         */
        GL11.glTranslatef(Interpolation.SINE_IN.interpolate(0F, -1f, adsSwitch), 0,0);

        /**
         * ACTION FORWARD
         */
        float f1 = (player.distanceWalkedModified - player.prevDistanceWalkedModified);
        float f2 = -(player.distanceWalkedModified + f1 * partialTicks);
        float f3 = (player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks);
        float f4 = (player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks);

        GL11.glTranslatef(0, bobModifier * Interpolation.SINE_IN.interpolate(0F, (-0.2f * (1f - adsSwitch)), GUN_BALANCING_Y),0);
        GL11.glTranslatef(0, bobModifier * ((float) (0.05f * (Math.sin(SMOOTH_SWING/10) * GUN_BALANCING_Y))),0);
        GL11.glRotatef(0.1f * Interpolation.SINE_OUT.interpolate(-GUN_BALANCING_Y, GUN_BALANCING_Y, MathHelper.sin(f2 * (float) Math.PI)), 0f,1f, 0f);

        GlStateManager.translate(bobModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, bobModifier * -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F);
        GlStateManager.rotate(bobModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(bobModifier * Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(bobModifier * f4, 1.0F, 0.0F, 0.0F);

        /**
         * ACTION GUN BALANCING X / Y
         */

        GL11.glTranslatef((float) (0.1f*GUN_BALANCING_X*Math.cos(Math.PI * RenderParameters.SMOOTH_SWING / 50)) * (0.75F - adsSwitch),0,0);
        GL11.glRotatef((GUN_BALANCING_X * 4F) + (float) (GUN_BALANCING_X * Math.sin(Math.PI * RenderParameters.SMOOTH_SWING / 35)) * (0.75F - adsSwitch), -1, 0, 0);
        GL11.glRotatef((float) Math.sin(Math.PI * GUN_BALANCING_X), -1, 0, 0);
        GL11.glRotatef((GUN_BALANCING_X) * 0.4F, -1, 0, 0);


        /**
         * ACTION GUN MOTION
         */

        float gunRotX = RenderParameters.GUN_ROT_X_LAST + (RenderParameters.GUN_ROT_X - RenderParameters.GUN_ROT_X_LAST) * ClientProxy.renderHooks.partialTicks;
        float gunRotY = RenderParameters.GUN_ROT_Y_LAST + (RenderParameters.GUN_ROT_Y - RenderParameters.GUN_ROT_Y_LAST) * ClientProxy.renderHooks.partialTicks;
        GL11.glRotatef(gunRotX, 0, -1, 0);
        GL11.glRotatef(gunRotY, 0, 0, -1);

        /**
         * ACTION GUN COLLIDE
         */

        float rotateZ = -(35F * collideFrontDistance);
        float translateY = -(0.7F * collideFrontDistance);
        GL11.glRotatef(rotateZ, 0, 0, 1);
        GL11.glTranslatef(translateY,0,0);

        model.render(controller.getTime());
    }

}
