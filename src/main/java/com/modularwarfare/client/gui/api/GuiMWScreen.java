package com.modularwarfare.client.gui.api;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;

public class GuiMWScreen extends GuiScreen {

    protected List<GuiMWContainer> containerList = Lists.newArrayList();

    @Override
    public void initGui() {
        super.initGui();
        this.containerList.clear();
        this.initButtons();
    }

    public void initButtons() {

    }

    public void addContainer(GuiMWContainer container) {
        container.initGui();
        this.containerList.add(container);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        updateContainers();
    }

    public void updateContainers() {
        for (GuiMWContainer gui : this.containerList) {
            gui.updateScreen();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        actionPerformedContainer(button);
    }

    public void actionPerformedContainer(GuiButton guiButton) {
        for (GuiMWContainer gui : this.containerList) {
            gui.parentActionPerformed(guiButton);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawContainers(int mouseX, int mouseY, float partialTicks) {
        for (GuiMWContainer gui : containerList) {
            GlStateManager.pushMatrix();
            gui.drawScreen(mouseX, mouseY, partialTicks);
            GlStateManager.popMatrix();
        }
    }

    public GuiMWContainer getContainer(int containerID) {
        for (GuiMWContainer cont : containerList) {
            if (cont.containerID == containerID) {
                return cont;
            }
        }
        return null;
    }

    public void onGuiClosed() {
        containerList.forEach(GuiMWContainer::onClose);
    }

    @Override
    public void handleMouseInput() {
        try {
            super.handleMouseInput();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            for (GuiMWContainer container : containerList) {
                container.handleScroll(mouseX, mouseY, dWheel);
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (GuiMWContainer container : containerList) {
            container.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int par1, int par2, int par3, long timeSinceLastClick) {
        super.mouseClickMove(par1, par2, par3, timeSinceLastClick);

        if (par3 >= 0) {
            for (GuiMWContainer container : containerList) {
                container.mouseReleased(par1, par2);
            }
        }
    }

}
