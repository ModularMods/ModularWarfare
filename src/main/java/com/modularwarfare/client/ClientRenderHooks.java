package com.modularwarfare.client;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.AnimationUtils;
import com.modularwarfare.api.RenderHandFisrtPersonEvent;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.fpp.basic.configs.ArmorRenderConfig;
import com.modularwarfare.client.fpp.basic.renderers.*;
import com.modularwarfare.client.fpp.enhanced.animation.EnhancedStateMachine;
import com.modularwarfare.client.fpp.enhanced.renderers.RenderGunEnhanced;
import com.modularwarfare.client.handler.ClientTickHandler;
import com.modularwarfare.client.scope.ScopeUtils;
import com.modularwarfare.client.model.ModelCustomArmor;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemMWArmor;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.entity.grenades.EntitySmokeGrenade;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.utility.OptifineHelper;
import com.modularwarfare.utility.RenderHelperMW;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.Project;

import java.util.HashMap;

public class ClientRenderHooks extends ForgeEvent {

    public static HashMap<EntityLivingBase, AnimStateMachine> weaponBasicAnimations = new HashMap<EntityLivingBase, AnimStateMachine>();
    public static HashMap<EntityLivingBase, EnhancedStateMachine> weaponEnhancedAnimations = new HashMap<EntityLivingBase, EnhancedStateMachine>();

    public static CustomItemRenderer[] customRenderers = new CustomItemRenderer[20];
    public static boolean isAimingScope;
    public static boolean isAiming;
    public float partialTicks;
    private Minecraft mc;
    private float equippedProgress = 1f, prevEquippedProgress = 1f;
    public static boolean debug=false;

    public static final ResourceLocation grenade_smoke = new ResourceLocation("modularwarfare", "textures/particles/smoke.png");


    public ClientRenderHooks() {
        mc = Minecraft.getMinecraft();
        customRenderers[0] = ClientProxy.gunEnhancedRenderer = new RenderGunEnhanced();
        customRenderers[1] = ClientProxy.gunStaticRenderer = new RenderGunStatic();
        customRenderers[2] = ClientProxy.ammoRenderer = new RenderAmmo();
        customRenderers[3] = ClientProxy.attachmentRenderer = new RenderAttachment();
        customRenderers[8] = ClientProxy.grenadeRenderer = new RenderGrenade();
    }

    public static AnimStateMachine getAnimMachine(EntityPlayer entityPlayer) {
        AnimStateMachine animation = null;
        if (weaponBasicAnimations.containsKey(entityPlayer)) {
            animation = weaponBasicAnimations.get(entityPlayer);
        } else {
            animation = new AnimStateMachine();
            weaponBasicAnimations.put(entityPlayer, animation);
        }
        return animation;
    }

