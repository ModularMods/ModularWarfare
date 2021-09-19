package com.modularwarfare.api;

import com.modularwarfare.client.model.ModelCustomArmor;
import com.modularwarfare.client.model.ModelCustomArmor.Bones;
import com.modularwarfare.client.model.ModelCustomArmor.Bones.BonePart.EnumBoneType;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RenderMWArmorEvent extends Event {
    public final ModelCustomArmor modelCustomArmor;
    public final EnumBoneType type;
    public final float scale;

    public RenderMWArmorEvent(ModelCustomArmor modelCustomArmor, EnumBoneType type, float scale) {
        this.modelCustomArmor = modelCustomArmor;
        this.type = type;
        this.scale = scale;
    }

    public static class Pre extends RenderMWArmorEvent {

        public Pre(ModelCustomArmor modelCustomArmor, EnumBoneType type, float scale) {
            super(modelCustomArmor, type, scale);
            // TODO Auto-generated constructor stub
        }

    }

    public static class Post extends RenderMWArmorEvent {

        public Post(ModelCustomArmor modelCustomArmor, EnumBoneType type, float scale) {
            super(modelCustomArmor, type, scale);
            // TODO Auto-generated constructor stub
        }

    }

    public static class RotationAngles extends Event {
        public float limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor;
        public Entity entityIn;
        public Bones bones;

        public RotationAngles(Bones bones, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                              float headPitch, float scaleFactor, Entity entityIn) {
            this.bones = bones;
            this.limbSwing = limbSwing;
            this.limbSwingAmount = limbSwingAmount;
            this.ageInTicks = ageInTicks;
            this.netHeadYaw = netHeadYaw;
            this.headPitch = headPitch;
            this.scaleFactor = scaleFactor;
            this.entityIn = entityIn;
        }
    }
}
