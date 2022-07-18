package com.modularwarfare.client.fpp.enhanced.renderers;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.IProcessNodeModelHandler;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.animations.AnimStateMachine;
import com.modularwarfare.client.fpp.basic.models.ModelAttachment;
import com.modularwarfare.client.fpp.basic.models.ModelGun;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.AnimationType;
import com.modularwarfare.client.fpp.enhanced.animation.AnimationController;
import com.modularwarfare.client.fpp.enhanced.animation.EnhancedStateMachine;
import com.modularwarfare.client.fpp.enhanced.animation.EnhancedStateMachine.Phase;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig.Attachment;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig.Transform;
import com.modularwarfare.client.fpp.enhanced.models.EnhancedModel;
import com.modularwarfare.client.handler.ClientTickHandler;
import com.modularwarfare.client.scope.ScopeUtils;
import com.modularwarfare.common.guns.AmmoType;
import com.modularwarfare.common.guns.AttachmentEnum;
import com.modularwarfare.common.guns.AttachmentType;
import com.modularwarfare.common.guns.AttachmentType.Sight;
import com.modularwarfare.common.guns.BulletType;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemAmmo;
import com.modularwarfare.common.guns.ItemAttachment;
import com.modularwarfare.common.guns.ItemBullet;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponFireMode;
import com.modularwarfare.common.guns.WeaponScopeType;
import com.modularwarfare.common.handler.data.VarBoolean;
import com.modularwarfare.common.network.PacketAimingRequest;
import com.modularwarfare.common.textures.TextureType;
import com.modularwarfare.utility.OptifineHelper;
import com.modularwarfare.utility.ReloadHelper;
import com.modularwarfare.utility.maths.Interpolation;
import com.timlee9024.mcgltf.DefaultMaterialHandler;

import mchhui.modularmovements.tactical.client.ClientLitener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Random;

import static com.modularwarfare.client.fpp.basic.renderers.RenderParameters.*;

public class RenderGunEnhanced extends CustomItemRenderer {

    public static final float PI = 3.14159265f;

    private Timer timer;

    public AnimationController controller;

    public FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
    
    public ResourceLocation bindingTexture;
    
    private boolean renderingMagazine=true;
    
    public float r=1;
    public float g=1;
    public float b=1;
    public float a=1;
    
    private static final int BULLET_MAX_RENDER=9999+1;
    private static float theata90=(float) Math.toRadians(90);
    private static final HashSet<String> DEFAULT_EXCEPT =new HashSet<String>();
    private static final String[] LEFT_HAND_PART=new String[]{
            "leftArmModel", "leftArmLayerModel"
    };
    private static final String[] LEFT_SLIM_HAND_PART=new String[]{
            "leftArmSlimModel", "leftArmLayerSlimModel"
    };
    private static final String[] RIGHT_HAND_PART=new String[]{
            "rightArmModel", "rightArmLayerModel"
    };
    private static final String[] RIGHT_SLIM_HAND_PART=new String[]{
            "rightArmSlimModel", "rightArmLayerSlimModel"
    };
    static {
        String[] strs=new String[] {
                "ammoModel",
                "leftArmModel", "leftArmLayerModel",
                "leftArmSlimModel", "leftArmLayerSlimModel",
                "rightArmModel", "rightArmLayerModel",
                "rightArmSlimModel", "rightArmLayerSlimModel",
                "flashModel","sprint_righthand","sprint_lefthand",
                "selector_semi","selector_full","selector_brust",
                "bulletModel"
        };
        for(String str:strs) {
            DEFAULT_EXCEPT.add(str);
        }
        for(int i=0;i<BULLET_MAX_RENDER;i++) {
            DEFAULT_EXCEPT.add("bulletModel_"+i);
        }
    }

