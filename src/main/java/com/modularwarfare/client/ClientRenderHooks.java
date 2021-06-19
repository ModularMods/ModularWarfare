package com.modularwarfare.client;

import com.modularwarfare.api.AnimationUtils;
import com.modularwarfare.api.RenderBonesEvent;
import com.modularwarfare.client.anim.AnimStateMachine;
import com.modularwarfare.client.model.ModelCustomArmor.Bones.BonePart.EnumBoneType;
import com.modularwarfare.client.model.objects.CustomItemRenderType;
import com.modularwarfare.client.model.objects.CustomItemRenderer;
import com.modularwarfare.client.model.renders.*;
import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.guns.ItemAttachment;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.network.BackWeaponsManager;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.utility.OptifineHelper;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.util.glu.Project;

import java.util.HashMap;

public class ClientRenderHooks extends ForgeEvent {

    public static HashMap<EntityLivingBase, AnimStateMachine> weaponAnimations = new HashMap<EntityLivingBase, AnimStateMachine>();
    public static CustomItemRenderer[] customRenderers = new CustomItemRenderer[8];
    public static boolean isAimingScope;
    public static boolean isAiming;
    public float partialTicks;
    private Minecraft mc;
    private float equippedProgress = 1f, prevEquippedProgress = 1f;

    public ClientRenderHooks() {
        mc = Minecraft.getMinecraft();
        customRenderers[0] = ClientProxy.gunStaticRenderer = new RenderGunStatic();
        customRenderers[1] = ClientProxy.ammoRenderer = new RenderAmmo();
        customRenderers[2] = ClientProxy.attachmentRenderer = new RenderAttachment();
        customRenderers[7] = ClientProxy.grenadeRenderer = new RenderGrenade();
    }

    public static AnimStateMachine getAnimMachine(EntityPlayer entityPlayer) {
        AnimStateMachine animation = null;
        if (weaponAnimations.containsKey(entityPlayer)) {
            animation = weaponAnimations.get(entityPlayer);
        } else {
            animation = new AnimStateMachine();
            weaponAnimations.put(entityPlayer, animation);
        }
        return animation;
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        switch (event.phase) {
            case START: {
                RenderParameters.smoothing = event.renderTickTime;
                SetPartialTick(event.renderTickTime);
                break;
            }
            case END: {
                if (mc.player == null || mc.world == null)
                    return;
                if (ClientProxy.gunUI.hitMarkerTime > 0)
                    ClientProxy.gunUI.hitMarkerTime--;
                break;
            }
        }
    }

    @SubscribeEvent
    public void renderItemFrame(RenderItemInFrameEvent event) {
        Item item = event.getItem().getItem();
        if (item instanceof ItemGun) {
            BaseType type = ((BaseItem) event.getItem().getItem()).baseType;
            if (type.hasModel()) {
                event.setCanceled(true);

                int rotation = event.getEntityItemFrame().getRotation();
                GlStateManager.rotate(-rotation * 45F, 0F, 0F, 1F);
                RenderHelper.enableStandardItemLighting();
                GlStateManager.rotate(rotation * 45F, 0F, 0F, 1F);
                GlStateManager.pushMatrix();
                float scale = 0.75F;
                GlStateManager.scale(scale, scale, scale);
                GlStateManager.translate(0.15F, -0.15F, 0F);
                customRenderers[type.id].renderItem(CustomItemRenderType.ENTITY, EnumHand.MAIN_HAND, event.getItem());
                GlStateManager.popMatrix();
            }
        }
    }

