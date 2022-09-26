package com.modularwarfare.client.fpp.basic.renderers;

import com.modularwarfare.api.GunBobbingEvent;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.model.ModelCustomArmor;
import com.modularwarfare.client.model.ModelGrenade;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.common.armor.ItemMWArmor;
import com.modularwarfare.common.grenades.GrenadeType;
import com.modularwarfare.common.grenades.ItemGrenade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Timer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.*;

public class RenderGrenade extends CustomItemRenderer {

    public static float smoothing;
    public static float randomOffset;
    public static float randomRotateOffset;
    public static float adsSwitch = 0f;
    private static TextureManager renderEngine;
    private int direction = 0;

    private Timer timer;

    @Override
    public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data) {
        if (!(item.getItem() instanceof ItemGrenade))
            return;

        GrenadeType grenadeType = ((ItemGrenade) item.getItem()).type;
        if (grenadeType == null)
            return;

        ModelGrenade model = (ModelGrenade) grenadeType.model;
        if (model == null)
            return;
        {
            renderGrenade(type, item, grenadeType, data);
        }
    }

    private void renderGrenade(CustomItemRenderType renderType, ItemStack item, GrenadeType grenadeType, Object... data) {

        ModelGrenade model = (ModelGrenade) grenadeType.model;
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        if (renderEngine == null)
            renderEngine = Minecraft.getMinecraft().renderEngine;

        if (model == null)
            return;

        GL11.glPushMatrix();
        {
            switch (renderType) {

                case ENTITY: {
                    GL11.glTranslatef(-0.45F, -0.05F, 0.0F);
                    break;
                }

                case EQUIPPED: {
                    float crouchOffset = player.isSneaking() ? 0.2f : 0.0f;
                    GL11.glRotatef(-10F, 1F, 0F, 0F);
                    GL11.glRotatef(-90F, 0F, 1F, 0F);
                    GL11.glRotatef(90F, 0F, 0F, 1F);
                    GL11.glTranslatef(-0.55F, 0.15F, 0.0F);
                    GL11.glScalef(1F, 1F, 1F);
                    GL11.glTranslatef(model.config.extra.thirdPersonOffset.x + crouchOffset, model.config.extra.thirdPersonOffset.y, model.config.extra.thirdPersonOffset.z);
                    break;
                }

                case EQUIPPED_FIRST_PERSON: {
                    float modelScale = model.config.extra.modelScale;
                    Vector3f vector3f = model.config.extra.translateAll;

                    float worldScale = 1F / 16F;

                    if (this.timer == null) {
                        this.timer = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
                    }
                    float partialTicks = this.timer.renderPartialTicks;

                    RenderParameters.VALSPRINT = (float) (Math.cos(RenderParameters.SMOOTH_SWING / 5) * 5);
                    RenderParameters.VAL = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 100) * 8);
                    RenderParameters.VAL2 = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 80) * 8);
                    RenderParameters.VALROT = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 90) * 1.2f);

                    GL11.glRotatef(46F - VALROT, 0F, 1F, 0F);

                    GL11.glTranslatef(-1.88F, 1.1F, -1.05F);
                    GL11.glTranslatef(0F, (VAL / 500), ((VAL2 / 500)));


                    // Custom view bobbing applies to gun models
                    float bobModifier = 0.4F;
                    float yawReducer = 1f;
                    if (ClientRenderHooks.isAimingScope) {
                        bobModifier *= 0.5f;
                        yawReducer = 0.5f;
                    }
                    GunBobbingEvent event = new GunBobbingEvent(bobModifier);
                    MinecraftForge.EVENT_BUS.post(event);
                    bobModifier = event.bobbing;
                    EntityPlayer entityplayer = (EntityPlayer) Minecraft.getMinecraft().getRenderViewEntity();
                    float f1 = (entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified) * bobModifier;
                    float f2 = -(entityplayer.distanceWalkedModified + f1 * partialTicks) * bobModifier;
                    float f3 = (entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks) * bobModifier * yawReducer;
                    float f4 = (entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks) * bobModifier;
                    GlStateManager.translate(MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F);
                    GlStateManager.rotate(MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(f4, 1.0F, 0.0F, 0.0F);


                    Minecraft mc = Minecraft.getMinecraft();
                    mc.getTextureManager().bindTexture(Minecraft.getMinecraft().player.getLocationSkin());
                    Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager().<AbstractClientPlayer>getEntityRenderObject(Minecraft.getMinecraft().player);
                    RenderPlayer renderplayer = (RenderPlayer) render;
                    //Render the armor staticModel on the arm

                    GL11.glPushMatrix();
                    GL11.glScalef(model.config.arms.rightArm.armScale.x, model.config.arms.rightArm.armScale.y, model.config.arms.rightArm.armScale.z);

                    GL11.glTranslatef(model.config.arms.rightArm.armPos.x, model.config.arms.rightArm.armPos.y, model.config.arms.rightArm.armPos.z);

                    GL11.glRotatef(model.config.arms.rightArm.armRot.x, 1, 0, 0);
                    GL11.glRotatef(model.config.arms.rightArm.armRot.y, 0, 1, 0);
                    GL11.glRotatef(model.config.arms.rightArm.armRot.z, 0, 0, 1);

                    renderplayer.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
                    renderplayer.getMainModel().bipedRightArm.offsetX = 0F;
                    renderplayer.renderRightArm(Minecraft.getMinecraft().player);
                    renderRightSleeve(player, renderplayer.getMainModel());

                    GL11.glPopMatrix();
                }

                default:
                    break;

            }

            GL11.glPushMatrix();
            {

                GL11.glTranslatef(0.7F, -0.06F, 0F);

                float f = 1F / 16F;
                float modelScale = model.config.extra.modelScale;
                int skinId = 0;
                String path = skinId > 0 ? "skins/" + grenadeType.modelSkins[skinId].getSkin() : grenadeType.modelSkins[0].getSkin();
                bindTexture("grenades", path);
                GL11.glScalef(modelScale, modelScale, modelScale);
                model.renderPart("grenadeModel", f);
                model.renderPart("pinModel", f);
            }
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }


    public void renderRightSleeve(EntityPlayer player, ModelBiped modelplayer) {
        if (player.inventory.armorItemInSlot(2) != null) {
            ItemStack armorStack = player.inventory.armorItemInSlot(2);
            if (armorStack.getItem() instanceof ItemMWArmor) {
                ModelCustomArmor modelArmor = ((ModelCustomArmor) ((ItemMWArmor) armorStack.getItem()).type.bipedModel);
                int skinId = 0;
                String path = skinId > 0 ? ((ItemMWArmor) armorStack.getItem()).type.modelSkins[skinId].getSkin() : ((ItemMWArmor) armorStack.getItem()).type.modelSkins[0].getSkin();
                bindTexture("armor", path);
                GL11.glPushMatrix();
                {
                    float modelScale = modelArmor.config.extra.modelScale;
                    GL11.glScalef(modelScale, modelScale, modelScale);
                    modelArmor.showChest(true);
                    //modelArmor.render("rightArmModel", modelplayer.bipedRightArm, 0.0625F, modelScale);
                    modelArmor.renderRightArm((AbstractClientPlayer) player, modelplayer);
                }
                GL11.glPopMatrix();
            }
        }
    }
}