    public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data) {
        if (!(item.getItem() instanceof ItemGun))
            return;

        GunType gunType = ((ItemGun) item.getItem()).type;
        if (gunType == null)
            return;

        EnhancedModel model = gunType.enhancedModel;

        if(!(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayerSP)) {
            return;
        }
        
        if (model == null)
            return;
        
        if(this.controller == null || this.controller.getConfig() != model.config){
            this.controller = new AnimationController(model.config);
        }


        if (this.timer == null) {
            this.timer = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
        }

        
        float partialTicks = this.timer.renderPartialTicks;

        EntityPlayerSP player = (EntityPlayerSP) Minecraft.getMinecraft().getRenderViewEntity();

        EnhancedStateMachine anim = ClientRenderHooks.getEnhancedAnimMachine(player);

        Matrix4f mat = new Matrix4f();
        
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        
        /**
         * INITIAL BLENDER POSITION
         * nonono this is minecrfat hand transform
         */
        //mat.rotate(toRadians(45.0F), new Vector3f(0,1,0));
        //mat.translate(new Vector3f(-1.8f,1.3f,-1.399f));
        
        /**
         * DEFAULT TRANSFORM
         * */
        //mat.translate(new Vector3f(0,1.3f,-1.8f));
        mat.rotate(toRadians(90.0F), new Vector3f(0,1,0));
        //Do hand rotations
        float f5 = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * partialTicks;
        float f6 = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * partialTicks;
        mat.rotate(toRadians((player.rotationPitch - f5) * 0.1F), new Vector3f(1, 0, 0));
        mat.rotate(toRadians((player.rotationYaw - f6) * 0.1F), new Vector3f(0, 1, 0));

        float rotateX=0;
        float adsModifier = (float) (0.95f - AnimationController.ADS);
        
        /**
         *  GOBAL
         * */
        mat.rotate(toRadians(90), new Vector3f(0, 1, 0));
        mat.translate(new Vector3f(model.config.gobal.gobalTranslate.x, model.config.gobal.gobalTranslate.y, model.config.gobal.gobalTranslate.z));
        mat.scale(new Vector3f(model.config.gobal.gobalScale.x,model.config.gobal.gobalScale.y,model.config.gobal.gobalScale.z));
        mat.rotate(toRadians(-90), new Vector3f(0, 1, 0));
        mat.rotate(model.config.gobal.gobalRotate.y/180*3.14f, new Vector3f(0, 1, 0));
        mat.rotate(model.config.gobal.gobalRotate.x/180*3.14f, new Vector3f(1, 0, 0));
        mat.rotate(model.config.gobal.gobalRotate.z/180*3.14f, new Vector3f(0, 0, 1));
        
        /**
         * ACTION GUN MOTION
         */
        float gunRotX = RenderParameters.GUN_ROT_X_LAST
                + (RenderParameters.GUN_ROT_X - RenderParameters.GUN_ROT_X_LAST) * ClientProxy.renderHooks.partialTicks;
        float gunRotY = RenderParameters.GUN_ROT_Y_LAST
                + (RenderParameters.GUN_ROT_Y - RenderParameters.GUN_ROT_Y_LAST) * ClientProxy.renderHooks.partialTicks;
        mat.rotate(toRadians(gunRotX), new Vector3f(0, -1, 0));
        mat.rotate(toRadians(gunRotY), new Vector3f(0, 0, -1));

        /**
         * ACTION FORWARD
         */
        float f1 = (player.distanceWalkedModified - player.prevDistanceWalkedModified);
        float f2 = -(player.distanceWalkedModified + f1 * partialTicks);
        float f3 = (player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks);
        float f4 = (player.prevCameraPitch + (player.cameraPitch - player.prevCameraPitch) * partialTicks);

        mat.translate(new Vector3f(0, adsModifier * Interpolation.SINE_IN.interpolate(0F, (-0.2f * (1F - (float)AnimationController.ADS)), GUN_BALANCING_Y),0));
        mat.translate(new Vector3f(0, adsModifier * ((float) (0.05f * (Math.sin(SMOOTH_SWING/10) * GUN_BALANCING_Y))),0));

        mat.rotate(toRadians(adsModifier * 0.1f * Interpolation.SINE_OUT.interpolate(-GUN_BALANCING_Y, GUN_BALANCING_Y, adsModifier * MathHelper.sin(f2 * (float) Math.PI))), new Vector3f(0f,1f, 0f));

        mat.translate(new Vector3f(adsModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 0.5F, adsModifier * -Math.abs(MathHelper.cos(f2 * (float) Math.PI) * f3), 0.0F));
        mat.rotate(toRadians(adsModifier * MathHelper.sin(f2 * (float) Math.PI) * f3 * 3.0F), new Vector3f(0.0F, 0.0F, 1.0F));
        mat.rotate(toRadians(adsModifier * Math.abs(MathHelper.cos(f2 * (float) Math.PI - 0.2F) * f3) * 5.0F), new Vector3f(1.0F, 0.0F, 0.0F));
        mat.rotate(toRadians(adsModifier * f4), new Vector3f(1.0F, 0.0F, 0.0F));

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
        mat.translate(new Vector3f(0f, ((VAL / 500) * (0.95f -  (float)AnimationController.ADS)),  ((VAL2 / 500 * (0.95f -  (float)AnimationController.ADS)))));
        mat.rotate(toRadians(adsModifier * VALROT), new Vector3f(1F, 0F, 0F));

        /**
         * ACTION GUN BALANCING X / Y
         */
        mat.translate(new Vector3f((float) (0.1f*GUN_BALANCING_X*Math.cos(Math.PI * RenderParameters.SMOOTH_SWING / 50)) * (1F -  (float)AnimationController.ADS),0,0));
        rotateX-=(GUN_BALANCING_X * 4F) + (float) (GUN_BALANCING_X * Math.sin(Math.PI * RenderParameters.SMOOTH_SWING / 35));
        rotateX-=(float) Math.sin(Math.PI * GUN_BALANCING_X);
        rotateX-=(GUN_BALANCING_X) * 0.4F;
        /**
         * ACTION PROBE
         */
        if(Loader.isModLoaded("modularmovements")) {
            rotateX+=15F * ClientLitener.cameraProbeOffset;
        }
        mat.rotate(toRadians(rotateX),  new Vector3f(1f, 0f, 0f));

        /**
         * ACTION SPRINT
         */
        RenderParameters.VALSPRINT = (float) (Math.cos(controller.SPRINT_RANDOM*2*Math.PI)) * gunType.moveSpeedModifier;
        RenderParameters.VALSPRINT2 = (float)(Math.sin(controller.SPRINT_RANDOM*2*Math.PI)) * gunType.moveSpeedModifier;
        
        /*
        OPTIONAL
        float VALSPRINT3 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 8) * 6) * gunType.moveSpeedModifier;
        float VALSPRINT4 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 9) * 7) * gunType.moveSpeedModifier;
        float VALSPRINT5 = (float)(Math.sin(RenderParameters.SMOOTH_SWING / 10) * 8) * gunType.moveSpeedModifier;
        */
        
        float springModifier=(float) (0.8f-controller.ADS);
        mat.rotate(toRadians(0.2f * VALSPRINT * springModifier), new Vector3f(1, 0, 0));
        mat.rotate(toRadians(VALSPRINT2 * springModifier), new Vector3f(0, 0, 1));
        mat.translate(new Vector3f(VALSPRINT * 0.2f * springModifier, 0, VALSPRINT2 * 0.2f * springModifier));

        /*
        OPTIONAL
        GL11.glRotatef(adsModifier * 0.3f * VALSPRINT3 * AnimationController.SPRINT, 0, 1, 0);
        GL11.glRotatef(adsModifier * 0.4f * VALSPRINT4 * AnimationController.SPRINT, 0, 0, 1);
        GL11.glRotatef(adsModifier * 0.5f * VALSPRINT5 * AnimationController.SPRINT, -1, -1, 0);
         */

        Vector3f customSprintRotation = new Vector3f((model.config.sprint.sprintRotate.x *  (float)AnimationController.SPRINT), (model.config.sprint.sprintRotate.y *  (float)AnimationController.SPRINT), (model.config.sprint.sprintRotate.z *  (float)AnimationController.SPRINT));
        Vector3f customSprintTranslate = new Vector3f((model.config.sprint.sprintTranslate.x *  (float)AnimationController.SPRINT), (model.config.sprint.sprintTranslate.y *  (float)AnimationController.SPRINT), (model.config.sprint.sprintTranslate.z *  (float)AnimationController.SPRINT));

        customSprintRotation.scale((1F -  (float)AnimationController.ADS));
        customSprintTranslate.scale((1F -  (float)AnimationController.ADS));

        /**
         * CUSTOM HIP POSITION
         */
        
        Vector3f customHipRotation = new Vector3f(model.config.aim.rotateHipPosition.x, model.config.aim.rotateHipPosition.y, model.config.aim.rotateHipPosition.z);
        Vector3f customHipTranslate = new Vector3f(model.config.aim.translateHipPosition.x, (model.config.aim.translateHipPosition.y), (model.config.aim.translateHipPosition.z));
        
        Vector3f customAimRotation = new Vector3f((model.config.aim.rotateAimPosition.x *  (float)AnimationController.ADS), (model.config.aim.rotateAimPosition.y *  (float)AnimationController.ADS), (model.config.aim.rotateAimPosition.z *  (float)AnimationController.ADS));
        Vector3f customAimTranslate = new Vector3f((model.config.aim.translateAimPosition.x *  (float)AnimationController.ADS), (model.config.aim.translateAimPosition.y *  (float)AnimationController.ADS), (model.config.aim.translateAimPosition.z *  (float)AnimationController.ADS));
        
        mat.rotate(toRadians(customHipRotation.x + customSprintRotation.x+customAimRotation.x), new Vector3f(1f,0f,0f));
        mat.rotate(toRadians(customHipRotation.y + customSprintRotation.y+customAimRotation.y), new Vector3f(0f,1f,0f));
        mat.rotate(toRadians(customHipRotation.z + customSprintRotation.z+customAimRotation.z), new Vector3f(0f,0f,1f));
        mat.translate(new Vector3f(customHipTranslate.x + customSprintTranslate.x+customAimTranslate.x, customHipTranslate.y + customSprintTranslate.y+customAimTranslate.y, customHipTranslate.z + customSprintTranslate.z+customAimTranslate.z));

        /**
         * ATTACHMENT AIM
         * */
        if(GunType.getAttachment(item, AttachmentEnum.Sight)!=null) {
            ItemAttachment sight = (ItemAttachment) GunType.getAttachment(item, AttachmentEnum.Sight).getItem();
            Attachment sightConfig=model.config.attachment.get(sight.type.internalName);
            if(sightConfig!=null) {
                //System.out.println("test");
                float ads=(float) controller.ADS;
                mat.translate((Vector3f) new Vector3f(sightConfig.sightAimPosOffset).scale(ads));
                mat.rotate(ads * sightConfig.sightAimRotOffset.y * 3.14f / 180, new Vector3f(0, 1, 0));
                mat.rotate(ads * sightConfig.sightAimRotOffset.x * 3.14f / 180, new Vector3f(1, 0, 0));
                mat.rotate(ads * sightConfig.sightAimRotOffset.z * 3.14f / 180, new Vector3f(0, 0, 1));
            }
        }
        
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
        
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        float worldScale = 1;
        float rotateXRendering=rotateX;
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        color(1, 1, 1, 1f);
        
        boolean applySprint = AnimationController.SPRINT > 0.1 && AnimationController.INSPECT >= 1;
        HashSet<String> exceptParts=model.config.defaultHidePart;
        if(exceptParts==null) {
            exceptParts=new HashSet<String>();
        }
        exceptParts.addAll(DEFAULT_EXCEPT);
        
        for (AttachmentEnum attachment : AttachmentEnum.values()) {
            ItemStack itemStack = GunType.getAttachment(item, attachment);
            if (itemStack != null && itemStack.getItem() != Items.AIR) {
                AttachmentType attachmentType = ((ItemAttachment) itemStack.getItem()).type;
                String bindding = "gunModel";
                if(model.config.attachmentGroup.containsKey(attachment.typeName)) {
                    if (model.config.attachmentGroup.get(attachment.typeName).hidePart != null) {
                        exceptParts.addAll(model.config.attachmentGroup.get(attachment.typeName).hidePart);
                    }
                }
                if (model.config.attachment.containsKey(attachmentType.internalName)) {
                    if (model.config.attachment.get(attachmentType.internalName).hidePart != null) {
                        exceptParts.addAll(model.config.attachment.get(attachmentType.internalName).hidePart);
                    }
                }
            }
        }
        
        for (AttachmentEnum attachment : AttachmentEnum.values()) {
            ItemStack itemStack = GunType.getAttachment(item, attachment);
            if (itemStack != null && itemStack.getItem() != Items.AIR) {
                AttachmentType attachmentType = ((ItemAttachment) itemStack.getItem()).type;
                String bindding = "gunModel";
                if(model.config.attachmentGroup.containsKey(attachment.typeName)) {
                    if (model.config.attachmentGroup.get(attachment.typeName).showPart != null) {
                        exceptParts.removeAll(model.config.attachmentGroup.get(attachment.typeName).showPart);
                    }
                }
                if (model.config.attachment.containsKey(attachmentType.internalName)) {
                    if (model.config.attachment.get(attachmentType.internalName).showPart != null) {
                        exceptParts.removeAll(model.config.attachment.get(attachmentType.internalName).showPart);
                    }
                }
            }
        }
        
        exceptParts.addAll(DEFAULT_EXCEPT);
        
        HashSet<String> exceptPartsRendering=exceptParts;
        
        /**
         * LEFT HAND GROUP
         * */
        
        model.updateAnimation(controller.getTime());
        
        applySprintHandTransform(model, controller.getTime(), controller.getSprintTime(),(float)AnimationController.SPRINT, "sprint_lefthand", applySprint, () -> {
            /**
             * player left hand
             * */
            bindPlayerSkin();
            if(!Minecraft.getMinecraft().player.getSkinType().equals("slim")) {
                model.renderPart(LEFT_HAND_PART);  
            }else {
                model.renderPart(LEFT_SLIM_HAND_PART);  
            }
        });
        
        /**
         * RIGHT HAND GROUP
         * */
        applySprintHandTransform(model, controller.getTime(), controller.getSprintTime(),(float)AnimationController.SPRINT, "sprint_righthand", applySprint, () -> {
            /**
             * player right hand
             * */
            bindPlayerSkin();
            if(!Minecraft.getMinecraft().player.getSkinType().equals("slim")) {
                model.renderPart(RIGHT_HAND_PART);  
            }else {
                model.renderPart(RIGHT_SLIM_HAND_PART);  
            }
            
            
            
            /**
             * gun
             * */
            int skinId = 0;
            if (item.hasTagCompound()) {
                if (item.getTagCompound().hasKey("skinId")) {
                    skinId = item.getTagCompound().getInteger("skinId");
                }
            }
            String gunPath = skinId > 0 ? gunType.modelSkins[skinId].getSkin() : gunType.modelSkins[0].getSkin();
            bindTexture("guns", gunPath);
            model.renderPartExcept(exceptPartsRendering);
            //model.renderPart(controller.getTime(),"flashModel", "gunModel");
            
            /**
             * selecotr
             * */
            WeaponFireMode fireMode = GunType.getFireMode(item);
            if(fireMode==WeaponFireMode.SEMI) {
                model.renderPart("selector_semi");
            }else if(fireMode==WeaponFireMode.FULL) {
                model.renderPart("selector_full");
            }else if(fireMode==WeaponFireMode.BURST){
                model.renderPart("selector_brust");
            }
           
            
            /**
             * ammo and bullet
             * */
            boolean flagDynamicAmmoRendered=false;
            ItemStack stackAmmo = new ItemStack(item.getTagCompound().getCompoundTag("ammo"));
            ItemStack orignalAmmo = stackAmmo;
            stackAmmo=controller.getRenderAmmo(stackAmmo);
            ItemStack renderAmmo=stackAmmo;
            ItemStack prognosisAmmo=ClientTickHandler.reloadEnhancedPrognosisAmmoRendering;
            
            ItemStack bulletStack=ItemStack.EMPTY;
            int currentAmmoCount=0;
            
            VarBoolean defaultBulletFlag=new VarBoolean();
            defaultBulletFlag.b=true;
            boolean defaultAmmoFlag=true;
            
            if (gunType.acceptedBullets != null) {
                currentAmmoCount= item.getTagCompound().getInteger("ammocount");
                if (anim.reloading) {
                    currentAmmoCount += anim.getAmmoCountOffset();
                }
                bulletStack= new ItemStack(item.getTagCompound().getCompoundTag("bullet"));
                if (anim.reloading) {
                    bulletStack = ClientProxy.gunEnhancedRenderer.controller.getRenderAmmo(bulletStack);
                }
            }else {
                Integer currentMagcount=null;
                if(stackAmmo!=null&&!stackAmmo.isEmpty()&&stackAmmo.hasTagCompound()) {
                    if(stackAmmo.getTagCompound().hasKey("magcount")) {
                        currentMagcount=stackAmmo.getTagCompound().getInteger("magcount");
                    }
                    currentAmmoCount=ReloadHelper.getBulletOnMag(stackAmmo, currentMagcount);
                    bulletStack= new ItemStack(stackAmmo.getTagCompound().getCompoundTag("bullet"));  
                }
            }
            int currentAmmoCountRendering=currentAmmoCount;
            
            if (bulletStack != null) {
                if (bulletStack.getItem() instanceof ItemBullet) {
                    BulletType bulletType = ((ItemBullet) bulletStack.getItem()).type;
                    if (bulletType.isDynamicBullet && bulletType.model != null) {
                        int skinIdBullet = 0;
                        if (bulletStack.hasTagCompound()) {
                            if (bulletStack.getTagCompound().hasKey("skinId")) {
                                skinIdBullet = bulletStack.getTagCompound().getInteger("skinId");
                            }
                        }
                        if (bulletType.sameTextureAsGun) {
                            bindTexture("guns", gunPath);
                        } else {
                            String pathAmmo = skinIdBullet > 0 ? bulletType.modelSkins[skinIdBullet].getSkin()
                                    : bulletType.modelSkins[0].getSkin();
                            bindTexture("bullets", pathAmmo);
                        }
                        for (int bullet = 0; bullet < currentAmmoCount && bullet < BULLET_MAX_RENDER; bullet++) {
                            int renderBullet=bullet;
                            model.applyGobalTransform("bulletModel_" + bullet, () -> {
                                renderAttachment(model.config, "bullet", bulletType.internalName, () -> {
                                    bulletType.model.renderPart("bulletModel", worldScale);
                                });
                            });
                        }
                        model.applyGobalTransform("bulletModel", () -> {
                            renderAttachment(model.config, "bullet", bulletType.internalName, () -> {
                                bulletType.model.renderPart("bulletModel", worldScale);
                            });
                        });
                        defaultBulletFlag.b=false;
                    }
                }
            }
            
            if (stackAmmo.getItem() instanceof ItemAmmo) {
                ItemAmmo itemAmmo = (ItemAmmo) stackAmmo.getItem();
                AmmoType ammoType = itemAmmo.type;
                if (ammoType.isDynamicAmmo && ammoType.model != null) {
                    int skinIdAmmo = 0;
                    int baseAmmoCount=0;

                    if (stackAmmo.hasTagCompound()) {
                        if (stackAmmo.getTagCompound().hasKey("skinId")) {
                            skinIdAmmo = stackAmmo.getTagCompound().getInteger("skinId");
                        }
                        if(stackAmmo.getTagCompound().hasKey("magcount")) {
                            baseAmmoCount=(stackAmmo.getTagCompound().getInteger("magcount")-1)*ammoType.ammoCapacity;
                        }
                    }
                    int baseAmmoCountRendering=baseAmmoCount;
                    
                    if (ammoType.sameTextureAsGun) {
                        bindTexture("guns", gunPath);
                    } else {
                        String pathAmmo = skinIdAmmo > 0 ? ammoType.modelSkins[skinIdAmmo].getSkin() : ammoType.modelSkins[0].getSkin();
                        bindTexture("ammo", pathAmmo);
                    }
                    
                    if (controller.shouldRenderAmmo()) {
                        model.applyGobalTransform("ammoModel", () -> {
                            GlStateManager.pushMatrix();
                            if(renderAmmo.getTagCompound().hasKey("magcount")) {
                                if(model.config.attachment.containsKey(itemAmmo.type.internalName)) {
                                    if(model.config.attachment.get(itemAmmo.type.internalName).multiMagazineTransform!=null) {
                                        if(renderAmmo.getTagCompound().getInteger("magcount")<=model.config.attachment.get(itemAmmo.type.internalName).multiMagazineTransform.size()) {
                                            //be careful, don't mod the config
                                            Transform ammoTransform=model.config.attachment.get(itemAmmo.type.internalName).multiMagazineTransform.get(renderAmmo.getTagCompound().getInteger("magcount")-1);      
                                            Transform renderTransform=ammoTransform;
                                            if (anim.reloading && (anim
                                                    .getReloadAnimationType() == AnimationType.RELOAD_FIRST_QUICKLY)) {
                                                float magAlpha = (float) controller.RELOAD;
                                                renderTransform=new Transform();
                                                ammoTransform=model.config.attachment.get(itemAmmo.type.internalName).multiMagazineTransform.get(prognosisAmmo.getTagCompound().getInteger("magcount")-1);    
                                                Transform beginTransform=model.config.attachment.get(itemAmmo.type.internalName).multiMagazineTransform.get(orignalAmmo.getTagCompound().getInteger("magcount")-1);
                                                
                                                renderTransform.translate.x = beginTransform.translate.x
                                                        + (ammoTransform.translate.x - beginTransform.translate.x)
                                                                * magAlpha;
                                                renderTransform.translate.y = beginTransform.translate.y
                                                        + (ammoTransform.translate.y - beginTransform.translate.y)
                                                                * magAlpha;
                                                renderTransform.translate.z = beginTransform.translate.z
                                                        + (ammoTransform.translate.z - beginTransform.translate.z)
                                                                * magAlpha;
                                                
                                                renderTransform.rotate.x = beginTransform.rotate.x
                                                        + (ammoTransform.rotate.x - beginTransform.rotate.x)
                                                                * magAlpha;
                                                renderTransform.rotate.y = beginTransform.rotate.y
                                                        + (ammoTransform.rotate.y - beginTransform.rotate.y)
                                                                * magAlpha;
                                                renderTransform.rotate.z = beginTransform.rotate.z
                                                        + (ammoTransform.rotate.z - beginTransform.rotate.z)
                                                                * magAlpha;
                                                
                                                renderTransform.scale.x = beginTransform.scale.x
                                                        + (ammoTransform.scale.x - beginTransform.scale.x)
                                                                * magAlpha;
                                                renderTransform.scale.y = beginTransform.scale.y
                                                        + (ammoTransform.scale.y - beginTransform.scale.y)
                                                                * magAlpha;
                                                renderTransform.scale.z = beginTransform.scale.z
                                                        + (ammoTransform.scale.z - beginTransform.scale.z)
                                                                * magAlpha;
                                            }
                                            GlStateManager.translate(renderTransform.translate.x,
                                                    renderTransform.translate.y, renderTransform.translate.z);
                                            GlStateManager.scale(renderTransform.scale.x, renderTransform.scale.y,
                                                    renderTransform.scale.z);
                                            GlStateManager.rotate(renderTransform.rotate.y, 0, 1, 0);
                                            GlStateManager.rotate(renderTransform.rotate.x, 1, 0, 0);
                                            GlStateManager.rotate(renderTransform.rotate.z, 0, 0, 1);
                                        }
                                    }
                                }
                            }
                            renderAttachment(model.config, "ammo", ammoType.internalName, () -> {
                                ammoType.model.renderPart("ammoModel", worldScale);
                                if(defaultBulletFlag.b) {
                                    if(renderAmmo.getTagCompound().hasKey("magcount")) {
                                        for(int i=1;i<=ammoType.magazineCount;i++) {
                                            int count=ReloadHelper.getBulletOnMag(renderAmmo, i);
                                            for (int bullet = 0; bullet < count && bullet < BULLET_MAX_RENDER; bullet++) {
                                                //System.out.println((ammoType.ammoCapacity*(i-1))+bullet);
                                                ammoType.model.renderPart("bulletModel_" + ((ammoType.ammoCapacity*(i-1))+bullet), worldScale);
                                            }  
                                        }
                                    }else {
                                        for (int bullet = 0; bullet < currentAmmoCountRendering && bullet < BULLET_MAX_RENDER; bullet++) {
                                            ammoType.model.renderPart("bulletModel_" + (baseAmmoCountRendering+bullet), worldScale);
                                        }  
                                    }

                                    defaultBulletFlag.b = false;
                                }
                            });
                            GlStateManager.popMatrix();
                        });
                        model.applyGobalTransform("bulletModel", () -> {
                            renderAttachment(model.config, "bullet", ammoType.internalName, () -> {
                                ammoType.model.renderPart("bulletModel", worldScale);
                            });
                        });
                        flagDynamicAmmoRendered=true;
                        defaultAmmoFlag=false;
                    }
                }
           }
            
            /**
             * default bullet and ammo
             * */
            
            bindTexture("guns", gunPath);
            
            if(defaultBulletFlag.b) {
                for (int bullet = 0; bullet < currentAmmoCount && bullet < BULLET_MAX_RENDER; bullet++) {
                    model.renderPart("bulletModel_" + bullet);
                }  
                model.renderPart("bulletModel");
            }
            
            if (controller.shouldRenderAmmo() && defaultAmmoFlag) {
                model.renderPart("ammoModel");
            }

            
            /**
             * attachment
             * */
            
            for (AttachmentEnum attachment : AttachmentEnum.values()) {
                ItemStack itemStack = GunType.getAttachment(item, attachment);
                if (itemStack != null && itemStack.getItem() != Items.AIR) {
                    AttachmentType attachmentType = ((ItemAttachment) itemStack.getItem()).type;
                    ModelAttachment attachmentModel = (ModelAttachment) attachmentType.model;
                    if (attachmentModel != null) {
                        String bindding = "gunModel";
                        if (model.config.attachment.containsKey(attachmentType.internalName)) {
                            bindding = model.config.attachment.get(attachmentType.internalName).bindding;
                        }
                        model.applyGobalTransform(bindding, () -> {
                            if (attachmentType.sameTextureAsGun) {
                                bindTexture("guns", gunPath);
                            } else {
                                int attachmentsSkinId = 0;
                                if (itemStack.hasTagCompound()) {
                                    if (itemStack.getTagCompound().hasKey("skinId")) {
                                        attachmentsSkinId = itemStack.getTagCompound().getInteger("skinId");
                                    }
                                }
                                String attachmentsPath = attachmentsSkinId > 0 ? attachmentType.modelSkins[attachmentsSkinId].getSkin()
                                        : attachmentType.modelSkins[0].getSkin();
                                bindTexture("attachments", attachmentsPath);
                            }
                            renderAttachment(model.config, attachment.typeName, attachmentType.internalName, () -> {
                                attachmentModel.renderAttachment(worldScale);
                                if(attachment==AttachmentEnum.Sight) {
                                    renderScopeGlass(attachmentType, attachmentModel, controller.ADS > 0,rotateXRendering, worldScale);
                                }
                            });
                        });
                    }
                    
                    if (attachment == AttachmentEnum.Sight) {
                        WeaponScopeType scopeType = attachmentType.sight.scopeType;
                        if (scopeType != WeaponScopeType.DEFAULT) {
                            if (controller.ADS == 1) {
                                if (!ClientRenderHooks.isAimingScope) {
                                    ClientRenderHooks.isAimingScope = true;
                                    ModularWarfare.NETWORK
                                            .sendToServer(new PacketAimingRequest(player.getDisplayNameString(), true));
                                }
                            } else {
                                if (ClientRenderHooks.isAimingScope) {
                                    ClientRenderHooks.isAimingScope = false;
                                    ModularWarfare.NETWORK.sendToServer(
                                            new PacketAimingRequest(player.getDisplayNameString(), false));
                                }
                            }
                        } else {
                            if (adsSwitch == 1.0F) {
                                if (!ClientRenderHooks.isAiming) {
                                    ClientRenderHooks.isAiming = true;
                                    ModularWarfare.NETWORK
                                            .sendToServer(new PacketAimingRequest(player.getDisplayNameString(), true));
                                }
                            } else {
                                if (ClientRenderHooks.isAiming) {
                                    ClientRenderHooks.isAiming = false;
                                    ModularWarfare.NETWORK.sendToServer(
                                            new PacketAimingRequest(player.getDisplayNameString(), false));
                                }
                            }
                        }

                    }
                }
            }
            /**
             *  flashmodel 
             *  */
            if (anim.shooting && anim.getShootingAnimationType() == AnimationType.FIRE && !player.isInWater()) {
                GlStateManager.disableLighting();
                float bx = OpenGlHelper.lastBrightnessX;
                float by = OpenGlHelper.lastBrightnessY;
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
                TextureType flashType = gunType.flashType;
                bindTexture(flashType.resourceLocations.get(anim.flashCount % flashType.resourceLocations.size()));
                model.renderPart("flashModel");
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, bx, by);
                GlStateManager.enableLighting();
            }
            
        });

        
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
    }
    
    @SideOnly(Side.CLIENT)
    public void renderScopeGlass(AttachmentType attachmentType, ModelAttachment modelAttachment, boolean isAiming, float rotateX,float worldScale) {
        if (attachmentType.sight.scopeType != WeaponScopeType.REDDOT) {

            if (Minecraft.getMinecraft().world != null) {
                float gunRotX = RenderParameters.GUN_ROT_X_LAST + (RenderParameters.GUN_ROT_X - RenderParameters.GUN_ROT_X_LAST) * this.timer.renderPartialTicks;
                if (isAiming&&(ClientProxy.scopeUtils.blurFramebuffer!=null||!ModConfig.INSTANCE.hud.ads_blur)) {
                    Minecraft mc=Minecraft.getMinecraft();
                    boolean blurFlag=false;
                    if(!OptifineHelper.isShadersEnabled()&&ModConfig.INSTANCE.hud.ads_blur) {
                        blurFlag=true;
                    }
                    if(blurFlag) {
                        ClientProxy.scopeUtils.blurFramebuffer.framebufferClear();
                        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mc.getFramebuffer().framebufferObject);
                        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, ClientProxy.scopeUtils.blurFramebuffer.framebufferObject);
                        GL30.glBlitFramebuffer(0, 0, mc.displayWidth, mc.displayHeight, 0, 0, mc.displayWidth, mc.displayHeight, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
                        ClientProxy.scopeUtils.blurFramebuffer.bindFramebuffer(false);  
                    }
                    GL11.glPushMatrix();
                    renderWorldOntoScope(attachmentType, modelAttachment,rotateX,worldScale,false);

                    /** Render Overlay when moving too fast **/
                    float alpha = 1f;
                    if (adsSwitch < 1.0f) {
                        alpha = 1 - adsSwitch;
                    } else {
                        alpha = gunRotX;
                        alpha = Math.abs(alpha / 8);
                    }

                    GlStateManager.disableLighting();
                    if(blurFlag) {
                    GlStateManager.colorMask(true, true, true, false);
                    }
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                    GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
                    renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/black.png"));
                    modelAttachment.renderOverlay(worldScale);
                    GlStateManager.disableBlend();
                    GlStateManager.colorMask(true, true, true, true);
                    GlStateManager.depthMask(true);
                    GlStateManager.enableLighting();
                    GL11.glPopMatrix();
                    if(blurFlag) {
                        mc.getFramebuffer().bindFramebuffer(false);  
                    }
                } else {
                    GL11.glPushMatrix();
                    renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "textures/skins/black.png"));
                    modelAttachment.renderOverlay(worldScale);
                    GL11.glPopMatrix();
                }
            }
        }
    }


    @SideOnly(Side.CLIENT)
    private void renderWorldOntoScope(AttachmentType type, ModelAttachment modelAttachment,float rotateX,float worldScale,boolean isLightOn) {
        GL11.glPushMatrix();

        if (isLightOn) {
            GlStateManager.bindTexture(ScopeUtils.MIRROR_TEX);
            GL11.glDisable(2896);
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
            ModelGun.glowOn(1);
            modelAttachment.renderScope(worldScale);
            ModelGun.glowOff();
            GL11.glEnable(2896);
            Minecraft.getMinecraft().entityRenderer.enableLightmap();
        } else {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.5, 0.5, 0);
            GlStateManager.rotate(-rotateX,0,0,1);
            GlStateManager.translate(-0.5, -0.5, 0);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            
            GlStateManager.bindTexture(ScopeUtils.MIRROR_TEX);
            ModelGun.glowOn(1);
            modelAttachment.renderScope(worldScale);
            ModelGun.glowOff();
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && mc.gameSettings.thirdPersonView == 0) {
            if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                final ItemStack gunStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                if (GunType.getAttachment(gunStack, AttachmentEnum.Flashlight) != null) {
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
                        modelAttachment.renderOverlay(worldScale);

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
    
    public void renderAttachment(GunEnhancedRenderConfig config,String type,String name,Runnable run) {
        if (config.attachment.containsKey(name)) {
            applyTransform(config.attachment.get(name));
        } else if (config.attachmentGroup.containsKey(type)) {
            applyTransform(config.attachmentGroup.get(type));
        }
        run.run();
    }
    
    public void applyTransform(Transform transform) {
        GlStateManager.translate(transform.translate.x,transform.translate.y,transform.translate.z);
        GlStateManager.scale(transform.scale.x,transform.scale.y,transform.scale.z);
        GlStateManager.rotate(transform.rotate.y, 0,1,0);
        GlStateManager.rotate(transform.rotate.x, 1,0,0);
        GlStateManager.rotate(transform.rotate.z, 0,0,1);
    }
    
    public void applySprintHandTransform(EnhancedModel model,float time,float sprintTime,float alpha,String hand,boolean applySprint,Runnable runnable) {
        if(!applySprint) {
            runnable.run();
            return;
        }
        IProcessNodeModelHandler handler=(IProcessNodeModelHandler) model.model;
        
        model.updateAnimation(sprintTime);
        float[] end_transform=handler.getGlobalTransform(model.getPart(hand));
        
        //updateAnimation current time
        model.updateAnimation(time);
        float[] begin_transform=handler.getGlobalTransform(model.getPart(hand));
        
        Matrix3f begin_rot_matrix=new Matrix3f();
        Matrix3f end_rot_matrix=new Matrix3f();
        genMatrix(begin_rot_matrix, begin_transform);
        genMatrix(end_rot_matrix, end_transform);
        Quaternion begin_quat = new Quaternion();
        begin_quat=Quaternion.setFromMatrix(begin_rot_matrix, begin_quat);
        Quaternion end_quat = new Quaternion();
        end_quat=Quaternion.setFromMatrix(end_rot_matrix, end_quat);
        Quaternion in_quat=interpolationRot(begin_quat, end_quat, alpha);
        Vector3f in_pos=new Vector3f(0,0,0);
        in_pos.x=begin_transform[12]+(end_transform[12]-begin_transform[12])*alpha;
        in_pos.y=begin_transform[13]+(end_transform[13]-begin_transform[13])*alpha;
        in_pos.z=begin_transform[14]+(end_transform[14]-begin_transform[14])*alpha;
        Matrix3f original_matrix=genMatrixFromQuaternion(in_quat);
        in_quat=in_quat.normalise(null);
        Matrix3f base_matrix=genMatrixFromQuaternion(in_quat);
        Matrix3f scale_matrix=Matrix3f.mul(original_matrix, Matrix3f.invert(base_matrix, null), null);
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(in_pos.x,in_pos.y,in_pos.z);
        GlStateManager.scale(scale_matrix.m00, scale_matrix.m11, scale_matrix.m22);
        GlStateManager.rotate(in_quat);
        model.applyGobalInverseTransform(hand, () -> {
            runnable.run();
        });
        GlStateManager.popMatrix();
    }
    
    private Matrix3f genMatrixFromQuaternion(Quaternion quaternion) {
        Matrix3f matrix3f=new Matrix3f();
        matrix3f.m00 = 1 - 2 * quaternion.y * quaternion.y - 2 * quaternion.z * quaternion.z;
        matrix3f.m01 = 2 * quaternion.x * quaternion.y + 2 * quaternion.w * quaternion.z;
        matrix3f.m02 = 2 * quaternion.x * quaternion.z - 2 * quaternion.w * quaternion.y;

        matrix3f.m10 = 2 * quaternion.x * quaternion.y - 2 * quaternion.w * quaternion.z;
        matrix3f.m11 = 1 - 2 * quaternion.x * quaternion.x - 2 * quaternion.z * quaternion.z;
        matrix3f.m12 = 2 * quaternion.y * quaternion.z + 2 * quaternion.w * quaternion.x;

        matrix3f.m20 = 2 * quaternion.x * quaternion.z + 2 * quaternion.w * quaternion.y;
        matrix3f.m21 = 2 * quaternion.y * quaternion.z - 2 * quaternion.w * quaternion.x;
        matrix3f.m22 = 1 - 2 * quaternion.x * quaternion.x - 2 * quaternion.y * quaternion.y;
        return matrix3f;
    }
    
    //4x4 floats
    private void genMatrix(Matrix3f m,float[] floats) {
        m.m00=floats[0];
        m.m01=floats[4];
        m.m02=floats[8];
        
        m.m10=floats[1];
        m.m11=floats[5];
        m.m12=floats[9];
        
        m.m20=floats[2];
        m.m21=floats[6];
        m.m22=floats[10];
    }
    
    public Quaternion interpolationRot(Quaternion q0, Quaternion q1, float t) {
            float theata = (float) Math.acos(Quaternion.dot(q0, q1));
            if (theata >= theata90 || -theata >= theata90) {
                q1.set(-q1.x, -q1.y, -q1.z, -q1.w);
                theata = Quaternion.dot(q0, q1);
            }
            float sinTheata = MathHelper.sin(theata);
            if (sinTheata == 0) {
                return new Quaternion(q0.x + (q1.x - q0.x) * t, q0.y + (q1.y - q0.y) * t, q0.z + (q1.z - q0.z) * t,
                        q0.w + (q1.w - q0.w) * t);
            }
            float c1 = (float) (MathHelper.sin(theata * (1 - t)) / sinTheata);
            float c2 = (float) (MathHelper.sin(theata * t) / sinTheata);
            return new Quaternion(c1 * q0.x + c2 * q1.x, c1 * q0.y + c2 * q1.y, c1 * q0.z + c2 * q1.z,
                    c1 * q0.w + c2 * q1.w);
    }
    
    public boolean onGltfRenderCallback(String part) {
        return false;
    }


    public static float toRadians(float angdeg) {
        return angdeg / 180.0f * PI;
    }
    
    public void color(float r,float g,float b,float a) {
        this.r=r;
        this.g=g;
        this.b=b;
        this.a=a;
    }

    @Override
    public void bindTexture(String type, String fileName) {
        super.bindTexture(type, fileName);
        String pathFormat = "skins/%s/%s.png";
        bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, String.format(pathFormat, type, fileName)));
    }

    public void bindTexture(ResourceLocation location) {
        bindingTexture = location;
    }

    public void bindPlayerSkin() {
        bindingTexture = Minecraft.getMinecraft().player.getLocationSkin();
    }
}