    @SubscribeEvent
    public void renderHeldItem(RenderSpecificHandEvent event) {
        EntityPlayer player = mc.player;
        ItemStack stack = event.getItemStack();

        if (stack != null && stack.getItem() instanceof BaseItem) {
            BaseType type = ((BaseItem) stack.getItem()).baseType;
            BaseItem item = ((BaseItem) stack.getItem());

            if (event.getHand() != EnumHand.MAIN_HAND) {
                event.setCanceled(true);
                return;
            }

            if (type.id > customRenderers.length)
                return;

            if (item.render3d && customRenderers[type.id] != null && type.hasModel() && !type.getAssetDir().equalsIgnoreCase("attachments")) {
                //Cancel the hand render event so that we can do our own.
                event.setCanceled(true);

                float partialTicks = event.getPartialTicks();
                EntityRenderer renderer = mc.entityRenderer;
                float farPlaneDistance = mc.gameSettings.renderDistanceChunks * 16F;
                ItemRenderer itemRenderer = mc.getItemRenderer();

                if (OptifineHelper.isLoaded()) {
                    if (!OptifineHelper.isShadersEnabled()) {
                        GlStateManager.clear(256);
                        GlStateManager.matrixMode(5889);
                        GlStateManager.loadIdentity();
                    }
                } else {
                    GlStateManager.clear(256);
                    GlStateManager.matrixMode(5889);
                    GlStateManager.loadIdentity();
                }

                Project.gluPerspective(getFOVModifier(partialTicks), (float) mc.displayWidth / (float) mc.displayHeight, 0.0001F, farPlaneDistance * 2.0F);
                GlStateManager.matrixMode(5888);
                GlStateManager.loadIdentity();

                GlStateManager.pushMatrix();

                boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

                if (mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator()) {
                    renderer.enableLightmap();
                    float f1 = 1.0F - (prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks);
                    EntityPlayerSP entityplayersp = this.mc.player;
                    float f2 = entityplayersp.getSwingProgress(partialTicks);
                    float f3 = entityplayersp.prevRotationPitch + (entityplayersp.rotationPitch - entityplayersp.prevRotationPitch) * partialTicks;
                    float f4 = entityplayersp.prevRotationYaw + (entityplayersp.rotationYaw - entityplayersp.prevRotationYaw) * partialTicks;

                    //Setup lighting
                    GlStateManager.disableLighting();
                    GlStateManager.pushMatrix();
                    GlStateManager.rotate(f3, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(f4, 0.0F, 1.0F, 0.0F);
                    RenderHelper.enableStandardItemLighting();
                    GlStateManager.popMatrix();

                    //Do lighting
                    int i = this.mc.world.getCombinedLight(new BlockPos(entityplayersp.posX, entityplayersp.posY + (double) entityplayersp.getEyeHeight(), entityplayersp.posZ), 0);
                    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) (i & 65535), (float) (i >> 16));


                    //Do hand rotations
                    float f5 = entityplayersp.prevRenderArmPitch + (entityplayersp.renderArmPitch - entityplayersp.prevRenderArmPitch) * partialTicks;
                    float f6 = entityplayersp.prevRenderArmYaw + (entityplayersp.renderArmYaw - entityplayersp.prevRenderArmYaw) * partialTicks;
                    GlStateManager.rotate((entityplayersp.rotationPitch - f5) * 0.1F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate((entityplayersp.rotationYaw - f6) * 0.1F, 0.0F, 1.0F, 0.0F);

                    GlStateManager.enableRescaleNormal();
                    GlStateManager.pushMatrix();

                    //Do vanilla weapon swing
                    float f7 = -0.4F * MathHelper.sin(MathHelper.sqrt(f2) * (float) Math.PI);
                    float f8 = 0.2F * MathHelper.sin(MathHelper.sqrt(f2) * (float) Math.PI * 2.0F);
                    float f9 = -0.2F * MathHelper.sin(f2 * (float) Math.PI);
                    GlStateManager.translate(f7, f8, f9);

                    GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
                    GlStateManager.translate(0.0F, f1 * -0.6F, 0.0F);
                    GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                    float f10 = MathHelper.sin(f2 * f2 * (float) Math.PI);
                    float f11 = MathHelper.sin(MathHelper.sqrt(f2) * (float) Math.PI);
                    GlStateManager.rotate(f10 * -20.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(f11 * -20.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.rotate(f11 * -80.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.scale(0.4F, 0.4F, 0.4F);

                    customRenderers[type.id].renderItem(CustomItemRenderType.EQUIPPED_FIRST_PERSON, event.getHand(), stack, mc.world, mc.player);

                    GlStateManager.popMatrix();
                    GlStateManager.disableRescaleNormal();
                    RenderHelper.disableStandardItemLighting();
                    renderer.disableLightmap();
                }

                GlStateManager.popMatrix();

                if (mc.gameSettings.thirdPersonView == 0 && !flag) {
                    itemRenderer.renderOverlays(partialTicks);
                }
            }
        }
    }

    public void SetPartialTick(float dT) {
        partialTicks = dT;
    }

    @SubscribeEvent
    public void renderThirdPose(RenderLivingEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer)) {
            return;
        }

        ItemStack itemstack = event.getEntity().getHeldItemMainhand();
        if (itemstack != ItemStack.EMPTY && !itemstack.isEmpty()) {
            if (!(itemstack.getItem() instanceof BaseItem)) {
                return;
            }
            BaseType type = ((BaseItem) itemstack.getItem()).baseType;
            if (!type.hasModel()) {
                return;
            }
            if (itemstack.getItem() instanceof ItemAttachment) {
                return;
            }
            if (itemstack.getItem() instanceof ItemBackpack) {
                return;
            }

            ModelBiped biped = (ModelBiped) event.getRenderer().getMainModel();
            Entity entity = event.getEntity();
            if (type.id == 0 && entity instanceof EntityPlayer) {
                if (AnimationUtils.isAiming.containsKey(((EntityPlayer) entity).getName())) {
                    biped.rightArmPose = ArmPose.BOW_AND_ARROW;
                } else {
                    biped.rightArmPose = ArmPose.BLOCK;
                    biped.leftArmPose = ArmPose.BLOCK;
                }
            } else {
                biped.rightArmPose = ArmPose.BLOCK;
            }
        }
    }

    @SubscribeEvent
    public void renderBackGun(RenderBonesEvent.Post event) {
        if (event.type != EnumBoneType.BODY) {
            return;
        }
        if (event.modelCustomArmor.renderingEntity instanceof AbstractClientPlayer) {
            ItemStack gun = BackWeaponsManager.INSTANCE
                    .getItemToRender((AbstractClientPlayer) event.modelCustomArmor.renderingEntity);
            if (gun != ItemStack.EMPTY && !gun.isEmpty()) {
                BaseType type = ((BaseItem) gun.getItem()).baseType;
                {
                    GlStateManager.pushMatrix();
                    if (customRenderers[type.id] != null) {
                        GlStateManager.translate(0, -0.6, 0.35);
                        boolean isSneaking = event.modelCustomArmor.renderingEntity.isSneaking();
                        if (event.modelCustomArmor.renderingEntity instanceof EntityPlayerSP) {
                            ((EntityPlayerSP) event.modelCustomArmor.renderingEntity).movementInput.sneak = false;
                        } else {
                            event.modelCustomArmor.renderingEntity.setSneaking(false);
                        }
                        customRenderers[type.id].renderItem(CustomItemRenderType.BACK, null, gun, mc.world,
                                event.modelCustomArmor.renderingEntity, partialTicks);
                        if (event.modelCustomArmor.renderingEntity instanceof EntityPlayerSP) {
                            ((EntityPlayerSP) event.modelCustomArmor.renderingEntity).movementInput.sneak = isSneaking;
                        } else {
                            event.modelCustomArmor.renderingEntity.setSneaking(isSneaking);
                        }
                    }
                    GlStateManager.popMatrix();
                }
            }

        }
    }

    private float getFOVModifier(float partialTicks) {
        Entity entity = this.mc.getRenderViewEntity();
        float f1 = 70.0F;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHealth() <= 0.0F) {
            float f2 = (float) ((EntityLivingBase) entity).deathTime + partialTicks;
            f1 /= (1.0F - 500.0F / (f2 + 500.0F)) * 2.0F + 1.0F;
        }

        IBlockState state = ActiveRenderInfo.getBlockStateAtEntityViewpoint(this.mc.world, entity, partialTicks);

        if (state.getMaterial() == Material.WATER)
            f1 = f1 * 60.0F / 70.0F;

        return f1;
    }

    private float interpolateRotation(float x, float y, float dT) {
        float f3;

        for (f3 = y - x; f3 < -180.0F; f3 += 360.0F) {
        }
        for (; f3 >= 180.0F; f3 -= 360.0F) {
        }

        return x + dT * f3;
    }

}