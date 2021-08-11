package com.modularwarfare.common.guns;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.config.AttachmentRenderConfig;
import com.modularwarfare.client.model.ModelAttachment;
import com.modularwarfare.common.type.BaseType;

public class AttachmentType extends BaseType {

    public AttachmentEnum attachmentType;

    public Grip grip = new Grip();

    public Barrel barrel = new Barrel();

    public Sight sight = new Sight();

    @Override
    public void loadExtraValues() {
        if (maxStackSize == null)
            maxStackSize = 1;

        loadBaseValues();
    }

    @Override
    public void reloadModel() {
        model = new ModelAttachment(ModularWarfare.getRenderConfig(this, AttachmentRenderConfig.class), this);
    }

    @Override
    public String getAssetDir() {
        return "attachments";
    }

    public static class Sight {
        public WeaponScopeType scopeType = WeaponScopeType.DEFAULT;
        public WeaponDotColorType dotColorType = WeaponDotColorType.RED;
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

}