    public static EnhancedStateMachine getEnhancedAnimMachine(EntityPlayer entityPlayer) {
        EnhancedStateMachine animation = null;
        if (weaponEnhancedAnimations.containsKey(entityPlayer)) {
            animation = weaponEnhancedAnimations.get(entityPlayer);
        } else {
            animation = new EnhancedStateMachine();
            weaponEnhancedAnimations.put(entityPlayer, animation);
        }
        return animation;
    }

    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        switch (event.phase) {
            case START: {
                RenderParameters.smoothing = event.renderTickTime;
                SetPartialTick(event.renderTickTime);
                if(!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
                    Minecraft.getMinecraft().getFramebuffer().enableStencil();
                }
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
    public void onWorldRenderLast(RenderWorldLastEvent event) {
        //For each entity loaded, process with layers
        for (Object o : mc.world.getLoadedEntityList()) {
            Entity givenEntity = (Entity) o;
            //If entity is smoke grenade, render smoke
            if (givenEntity instanceof EntitySmokeGrenade) {
                EntitySmokeGrenade smokeGrenade = (EntitySmokeGrenade) givenEntity;
                if (smokeGrenade.exploded) {
                    if (smokeGrenade.smokeTime <= 220) {
                        RenderHelperMW.renderSmoke(grenade_smoke, smokeGrenade.posX, smokeGrenade.posY + 1, smokeGrenade.posZ, partialTicks, 600, 600, "0xFFFFFF", 0.8f);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onRenderHeldItem(RenderSpecificHandEvent event) {
        event.setCanceled(renderHeldItem(event.getItemStack(), event.getHand(), event.getPartialTicks(),getFOVModifier(event.getPartialTicks())));
    }
    
    public boolean renderHeldItem(ItemStack stack,EnumHand hand,float partialTicksTime,float fov) {
        EntityPlayer player = mc.player;
        boolean result=false;

        if (stack != null && stack.getItem() instanceof BaseItem) {
            BaseType type = ((BaseItem) stack.getItem()).baseType;
            BaseItem item = ((BaseItem) stack.getItem());

            if (hand != EnumHand.MAIN_HAND) {
                result=true;
                return result;
            }

            if (type.id > customRenderers.length)
                return result;

            if (item.render3d && customRenderers[type.id] != null && type.hasModel() && !type.getAssetDir().equalsIgnoreCase("attachments")) {
                result=true;

                float partialTicks = partialTicksTime;
                EntityRenderer renderer = mc.entityRenderer;
                float farPlaneDistance = mc.gameSettings.renderDistanceChunks * 16F;
                ItemRenderer itemRenderer = mc.getItemRenderer();
                
                GL11.glDepthRange(0, ModConfig.INSTANCE.hud.handDepthRange);
                
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                
                float zFar=2*farPlaneDistance;
                Project.gluPerspective(fov, (float) mc.displayWidth / (float) mc.displayHeight, 0.00001F, zFar);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.scale(1 / zFar, 1 / zFar, 1 / zFar);
                
                /**
                 * Fixed the bug gun renders bug
                 * */
                if(Double.isNaN(RenderParameters.collideFrontDistance)) {
                    RenderParameters.collideFrontDistance=0;
                }

                boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();

                if (mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator() && mc.getRenderViewEntity().equals(mc.player)) {
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
                    
                    if(debug) {
                        System.out.println(new float[] {
                                f1,f2,f3,f4,f5,f6,f7,f8,f9,
                                f10,f11
                        });
                    }
                    if(!ScopeUtils.isIndsideGunRendering) {
                        ClientProxy.scopeUtils.initBlur();  
                        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, OptifineHelper.getDrawFrameBuffer());  
                    }
                    GlStateManager.pushMatrix();

                    //Check if model is Basic or Enhanced for gun render
                    if(item instanceof ItemGun) {
                        if(((GunType)type).animationType.equals(WeaponAnimationType.BASIC)){
                            customRenderers[1].renderItem(CustomItemRenderType.EQUIPPED_FIRST_PERSON, hand, (ClientTickHandler.lastItemStack.isEmpty() ? stack : ClientTickHandler.lastItemStack), mc.world, mc.player);
                        } else{
                            
                            //客户端预测需要 必须是即时物品
                            if (GunType.getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight) != null) {
                                final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight).getItem();
                                if(itemAttachment.type.sight.modeType.insideGunRendering) {
                                    renderInsideGun(stack, hand, partialTicksTime, fov);
                                    GL11.glDepthRange(0, ModConfig.INSTANCE.hud.handDepthRange);
                                }
                            }
                            customRenderers[0].renderItem(CustomItemRenderType.EQUIPPED_FIRST_PERSON, hand, mc.player.getHeldItemMainhand(), mc.world, mc.player);
                             
                            ScopeUtils.needRenderHand1=true;
                        }
                    } else {
                        customRenderers[type.id].renderItem(CustomItemRenderType.EQUIPPED_FIRST_PERSON, hand, stack, mc.world, mc.player);
                    }
                    
                    GlStateManager.popMatrix();
                    
                    GlStateManager.popMatrix();
                }
                
                GlStateManager.matrixMode(GL11.GL_PROJECTION);
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                GlStateManager.popMatrix();
                
                GL11.glDepthRange(0, 1);
                if (mc.gameSettings.thirdPersonView == 0 && !flag) {
                    if(!ScopeUtils.isIndsideGunRendering) {
                        itemRenderer.renderOverlays(partialTicks);  
                    }
                }
                
            }
        }
        return result;
    }
    
    public void renderInsideGun(ItemStack stack,EnumHand hand,float partialTicksTime,float fov) {
        if(ScopeUtils.isIndsideGunRendering) {
            return;
        }
        if(!ScopeUtils.isRenderHand0&&OptifineHelper.isShadersEnabled()) {
            return;
        }
        ScopeUtils.isIndsideGunRendering=true;
        
        int tex=ClientProxy.scopeUtils.blurFramebuffer.framebufferTexture;
        ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);
        GL30.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, ScopeUtils.INSIDE_GUN_TEX, 0);
        GlStateManager.clearColor(0, 0, 0, 0);
        GL11.glClearColor(0, 0, 0, 0);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GlStateManager.clear (GL11.GL_DEPTH_BUFFER_BIT);
        copyDepthBuffer();
        ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
        renderHeldItem(stack, hand, partialTicksTime, fov);
        ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);
        GL30.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, tex, 0);
        ClientProxy.scopeUtils.blurFramebuffer.framebufferClear();
        
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, OptifineHelper.getDrawFrameBuffer());  
        ScopeUtils.isIndsideGunRendering=false;
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

