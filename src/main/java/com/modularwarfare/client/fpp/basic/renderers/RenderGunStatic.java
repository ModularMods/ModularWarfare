package com.modularwarfare.client.fpp.basic.renderers;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.GunBobbingEvent;
import com.modularwarfare.api.RenderHandFisrtPersonEvent;
import com.modularwarfare.api.RenderHandSleeveEvent;
import com.modularwarfare.api.WeaponAnimation;
import com.modularwarfare.api.WeaponAnimations;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.fpp.basic.animations.ReloadType;
import com.modularwarfare.client.fpp.basic.animations.StateEntry;
import com.modularwarfare.client.fpp.basic.animations.StateType;
import com.modularwarfare.client.model.ModelAmmo;
import com.modularwarfare.client.model.ModelAttachment;
import com.modularwarfare.client.model.ModelBullet;
import com.modularwarfare.client.model.ModelCustomArmor;
import com.modularwarfare.client.model.ModelGun;
import com.modularwarfare.client.fpp.basic.models.objects.BreakActionData;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.client.fpp.basic.models.objects.RenderVariables;
import com.modularwarfare.client.scope.ScopeUtils;
import com.modularwarfare.client.shader.Programs;
import com.modularwarfare.common.armor.ItemMWArmor;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.network.PacketAimingRequest;
import com.modularwarfare.common.textures.TextureType;
import com.modularwarfare.loader.api.model.ObjModelRenderer;
import com.modularwarfare.utility.ModUtil;
import com.modularwarfare.utility.OptifineHelper;
import com.modularwarfare.client.fpp.basic.configs.GunRenderConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.optifine.shaders.Shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector3f;

import java.util.Optional;
import java.util.Random;

import static com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType.BACK;
import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.*;

public class RenderGunStatic extends CustomItemRenderer {

    public static float prevBobModifier = 0f;
    public static boolean isLightOn;
    public int oldMagCount;
    /**
     * Used for flashlight
     **/
    private float slowDiff;
    private ItemStack light;
    private Timer timer;

    //Determine the state of the static arm
    public static String getStaticArmState(ModelGun model, AnimStateMachine anim) {
        Optional<StateEntry> currentShootState = anim.getShootState();
        Optional<StateEntry> currentReloadState = anim.getReloadState();
        float pumpCurrent = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().currentValue : 1f : 1f;
        float chargeCurrent = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().currentValue : 1f : currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.Charge || currentShootState.get().stateType == StateType.Uncharge) ? currentShootState.get().currentValue : 1f : 1f;

