package com.modularwarfare.client.gui.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.util.List;
import java.util.function.Consumer;

public class Buttons {

    public static void draw(List<GuiButton> buttons, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        for (GuiButton button : buttons) {
            button.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }

    public static void click(List<GuiButton> buttons, int mouseX, int mouseY, Consumer<GuiButton> clickHandler) {
        Minecraft mc = Minecraft.getMinecraft();
        for (GuiButton button : buttons) {
            if (button.mousePressed(mc, mouseX, mouseY)) {
                button.playPressSound(mc.getSoundHandler());
                clickHandler.accept(button);
                return;
            }
        }
    }

}