    public void SetPartialTick(float dT) {
        partialTicks = dT;
    }

    @SubscribeEvent
    public void renderThirdPose(RenderLivingEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer)) {
            return;
        }

        AbstractClientPlayer clientPlayer = (AbstractClientPlayer)event.getEntity();
        Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager().<AbstractClientPlayer>getEntityRenderObject(event.getEntity());
        RenderPlayer renderplayer = (RenderPlayer) render;

        if(clientPlayer.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty()){
            renderplayer.getMainModel().bipedHeadwear.isHidden = false;
        } else {
            renderplayer.getMainModel().bipedHeadwear.isHidden = true;
        }
        if(clientPlayer.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()){
            renderplayer.getMainModel().bipedLeftArmwear.isHidden = false;
            renderplayer.getMainModel().bipedRightArmwear.isHidden = false;
            renderplayer.getMainModel().bipedBodyWear.isHidden = false;
        } else {
            renderplayer.getMainModel().bipedLeftArmwear.isHidden = true;
            renderplayer.getMainModel().bipedRightArmwear.isHidden = true;
            renderplayer.getMainModel().bipedBodyWear.isHidden = true;
        }
        if(clientPlayer.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty()){
            renderplayer.getMainModel().bipedLeftLegwear.isHidden = false;
            renderplayer.getMainModel().bipedRightLegwear.isHidden = false;
        } else {
            renderplayer.getMainModel().bipedLeftLegwear.isHidden = true;
            renderplayer.getMainModel().bipedRightLegwear.isHidden = true;
        }
        
        //hide begin
        renderplayer.getMainModel().bipedHead.isHidden = false;
        renderplayer.getMainModel().bipedBody.isHidden = false;
        renderplayer.getMainModel().bipedLeftArm.isHidden = false;
        renderplayer.getMainModel().bipedRightArm.isHidden = false;
        renderplayer.getMainModel().bipedLeftLeg.isHidden = false;
        renderplayer.getMainModel().bipedRightLeg.isHidden = false;
        renderplayer.getMainModel().bipedHead.showModel = true;
        renderplayer.getMainModel().bipedBody.showModel = true;
        renderplayer.getMainModel().bipedLeftArm.showModel = true;
        renderplayer.getMainModel().bipedRightArm.showModel = true;
        renderplayer.getMainModel().bipedLeftLeg.showModel = true;
        renderplayer.getMainModel().bipedRightLeg.showModel = true;
        clientPlayer.getArmorInventoryList().forEach((stack) -> {
            ArmorType type = null;
            if (stack.getItem() instanceof ItemMWArmor) {
                type = ((ItemMWArmor) stack.getItem()).type;
            }
            if (stack.getItem() instanceof ItemSpecialArmor) {
                type = ((ItemSpecialArmor) stack.getItem()).type;
            }
            if (type != null) {
                ArmorRenderConfig config = ((ModelCustomArmor)type.bipedModel).config;
                if (config.extra.hidePlayerModel) {
                    boolean hide = true;
                    if (config.extra.isSuit) {
                        renderplayer.getMainModel().bipedHead.isHidden = true;
                        renderplayer.getMainModel().bipedBody.isHidden = true;
                        renderplayer.getMainModel().bipedLeftArm.isHidden = true;
                        renderplayer.getMainModel().bipedRightArm.isHidden = true;
                        renderplayer.getMainModel().bipedLeftLeg.isHidden = true;
                        renderplayer.getMainModel().bipedRightLeg.isHidden = true;
                    } else {
                        switch (((ItemArmor) stack.getItem()).armorType) {
                        case HEAD:
                            renderplayer.getMainModel().bipedHead.isHidden = hide;
                            break;
                        case CHEST:
                            renderplayer.getMainModel().bipedBody.isHidden = hide;
                            renderplayer.getMainModel().bipedLeftArm.isHidden = hide;
                            renderplayer.getMainModel().bipedRightArm.isHidden = hide;
                            break;
                        case LEGS:
                            renderplayer.getMainModel().bipedLeftLeg.isHidden = hide;
                            renderplayer.getMainModel().bipedRightLeg.isHidden = hide;
                            break;
                        case FEET:
                            renderplayer.getMainModel().bipedLeftLeg.isHidden = hide;
                            renderplayer.getMainModel().bipedRightLeg.isHidden = hide;
                            break;
                        default:
                            break;
                        }
                    }
                }
                if (config.extra.hideAllPlayerWearModel) {
                    renderplayer.getMainModel().bipedHeadwear.isHidden = true;
                    renderplayer.getMainModel().bipedLeftArmwear.isHidden = true;
                    renderplayer.getMainModel().bipedRightArmwear.isHidden = true;
                    renderplayer.getMainModel().bipedBodyWear.isHidden = true;
                    renderplayer.getMainModel().bipedLeftLegwear.isHidden = true;
                    renderplayer.getMainModel().bipedRightLegwear.isHidden = true;
                }
            }
        });
        //hide end


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
            if (type.id == 1 && entity instanceof EntityPlayer) {
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
    public void onRenderHand(RenderHandFisrtPersonEvent.Pre event) {
        AbstractClientPlayer clientPlayer = Minecraft.getMinecraft().player;
        clientPlayer.getArmorInventoryList().forEach((stack) -> {
            if (event.isCanceled()) {
                return;
            }
            ArmorType type = null;
            if (stack.getItem() instanceof ItemMWArmor) {
                type = ((ItemMWArmor) stack.getItem()).type;
            }
            if (stack.getItem() instanceof ItemSpecialArmor) {
                type = ((ItemSpecialArmor) stack.getItem()).type;
            }
            if (type != null) {
                ArmorRenderConfig config = type.renderConfig;
                if(config!=null) {
                    if (config.extra.hidePlayerModel) {
                        if (config.extra.isSuit) {
                            event.setCanceled(true);
                        } else if (((ItemArmor) stack.getItem()).armorType == EntityEquipmentSlot.CHEST) {
                            event.setCanceled(true);
                        }
                    }  
                }
            }
        });
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