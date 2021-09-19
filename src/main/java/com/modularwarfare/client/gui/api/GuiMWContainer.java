package com.modularwarfare.client.gui.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class GuiMWContainer {

    public List<GuiButton> buttons = new ArrayList<>();
    public int containerID;
    public int posX;
    public int posY;
    public int width;
    public int height;
    public Minecraft mc;
    public GuiScreen parentGUI;
    protected boolean hovered = false;

    public GuiMWContainer(int givenID, int givenPosX, int givenPosY, int givenWidth, int givenHeight, GuiScreen givenParentGUI) {
        this.containerID = givenID;

        this.posX = givenPosX;
        this.posY = givenPosY;
        this.width = givenWidth;
        this.height = givenHeight;

        this.parentGUI = givenParentGUI;

        this.mc = givenParentGUI.mc;

    }

    public void initGui() {
        this.buttons.clear();
    }

    public void updateScreen() {

    }

    public void parentActionPerformed(GuiButton givenButton) {

    }

    public void actionPerformed(GuiButton givenButton) {

    }

    public void handleScroll(int mouseX, int mouseY, int dWheel) {

    }

    public void mouseReleased(int mouseX, int mouseY) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            Buttons.click(buttons, mouseX, mouseY, this::actionPerformed);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        drawButtons(mouseX, mouseY, partialTicks);
        hovered = mouseX >= this.posX && mouseY >= this.posY && mouseX < this.posX + this.width && mouseY < this.posY + this.height;
    }

    public void drawBackground() {
    }

    public void drawButtons(int mouseX, int mouseY, float partialTicks) {
        for (GuiButton button : buttons) {
            GlStateManager.pushMatrix();
            button.drawButton(mc, mouseX, mouseY, partialTicks);
            GlStateManager.popMatrix();
        }
    }

    public void addButton(GuiButton givenButton) {
        this.buttons.add(givenButton);
    }

    public void onClose() {

    }

    public boolean isMouseOver() {
        return this.hovered;
    }

}
