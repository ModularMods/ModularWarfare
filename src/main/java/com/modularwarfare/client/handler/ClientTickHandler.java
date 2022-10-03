package com.modularwarfare.client.handler;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.OnTickRenderEvent;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.fpp.basic.animations.StateEntry;
import com.modularwarfare.client.fpp.enhanced.animation.EnhancedStateMachine;
import com.modularwarfare.client.hud.FlashSystem;
import com.modularwarfare.client.model.InstantBulletRenderer;
import com.modularwarfare.client.model.ModelGun;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.common.grenades.ItemGrenade;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.ItemSpray;
import com.modularwarfare.common.guns.WeaponAnimationType;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.utility.MWSound;
import com.modularwarfare.utility.RayUtil;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.*;

public class ClientTickHandler extends ForgeEvent {

    public static ConcurrentHashMap<UUID, Integer> playerShootCooldown = new ConcurrentHashMap<UUID, Integer>();
    public static ConcurrentHashMap<UUID, Integer> playerReloadCooldown = new ConcurrentHashMap<UUID, Integer>();
    public static ItemStack reloadEnhancedPrognosisAmmo = ItemStack.EMPTY;
    public static ItemStack reloadEnhancedPrognosisAmmoRendering = ItemStack.EMPTY;
    public static boolean reloadEnhancedIsQuickly=false;
    public static boolean reloadEnhancedIsQuicklyRendering=false;

    public static int oldCurrentItem;
    public static ItemStack oldItemStack = ItemStack.EMPTY;
    public static ItemStack lastItemStack = ItemStack.EMPTY;
    int i = 0;

