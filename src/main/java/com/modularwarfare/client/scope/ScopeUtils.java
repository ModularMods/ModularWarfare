package com.modularwarfare.client.scope;


import com.google.gson.JsonSyntaxException;
import com.modularmods.mcgltf.MCglTF;
import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.configs.AttachmentRenderConfig.Sight;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.model.ModelAttachment;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.renderers.RenderGunEnhanced;
import com.modularwarfare.client.shader.Programs;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.mixin.client.accessor.IShaderGroup;
import com.modularwarfare.utility.OptifineHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.optifine.shaders.MWFOptifineShadesHelper;
import net.optifine.shaders.Shaders;

import org.lwjgl.opengl.*;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.CROSS_ROTATE;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class ScopeUtils {

    public static int MIRROR_TEX;
    public static int OVERLAY_TEX;
    public static int INSIDE_GUN_TEX;
    public static int DEPTH_TEX;
    public static int DEPTH_ERASE_TEX;
    public static int SCOPE_MASK_TEX;
    public static int SCOPE_LIGHTMAP_TEX;
    public static ResourceLocation NOT_COMPATIBLE = new ResourceLocation(ModularWarfare.MOD_ID, "textures/gui/notcompatible.png");
    public static ResourceLocation SCOPE_BACK = new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/scope_back.png");
    private static Minecraft mc = Minecraft.getMinecraft();
    private ScopeRenderGlobal scopeRenderGlobal;
    
    public static boolean isRenderHand0=false;
    public static boolean needRenderHand1=false;

    public boolean hasBeenReseted = true;
    public float mouseSensitivityBackup;
    private Field renderEndNanoTime;

    public ShaderGroup blurShader;
    public Framebuffer blurFramebuffer;
    private static int lastScale;
    private static int lastScaleWidth;
    private static int lastScaleHeight;
    private static int lastWidth;
    private static int lastHeight;
    private static boolean lastShadersEnabled;
    private static int lastGbuffersFormat0;
    
    public static boolean isIndsideGunRendering=false;

    public ScopeUtils() {
        scopeRenderGlobal = new ScopeRenderGlobal(mc);
        ((IReloadableResourceManager)mc.getResourceManager()).registerReloadListener(this.scopeRenderGlobal);
        try {
            this.renderEndNanoTime = EntityRenderer.class.getDeclaredField("renderEndNanoTime");
        } catch (Exception ignored) {
        }
        if (this.renderEndNanoTime == null) try {
            this.renderEndNanoTime = EntityRenderer.class.getDeclaredField("field_78534_ac");
        } catch (Exception ignored) {
        }
        if (this.renderEndNanoTime != null) {
            this.renderEndNanoTime.setAccessible(true);
        }
    }


    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

            if (mc.player != null && mc.currentScreen == null) {
                //If player has gun, update scope
                if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun && mc.gameSettings.thirdPersonView == 0) {
                    if (GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentPresetEnum.Sight) != null) {
                        final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentPresetEnum.Sight).getItem();
                        if (itemAttachment != null) {
                            if (itemAttachment.type != null) {
                                if (itemAttachment.type.sight.modeType.isMirror) {
                                    if(OVERLAY_TEX==-1||(lastWidth!=mc.displayWidth||lastHeight!=mc.displayHeight)) {
                                        GL11.glPushMatrix();
                                        if(OVERLAY_TEX!=-1) {
                                            GL11.glDeleteTextures(OVERLAY_TEX);
                                        }
                                        OVERLAY_TEX = GL11.glGenTextures();
                                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, OVERLAY_TEX);
                                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, mc.displayWidth, mc.displayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
                                        
                                        if(INSIDE_GUN_TEX!=-1) {
                                            GL11.glDeleteTextures(INSIDE_GUN_TEX);
                                        }
                                        INSIDE_GUN_TEX = GL11.glGenTextures();
                                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, INSIDE_GUN_TEX);
                                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, mc.displayWidth, mc.displayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
                                        
                                        if(SCOPE_MASK_TEX!=-1) {
                                            GL11.glDeleteTextures(SCOPE_MASK_TEX);
                                        }
                                        SCOPE_MASK_TEX = GL11.glGenTextures();
                                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, SCOPE_MASK_TEX);
                                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, mc.displayWidth, mc.displayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
                                        
                                        if(SCOPE_LIGHTMAP_TEX!=-1) {
                                            GL11.glDeleteTextures(SCOPE_LIGHTMAP_TEX);
                                        }
                                        SCOPE_LIGHTMAP_TEX = GL11.glGenTextures();
                                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, SCOPE_LIGHTMAP_TEX);
                                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, mc.displayWidth, mc.displayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
                                        
                                        lastWidth=mc.displayWidth;
                                        lastHeight=mc.displayHeight;
                                        GL11.glPopMatrix();
                                    }
                                    if(itemAttachment.type.sight.modeType.isPIP&&RenderParameters.adsSwitch != 0) {
                                        renderWorld(mc, itemAttachment, event.renderTickTime);  
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        //genMirror();
        isRenderHand0=false;
    }
    
    public void onPreRenderHand0() {
        isRenderHand0=true;
        if(blurFramebuffer!=null) {
            copyEraseDepthBuffer();  
        }
    }
    
    public void onPreRenderHand1() {
        if(needRenderHand1) {
            needRenderHand1=false;
            Shaders.setHandsRendered(false, true);  
        }
    }
    
    public void genMirror() {
        boolean skip=true;
        if (mc.player.getHeldItemMainhand() != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemGun
                && RenderParameters.adsSwitch != 0 && mc.gameSettings.thirdPersonView == 0) {
            if (GunType.getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight) != null) {
                final ItemAttachment itemAttachment = (ItemAttachment) GunType
                        .getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight).getItem();
                if (itemAttachment != null) {
                    if (itemAttachment.type != null) {
                        skip=false;
                        if (itemAttachment.type.sight.modeType.isPIP) {
                            skip=true;
                        }
                        if (!itemAttachment.type.sight.modeType.isMirror) {
                            skip=true;
                        }
                    }
                }
            }
        }
        if(OptifineHelper.isShadersEnabled()) {
            if(Shaders.isShadowPass) {
                return;
            }
        }

        
        if(skip) {
            return;
        }
        
        initBlur();
        
        Minecraft mc=Minecraft.getMinecraft();
        
        if(OptifineHelper.isShadersEnabled()) {
            Shaders.renderCompositeFinal();
            GL43.glCopyImageSubData(Minecraft.getMinecraft().getFramebuffer().framebufferTexture, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, MIRROR_TEX, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, lastWidth, lastHeight, 1);
            Shaders.isCompositeRendered=false;
            Shaders.isRenderingWorld=true;
            Shaders.isRenderingDfb=true;
            OptifineHelper.bindGbuffersTextures();
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, MWFOptifineShadesHelper.getDFB());
            Shaders.setDrawBuffers(MWFOptifineShadesHelper.getDFBDrawBuffers());
            for (int i = 0; i < MWFOptifineShadesHelper.getUsedColorBuffers(); i++)
                OpenGlHelper.glFramebufferTexture2D(36160, 36064 + i, 3553, MWFOptifineShadesHelper.getFlipTextures().getA(i), 0); 
            GlStateManager.setActiveTexture(33984);
        }else {
            int tex=blurFramebuffer.framebufferObject;
            blurFramebuffer.bindFramebuffer(false);
            OpenGlHelper.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, MIRROR_TEX, 0);
            OpenGlHelper.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mc.getFramebuffer().framebufferObject);
            OpenGlHelper.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, blurFramebuffer.framebufferObject);
            GL30.glBlitFramebuffer(0, 0, lastWidth, lastHeight, 0, 0, lastWidth, lastHeight, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            blurFramebuffer.bindFramebuffer(false);
            OpenGlHelper.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, tex, 0);
            mc.getFramebuffer().bindFramebuffer(false);
        }
    }
    
    @SubscribeEvent
    public void onFovMod(FOVModifier event) {
        if (mc.player.getHeldItemMainhand() != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemGun && RenderParameters.adsSwitch != 0 && mc.gameSettings.thirdPersonView == 0) {
            if (GunType.getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight) != null) {
                final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight).getItem();
                if (itemAttachment != null) {
                    if (itemAttachment.type != null) {
                            if(!itemAttachment.type.sight.modeType.isPIP) {
                                float dst=getFov(itemAttachment);
                                if(ModConfig.INSTANCE.hud.isDynamicFov) {
                                    dst+=event.getFOV()-mc.gameSettings.fovSetting;
                                }
                                float src=event.getFOV();
                                event.setFOV(Math.max(1, src+(dst-src)*RenderParameters.adsSwitch));
                                if(RenderParameters.adsSwitch!=0&&RenderParameters.adsSwitch!=1) {
                                    //更新视角内的区块
                                    mc.renderGlobal.setDisplayListEntitiesDirty();
                                }
                            }
                        }
                    }
                }
            }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderHUD(RenderGameOverlayEvent.Pre event) {
        if(event.getType()!=ElementType.ALL) {
            return;
        }
        ItemStack stack=Minecraft.getMinecraft().player.getHeldItemMainhand();
        if (stack != null && stack.getItem() instanceof ItemGun) {
            if (GunType.getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight) != null) {
                final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(mc.player.getHeldItemMainhand(), AttachmentPresetEnum.Sight).getItem();
                if(itemAttachment.type.sight.modeType.isPIP) {
                    renderPostScope(event.getPartialTicks(),false,true,true, 1 );
                    GlStateManager.enableDepth();
                    GlStateManager.disableAlpha();
                    GlStateManager.enableBlend();
                    GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);  
                }
            }
        }
    }
    
    public void renderPostScope(float renderTickTime,boolean isDepthMode,boolean isInsideRendering,boolean isOverlayRendering,float alpha) {
        if (RenderParameters.adsSwitch > 0) {
            ScaledResolution resolution = new ScaledResolution(mc);
            ItemAttachment attachment=null;
            
            if (GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentPresetEnum.Sight) != null) {
                attachment = (ItemAttachment) GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentPresetEnum.Sight).getItem();
            }
            
            if(attachment==null||attachment==Items.AIR) {
                return;
            }
            if (!attachment.type.sight.modeType.isMirror) {
                return;
            }

            if(!attachment.type.sight.modeType.isPIP||RenderGunEnhanced.debug1) {
                if(OptifineHelper.isRenderingDfb()) {
                    //TODO: Optifine and OpenGL 2.1 Compatibility ?
                    GL43.glCopyImageSubData(MWFOptifineShadesHelper.getFlipTextures().getA(ModConfig.INSTANCE.hud.shadersColorTexID), GL11.GL_TEXTURE_2D, 0, 0, 0, 0, MIRROR_TEX, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, lastWidth, lastHeight, 1);
                } else {
                    ContextCapabilities contextCapabilities = GLContext.getCapabilities();
                    if (contextCapabilities.OpenGL43) {
                        GL43.glCopyImageSubData(mc.getFramebuffer().framebufferTexture, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, MIRROR_TEX, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, lastWidth, lastHeight, 1);

                    } else {
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, MIRROR_TEX);
                        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0,  0, 0,0,  mc.displayWidth, mc.displayHeight);
                    }
                }
            }

            
            boolean needBlur = false;
            if(ModConfig.INSTANCE.hud.ads_blur) {
                if (attachment != null) {
                    if (attachment.type != null) {
                        if (attachment.type.sight.modeType.isMirror) {
                            needBlur = true;
                            ClientProxy.scopeUtils.renderBlur();
                        }
                    }
                }
            }
            
            if(OptifineHelper.isShadersEnabled()) {
                Shaders.pushProgram();  
                Shaders.useProgram(Shaders.ProgramNone);
            }
            GlStateManager.enableBlend();
            
            Sight config=((ModelAttachment)attachment.type.model).config.sight;
            
            GlStateManager.pushMatrix();
            GlStateManager.color(1, 1, 1, 1);
            setupOverlayRendering();
            GlStateManager.disableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0);
            
            ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);
            GlStateManager.clearDepth(1);
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
            GlStateManager.clearDepth(1);
            GL11.glDepthRange(0, 0);
            GlStateManager.depthMask(false);
            GL20.glUseProgram(Programs.scopeBorderProgram);
            GL20.glUniform2f(GL20.glGetUniformLocation(Programs.scopeBorderProgram, "size"), mc.displayWidth,mc.displayHeight);
            GL20.glUniform1f(GL20.glGetUniformLocation(Programs.scopeBorderProgram, "maskRange"), config.uniformMaskRange);
            GL20.glUniform1f(GL20.glGetUniformLocation(Programs.scopeBorderProgram, "drawRange"), config.uniformDrawRange);
            GL20.glUniform1f(GL20.glGetUniformLocation(Programs.scopeBorderProgram, "strength"), config.uniformStrength);
            GL20.glUniform1f(GL20.glGetUniformLocation(Programs.scopeBorderProgram, "scaleRangeY"), config.uniformScaleRangeY);
            GL20.glUniform1f(GL20.glGetUniformLocation(Programs.scopeBorderProgram, "scaleStrengthY"), config.uniformScaleStrengthY);
            GL20.glUniform1f(GL20.glGetUniformLocation(Programs.scopeBorderProgram, "verticality"), config.uniformVerticality);
            
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.colorMask(true, true, true, false);
            GlStateManager.bindTexture(ClientProxy.scopeUtils.MIRROR_TEX);
            ClientProxy.scopeUtils.drawScaledCustomSizeModalRectFlipY(0, 0, 0, 0, 1, 1, resolution.getScaledWidth(), resolution.getScaledHeight(), 1, 1);  
            GlStateManager.depthMask(true);
            
            if(isDepthMode) {
                GlStateManager.enableDepth();
                GlStateManager.alphaFunc(GL11.GL_GEQUAL,1f);
            }else {
                GlStateManager.alphaFunc(GL11.GL_GREATER, 0f);  
            }

            if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                if(((ItemGun) mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem()).type.animationType == WeaponAnimationType.ENHANCED){
                    GlStateManager.bindTexture(ClientProxy.scopeUtils.INSIDE_GUN_TEX);
                    ClientProxy.scopeUtils.drawScaledCustomSizeModalRectFlipY(0, 0, 0, 0, 1, 1, resolution.getScaledWidth(), resolution.getScaledHeight(), 1, 1);
                }
            }

            GL20.glUseProgram(Programs.normalProgram);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.pushMatrix();
            float width=config.maskSize*resolution.getScaledHeight();
            float height=config.maskSize*resolution.getScaledHeight();
            GlStateManager.translate(resolution.getScaledWidth()/2f, resolution.getScaledHeight()/2f, 0);
            GlStateManager.rotate(CROSS_ROTATE,0,0,1);  
            GlStateManager.translate(-width/2f, -height/2f, 0);
            ClientProxy.gunStaticRenderer.bindTexture("mask", config.maskTexture);
            ClientProxy.scopeUtils.drawScaledCustomSizeModalRectFlipY(0, 0, 0, 0, 1, 1,(int)width,(int)height, 1, 1);
            GlStateManager.popMatrix();
            
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.color(1, 1, 1, 1);
            
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, OptifineHelper.getDrawFrameBuffer());
            
            GlStateManager.alphaFunc(GL11.GL_GEQUAL,1f);
            GlStateManager.blendFunc(SourceFactor.ONE, DestFactor.ZERO);
            
            if(isInsideRendering) {
                ClientProxy.scopeUtils.blurFramebuffer.bindFramebufferTexture();
                ClientProxy.scopeUtils.drawScaledCustomSizeModalRectFlipY(0, 0, 0, 0, 1, 1, resolution.getScaledWidth(), resolution.getScaledHeight(), 1, 1);
            }

            GlStateManager.color(1, 1, 1, alpha);
            if(isOverlayRendering) {
                if(isDepthMode) {
                    blurFramebuffer.bindFramebuffer(false);
                    
                    GlStateManager.enableDepth();
                    GlStateManager.depthMask(true);
                    GlStateManager.alphaFunc(GL11.GL_GEQUAL,1f);
                    
                    GlStateManager.colorMask(false, false, false, false);
                    GlStateManager.bindTexture(ClientProxy.scopeUtils.OVERLAY_TEX);
                    ClientProxy.scopeUtils.drawScaledCustomSizeModalRectFlipY(0, 0, 0, 0, 1, 1, resolution.getScaledWidth(), resolution.getScaledHeight(), 1, 1);
                    GlStateManager.colorMask(true, true, true, true);
                    GlStateManager.depthMask(false);
                    GlStateManager.disableDepth();
                    
                    OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, OptifineHelper.getDrawFrameBuffer());
                }else {
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f);
                    
                    GL20.glUseProgram(Programs.overlayProgram);
                    GL20.glUniform2f(GL20.glGetUniformLocation(Programs.overlayProgram, "size"), mc.displayWidth,mc.displayHeight);
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
                    int tex3=GlStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                    GlStateManager.bindTexture(blurFramebuffer.framebufferTexture);
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE4);
                    int tex4=GlStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                    GlStateManager.bindTexture(SCOPE_LIGHTMAP_TEX);
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
                    GlStateManager.bindTexture(ClientProxy.scopeUtils.OVERLAY_TEX);
                    ClientProxy.scopeUtils.drawScaledCustomSizeModalRectFlipY(0, 0, 0, 0, 1, 1, resolution.getScaledWidth(), resolution.getScaledHeight(), 1, 1);
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE3);
                    GlStateManager.bindTexture(tex3);
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE4);
                    GlStateManager.bindTexture(tex4);
                    GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
                }
            }
           
            GL20.glUseProgram(0);
            
            if(OptifineHelper.isShadersEnabled()) {
                Shaders.popProgram();  
            }
            
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.enableDepth();
            GL11.glDepthRange(0, 1);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
    }
    
    public void setupOverlayRendering()
    {
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth_double(), scaledresolution.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
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
    
    public void copyEraseDepthBuffer() {
        GL43.glCopyImageSubData(MWFOptifineShadesHelper.getDFBDepthTextures().get(0), GL11.GL_TEXTURE_2D, 0, 0, 0, 0, DEPTH_ERASE_TEX, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, lastWidth, lastHeight, 1);
    }
    
    public void renderSunglassesPostProgram() {
        if(!OptifineHelper.isShadersEnabled()) {
            return;
        }
        GlStateManager.color(1, 1, 1,1);
        Shaders.pushProgram();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();

        setupOverlayRendering();
        
        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT|GL11.GL_VIEWPORT_BIT);
        
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
        GL11.glDepthRange(0, 1);
        
        
        ScaledResolution resolution = new ScaledResolution(this.mc);
        GL20.glDrawBuffers(GL30.GL_COLOR_ATTACHMENT1);
        
        GL20.glUseProgram(Programs.sunglassesProgram);
        
        GlStateManager.disableBlend();
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GlStateManager.bindTexture(ClientProxy.scopeUtils.OVERLAY_TEX);
        ClientProxy.scopeUtils.drawScaledCustomSizeModalRectFlipY(0, 0, 0, 0, 1, 1, resolution.getScaledWidth(), resolution.getScaledHeight(), 1, 1);
        GlStateManager.enableBlend();
        
        GL11.glPopAttrib();
        
        GL20.glUseProgram(0);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        Shaders.popProgram();
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        switch (event.phase) {
            case START:
                if (ClientRenderHooks.isAimingScope) {
                    if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun && RenderParameters.adsSwitch != 0 && mc.gameSettings.thirdPersonView == 0) {
                        if (GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentPresetEnum.Sight) != null) {
                            final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND), AttachmentPresetEnum.Sight).getItem();
                            if (itemAttachment != null) {
                                if (itemAttachment.type != null) {
                                    mc.gameSettings.mouseSensitivity = mouseSensitivityBackup * ((ModelAttachment) itemAttachment.type.model).config.sight.mouseSensitivityFactor;
                                    hasBeenReseted = false;
                                }
                            }
                        }
                    }
                } else if (!hasBeenReseted) {
                    mc.gameSettings.mouseSensitivity = mouseSensitivityBackup;
                    //mc.gameSettings.fovSetting = 90;
                    hasBeenReseted = true;
                } else if (mouseSensitivityBackup != mc.gameSettings.mouseSensitivity) {
                    mouseSensitivityBackup = mc.gameSettings.mouseSensitivity;
                }
        }
    }
    
    public static float getFov(ItemAttachment itemAttachment) {
        return (50.0f / ((ModelAttachment) itemAttachment.type.model).config.sight.fovZoom);
    }

    public void renderWorld(Minecraft mc, ItemAttachment itemAttachment, float partialTick) {

        float zoom = getFov(itemAttachment);

        GL11.glPushMatrix();
        GlStateManager.color(1, 1, 1,1);

        RenderGlobal renderBackup = mc.renderGlobal;
        //Save the current settings to be reset later
        long endTime = 0;
        boolean hide = mc.gameSettings.hideGUI;
        int view = mc.gameSettings.thirdPersonView;
        int limit = mc.gameSettings.limitFramerate;
        RayTraceResult mouseOver = mc.objectMouseOver;
        float fov = mc.gameSettings.fovSetting;
        boolean bobbingBackup = mc.gameSettings.viewBobbing;
        float mouseSensitivityBackup = mc.gameSettings.mouseSensitivity;

        mc.renderGlobal = scopeRenderGlobal;

        //Change game settings for the Scope
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.thirdPersonView = 0;
        mc.gameSettings.fovSetting = zoom;
        mc.gameSettings.viewBobbing = false;
        //Make sure the FOV isn't less than 1
        if (mc.gameSettings.fovSetting < 0) {
            mc.gameSettings.fovSetting = 1;
        }

        if (limit != 0 && renderEndNanoTime != null) {
            try {
                endTime = renderEndNanoTime.getLong(mc.entityRenderer);
            } catch (Exception ignored) {
            }
        }

        int fps = Math.max(30, mc.gameSettings.limitFramerate);
        //Minecraft.getMinecraft().getFramebuffer().framebufferClear();
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, Minecraft.getMinecraft().getFramebuffer().framebufferObject);
        
        int tex=Minecraft.getMinecraft().getFramebuffer().framebufferTexture;
        Minecraft.getMinecraft().getFramebuffer().framebufferTexture = MIRROR_TEX;
        GL30.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, MIRROR_TEX, 0);
        
        mc.entityRenderer.renderWorld(partialTick, endTime);
        
        GL20.glUseProgram(0);
        
        
        Minecraft.getMinecraft().getFramebuffer().framebufferTexture = tex;
        GL30.glFramebufferTexture2D(OpenGlHelper.GL_FRAMEBUFFER, OpenGlHelper.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, tex, 0);
        //GL43.glCopyImageSubData(Minecraft.getMinecraft().getFramebuffer().framebufferTexture, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, MIRROR_TEX, GL11.GL_TEXTURE_2D, 0, 0, 0, 0, lastWidth, lastHeight, 1);
        
        if (limit != 0 && renderEndNanoTime != null) {
            try {
                renderEndNanoTime.setLong(mc.entityRenderer, endTime);
            } catch (Exception ignored) {
            }
        }


        //Go back to the original Settings
        mc.objectMouseOver = mouseOver;
        mc.gameSettings.limitFramerate = limit;
        mc.gameSettings.thirdPersonView = view;
        mc.gameSettings.hideGUI = hide;
        mc.gameSettings.viewBobbing = bobbingBackup;
        mc.gameSettings.mouseSensitivity = mouseSensitivityBackup;

        mc.gameSettings.fovSetting = fov;
        mc.renderGlobal = renderBackup;


        GL11.glPopMatrix();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (event.getWorld().isRemote) {
            scopeRenderGlobal.setWorldAndLoadRenderers((WorldClient) event.getWorld());
        }
    }

    /**
     * Blur Shader
     */
    public void drawScaledCustomSizeModalRectFlipY(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight)
    {
        float f = 1.0F / tileWidth;
        float f1 = 1.0F / tileHeight;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double) x, (double) (y + height), 0.0D)
                .tex((double) (u * f), 1 - (double) ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.pos((double) (x + width), (double) (y + height), 0.0D)
                .tex((double) ((u + (float) uWidth) * f), 1 - (double) ((v + (float) vHeight) * f1)).endVertex();
        bufferbuilder.pos((double) (x + width), (double) y, 0.0D)
                .tex((double) ((u + (float) uWidth) * f), 1 - (double) (v * f1)).endVertex();
        bufferbuilder.pos((double) x, (double) y, 0.0D).tex((double) (u * f), 1 - (double) (v * f1)).endVertex();
        tessellator.draw();
    }

    public void initBlur() {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());

        int scaleFactor = resolution.getScaleFactor();
        int widthFactor = resolution.getScaledWidth();
        int heightFactor = resolution.getScaledHeight();
        int gbuffersFormat0 = -1;
        if(OptifineHelper.isShadersEnabled()) {
            gbuffersFormat0=OptifineHelper.getGbuffersFormat()[ModConfig.INSTANCE.hud.shadersColorTexID];
        }
        if(blurFramebuffer!=null) {
            ClientProxy.scopeUtils.blurFramebuffer.framebufferClear();
            OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, OptifineHelper.getDrawFrameBuffer());
        }
        
        if (!(OptifineHelper.isShadersEnabled() != lastShadersEnabled || gbuffersFormat0 != lastGbuffersFormat0
                || lastScale != scaleFactor || lastScaleWidth != widthFactor || lastScaleHeight != heightFactor
                || blurFramebuffer == null || blurShader == null)) {
            return;
        }

        lastGbuffersFormat0=gbuffersFormat0;
        lastScale = scaleFactor;
        lastScaleWidth = widthFactor;
        lastScaleHeight = heightFactor;
        lastShadersEnabled = OptifineHelper.isShadersEnabled();

        if(MIRROR_TEX!=-1) {
            GL11.glDeleteTextures(MIRROR_TEX);
        }
        MIRROR_TEX = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, MIRROR_TEX);
        if(OptifineHelper.isShadersEnabled()) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, gbuffersFormat0, mc.displayWidth, mc.displayHeight, 0, OptifineHelper.getPixelFormat(gbuffersFormat0), 33639, (ByteBuffer)null);
        }else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, mc.displayWidth, mc.displayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        
        if(DEPTH_TEX!=-1) {
            GL11.glDeleteTextures(DEPTH_TEX);
        }
        DEPTH_TEX = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, DEPTH_TEX);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, mc.displayWidth, mc.displayHeight, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_LUMINANCE);
        
        if(DEPTH_ERASE_TEX!=-1) {
            GL11.glDeleteTextures(DEPTH_ERASE_TEX);
        }
        DEPTH_ERASE_TEX = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, DEPTH_ERASE_TEX);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, mc.displayWidth, mc.displayHeight, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (FloatBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_DEPTH_TEXTURE_MODE, GL11.GL_LUMINANCE);
        
        try {
            blurFramebuffer=null;
            if(!OptifineHelper.isShadersEnabled()) {
                blurFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
            }else {
                blurFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, false);
            }
            blurFramebuffer.setFramebufferColor(0,0,0,0);
            if(mc.getFramebuffer().isStencilEnabled()&&!blurFramebuffer.isStencilEnabled()) {
                blurFramebuffer.enableStencil();  
            }
            if(OptifineHelper.isShadersEnabled()) {
                blurFramebuffer.bindFramebuffer(false);
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, DEPTH_TEX, 0);
            }
            blurShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), new ResourceLocation(ModularWarfare.MOD_ID,"shaders/post/blurex.json"));
            blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
        } catch (JsonSyntaxException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void renderBlur() {
        for(Shader shader : ((IShaderGroup)blurShader).getListShaders()){
            if(shader.getShaderManager().getShaderUniform("Progress") != null){
                shader.getShaderManager().getShaderUniform("Progress").set(RenderParameters.adsSwitch);
            }
        }
        blurShader.render(ClientProxy.renderHooks.partialTicks);
    }

}