        if (model.config.arms.leftHandAmmo) {
            if ((anim.isReloadState(StateType.MoveHands) || anim.isReloadState(StateType.ReturnHands))) return "ToFrom";
            else if ((anim.isShootState(StateType.MoveHands) || anim.isShootState(StateType.ReturnHands)))
                return "ToFrom";
            else if (!anim.reloading && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Pump))
                return "Pump";
            else if (chargeCurrent < 0.66 && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Charge) && chargeCurrent != -1.0F)
                return "Charge";
            else if ((anim.isReloadState(StateType.Charge) || anim.isReloadState(StateType.Uncharge)) && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Bolt))
                return "Bolt";
            else if ((anim.isShootState(StateType.Charge) || anim.isShootState(StateType.Uncharge)) && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Bolt))
                return "Bolt";
            else if (!anim.reloading && !model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Pump))
                return "Default";
            else return "Reload";
        } else {
            if (!anim.reloading && model.isType(GunRenderConfig.Arms.EnumArm.Left, GunRenderConfig.Arms.EnumAction.Pump))
                return "Pump";
            else if (chargeCurrent < 0.9 && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Charge) && chargeCurrent != -1.0F)
                return "Charge";
            else if (chargeCurrent < 0.9 && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Bolt))
                return "Bolt";
            else if (!anim.reloading && !model.isType(GunRenderConfig.Arms.EnumArm.Left, GunRenderConfig.Arms.EnumAction.Pump))
                return "Default";
            else return "Reload";
        }
    }

    //Determine the state of the moving arm
    public static String getMovingArmState(ModelGun model, AnimStateMachine anim) {
        WeaponAnimation wepAnim = WeaponAnimations.getAnimation(model.config.extra.reloadAnimation);
        Optional<StateEntry> currentShootState = anim.getShootState();
        Optional<StateEntry> currentReloadState = anim.getReloadState();
        float pumpCurrent = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().currentValue : 1f : 1f;
        float chargeCurrent = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().currentValue : 1f : 1f;

        //Calls reload animation from the specified animation file
        if (!model.config.arms.leftHandAmmo) {
            if ((anim.isShootState(StateType.PumpIn) || anim.isShootState(StateType.PumpOut)) && pumpCurrent < 0.9 && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Charge) && pumpCurrent != -1.0F)
                return "Pump";
            else if (anim.isReloadState(StateType.Charge) && chargeCurrent < 0.9 && model.isType(GunRenderConfig.Arms.EnumArm.Right, GunRenderConfig.Arms.EnumAction.Bolt))
                return "Bolt";
            else if (!anim.reloading) return "Default";
            else if (anim.isReloadState(StateType.Load)) return "Load";
                //else if() movingArmState = "Unload";
            else return "Reload";
        } else {
            if (anim.isReloadState(StateType.Charge) && model.isType(GunRenderConfig.Arms.EnumArm.Left, GunRenderConfig.Arms.EnumAction.Charge) && chargeCurrent != -1.0F)
                return "Charge";
            else if ((anim.isShootState(StateType.PumpIn) || anim.isShootState(StateType.PumpOut)) && !anim.reloading && model.isType(GunRenderConfig.Arms.EnumArm.Left, GunRenderConfig.Arms.EnumAction.Pump))
                return "Pump";
            else if (!anim.reloading) return "Default";
            else if (anim.isReloadState(StateType.Load)) return "Load";
            else if (anim.isReloadState(StateType.Unload)) return "Unload";
            else return "Reload";
        }
    }

    @Override
    public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data) {
        if (!(item.getItem() instanceof ItemGun))
            return;

        GunType gunType = ((ItemGun) item.getItem()).type;
        if (gunType == null)
            return;

        ModelGun model = (ModelGun) gunType.model;
        if (model == null)
            return;
        {
            AnimStateMachine anim = data.length >= 2 ? data[1] instanceof EntityPlayer ? ClientRenderHooks.getAnimMachine((EntityPlayer) data[1]) : new AnimStateMachine() : new AnimStateMachine();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            boolean glow = ObjModelRenderer.glowTxtureMode;
            ObjModelRenderer.glowTxtureMode = true;
            renderGun(type, item, anim, gunType, data);
            GlStateManager.shadeModel(GL11.GL_FLAT);
            ObjModelRenderer.glowTxtureMode = glow;
        }
    }

    private void renderGun(CustomItemRenderType renderType, ItemStack item, AnimStateMachine anim, GunType gunType, Object... data) {
        Minecraft mc = Minecraft.getMinecraft();
        ModelGun model = (ModelGun) gunType.model;

        /** Random Shake */
        float min = -1.5f;
        float max = 1.5f;
        float randomNum = new Random().nextFloat();
        float randomShake = min + (randomNum * (max - min));

        /** Current States */
        Optional<StateEntry> currentReloadState = anim.getReloadState();
        Optional<StateEntry> currentShootState = anim.getShootState();

        float tiltProgress = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Tilt || currentReloadState.get().stateType == StateType.Untilt) ? currentReloadState.get().currentValue : anim.tiltHold ? 1f : 0f : 0f;
        float worldScale = 1F / 16F;

        if (renderEngine == null)
            renderEngine = Minecraft.getMinecraft().renderEngine;

        if (model == null)
            return;

        GL11.glPushMatrix();
        {
            switch (renderType) {

                case ENTITY: {
                    GL11.glTranslatef(-0.5F, -0.08F, 0F);
                    GL11.glRotatef(0, 0F, 0F, 1F); //ANGLE UP-DOWN

                    GL11.glTranslatef(model.config.itemFrame.translate.x * worldScale, model.config.itemFrame.translate.y * worldScale, model.config.itemFrame.translate.z * worldScale);

                    break;
                }

                case EQUIPPED: {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) data[1];
                    //float crouchOffset = entityLivingBase.isSneaking() ? -0.18f : 0.0f;
                    float crouchOffset = 0;
                    GL11.glRotatef(0F, 1F, 0F, 0F);
                    GL11.glRotatef(-90F, 0F, 1F, 0F);
                    GL11.glRotatef(90F, 0F, 0F, 1F);
                    GL11.glTranslatef(0.25F, 0F, -0.05F);
                    GL11.glScalef(1F, 1F, 1F);

                    GL11.glScalef(model.config.thirdPerson.thirdPersonScale, model.config.thirdPerson.thirdPersonScale, model.config.thirdPerson.thirdPersonScale);
                    GL11.glTranslatef(model.config.thirdPerson.thirdPersonOffset.x, model.config.thirdPerson.thirdPersonOffset.y + crouchOffset, model.config.thirdPerson.thirdPersonOffset.z);
                    break;
                }

                case BACK: {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) data[1];

                    GL11.glScalef(model.config.thirdPerson.thirdPersonScale, model.config.thirdPerson.thirdPersonScale, model.config.thirdPerson.thirdPersonScale);
                    GL11.glTranslatef(-0.32F, 1.3F, -0.23F);
                    GL11.glTranslatef(model.config.thirdPerson.backPersonOffset.x, model.config.thirdPerson.backPersonOffset.y, model.config.thirdPerson.backPersonOffset.z);
                    /*if (entityLivingBase.isSneaking()) {
                        GlStateManager.rotate(20, 1, 0, 0);
                        GlStateManager.translate(0, -0.3f, -0.2f);
                    }*/
                    GL11.glRotatef(90.0f, 0.0f, 20.0f, 0.0f);
                    GL11.glRotatef(270.0f, 0.0f, 0.0f, -90.0f);
                    GL11.glRotatef(90.0f, 20.0f, 0.0f, 0.0f);

                    GL11.glRotatef(20.0f, 0.0f, 0.0f, 20.0f);

                    break;
                }

                case EQUIPPED_FIRST_PERSON: {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) data[1];
                    float modelScale = model.config.extra.modelScale;
                    float rotateX = 0;
                    float rotateY = 0;
                    float rotateZ = 0;
                    float translateX = 0;
                    float translateY = 0;
                    float translateZ = 0;
                    float crouchZoom = anim.reloading ? 0f : anim.isReloadState(StateType.Charge) ? 0f : model.config.extra.crouchZoom;
                    float hipRecover = reloadSwitch;

                    // Store the staticModel settings as local variables to reduce calls
                    Vector3f customHipRotation = new Vector3f(model.config.aim.rotateHipPosition.x + (model.config.sprint.sprintRotate.x * sprintSwitch * hipRecover), model.config.aim.rotateHipPosition.y + (model.config.sprint.sprintRotate.y * sprintSwitch * hipRecover), model.config.aim.rotateHipPosition.z + (model.config.sprint.sprintRotate.z * sprintSwitch * hipRecover));
                    Vector3f customHipTranslate = new Vector3f(model.config.aim.translateHipPosition.x + (model.config.sprint.sprintTranslate.x * sprintSwitch * hipRecover), (model.config.aim.translateHipPosition.y + 0.04f) + (model.config.sprint.sprintTranslate.y * sprintSwitch * hipRecover), (model.config.aim.translateHipPosition.z - 0.15f) + (model.config.sprint.sprintTranslate.z * sprintSwitch * hipRecover));

                    Vector3f customAimRotation = new Vector3f(model.config.aim.rotateAimPosition.x, model.config.aim.rotateAimPosition.y, model.config.aim.rotateAimPosition.z);
                    Vector3f customAimTranslate = new Vector3f(model.config.aim.translateAimPosition.x, model.config.aim.translateAimPosition.y, model.config.aim.translateAimPosition.z);

                    for (AttachmentPresetEnum attachment : AttachmentPresetEnum.values()) {
                        ItemStack itemStack = GunType.getAttachment(item, attachment);
                        if (itemStack != null && itemStack.getItem() != Items.AIR) {
                            AttachmentType attachmentType = ((ItemAttachment) itemStack.getItem()).type;
                            if (attachmentType.attachmentType == AttachmentPresetEnum.Sight) {
                                if (model.config.attachments.aimPointMap != null) {
                                    for (String internalName : model.config.attachments.aimPointMap.keySet()) {
                                        if (internalName.equals(attachmentType.internalName)) {
                                            Vector3f trans = model.config.attachments.aimPointMap.get(internalName).get(0);
                                            Vector3f rot = model.config.attachments.aimPointMap.get(internalName).get(1);
                                            customAimTranslate.translate(trans.x * worldScale, -trans.y * worldScale, -trans.z * worldScale);
                                            customAimRotation.translate(rot.x, rot.y, rot.z);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    RenderParameters.VAL = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 140) * 1.0f);
                    RenderParameters.VAL2 = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 100) * 1.0f);
                    RenderParameters.VALROT = (float) (Math.sin(RenderParameters.SMOOTH_SWING / 90) * 1.2f);
                    RenderParameters.CROSS_ROTATE = 0;


                    if (!anim.shooting) {
                        RenderParameters.VALSPRINT = (float) (Math.cos(RenderParameters.SMOOTH_SWING / 5) * 5) * (0.95f - adsSwitch) * gunType.moveSpeedModifier;
                    }

                    adsSwitch = anim.reloading ? 0f : adsSwitch;

                    rotateX = (0 + customHipRotation.x) - (VALROT * (0.95f - adsSwitch)) - (0F + customAimRotation.x + customHipRotation.x * adsSwitch);
                    rotateY = (46F + customHipRotation.y) - (1F + customAimRotation.y + customHipRotation.y) * adsSwitch;
                    rotateZ = (35F * collideFrontDistance) + (1 + customHipRotation.z) - (1.0F + customAimRotation.z + customHipRotation.z) * adsSwitch;

                    translateX = (-1.3F + customHipTranslate.x) - (0.0F + customAimTranslate.x + customHipTranslate.x) * adsSwitch;
                    translateY = (0.7F * collideFrontDistance) + (0.834F + customHipTranslate.y) - ((VAL / 500) * (0.95f - adsSwitch)) - (-0.064F + customAimTranslate.y + customHipTranslate.y) * adsSwitch;
                    translateZ = (-1.05F + customHipTranslate.z) - ((VAL2 / 500 * (0.95f - adsSwitch))) - (0.35F + customAimTranslate.z + customHipTranslate.z) * adsSwitch;

                    if (this.timer == null) {
                        this.timer = ReflectionHelper.getPrivateValue(Minecraft.class, mc, "timer", "field_71428_T");
                    }

                    float partialTicks = this.timer.renderPartialTicks;

                    // Custom view bobbing applies to gun models
                    float bobModifier = !entityLivingBase.isSprinting() ? adsSwitch == 0F ? !anim.reloading ? 0.7F : 0.2F : 0.15F : !anim.reloading ? adsSwitch == 0 ? 0.75F : 0.15F : 0.4F;
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
                    GlStateManager.translate(MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F * anim.reloadProgress, -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3) * anim.reloadProgress, 0.0F);
                    GlStateManager.rotate(MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(f4, 1.0F, 0.0F, 0.0F);
                    prevBobModifier = bobModifier;

                    // Position calls and apply a special position if player is sprinting or crouching
                    GL11.glRotatef(rotateX, 1F, 0F, 0F); //ROLL LEFT-RIGHT
                    GL11.glRotatef(rotateY, 0F, 1F, 0F); //ANGLE LEFT-RIGHT
                    GL11.glRotatef(rotateZ, 0F, 0F, 1F); //ANGLE UP-DOWN
                    GL11.glTranslatef(translateX + (crouchZoom * crouchSwitch), 0F, 0F);
                    GL11.glTranslatef(0F, translateY, 0F);
                    GL11.glTranslatef(0F, 0F, translateZ);

                    if (!Minecraft.getMinecraft().player.onGround) {
                        VALSPRINT *= 0.15f;
                    }
                    GL11.glRotatef(((Minecraft.getMinecraft().player.isSprinting()) ? VALSPRINT : 0), 1, 1, -1);

                    Vector3f customAttachmentModeRotation = new Vector3f((model.config.attachments.attachmentModeRotate.x * attachmentSwitch), (model.config.attachments.attachmentModeRotate.y * attachmentSwitch), (model.config.attachments.attachmentModeRotate.z * attachmentSwitch));
                    GL11.glRotatef(customAttachmentModeRotation.x, 1F, 0F, 0F); //ROLL LEFT-RIGHT
                    GL11.glRotatef(customAttachmentModeRotation.y, 0F, 1F, 0F); //ANGLE LEFT-RIGHT
                    GL11.glRotatef(customAttachmentModeRotation.z, 0F, 0F, 1F); //ANGLE UP-DOWN

                    float gunRotX = RenderParameters.GUN_ROT_X_LAST + (RenderParameters.GUN_ROT_X - RenderParameters.GUN_ROT_X_LAST) * partialTicks;
                    float gunRotY = RenderParameters.GUN_ROT_Y_LAST + (RenderParameters.GUN_ROT_Y - RenderParameters.GUN_ROT_Y_LAST) * partialTicks;

                    GL11.glRotatef(gunRotX, 0, -1, 0);
                    GL11.glRotatef(gunRotY, 0, 0, -1);

                    GL11.glRotatef((GUN_BALANCING_X * 4F) * (1F - adsSwitch), -1, 0, 0);
                    GL11.glRotatef((float) Math.sin(Math.PI * GUN_BALANCING_X) * (1F - adsSwitch), -1, 0, 0);
                    GL11.glRotatef((GUN_BALANCING_X) * adsSwitch * 0.4F, -1, 0, 0);

                    GL11.glRotatef((GUN_BALANCING_Y * 2F) * (1F - adsSwitch), 0, 0, -1);
                    //GL11.glRotatef((GUN_BALANCING_Y) * adsSwitch * 0.4F, 0, 0, -1);

                    GL11.glTranslatef(0F, (float) Math.sin(Math.PI* -GUN_CHANGE_Y) * 1.5F, 0F);
                    GL11.glRotatef(80 * (float) Math.sin(Math.PI* -GUN_CHANGE_Y), 0, 0, -1);
                    GL11.glRotatef(-120 * (float) Math.sin(Math.PI* -GUN_CHANGE_Y), -1, 0, 0);

                    //Render Scope
                    WeaponScopeModeType modeType = gunType.scopeModeType;

                    if (GunType.getAttachment(item, AttachmentPresetEnum.Sight) != null) {
                        if (GunType.getAttachment(item, AttachmentPresetEnum.Sight).getItem() != null) {
                            ItemAttachment attachmentSight = (ItemAttachment) GunType.getAttachment(item, AttachmentPresetEnum.Sight).getItem();
                            if (attachmentSight != null) {
                                modeType = attachmentSight.type.sight.modeType;
                            }
                        }
                    }

                    if (modeType.isMirror) {
                        if (adsSwitch == 1.0F) {
                            GL11.glTranslatef(model.config.extra.gunOffsetScoping, 0F, 0F);
                            if (!ClientRenderHooks.isAimingScope) {
                                ClientRenderHooks.isAimingScope = true;
                                ModularWarfare.NETWORK.sendToServer(new PacketAimingRequest(entityplayer.getDisplayNameString(), true));
                            }
                        } else {
                            if (ClientRenderHooks.isAimingScope) {
                                ClientRenderHooks.isAimingScope = false;
                                ModularWarfare.NETWORK.sendToServer(new PacketAimingRequest(entityplayer.getDisplayNameString(), false));
                            }
                        }
                    } else {
                        if (adsSwitch == 1.0F) {
                            if (!ClientRenderHooks.isAiming) {
                                ClientRenderHooks.isAiming = true;
                                ModularWarfare.NETWORK.sendToServer(new PacketAimingRequest(entityplayer.getDisplayNameString(), true));
                            }
                        } else {
                            if (ClientRenderHooks.isAiming) {
                                ClientRenderHooks.isAiming = false;
                                ModularWarfare.NETWORK.sendToServer(new PacketAimingRequest(entityplayer.getDisplayNameString(), false));
                            }
                        }
                    }


                    //GL11.glRotatef(((Minecraft.getMinecraft().player.isSprinting() && Minecraft.getMinecraft().player.onGround) ? aniSprint : 0), 1, 1, -1);

                    // Calls reload animation from the specified animation file
                    if (anim.reloading && WeaponAnimations.getAnimation(model.config.extra.reloadAnimation) != null) {
                        WeaponAnimations.getAnimation(model.config.extra.reloadAnimation).onGunAnimation(tiltProgress, anim);
                    }

                    // Recoil
                    GL11.glTranslatef(-(anim.lastGunRecoil + (anim.gunRecoil - anim.lastGunRecoil) * smoothing) * model.config.extra.modelRecoilBackwards, 0F, 0F);
                    GL11.glRotatef((anim.lastGunRecoil + (anim.gunRecoil - anim.lastGunRecoil) * smoothing) * model.config.extra.modelRecoilUpwards, 0F, 0F, 1F);
                    GL11.glRotatef(((-anim.lastGunRecoil + (anim.gunRecoil - anim.lastGunRecoil) * smoothing) * randomShake * model.config.extra.modelRecoilShake), 0.0f, 1.0f, 0.0f);
                    GL11.glRotatef(((-anim.lastGunRecoil + (anim.gunRecoil - anim.lastGunRecoil) * smoothing) * randomShake * model.config.extra.modelRecoilShake), 1.0f, 0.0f, 0.0f);

                    GL11.glPushMatrix();

                    /** Flashlight **/
                    if (this.light == null) {
                        this.light = new ItemStack(ClientProxy.itemLight, 1);
                    }
                    final IBakedModel lightmodel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(this.light);

                    final int lightVar = ModUtil.getBrightness(Minecraft.getMinecraft().player);
                    final float wantedDiff = 15 - lightVar;
                    if (wantedDiff > this.slowDiff) {
                        this.slowDiff = Math.min(this.slowDiff + 0.1f, wantedDiff);
                    }
                    if (wantedDiff < this.slowDiff) {
                        this.slowDiff = Math.max(this.slowDiff - 0.1f, wantedDiff);
                    }

                    if (isLightOn && GunType.getAttachment(item, AttachmentPresetEnum.Flashlight) != null) {
                        final float alpha = 0.25f + this.slowDiff * 0.05f;
                        GlStateManager.rotate(-90, 0,1,0);

                        GL11.glDisable(2896);
                        Minecraft.getMinecraft().entityRenderer.disableLightmap();
                        GL11.glDisable(3042);
                        GL11.glPushMatrix();
                        GL11.glPushAttrib(16384);
                        GL11.glEnable(3042);
                        GL11.glDepthMask(false);
                        GL11.glBlendFunc(774, 770);
                        GlStateManager.translate(-0.33, -0.33, -3.0);
                        GlStateManager.scale(3.5, 3.5, 1.0);

                        ModUtil.renderLightModel(lightmodel, (int) ((alpha) * (this.slowDiff * 10.0f)));
                        ModUtil.renderLightModel(lightmodel, (int) (alpha * 255.0f));
                        if (alpha > 0.9) {
                            ModUtil.renderLightModel(lightmodel, (int) 255.0f);
                        }
                        GL11.glBlendFunc(770, 771);
                        GL11.glDepthMask(true);
                        GL11.glDisable(3042);
                        GL11.glPopAttrib();
                        GL11.glPopMatrix();
                        GL11.glEnable(2896);
                        Minecraft.getMinecraft().entityRenderer.enableLightmap();
                    }
                    GL11.glPopMatrix();

                    if (anim.gunRecoil > 0.1F && entityplayer.isSprinting()) {
                        RenderParameters.reloadSwitch = 0f;
                        RenderParameters.sprintSwitch = 0f;
                    }
                    break;
                }
                default:
                    break;
            }

            //Render call for the static arm
            if (renderType == CustomItemRenderType.EQUIPPED_FIRST_PERSON && model.hasArms()) {
                renderStaticArm(mc.player, model, anim, currentReloadState);
            }


            GL11.glPushMatrix();
            {


                float modelScale = model.config.extra.modelScale;

                /** Weapon Texture */
                int skinId = 0;
                if (item.hasTagCompound()) {
                    if (item.getTagCompound().hasKey("skinId")) {
                        skinId = item.getTagCompound().getInteger("skinId");
                    }
                }

                String path = skinId > 0 ? gunType.modelSkins[skinId].getSkin() : gunType.modelSkins[0].getSkin();
                String gunPath = path;
                bindTexture("guns", path);

                GL11.glEnable(GL11.GL_TEXTURE_2D);

                GL11.glScalef(modelScale, modelScale, modelScale);

                /** FOR BLENDER **/
                GL11.glTranslatef(3.0f * worldScale, -(-5.37f) * worldScale, -(-0.01f) * worldScale);

                GL11.glTranslatef(model.config.extra.translateAll.x * worldScale, -model.config.extra.translateAll.y * worldScale, -model.config.extra.translateAll.z * worldScale);

                // Item frame rendering properties
                if (renderType == CustomItemRenderType.ENTITY) {
                    if (!(Minecraft.getMinecraft().currentScreen instanceof GuiInventory)) {
                        GlStateManager.enableRescaleNormal();
                        RenderHelper.enableStandardItemLighting();
                        GlStateManager.shadeModel(GL11.GL_SMOOTH);
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                    }
                }

                model.renderPart("gunModel", worldScale);

                //Render any attachments
                if (GunType.getAttachment(item, AttachmentPresetEnum.Sight) == null && !model.config.attachments.scopeIsOnSlide)
                    model.renderPart("defaultScopeModel", worldScale);

                //Render any attachments
                if (GunType.getAttachment(item, AttachmentPresetEnum.Barrel) == null)
                    model.renderPart("defaultBarrelModel", worldScale);

                model.renderPart("defaultStockModel", worldScale);
                model.renderPart("defaultGripModel", worldScale);
                model.renderPart("defaultGadgetModel", worldScale);

                //Render pump action
                ItemStack pumpAttachment = null;
                if (pumpAttachment == null) {
                    GL11.glPushMatrix();
                    {
                        float pumpCurrent = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().currentValue : 1f : 1f;
                        float pumpLast = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().lastValue : 1f : 1f;

                        boolean isAmmoEmpty = !ItemGun.hasNextShot(item);

                        //Doubles as bolt action animation if set
                        if (model.config.arms.actionType == GunRenderConfig.Arms.EnumAction.Bolt) {
                            if (anim.isReloadState(StateType.Uncharge) || anim.isReloadState(StateType.Charge)) {
                                StateEntry boltState = anim.getReloadState().get();
                                pumpCurrent = boltState.currentValue;
                                pumpLast = boltState.lastValue;
                            }

                            if ((anim.isShootState(StateType.Charge) && !isAmmoEmpty) || anim.isShootState(StateType.Uncharge)) {
                                StateEntry boltState = anim.getShootState().get();
                                pumpCurrent = boltState.currentValue;
                                pumpLast = boltState.lastValue;

                            }

                            if ((isAmmoEmpty || anim.reloading) && !anim.isReloadState(StateType.Uncharge)) {
                                GL11.glTranslatef(-model.config.extra.gunSlideDistance, 0F, 0F);
                            }

                            GL11.glTranslatef(model.config.bolt.boltRotationPoint.x, model.config.bolt.boltRotationPoint.y, model.config.bolt.boltRotationPoint.z);
                            GL11.glRotatef(model.config.bolt.boltRotation * (1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)), 1, 0, 0);
                            GL11.glTranslatef(-model.config.bolt.boltRotationPoint.x, -model.config.bolt.boltRotationPoint.y, -model.config.bolt.boltRotationPoint.z);
                        }

                        GL11.glTranslatef(-(1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)) * model.config.bolt.pumpHandleDistance, 0F, 0F);

                        if (gunType.weaponType == WeaponType.DMR) {
                            if (!anim.isGunEmpty) {
                                GL11.glTranslatef(-(anim.lastGunSlide + (anim.gunSlide - anim.lastGunSlide) * smoothing) * model.config.extra.gunSlideDistance, 0F, 0F);
                            }
                        }

                        if (model.config.arms.actionType == GunRenderConfig.Arms.EnumAction.Bolt) {
                            model.renderPart("boltModel", worldScale);
                        }

                        model.renderPart("pumpModel", worldScale);
                    }
                    GL11.glPopMatrix();
                }

                //Render charge handle
                if (model.config.extra.chargeHandleDistance != 0F && gunType.weaponType == WeaponType.Shotgun) {
                    GL11.glPushMatrix();
                    {
                        float pumpCurrent = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().currentValue : 1f : 1f;
                        float pumpLast = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().lastValue : 1f : 1f;
                        GL11.glTranslatef(-(1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)) * model.config.extra.chargeHandleDistance, 0F, 0F);

                        model.renderPart("chargeModel", worldScale);
                    }
                    GL11.glPopMatrix();
                }


                //Render Slide
                if (GunType.getAttachment(item, AttachmentPresetEnum.Slide) == null) {
                    float currentCharge = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().currentValue : 1f : 1f;
                    float lastCharge = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().lastValue : 1f : 1f;

                    if (model.config.extra.needExtraChargeModel) {
                        GL11.glPushMatrix();
                        GL11.glTranslatef(-(1 - Math.abs(lastCharge + (currentCharge - lastCharge) * smoothing)) * model.config.extra.chargeHandleDistance, 0F, 0F);
                        model.renderPart("chargeModel", worldScale);
                        GL11.glPopMatrix();
                    }

                    GL11.glPushMatrix();
                    {


                        GL11.glPushMatrix();

                        if (!anim.isGunEmpty) {
                            GL11.glTranslatef(-(anim.lastGunSlide + (anim.gunSlide - anim.lastGunSlide) * smoothing) * model.config.extra.gunSlideDistance, 0F, 0F);
                        } else {
                            GL11.glTranslatef(-model.config.extra.gunSlideDistance, 0F, 0F);
                        }
                        GL11.glTranslatef(-(1 - Math.abs(lastCharge + (currentCharge - lastCharge) * smoothing)) * model.config.extra.chargeHandleDistance, 0F, 0F);

                        model.renderPart("slideModel", worldScale);
                        GL11.glPopMatrix();

                        if (GunType.getAttachment(item, AttachmentPresetEnum.Sight) == null && model.config.attachments.scopeIsOnSlide)
                            model.renderPart("defaultScopeModel", worldScale);

                        //Render the scope on the slide, if its set on slide
                        if (model.switchIsOnSlide) {
                            GL11.glPushMatrix();
                            {
                                WeaponFireMode fireMode = GunType.getFireMode(item);
                                float switchAngle = fireMode == WeaponFireMode.SEMI ? model.switchSemiRot : fireMode == WeaponFireMode.FULL ? model.switchAutoRot : fireMode == WeaponFireMode.BURST ? model.switchBurstRot : 0F;
                                GL11.glTranslatef(model.switchRotationPoint.x, model.switchRotationPoint.y, model.switchRotationPoint.z);
                                GL11.glRotatef(switchAngle, 0, 0, 1);
                                GL11.glTranslatef(-model.switchRotationPoint.x, -model.switchRotationPoint.y, -model.switchRotationPoint.z);
                                model.renderPart("switchModel", worldScale);
                            }
                            GL11.glPopMatrix();
                        }
                    }
                    GL11.glPopMatrix();
                }

                //Render break action, uses an array system to allow multiple different break action types on a gun
                for (BreakActionData breakAction : model.config.breakAction.breakActions) {
                    float breakProgress = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Tilt || currentReloadState.get().stateType == StateType.Untilt) ? currentReloadState.get().currentValue : anim.tiltHold ? 1f : 0f : 0f;
                    GL11.glPushMatrix();
                    {
                        GL11.glTranslatef(breakAction.breakPoint.x, breakAction.breakPoint.y, breakAction.breakPoint.z);
                        GL11.glRotatef(breakProgress * -breakAction.angle, 0F, 0F, 1F);
                        GL11.glTranslatef(-breakAction.breakPoint.x, -breakAction.breakPoint.y, -breakAction.breakPoint.z);
                        model.renderPart(breakAction.modelName, worldScale);
                        if (GunType.getAttachment(item, AttachmentPresetEnum.Sight) == null && model.config.breakAction.scopeIsOnBreakAction && breakAction.scopePart)
                            model.renderPart("defaultScopeModel", worldScale);
                    }
                    GL11.glPopMatrix();
                }

                // Slide lock - Keeps slide in the back position when empty if true
                boolean isAmmoEmpty = !ItemGun.hasNextShot(item);
                if (model.slideLockOnEmpty) {
                    if (isAmmoEmpty)
                        anim.isGunEmpty = true;

                    else if (!isAmmoEmpty && !anim.reloading)
                        anim.isGunEmpty = false;
                }

                //Render hammer actions
                GL11.glPushMatrix();
                {
                    GL11.glTranslatef(model.config.hammerAction.hammerRotationPoint.x * worldScale, model.config.hammerAction.hammerRotationPoint.y * worldScale, model.config.hammerAction.hammerRotationPoint.z * worldScale);
                    if (!anim.isGunEmpty) {
                        GL11.glRotatef(50F, 0F, 0F, 1F);
                        GL11.glRotatef(-anim.hammerRotation * 2, 0F, 0F, 1F);
                    }
                    GL11.glTranslatef(-model.config.hammerAction.hammerRotationPoint.x * worldScale, -model.config.hammerAction.hammerRotationPoint.y * worldScale, -model.config.hammerAction.hammerRotationPoint.z * worldScale);
                    model.renderPart("hammerModel", worldScale);
                }
                GL11.glPopMatrix();

                // Render lever action
                GL11.glPushMatrix();
                {
                    float pumpCurrent = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().currentValue : 1f : 1f;
                    float pumpLast = currentShootState.isPresent() ? (currentShootState.get().stateType == StateType.PumpOut || currentShootState.get().stateType == StateType.PumpIn) ? currentShootState.get().lastValue : 1f : 1f;
                    GL11.glTranslatef(model.leverRotationPoint.x, model.leverRotationPoint.y, model.leverRotationPoint.z);
                    GL11.glRotatef(model.leverRotation * (1 - Math.abs(pumpLast + (pumpCurrent - pumpLast) * smoothing)), 0, 0, 1);
                    GL11.glTranslatef(-model.leverRotationPoint.x, -model.leverRotationPoint.y, -model.leverRotationPoint.z);
                    model.renderPart("leverActionModel", worldScale);
                }
                GL11.glPopMatrix();

                // Render trigger
                GL11.glPushMatrix();
                {
                    GL11.glTranslatef(model.triggerRotationPoint.x, model.triggerRotationPoint.y, model.triggerRotationPoint.z);
                    GL11.glRotatef(model.triggerRotation * (triggerPullSwitch * 50), 0, 0, 1);
                    GL11.glTranslatef(-model.triggerRotationPoint.x, -model.triggerRotationPoint.y, -model.triggerRotationPoint.z);
                    model.renderPart("triggerModel", worldScale);
                }
                GL11.glPopMatrix();

                // Render fire mode switch
                if (!model.switchIsOnSlide) {
                    GL11.glPushMatrix();
                    {
                        WeaponFireMode fireMode = GunType.getFireMode(item);
                        float switchAngle = fireMode == WeaponFireMode.SEMI ? model.switchSemiRot : fireMode == WeaponFireMode.FULL ? model.switchAutoRot : fireMode == WeaponFireMode.BURST ? model.switchBurstRot : 0F;
                        GL11.glTranslatef(model.switchRotationPoint.x, model.switchRotationPoint.y, model.switchRotationPoint.z);
                        GL11.glRotatef(switchAngle, 0, 0, 1);
                        GL11.glTranslatef(-model.switchRotationPoint.x, -model.switchRotationPoint.y, -model.switchRotationPoint.z);
                        model.renderPart("switchModel", worldScale);
                    }
                    GL11.glPopMatrix();
                }

                if (gunType.weaponType == WeaponType.Revolver) {
                    // Render the revolver barrel
                    GL11.glPushMatrix();
                    {

                        GL11.glTranslatef(model.config.revolverBarrel.cylinderOriginPoint.x * worldScale, model.config.revolverBarrel.cylinderOriginPoint.y * worldScale, model.config.revolverBarrel.cylinderOriginPoint.z * worldScale);
                        float updatedTiltProgress = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Tilt || currentReloadState.get().stateType == StateType.Untilt) ? currentReloadState.get().currentValue : anim.tiltHold ? 1f : 0f : 0f;
                        GL11.glTranslatef(updatedTiltProgress * model.config.revolverBarrel.cylinderReloadTranslation.x * worldScale, updatedTiltProgress * model.config.revolverBarrel.cylinderReloadTranslation.y * worldScale, updatedTiltProgress * model.config.revolverBarrel.cylinderReloadTranslation.z * worldScale);
                        GL11.glRotatef(anim.revolverBarrelRotation, 1F, 0F, 0F);
                        GL11.glTranslatef(-model.config.revolverBarrel.cylinderOriginPoint.x * worldScale, -model.config.revolverBarrel.cylinderOriginPoint.y * worldScale, -model.config.revolverBarrel.cylinderOriginPoint.z * worldScale);
                        model.renderPart("revolverBarrelModel", worldScale);
                    }
                    GL11.glPopMatrix();
                }

                // Ammo
                GL11.glPushMatrix();
                {
                    boolean cachedUnload = (anim.isReloadType(ReloadType.Unload) && anim.cachedAmmoStack != null);
                    if (ItemGun.hasAmmoLoaded(item) || cachedUnload) {
                        ItemStack stackAmmo = cachedUnload ? anim.cachedAmmoStack : new ItemStack(item.getTagCompound().getCompoundTag("ammo"));
                        if (stackAmmo.getItem() instanceof ItemAmmo) {
                            ItemAmmo itemAmmo = (ItemAmmo) stackAmmo.getItem();
                            AmmoType ammoType = itemAmmo.type;
                            boolean shouldNormalRender = true;

                            if (anim.reloading && model.config.extra.reloadAnimation != null && WeaponAnimations.getAnimation(model.config.extra.reloadAnimation) != null) {
                                //Unload/Load ammo
                                float ammoProgress = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Unload || currentReloadState.get().stateType == StateType.Load) ? currentReloadState.get().currentValue : 0f : 1f;
                                WeaponAnimations.getAnimation(model.config.extra.reloadAnimation).onAmmoAnimation(model, ammoProgress, anim.reloadAmmoCount, anim);
                            }

                            if (ammoType.isDynamicAmmo && ammoType.model != null) {
                                ModelAmmo modelAmmo = (ModelAmmo) ammoType.model;
                                if (model.config.maps.ammoMap.containsKey(ammoType.internalName)) {
                                    Vector3f ammoOffset = model.config.maps.ammoMap.get(ammoType.internalName).offset;
                                    Vector3f ammoScale = model.config.maps.ammoMap.get(ammoType.internalName).scale;

                                    GL11.glTranslatef(ammoOffset.x, ammoOffset.y, ammoOffset.z);
                                    if (ammoType.magazineCount > 1) {
                                        int magCount = stackAmmo.getTagCompound().getInteger("magcount");
                                        if (!anim.reloading)
                                            oldMagCount = magCount;
                                        else if (anim.reloading)
                                            magCount = oldMagCount;

                                        if (modelAmmo.magCountOffset.containsKey(magCount)) {
                                            shouldNormalRender = false;
                                            GL11.glPushMatrix();
                                            {
                                                RenderVariables magRenderVar = modelAmmo.magCountOffset.get(magCount);
                                                Vector3f magOffset = magRenderVar.offset;
                                                Vector3f magRotate = magRenderVar.rotation;
                                                GL11.glTranslatef(magOffset.x, magOffset.y, magOffset.z);
                                                if (magRotate != null && magRenderVar.angle != null) {
                                                    GL11.glRotatef(magRenderVar.angle, magRotate.x, magRotate.y, magRotate.z);
                                                }

                                                Vector3f adjustedScale = new Vector3f(ammoScale.x / modelScale, ammoScale.y / modelScale, ammoScale.z / modelScale);
                                                GL11.glScalef(adjustedScale.x, adjustedScale.y, adjustedScale.z);

                                                int skinIdAmmo = 0;

                                                if (stackAmmo.hasTagCompound()) {
                                                    if (stackAmmo.getTagCompound().hasKey("skinId")) {
                                                        skinIdAmmo = stackAmmo.getTagCompound().getInteger("skinId");
                                                    }
                                                }
                                                if (ammoType.sameTextureAsGun) {
                                                    bindTexture("guns", path);
                                                } else {
                                                    String pathAmmo = skinIdAmmo > 0 ? ammoType.modelSkins[skinIdAmmo].getSkin() : ammoType.modelSkins[0].getSkin();
                                                    bindTexture("ammo", pathAmmo);
                                                }

                                                if (anim.shouldRenderAmmo()) {
                                                    if (!cachedUnload)
                                                        anim.cachedAmmoStack = stackAmmo;

                                                    modelAmmo.renderAmmo(worldScale);
                                                }
                                            }
                                            GL11.glPopMatrix();
                                        }
                                    }

                                    if (shouldNormalRender) {
                                        Vector3f adjustedScale = new Vector3f(ammoScale.x / modelScale, ammoScale.y / modelScale, ammoScale.z / modelScale);
                                        GL11.glScalef(adjustedScale.x, adjustedScale.y, adjustedScale.z);
                                    }
                                }
                                if (shouldNormalRender && anim.shouldRenderAmmo()) {
                                    if (!cachedUnload)
                                        anim.cachedAmmoStack = stackAmmo;

                                    int skinIdAmmo = 0;

                                    if (stackAmmo.hasTagCompound()) {
                                        if (stackAmmo.getTagCompound().hasKey("skinId")) {
                                            skinIdAmmo = stackAmmo.getTagCompound().getInteger("skinId");
                                        }
                                    }
                                    if (ammoType.sameTextureAsGun) {
                                        bindTexture("guns", path);
                                    } else {
                                        String pathAmmo = skinIdAmmo > 0 ? ammoType.modelSkins[skinIdAmmo].getSkin() : ammoType.modelSkins[0].getSkin();
                                        bindTexture("ammo", pathAmmo);
                                    }
                                    modelAmmo.renderAmmo(worldScale);
                                }
                            } else {
                                if (anim.shouldRenderAmmo()) {
                                    if (!cachedUnload)
                                        anim.cachedAmmoStack = stackAmmo;
                                    //These translates/rotate was just a test but seems to work well for moving ammo with revolver cylinder
                                    float updatedTiltProgress = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Tilt || currentReloadState.get().stateType == StateType.Untilt) ? currentReloadState.get().currentValue : anim.tiltHold ? 1f : 0f : 0f;

                                    GL11.glPushMatrix();

                                    GL11.glTranslatef(model.cylinderRotationPoint.x, model.cylinderRotationPoint.y, model.cylinderRotationPoint.z);
                                    GL11.glRotatef(updatedTiltProgress * model.cylinderRotation, 1F, 0F, 0F);
                                    GL11.glTranslatef(-model.cylinderRotationPoint.x, -model.cylinderRotationPoint.y, -model.cylinderRotationPoint.z);
                                    model.renderPart("ammoModel", worldScale);

                                    GL11.glPopMatrix();
                                }
                            }
                        }
                    } else if (ItemGun.getUsedBullet(item, gunType) != null) {
                        ItemBullet itemBullet = ItemGun.getUsedBullet(item, gunType);
                        ModelBullet bulletModel = (ModelBullet) itemBullet.type.model;

                        //Revolvers bullets rendering
                        if (gunType.weaponType == WeaponType.Revolver) {
                            GlStateManager.pushMatrix();
                            if (itemBullet.type.model != null && anim.reloading) {
                                GL11.glTranslatef(model.config.revolverBarrel.cylinderOriginPoint.x * worldScale, model.config.revolverBarrel.cylinderOriginPoint.y * worldScale, model.config.revolverBarrel.cylinderOriginPoint.z * worldScale);
                                float updatedTiltProgress = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Tilt || currentReloadState.get().stateType == StateType.Untilt) ? currentReloadState.get().currentValue : anim.tiltHold ? 1f : 0f : 0f;
                                GL11.glTranslatef(updatedTiltProgress * model.config.revolverBarrel.cylinderReloadTranslation.x * worldScale, updatedTiltProgress * model.config.revolverBarrel.cylinderReloadTranslation.y * worldScale, updatedTiltProgress * model.config.revolverBarrel.cylinderReloadTranslation.z * worldScale);
                                GL11.glRotatef(anim.revolverBarrelRotation, 1F, 0F, 0F);
                                GL11.glTranslatef(-model.config.revolverBarrel.cylinderOriginPoint.x * worldScale, -model.config.revolverBarrel.cylinderOriginPoint.y * worldScale, -model.config.revolverBarrel.cylinderOriginPoint.z * worldScale);
                            }
                            bindTexture("bullets", itemBullet.type.modelSkins[0].getSkin());

                            if (currentReloadState.isPresent() && currentReloadState.get().stateType == StateType.Tilt) {
                                GL11.glRotatef(-90, 1F, 0F, 0F);
                                bulletModel.renderAll(worldScale);
                            } else {
                                if (model.config.revolverBarrel.numberBullets != null) {
                                    bulletModel.renderBullet(anim.bulletsToRender, worldScale);
                                }
                            }
                            GlStateManager.popMatrix();
                        }

                        if (anim.reloading && model.config.extra.reloadAnimation != null && WeaponAnimations.getAnimation(model.config.extra.reloadAnimation) != null) {
                            if (anim.reloading && model.config.extra.reloadAnimation != null && WeaponAnimations.getAnimation(model.config.extra.reloadAnimation) != null) {
                                //Unload/Load ammo
                                float ammoProgress = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Unload || currentReloadState.get().stateType == StateType.Load) ? currentReloadState.get().currentValue : 0f : 1f;
                                WeaponAnimations.getAnimation(model.config.extra.reloadAnimation).onAmmoAnimation(model, ammoProgress, anim.reloadAmmoCount, anim);
                            }
                        }

                        if (itemBullet.type.model != null && anim.reloading && gunType.weaponType != WeaponType.Launcher) {
                            GL11.glPushMatrix();
                            {
                                if (model.config.maps.bulletMap.containsKey(itemBullet.baseType.internalName)) {
                                    RenderVariables renderVar = model.config.maps.bulletMap.get(itemBullet.type.internalName);
                                    Vector3f offset = renderVar.offset;
                                    GL11.glTranslatef(offset.x, offset.y, offset.z);
                                    if (renderVar.scale != null) {
                                        Vector3f scale = renderVar.scale;
                                        GL11.glScalef(scale.x, scale.y, scale.z);
                                    }
                                }
                                bindTexture("bullets", itemBullet.type.modelSkins[0].getSkin());
                                bulletModel.renderBullet(worldScale);
                            }
                            GL11.glPopMatrix();
                        }

                        if (itemBullet.type.model != null && gunType.weaponType == WeaponType.Launcher) {
                            GL11.glPushMatrix();
                            {
                                if (model.config.maps.bulletMap.containsKey(itemBullet.baseType.internalName)) {
                                    RenderVariables renderVar = model.config.maps.bulletMap.get(itemBullet.type.internalName);
                                    Vector3f offset = renderVar.offset;
                                    GL11.glTranslatef(offset.x, offset.y, offset.z);
                                    if (renderVar.scale != null) {
                                        Vector3f scale = renderVar.scale;
                                        GL11.glScalef(scale.x, scale.y, scale.z);
                                    }
                                }
                                int ammoCount = item.getTagCompound().getInteger("ammocount");
                                boolean isLoading = currentReloadState.isPresent() && (currentReloadState.get().stateType == StateType.Load);
                                if(isLoading || (ammoCount > 0 && !currentReloadState.isPresent())) {
                                    bindTexture("bullets", itemBullet.type.modelSkins[0].getSkin());
                                    bulletModel.renderBullet(worldScale);
                                }
                            }
                            GL11.glPopMatrix();
                        }

                    }
                }


                boolean shouldRenderFlash = true;

                if ((GunType.getAttachment(item, AttachmentPresetEnum.Barrel) != null)) {
                    AttachmentType attachmentType = ((ItemAttachment) GunType.getAttachment(item, AttachmentPresetEnum.Barrel).getItem()).type;
                    if (attachmentType.attachmentType == AttachmentPresetEnum.Barrel) {
                        shouldRenderFlash = !attachmentType.barrel.hideFlash;
                    }
                }

                TextureType flashType = gunType.flashType;

                if (anim.muzzleFlashTime > 0 && model.staticModel.getPart("flashModel") != null && !mc.player.isInWater() && renderType != BACK && shouldRenderFlash) {
                    GlStateManager.pushMatrix();
                    {

                        GL11.glEnable(3042);
                        GL11.glEnable(2832);
                        GL11.glHint(3153, 4353);

                        RenderGunStatic.renderEngine.bindTexture(flashType.resourceLocations.get(anim.flashInt));

                        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
                        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
                        GlStateManager.depthMask(false);
                        GlStateManager.disableLighting();
                        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                        
                        boolean glowMode=ObjModelRenderer.glowTxtureMode;
                        ObjModelRenderer.glowTxtureMode=false;
                        model.renderPart("flashModel", worldScale);
                        ObjModelRenderer.glowTxtureMode=glowMode;
                        
                        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
                        GlStateManager.enableLighting();
                        GlStateManager.depthMask(true);

                        GL11.glDisable(3042);
                        GL11.glDisable(2832);
                    }
                    GlStateManager.popMatrix();
                }

                if (renderType == CustomItemRenderType.EQUIPPED_FIRST_PERSON && model.hasArms()) {
                    renderMovingArm(mc.player, model, anim, currentReloadState);
                }

                GL11.glPopMatrix();

                GL11.glPushMatrix();
                {
                    for (AttachmentPresetEnum attachment : AttachmentPresetEnum.values()) {
                        ItemStack itemStack = GunType.getAttachment(item, attachment);
                        if (itemStack != null && itemStack.getItem() != Items.AIR) {
                            AttachmentType attachmentType = ((ItemAttachment) itemStack.getItem()).type;
                            ModelAttachment attachmentModel = (ModelAttachment) attachmentType.model;
                            if (attachmentModel != null) {
                                GL11.glPushMatrix();
                                {
                                    skinId = 0;
                                    if (itemStack.hasTagCompound()) {
                                        if (itemStack.getTagCompound().hasKey("skinId")) {
                                            skinId = itemStack.getTagCompound().getInteger("skinId");
                                        }
                                    }

                                    Vector3f adjustedScale = new Vector3f(attachmentModel.config.extra.modelScale, attachmentModel.config.extra.modelScale, attachmentModel.config.extra.modelScale);
                                    GL11.glScalef(adjustedScale.x, adjustedScale.y, adjustedScale.z);

                                    if (model.config.attachments.attachmentPointMap != null && model.config.attachments.attachmentPointMap.size() >= 1) {
                                        if (model.config.attachments.attachmentPointMap.containsKey(attachment)) {
                                            Vector3f attachmentVecTranslate = model.config.attachments.attachmentPointMap.get(attachment).get(0);
                                            Vector3f attachmentVecRotate = model.config.attachments.attachmentPointMap.get(attachment).get(1);
                                            GL11.glTranslatef(attachmentVecTranslate.x / attachmentModel.config.extra.modelScale, attachmentVecTranslate.y / attachmentModel.config.extra.modelScale, attachmentVecTranslate.z / attachmentModel.config.extra.modelScale);

                                            GL11.glRotatef(attachmentVecRotate.x, 1F, 0F, 0F); //ROLL LEFT-RIGHT
                                            GL11.glRotatef(attachmentVecRotate.y, 0F, 1F, 0F); //ANGLE LEFT-RIGHT
                                            GL11.glRotatef(attachmentVecRotate.z, 0F, 0F, 1F); //ANGLE UP-DOWN
                                        }
                                    }

                                    if (model.config.attachments.positionPointMap != null) {
                                        for (String internalName : model.config.attachments.positionPointMap.keySet()) {
                                            if (internalName.equals(attachmentType.internalName)) {
                                                Vector3f trans = model.config.attachments.positionPointMap.get(internalName).get(0);
                                                Vector3f rot = model.config.attachments.positionPointMap.get(internalName).get(1);
                                                GL11.glTranslatef(trans.x / attachmentModel.config.extra.modelScale * worldScale, trans.y / attachmentModel.config.extra.modelScale * worldScale, trans.z / attachmentModel.config.extra.modelScale * worldScale);

                                                GL11.glRotatef(rot.x, 1F, 0F, 0F); //ROLL LEFT-RIGHT
                                                GL11.glRotatef(rot.y, 0F, 1F, 0F); //ANGLE LEFT-RIGHT
                                                GL11.glRotatef(rot.z, 0F, 0F, 1F); //ANGLE UP-DOWN
                                            }
                                        }
                                    }

                                    if(attachmentType.sameTextureAsGun) {
                                        bindTexture("guns", gunPath);
                                    } else {
                                        path = skinId > 0 ? attachmentType.modelSkins[skinId].getSkin() : attachmentType.modelSkins[0].getSkin();
                                        bindTexture("attachments", path);
                                    }

                                    if (attachmentType.attachmentType == AttachmentPresetEnum.Sight && model.config.attachments.scopeIsOnSlide) {
                                        float currentCharge = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().currentValue : 1f : 1f;
                                        float lastCharge = currentReloadState.isPresent() ? (currentReloadState.get().stateType == StateType.Charge || currentReloadState.get().stateType == StateType.Uncharge) ? currentReloadState.get().lastValue : 1f : 1f;
                                        if (!anim.isGunEmpty) {
                                            GL11.glTranslatef(-(anim.lastGunSlide + (anim.gunSlide - anim.lastGunSlide) * smoothing) * model.config.extra.gunSlideDistance, 0F, 0F);
                                        } else {
                                            GL11.glTranslatef(-model.config.extra.gunSlideDistance, 0F, 0F);
                                        }
                                        GL11.glTranslatef(-(1 - Math.abs(lastCharge + (currentCharge - lastCharge) * smoothing)) * model.config.extra.chargeHandleDistance, 0F, 0F);
                                    }

                                    attachmentModel.renderAttachment(worldScale);
                                    if (attachmentType.attachmentType == AttachmentPresetEnum.Sight && mc.gameSettings.thirdPersonView == 0 && renderType == CustomItemRenderType.EQUIPPED_FIRST_PERSON) {
                                        boolean glowTxtureMode=ObjModelRenderer.glowTxtureMode;
                                        ObjModelRenderer.glowTxtureMode = false;
                                        renderScopeGlass(attachmentType, attachmentModel, adsSwitch != 0F);
                                        ObjModelRenderer.glowTxtureMode = glowTxtureMode;
                                    }
                                }
                                GL11.glPopMatrix();
                            }
                        }
                    }
                }
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();

        }
        GL11.glPopMatrix();

    }

    //Renders the static left or right hand that does not move with the ammo depending on leftHandAmmo setting
    private void renderStaticArm(EntityPlayer player, ModelGun model, AnimStateMachine anim, Optional<StateEntry> currentState) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(Minecraft.getMinecraft().player.getLocationSkin());
        Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(Minecraft.getMinecraft().player);
        RenderPlayer renderplayer = (RenderPlayer) render;

        float tiltProgress = currentState.isPresent() ? (currentState.get().stateType == StateType.Tilt || currentState.get().stateType == StateType.Untilt) ? currentState.get().currentValue : anim.tiltHold ? 1f : 0f : 0f;
        String staticArmState = getStaticArmState(model, anim);

        GL11.glPushMatrix();
        {

            boolean rightArm = model.config.arms.leftHandAmmo && model.config.arms.rightArm.armPos != null;
            if (staticArmState == "ToFrom" && rightArm && model.config.arms.actionArm == GunRenderConfig.Arms.EnumArm.Left) {
                rightArm = false;
            }
            Vector3f armScale = rightArm ? model.config.arms.rightArm.armScale : model.config.arms.leftArm.armScale;
            Vector3f armRot = rightArm ? model.config.arms.rightArm.armRot : model.config.arms.leftArm.armRot;
            Vector3f armPos = rightArm ? model.config.arms.rightArm.armPos : model.config.arms.leftArm.armPos;

            Vector3f chargeArmRot = model.config.arms.actionArm == GunRenderConfig.Arms.EnumArm.Right ? model.config.arms.rightArm.armChargeRot : model.config.arms.leftArm.armChargeRot;
            Vector3f chargeArmPos = model.config.arms.actionArm == GunRenderConfig.Arms.EnumArm.Right ? model.config.arms.rightArm.armChargePos : model.config.arms.leftArm.armChargePos;
            Vector3f reloadArmRot = rightArm ? model.config.arms.rightArm.armReloadRot : model.config.arms.leftArm.armReloadRot;
            Vector3f reloadArmPos = rightArm ? model.config.arms.rightArm.armReloadPos : model.config.arms.leftArm.armReloadPos;

            if (staticArmState == "Pump")
                RenderArms.renderArmPump(model, anim, smoothing, armRot, armPos, !model.config.arms.leftHandAmmo);
            else if (staticArmState == "Charge")
                RenderArms.renderArmCharge(model, anim, smoothing, chargeArmRot, chargeArmPos, armRot, armPos, !model.config.arms.leftHandAmmo);
            else if (staticArmState == "Bolt")
                RenderArms.renderArmBolt(model, anim, smoothing, chargeArmRot, chargeArmPos, !model.config.arms.leftHandAmmo);
            else if (staticArmState == "Default")
                RenderArms.renderArmDefault(model, anim, smoothing, armRot, armPos, rightArm, !model.config.arms.leftHandAmmo);
            else if (staticArmState == "Reload")
                RenderArms.renderStaticArmReload(model, anim, smoothing, tiltProgress, reloadArmRot, reloadArmPos, armRot, armPos, !model.config.arms.leftHandAmmo);
            else if (staticArmState == "ToFrom")
                RenderArms.renderToFrom(model, anim, smoothing, chargeArmRot, chargeArmPos, armRot, armPos, !model.config.arms.leftHandAmmo);

            //Render the armor staticModel on the arm
            GL11.glScalef(armScale.x, armScale.y, armScale.z);
            renderplayer.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
            renderplayer.getMainModel().bipedRightArm.offsetX = 0F;
            if (rightArm) {
                if(!MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Pre(this,EnumHandSide.RIGHT))) {
                    renderplayer.renderRightArm(Minecraft.getMinecraft().player);
                    MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Post(this,EnumHandSide.RIGHT));
                }
                renderRightSleeve(player, renderplayer.getMainModel());
            } else {
                if(!MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Pre(this,EnumHandSide.LEFT))) {
                    renderplayer.renderLeftArm(Minecraft.getMinecraft().player);
                    MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Post(this,EnumHandSide.LEFT));
                }
                renderLeftSleeve(player, renderplayer.getMainModel());
            }
        }
        GL11.glPopMatrix();
    }

    // Renders a left or right hand that moves with ammo depending on leftHandAmmo setting
    private void renderMovingArm(EntityPlayer player, ModelGun model, AnimStateMachine anim, Optional<StateEntry> currentState) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(Minecraft.getMinecraft().player.getLocationSkin());
        Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(Minecraft.getMinecraft().player);
        RenderPlayer renderplayer = (RenderPlayer) render;

        boolean rightArm = model.config.arms.leftHandAmmo && model.config.arms.rightArm.armPos != null;
        String movingArmState = getMovingArmState(model, anim);
        WeaponAnimation weaponAnimation = WeaponAnimations.getAnimation(model.config.extra.reloadAnimation);

        float tiltProgress = currentState.isPresent() ? (currentState.get().stateType == StateType.Tilt || currentState.get().stateType == StateType.Untilt) ? currentState.get().currentValue : anim.tiltHold ? 1f : 0f : 0f;

        /** Check if the hand need to be offset (grip attachment) **/
        Vector3f leftArmOffset = new Vector3f(0, 0, 0);
        if (player.getHeldItemMainhand() != null) {
            if (player.getHeldItemMainhand().getItem() instanceof ItemGun) {
                ItemStack itemStack = GunType.getAttachment(player.getHeldItemMainhand(), AttachmentPresetEnum.Grip);
                if (itemStack != null && itemStack.getItem() != Items.AIR) {
                    ItemAttachment itemAttachment = (ItemAttachment) itemStack.getItem();
                    leftArmOffset = ((ModelAttachment) itemAttachment.type.model).config.grip.leftArmOffset;
                }
            }
        }


        GL11.glPushMatrix();
        {

            GL11.glScalef(1 / model.config.extra.modelScale, 1 / model.config.extra.modelScale, 1 / model.config.extra.modelScale);

            GL11.glTranslatef(leftArmOffset.x, leftArmOffset.y, leftArmOffset.z);


            if (!model.config.arms.leftHandAmmo && model.config.arms.rightArm.armPos != null && model.config.arms.rightArm.armReloadPos != null) {
                GL11.glPushMatrix();
                {
                    if (movingArmState == "Pump") {
                        RenderArms.renderArmPump(model, anim, smoothing, model.config.arms.rightArm.armRot, model.config.arms.rightArm.armPos, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Bolt") {
                        RenderArms.renderArmBolt(model, anim, smoothing, model.config.arms.rightArm.armChargeRot, model.config.arms.rightArm.armChargePos, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Default") {
                        GL11.glTranslatef(leftArmOffset.x, leftArmOffset.y, leftArmOffset.z);
                        RenderArms.renderArmDefault(model, anim, smoothing, model.config.arms.rightArm.armRot, model.config.arms.rightArm.armPos, true, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Load") {
                        RenderArms.renderArmLoad(model, anim, weaponAnimation, smoothing, tiltProgress, model.config.arms.rightArm.armReloadRot, model.config.arms.rightArm.armReloadPos, model.config.arms.rightArm.armRot, model.config.arms.rightArm.armPos, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Reload") {
                        RenderArms.renderArmReload(model, anim, weaponAnimation, smoothing, tiltProgress, model.config.arms.rightArm.armReloadRot, model.config.arms.rightArm.armReloadPos, model.config.arms.rightArm.armRot, model.config.arms.rightArm.armPos, model.config.arms.leftHandAmmo);
                    }
                    GL11.glScalef(model.config.arms.rightArm.armScale.x, model.config.arms.rightArm.armScale.y, model.config.arms.rightArm.armScale.z);
                    renderplayer.getMainModel().setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
                    renderplayer.getMainModel().bipedRightArm.offsetX = 0F;
                    if(!MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Pre(this,EnumHandSide.RIGHT))) {
                        renderplayer.renderRightArm(Minecraft.getMinecraft().player);
                        MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Post(this,EnumHandSide.RIGHT));
                    }
                    renderRightSleeve(player, renderplayer.getMainModel());
                }
                GL11.glPopMatrix();
            }

            if (model.config.arms.leftHandAmmo && model.config.arms.leftArm.armPos != null && model.config.arms.leftArm.armReloadPos != null) {
                GL11.glPushMatrix();
                {
                    GL11.glTranslatef(leftArmOffset.x, leftArmOffset.y, leftArmOffset.z);

                    if (movingArmState == "Charge") {
                        RenderArms.renderArmCharge(model, anim, smoothing, model.config.arms.leftArm.armChargeRot, model.config.arms.leftArm.armChargePos, model.config.arms.leftArm.armRot, model.config.arms.leftArm.armPos, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Pump") {
                        RenderArms.renderArmPump(model, anim, smoothing, model.config.arms.leftArm.armRot, model.config.arms.leftArm.armPos, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Default") {
                        GL11.glTranslatef(leftArmOffset.x, leftArmOffset.y, leftArmOffset.z);
                        RenderArms.renderArmDefault(model, anim, smoothing, model.config.arms.leftArm.armRot, model.config.arms.leftArm.armPos, false, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Load") {
                        RenderArms.renderArmLoad(model, anim, weaponAnimation, smoothing, tiltProgress, model.config.arms.leftArm.armReloadRot, model.config.arms.leftArm.armReloadPos, model.config.arms.leftArm.armRot, model.config.arms.leftArm.armPos, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Unload") {
                        RenderArms.renderArmUnload(model, anim, weaponAnimation, smoothing, tiltProgress, model.config.arms.leftArm.armReloadRot, model.config.arms.leftArm.armReloadPos, model.config.arms.leftArm.armRot, model.config.arms.leftArm.armPos, model.config.arms.leftHandAmmo);
                    } else if (movingArmState == "Reload") {
                        RenderArms.renderArmReload(model, anim, weaponAnimation, smoothing, tiltProgress, model.config.arms.leftArm.armReloadRot, model.config.arms.leftArm.armReloadPos, model.config.arms.leftArm.armRot, model.config.arms.leftArm.armPos, model.config.arms.leftHandAmmo);
                    }

                    GL11.glScalef(model.config.arms.leftArm.armScale.x, model.config.arms.leftArm.armScale.y, model.config.arms.leftArm.armScale.z);
                    renderplayer.getMainModel().bipedLeftArm.offsetY = 0F;
                    if(!MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Pre(this,EnumHandSide.LEFT))) {
                        renderplayer.renderLeftArm(Minecraft.getMinecraft().player);
                        MinecraftForge.EVENT_BUS.post(new RenderHandFisrtPersonEvent.Post(this,EnumHandSide.LEFT));
                    }
                    renderLeftSleeve(player, renderplayer.getMainModel());
                }
                GL11.glPopMatrix();
            }
        }
        GL11.glPopMatrix();
    }


    public void renderLeftSleeve(EntityPlayer player, ModelBiped modelplayer) {
        if (!MinecraftForge.EVENT_BUS.post(new RenderHandSleeveEvent.Pre(this, EnumHandSide.LEFT, modelplayer))) {
            if (player.inventory.armorItemInSlot(2) != null) {
                ItemStack armorStack = player.inventory.armorItemInSlot(2);
                if (armorStack.getItem() instanceof ItemMWArmor) {
                    int skinId = 0;
                    String path = skinId > 0 ? ((ItemMWArmor) armorStack.getItem()).type.modelSkins[skinId].getSkin()
                            : ((ItemMWArmor) armorStack.getItem()).type.modelSkins[0].getSkin();

                    if (!((ItemMWArmor) armorStack.getItem()).type.simpleArmor) {
                        ModelCustomArmor modelArmor = ((ModelCustomArmor) ((ItemMWArmor) armorStack
                                .getItem()).type.bipedModel);

                        bindTexture("armor", path);
                        GL11.glPushMatrix();
                        {
                            float modelScale = modelArmor.config.extra.modelScale;
                            GL11.glScalef(modelScale, modelScale, modelScale);
                            modelArmor.showChest(true);
                            modelplayer.bipedLeftArm.rotateAngleX=0;
                            modelplayer.bipedLeftArm.rotateAngleY=0;
                            modelplayer.bipedLeftArm.rotateAngleZ=-0.1f;
                            modelArmor.renderLeftArm((AbstractClientPlayer) player, modelplayer);
                        }
                        GL11.glPopMatrix();
                    } else {
                        Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager()
                                .getEntityRenderObject(Minecraft.getMinecraft().player);
                        RenderPlayer renderplayer = (RenderPlayer) render;
                        GlStateManager.scale(1.00000001F, 1.00000001F, 1.00000001F);
                        bindTexture("armor", path);
                        renderplayer.renderLeftArm(Minecraft.getMinecraft().player);
                    }
                }
            }
            MinecraftForge.EVENT_BUS.post(new RenderHandSleeveEvent.Post(this, EnumHandSide.LEFT, modelplayer));
        }
    }

    public void renderRightSleeve(EntityPlayer player, ModelBiped modelplayer) {
        if (!MinecraftForge.EVENT_BUS.post(new RenderHandSleeveEvent.Pre(this, EnumHandSide.RIGHT, modelplayer))) {
            if (player.inventory.armorItemInSlot(2) != null) {
                ItemStack armorStack = player.inventory.armorItemInSlot(2);
                if (armorStack.getItem() instanceof ItemMWArmor) {
                    int skinId = 0;
                    String path = skinId > 0 ? ((ItemMWArmor) armorStack.getItem()).type.modelSkins[skinId].getSkin()
                            : ((ItemMWArmor) armorStack.getItem()).type.modelSkins[0].getSkin();
                    if (!((ItemMWArmor) armorStack.getItem()).type.simpleArmor) {
                        ModelCustomArmor modelArmor = ((ModelCustomArmor) ((ItemMWArmor) armorStack
                                .getItem()).type.bipedModel);

                        bindTexture("armor", path);
                        GL11.glPushMatrix();
                        {
                            float modelScale = modelArmor.config.extra.modelScale;
                            GL11.glScalef(modelScale, modelScale, modelScale);
                            modelArmor.showChest(true);
                            modelplayer.bipedRightArm.rotateAngleX=0;
                            modelplayer.bipedRightArm.rotateAngleY=0;
                            modelplayer.bipedRightArm.rotateAngleZ=0.1f;
                            modelArmor.renderRightArm((AbstractClientPlayer) player, modelplayer);
                        }
                        GL11.glPopMatrix();
                    } else {
                        Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager()
                                .getEntityRenderObject(Minecraft.getMinecraft().player);
                        RenderPlayer renderplayer = (RenderPlayer) render;
                        GlStateManager.scale(1.00000001F, 1.00000001F, 1.00000001F);
                        bindTexture("armor", path);
                        renderplayer.renderRightArm(Minecraft.getMinecraft().player);
                    }
                }
            }
            MinecraftForge.EVENT_BUS.post(new RenderHandSleeveEvent.Post(this, EnumHandSide.RIGHT, modelplayer));
        }
    }


    @SideOnly(Side.CLIENT)
    public void renderScopeGlass(AttachmentType attachmentType, ModelAttachment modelAttachment, boolean isAiming) {
        if(ScopeUtils.isIndsideGunRendering) {
            return;
        }

        if (Minecraft.getMinecraft().world != null) {
            float gunRotX = RenderParameters.GUN_ROT_X_LAST + (RenderParameters.GUN_ROT_X - RenderParameters.GUN_ROT_X_LAST) * this.timer.renderPartialTicks;
            if (isAiming&&(ClientProxy.scopeUtils.blurFramebuffer!=null||!ModConfig.INSTANCE.hud.ads_blur)) {
                if(OptifineHelper.isShadersEnabled()) {
                    Shaders.pushProgram();  
                }
                Minecraft mc=Minecraft.getMinecraft();
                
                GL20.glUseProgram(Programs.overlayProgram);
                GL20.glUniform2f(GL20.glGetUniformLocation(Programs.overlayProgram, "size"), mc.displayWidth,mc.displayHeight);
                
                GL11.glPushMatrix();
                
                int tex=ClientProxy.scopeUtils.blurFramebuffer.framebufferTexture;
                ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);
                GL30.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, ScopeUtils.OVERLAY_TEX, 0);
                GlStateManager.clearColor(0, 0, 0, 0);
                GL11.glClearColor(0, 0, 0, 0);
                GlStateManager.colorMask(true, true, true, true);
                GlStateManager.depthMask(true);
                GlStateManager.clear (GL11.GL_DEPTH_BUFFER_BIT);
                copyDepthBuffer();
                ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);
                GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
                
                /** Render Overlay when moving too fast **/
                float alpha = 1 - adsSwitch;
                
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ZERO);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                modelAttachment.renderOverlaySolid(0.0625f);
                
                GL20.glUseProgram(0);
                if(OptifineHelper.isShadersEnabled()) {
                    Shaders.popProgram();  
                }
                
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);
                if(attachmentType.sight.usedDefaultOverlayModelTexture) {
                    renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/black.png"));  
                }
                modelAttachment.renderOverlay(0.0625f);
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                
                GL30.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, tex, 0);
                GlStateManager.clear (GL11.GL_DEPTH_BUFFER_BIT);
                copyDepthBuffer();
                ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);
                GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                
                GlStateManager.disableBlend();
                renderWorldOntoScope(attachmentType, modelAttachment);
                GlStateManager.enableBlend();
                
                OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, OptifineHelper.getDrawFrameBuffer());  
                
                GL11.glPopMatrix();
                
            } else {
                GL11.glPushMatrix();
                renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/black.png"));
                modelAttachment.renderOverlay(0.0625f);
                GL11.glPopMatrix();
            }
        }
        
    }
    
    public void copyDepthBuffer() {
        Minecraft mc=Minecraft.getMinecraft();
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, OptifineHelper.getDrawFrameBuffer());
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, ClientProxy.scopeUtils.blurFramebuffer.framebufferObject);
        GlStateManager.colorMask(false,false,false,false);
        GL30.glBlitFramebuffer(0, 0, mc.displayWidth, mc.displayHeight, 0, 0, mc.displayWidth, mc.displayHeight, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
        GlStateManager.colorMask(true,true,true,true);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, GL11.GL_NONE);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, GL11.GL_NONE);
    }


    @SideOnly(Side.CLIENT)
    private void renderWorldOntoScope(AttachmentType type, ModelAttachment modelAttachment) {
        GL11.glPushMatrix();

        if (isLightOn) {
            renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/black.png"));
            GL11.glDisable(2896);
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
            ModelGun.glowOn(1);
            modelAttachment.renderScope(0.0625f);
            ModelGun.glowOff();
            GL11.glEnable(2896);
            Minecraft.getMinecraft().entityRenderer.enableLightmap();
        } else {
            renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/black.png"));
            ModelGun.glowOn(1);
            modelAttachment.renderScope(0.0625f);
            ModelGun.glowOff();
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && mc.gameSettings.thirdPersonView == 0) {
            if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                final ItemStack gunStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                if (GunType.getAttachment(gunStack, AttachmentPresetEnum.Flashlight) != null) {
                    if (isLightOn) {
                        GL11.glDisable(2896);
                        Minecraft.getMinecraft().entityRenderer.disableLightmap();
                        GL11.glDisable(3042);
                        GL11.glPushMatrix();
                        GL11.glPushAttrib(16384);
                        GL11.glEnable(3042);
                        GL11.glDepthMask(false);
                        GL11.glBlendFunc(774, 770);

                        renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "textures/gui/light.png"));
                        modelAttachment.renderOverlay(0.0625f);

                        GL11.glBlendFunc(770, 771);
                        GL11.glDepthMask(true);
                        GL11.glDisable(3042);
                        GL11.glPopAttrib();
                        GL11.glPopMatrix();
                        GL11.glEnable(2896);
                        Minecraft.getMinecraft().entityRenderer.enableLightmap();
                    }
                }
            }
        }
        GL11.glPopMatrix();
    }
}