    public ClientTickHandler() {
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                onClientTickStart(Minecraft.getMinecraft());
                ModularWarfare.NETWORK.handleClientPackets();

                // Player shoot cooldown
                for (UUID uuid : playerShootCooldown.keySet()) {
                    i += 1;
                    int value = playerShootCooldown.get(uuid) - 1;
                    if (value <= 0) {
                        playerShootCooldown.remove(uuid);
                    } else {
                        playerShootCooldown.replace(uuid, value);
                    }
                }

                // Player reload cooldown
                for (UUID uuid : playerReloadCooldown.keySet()) {
                    i += 1;
                    int value = playerReloadCooldown.get(uuid) - 1;
                    if (value <= 0) {
                        playerReloadCooldown.remove(uuid);
                    } else {
                        playerReloadCooldown.replace(uuid, value);
                    }
                }
                break;
            case END:
                onClientTickEnd(Minecraft.getMinecraft());

        }
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        switch (event.phase) {
            case START: {
                float renderTick = event.renderTickTime;
                renderTick *= 60d / (double) Minecraft.getDebugFPS();
                StateEntry.smoothing = renderTick;
                onRenderTickStart(Minecraft.getMinecraft(), renderTick);
                break;
            }
        }
    }

    public void onRenderTickStart(Minecraft minecraft, float renderTick) {
        if (minecraft.player == null || minecraft.world == null)
            return;

        EntityPlayerSP player = minecraft.player;
        
        reloadEnhancedPrognosisAmmoRendering=reloadEnhancedPrognosisAmmo;
        reloadEnhancedIsQuicklyRendering=reloadEnhancedIsQuickly;

        OnTickRenderEvent event = new OnTickRenderEvent(renderTick);
        MinecraftForge.EVENT_BUS.post(event);

        /**
         *EnhancedGunRendered update currentItem 
         */
        if (ClientProxy.gunEnhancedRenderer.controller != null) {
            ClientProxy.gunEnhancedRenderer.controller.updateCurrentItem();
        }
        
        for (EnhancedStateMachine stateMachine : ClientRenderHooks.weaponEnhancedAnimations.values()) {
            stateMachine.updateCurrentItem();
        }

        if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemGun) {
            float adsSpeed = 0F;
            if(((ItemGun) player.getHeldItemMainhand().getItem()).type.animationType.equals(WeaponAnimationType.BASIC)){
                ModelGun model = (ModelGun) ((ItemGun) player.getHeldItemMainhand().getItem()).type.model;
                if (!RenderParameters.lastModel.equalsIgnoreCase(model.getClass().getName())) {
                    RenderParameters.resetRenderMods();
                    RenderParameters.lastModel = model.getClass().getName();
                    adsSpeed = model.config.extra.adsSpeed;
                }
                AnimStateMachine anim = ClientRenderHooks.getAnimMachine(player);

                float adsSpeedFinal = (0.10f + adsSpeed) * renderTick;
                boolean aimChargeMisc = !anim.reloading;
                float value = (Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(1) && aimChargeMisc && !ClientRenderHooks.getAnimMachine(player).attachmentMode) ? RenderParameters.adsSwitch + adsSpeedFinal : RenderParameters.adsSwitch - adsSpeedFinal;
                RenderParameters.adsSwitch = Math.max(0, Math.min(1, value));
                

                float sprintSpeed = 0.15f * renderTick;
                float sprintValue = (player.isSprinting() && !ClientRenderHooks.getAnimMachine(player).attachmentMode) ? RenderParameters.sprintSwitch + sprintSpeed : RenderParameters.sprintSwitch - sprintSpeed;
                RenderParameters.sprintSwitch = Math.max(0, Math.min(1, sprintValue));
                

                float attachmentSpeed = 0.15f * renderTick;
                float attachmentValue = ClientRenderHooks.getAnimMachine(player).attachmentMode ? RenderParameters.attachmentSwitch + attachmentSpeed : RenderParameters.attachmentSwitch - attachmentSpeed;
                RenderParameters.attachmentSwitch = Math.max(0, Math.min(1, attachmentValue));
                

                float crouchSpeed = 0.15f * renderTick;
                float crouchValue = player.isSneaking() ? RenderParameters.crouchSwitch + crouchSpeed : RenderParameters.crouchSwitch - crouchSpeed;
                RenderParameters.crouchSwitch = Math.max(0, Math.min(1, crouchValue));
                

                float reloadSpeed = 0.15f * renderTick;
                float reloadValue = anim.reloading ? RenderParameters.reloadSwitch - reloadSpeed : RenderParameters.reloadSwitch + reloadSpeed;
                RenderParameters.reloadSwitch = Math.max(0, Math.min(1, reloadValue));
                

                float triggerPullSpeed = 0.03f * renderTick;
                float triggerPullValue = Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(0) && !ClientRenderHooks.getAnimMachine(player).attachmentMode ? RenderParameters.triggerPullSwitch + triggerPullSpeed : RenderParameters.triggerPullSwitch - triggerPullSpeed;
                RenderParameters.triggerPullSwitch = Math.max(0, Math.min(0.02f, triggerPullValue));

                float modeSwitchSpeed = 0.03f * renderTick;
                float modeSwitchValue = Minecraft.getMinecraft().inGameHasFocus && Mouse.isButtonDown(0) ? RenderParameters.triggerPullSwitch + triggerPullSpeed : RenderParameters.triggerPullSwitch - triggerPullSpeed;
                RenderParameters.triggerPullSwitch = Math.max(0, Math.min(0.02f, triggerPullValue));
            }else {
                if (ClientProxy.gunEnhancedRenderer.controller != null) {
                    RenderParameters.adsSwitch = (float) ClientProxy.gunEnhancedRenderer.controller.ADS;
                }
            }

            float balancing_speed_x = 0.08f * renderTick;
            if(player.moveStrafing > 0){
                RenderParameters.GUN_BALANCING_X = Math.min(1.0F, RenderParameters.GUN_BALANCING_X + balancing_speed_x);
            } else if(player.moveStrafing < 0){
                RenderParameters.GUN_BALANCING_X = Math.max(-1.0F, RenderParameters.GUN_BALANCING_X - balancing_speed_x);
            } else if(player.moveStrafing == 0 && RenderParameters.GUN_BALANCING_X != 0F){
                if(RenderParameters.GUN_BALANCING_X > 0F){
                    RenderParameters.GUN_BALANCING_X = Math.max(0, RenderParameters.GUN_BALANCING_X - balancing_speed_x);
                } else if(RenderParameters.GUN_BALANCING_X < 0F){
                    RenderParameters.GUN_BALANCING_X = Math.min(0, RenderParameters.GUN_BALANCING_X + balancing_speed_x);
                }
            }

            float balancing_speed_y = 0.08f * renderTick;
            if(player.moveForward > 0){
                RenderParameters.GUN_BALANCING_Y = Math.min((player.isSprinting() ? 3.0F : 1.0F), RenderParameters.GUN_BALANCING_Y + balancing_speed_y);
            } else if(player.moveForward < 0){
                RenderParameters.GUN_BALANCING_Y = Math.max(-1.0F, RenderParameters.GUN_BALANCING_Y - balancing_speed_y);
            } else if(player.moveForward == 0 && RenderParameters.GUN_BALANCING_Y != 0F){
                if(RenderParameters.GUN_BALANCING_Y > 0F){
                    RenderParameters.GUN_BALANCING_Y = Math.max(0, RenderParameters.GUN_BALANCING_Y - balancing_speed_y*2);
                } else if(RenderParameters.GUN_BALANCING_Y < 0F){
                    RenderParameters.GUN_BALANCING_Y = Math.min(0, RenderParameters.GUN_BALANCING_Y + balancing_speed_y*2);
                }
            }


            //Gun change animation
            if(player.inventory.currentItem != oldCurrentItem){
                if(oldItemStack.isEmpty()|| !(oldItemStack.getItem() instanceof ItemGun)) {
                    GUN_CHANGE_Y=0.5f;
                }
                if(RenderParameters.GUN_CHANGE_Y<=0.5) {
                    if(!oldItemStack.isEmpty() && oldItemStack.getItem() instanceof ItemGun) {
                        lastItemStack = oldItemStack;
                    }
                    RenderParameters.GUN_CHANGE_Y = 1.0f-RenderParameters.GUN_CHANGE_Y;
                }
            }
            if(GUN_CHANGE_Y<0.5) {
                lastItemStack = ItemStack.EMPTY;
            }
            float change_speed_y = 0.04f * renderTick;
            RenderParameters.GUN_CHANGE_Y = Math.max(0, RenderParameters.GUN_CHANGE_Y - change_speed_y);

            Vec3d vecStart = player.getPositionEyes(1.0f);
            RayTraceResult rayTraceResult = RayUtil.rayTrace(player,1.0, 1.0f);
            if(rayTraceResult != null) {
                if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    if (rayTraceResult.hitVec != null) {
                        double d = vecStart.distanceTo(rayTraceResult.hitVec);
                        if (d <= 1.0f) {
                            RenderParameters.collideFrontDistance = (float) (RenderParameters.collideFrontDistance + ((1.0f - d) - RenderParameters.collideFrontDistance) * renderTick * 0.5f);
                        } else {
                            RenderParameters.collideFrontDistance = Math.max(0f, RenderParameters.collideFrontDistance - renderTick * 0.1f);
                        }
                    } else {
                        RenderParameters.collideFrontDistance = Math.max(0f, RenderParameters.collideFrontDistance - renderTick * 0.1f);
                    }
                } else {
                    RenderParameters.collideFrontDistance = Math.max(0f, RenderParameters.collideFrontDistance - renderTick * 0.1f);
                }
            } else {
                RenderParameters.collideFrontDistance = Math.max(0f, RenderParameters.collideFrontDistance - renderTick * 0.1f);
            }

            /**
             * AnimStatesMachines Updates
             */
            for (AnimStateMachine stateMachine : ClientRenderHooks.weaponBasicAnimations.values()) {
                stateMachine.onRenderTickUpdate();
            }
            for (EnhancedStateMachine stateMachine : ClientRenderHooks.weaponEnhancedAnimations.values()) {
                stateMachine.onRenderTickUpdate(renderTick);
            }
            /**
             * EnhancedGunRendered Updates
             */
            if (ClientProxy.gunEnhancedRenderer.controller != null) {
                if(((ItemGun) player.getHeldItemMainhand().getItem()).type.animationType.equals(WeaponAnimationType.ENHANCED)) {
                    ClientProxy.gunEnhancedRenderer.controller.onTickRender(renderTick);
                }
            }
        } else {
            RenderParameters.resetRenderMods();
        }
    }


    public void onClientTickStart(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.world == null)
            return;

        ModularWarfare.PLAYERHANDLER.clientTick();

        GUN_ROT_X_LAST = GUN_ROT_X;
        GUN_ROT_Y_LAST = GUN_ROT_Y;
        GUN_ROT_Z_LAST = GUN_ROT_Z;

        Minecraft mc = FMLClientHandler.instance().getClient();

        if (mc.getRenderViewEntity() != null) {
            if (mc.getRenderViewEntity().getRotationYawHead() > mc.getRenderViewEntity().prevRotationYaw) {
                GUN_ROT_X += (mc.getRenderViewEntity().getRotationYawHead() - mc.getRenderViewEntity().prevRotationYaw) / 1.5;
            } else if (mc.getRenderViewEntity().getRotationYawHead() < mc.getRenderViewEntity().prevRotationYaw) {
                GUN_ROT_X -= (mc.getRenderViewEntity().prevRotationYaw - mc.getRenderViewEntity().getRotationYawHead()) / 1.5;
            }
            if (mc.getRenderViewEntity().rotationPitch > prevPitch) {
                GUN_ROT_Y += (mc.getRenderViewEntity().rotationPitch - prevPitch) / 5;
            } else if (mc.getRenderViewEntity().rotationPitch < prevPitch) {
                GUN_ROT_Y -= (prevPitch - mc.getRenderViewEntity().rotationPitch) / 5;
            }
            prevPitch = mc.getRenderViewEntity().rotationPitch;
        }

        GUN_ROT_X *= .2F;
        GUN_ROT_Y *= .2F;
        GUN_ROT_Z *= .2F;

        if (GUN_ROT_X > 20) {
            GUN_ROT_X = 20;
        } else if (GUN_ROT_X < -20) {
            GUN_ROT_X = -20;
        }

        if (GUN_ROT_Y > 20) {
            GUN_ROT_Y = 20;
        } else if (GUN_ROT_Y < -20) {
            GUN_ROT_Y = -20;
        }

        this.processGunChange();
        ItemGun.fireButtonHeld = Mouse.isButtonDown(0);

        if (ClientProxy.gunUI.bulletSnapFade > 0) {
            ClientProxy.gunUI.bulletSnapFade -= 0.01F;
        }
        //Client Flash Grenade
        if (FlashSystem.flashValue > 0) {
            FlashSystem.flashValue -= 2;
        } else if (FlashSystem.flashValue < 0) {
            FlashSystem.flashValue = 0;
        }
    }

    public void onClientTickEnd(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.world == null)
            return;

        EntityPlayerSP player = minecraft.player;

        if (playerRecoilPitch > 0)
            playerRecoilPitch *= 0.8F;

        if (playerRecoilYaw > 0)
            playerRecoilYaw *= 0.8F;

        player.rotationPitch -= playerRecoilPitch;
        player.rotationYaw -= playerRecoilYaw;
        antiRecoilPitch += playerRecoilPitch;
        antiRecoilYaw += playerRecoilYaw;

        player.rotationPitch += antiRecoilPitch * 0.25F;
        player.rotationYaw += antiRecoilYaw * 0.25F;
        antiRecoilPitch *= 0.75F;
        antiRecoilYaw *= 0.75F;

        if(!ItemGun.fireButtonHeld)
        RenderParameters.rate = Math.max(RenderParameters.rate - 0.05f , 0f);

        for (AnimStateMachine stateMachine : ClientRenderHooks.weaponBasicAnimations.values()) {
            stateMachine.onTickUpdate();
        }

        for (EnhancedStateMachine stateMachine : ClientRenderHooks.weaponEnhancedAnimations.values()) {
            stateMachine.onTickUpdate();
        }

        InstantBulletRenderer.UpdateAllTrails();
    }


    public void processGunChange() {
        final EntityPlayer player = Minecraft.getMinecraft().player;
        if (player.inventory.currentItem != this.oldCurrentItem) {
            if (player.getHeldItemMainhand().getItem() instanceof ItemGun) {
                GunType type=((ItemGun)player.getHeldItemMainhand().getItem()).type;
                type.playClientSound(player, WeaponSoundType.Equip);
            } else if (player.getHeldItemMainhand().getItem() instanceof ItemSpray) {
                ModularWarfare.PROXY.playSound(new MWSound(player.getPosition(), "shake", 1f, 1f));
            } else if (player.getHeldItemMainhand().getItem() instanceof ItemGrenade) {
                ModularWarfare.PROXY.playSound(new MWSound(player.getPosition(), "human.equip.extra", 1f, 1f));
            }
        }
        if (this.oldCurrentItem != player.inventory.currentItem) {
            this.oldCurrentItem = player.inventory.currentItem;
            this.oldItemStack = player.getHeldItemMainhand();
        }
    }
}
