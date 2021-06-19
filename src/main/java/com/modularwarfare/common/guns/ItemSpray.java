package com.modularwarfare.common.guns;

import com.modularwarfare.common.type.BaseItem;

import java.util.function.Function;

public class ItemSpray extends BaseItem {

    public static final Function<SprayType, ItemSpray> factory = type -> {
        return new ItemSpray(type);
    };
    public SprayType type;

    public ItemSpray(SprayType type) {
        super(type);
        this.type = type;
        this.render3d = false;
        this.setMaxDamage(type.usableMaxAmount);
    }


    @Override
    public boolean getShareTag() {
        return true;
    }

}