package com.modularwarfare.api;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IMWModel {
    @SideOnly(Side.CLIENT)
    public void renderPart(String part,float scale);
}
