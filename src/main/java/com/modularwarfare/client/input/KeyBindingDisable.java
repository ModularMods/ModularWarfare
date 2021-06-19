package com.modularwarfare.client.input;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyBindingDisable extends KeyBinding {

    public KeyBindingDisable(KeyBinding keybinding) {
        super(keybinding.getKeyDescription(), keybinding.getKeyConflictContext(), keybinding.getKeyModifier(), keybinding.getKeyCode(), keybinding.getKeyCategory());
    }

    public boolean isKeyDown() {
        return false;
    }

    public boolean isPressed() {
        return false;
    }
}
