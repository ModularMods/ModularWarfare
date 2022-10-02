package com.modularwarfare.common.type;

import com.modularwarfare.ModularWarfare;
import net.minecraft.item.Item;
import net.minecraft.util.text.TextFormatting;

public class BaseItem extends Item {

    public BaseType baseType;
    public boolean render3d = true;

    public BaseItem(BaseType type) {
        setUnlocalizedName(type.internalName);
        setRegistryName(type.internalName);
        setCreativeTab(ModularWarfare.MODS_TABS.get(type.contentPack));

        this.baseType = type;
        if(type.maxStackSize != null) {
            this.setMaxStackSize(type.maxStackSize);
        } else {
            this.setMaxStackSize(1);
        }
        this.canRepair = false;
    }

    public void setType(BaseType type) {

    }

    public String generateLoreLine(String prefix, String value) {
        String baseDisplayLine = "%b%s: %g%s";
        baseDisplayLine = baseDisplayLine.replaceAll("%b", TextFormatting.BLUE.toString());
        baseDisplayLine = baseDisplayLine.replaceAll("%g", TextFormatting.GRAY.toString());
        return String.format(baseDisplayLine, prefix, value);
    }

    public String generateLoreHeader(String prefix) {
        String baseDisplayLine = "%b%s";
        baseDisplayLine = baseDisplayLine.replaceAll("%b", TextFormatting.BLUE.toString());
        return String.format(baseDisplayLine, prefix);
    }

    public String generateLoreListEntry(String prefix, String value) {
        String baseDisplayLine = " - %s %g%s";
        baseDisplayLine = baseDisplayLine.replaceAll("%b", TextFormatting.BLUE.toString());
        baseDisplayLine = baseDisplayLine.replaceAll("%g", TextFormatting.GRAY.toString());
        return String.format(baseDisplayLine, value, prefix);
    }

    public String generateLoreLineAlt(String prefix, String current, String max) {
        String baseDisplayLine = "%b%s: %g%s%dg/%g%s";
        baseDisplayLine = baseDisplayLine.replaceAll("%b", TextFormatting.BLUE.toString());
        baseDisplayLine = baseDisplayLine.replaceAll("%g", TextFormatting.GRAY.toString());
        baseDisplayLine = baseDisplayLine.replaceAll("%dg", TextFormatting.DARK_GRAY.toString());
        return String.format(baseDisplayLine, prefix, current, max);
    }

}
