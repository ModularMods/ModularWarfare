package com.modularwarfare.common.guns;


import com.google.gson.annotations.SerializedName;

public enum AttachmentEnum {

    @SerializedName("sight") Sight("sight"),
    @SerializedName("slide") Slide("slide"),
    @SerializedName("grip") Grip("grip"),
    @SerializedName("flashlight") Flashlight("flashlight"),
    @SerializedName("charm") Charm("charm"),
    @SerializedName("skin") Skin("skin"),
    @SerializedName("barrel") Barrel("barrel");

    public String typeName;

    AttachmentEnum(String typeName) {
        this.typeName = typeName;
    }

    public static AttachmentEnum getAttachment(String typeName) {
        for (AttachmentEnum attachmentEnum : values()) {
            if (attachmentEnum.typeName.equalsIgnoreCase(typeName)) {
                return attachmentEnum;
            }
        }
        return AttachmentEnum.Sight;
    }

    public String getName() {
        return this.typeName;
    }

}
