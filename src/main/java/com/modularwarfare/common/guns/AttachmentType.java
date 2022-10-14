package com.modularwarfare.common.guns;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.configs.AttachmentRenderConfig;
import com.modularwarfare.client.model.ModelAttachment;
import com.modularwarfare.common.textures.TextureEnumType;
import com.modularwarfare.common.textures.TextureType;
import com.modularwarfare.common.type.BaseType;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class AttachmentType extends BaseType {

    public AttachmentPresetEnum attachmentType;

    public Grip grip = new Grip();

    public Barrel barrel = new Barrel();

    public Sight sight = new Sight();
    
    public Stock stock = new Stock();

    public boolean sameTextureAsGun = false;
    


    @Override
    public void loadExtraValues() {
        if (maxStackSize == null)
            maxStackSize = 1;

        loadBaseValues();

        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
            if (sight.customOverlayTexture != null) {
                if (ModularWarfare.textureTypes.containsKey(sight.customOverlayTexture)) {
                    sight.overlayType = ModularWarfare.textureTypes.get(sight.customOverlayTexture);
                }
            } else {
                sight.overlayType = new TextureType();
                sight.overlayType.initDefaultTextures(TextureEnumType.Overlay);
            }
        }
    }

    @Override
    public void reloadModel() {
        model = new ModelAttachment(ModularWarfare.getRenderConfig(this, AttachmentRenderConfig.class), this);
        ((ModelAttachment)model).config.init();
    }

    @Override
    public String getAssetDir() {
        return "attachments";
    }

    public static class Sight {
        //public WeaponScopeType scopeType = WeaponScopeType.DEFAULT;
        public WeaponDotColorType dotColorType = WeaponDotColorType.RED;
        public WeaponScopeModeType modeType = WeaponScopeModeType.NORMAL;

        public String customOverlayTexture;
        public transient TextureType overlayType;
        public boolean plumbCrossHair = false;
        
        public boolean usedDefaultOverlayModelTexture=true;
    }

    public static class Barrel {
        public boolean isSuppressor;
        public boolean hideFlash;

        public float recoilPitchFactor = 1.0f;
        public float recoilYawFactor = 1.0f;
    }

    public static class Grip {
        public float recoilPitchFactor = 1.0f;
        public float recoilYawFactor = 1.0f;
    }
    
    public static class Stock{
        
    }

}
