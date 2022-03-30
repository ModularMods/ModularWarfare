package com.modularwarfare.client.gui.customization.containers;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.gui.api.GuiMWButton;
import com.modularwarfare.client.gui.api.GuiMWContainer;
import com.modularwarfare.client.gui.api.GuiUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ContainerGunParts extends GuiMWContainer {

    public int colorTheme = 0x55000000;

    public List<String> gunParts = new ArrayList<>();
    public HashMap<String, Boolean> partsSets = new HashMap<>();

    private int buttonWidth = 20;
    private int buttonHeight = 12;

    public ContainerGunParts(int givenID, int givenPosX, int givenPosY, int givenWidth, int givenHeight, GuiScreen givenParentGUI) {
        super(givenID, givenPosX, givenPosY, givenWidth, givenHeight, givenParentGUI);
    }


    public void initGui(){

        //this.gunParts = Arrays.asList("body", "stock", "slide", "sight", "pipe", "muzzle", "magazine", "grip", "front_receiver", "forearm", "back_receiver");
        this.gunParts = Arrays.asList("gunModel", "slideModel", "ammoModel");

        for(int i = 0; i < gunParts.size(); i++){
            this.partsSets.put(gunParts.get(i), true);
        }
        this.partsSets.put("gunModel", true);

        for (int i = 0; i < this.gunParts.size(); i++) {
            String displayName = this.gunParts.get(i).substring(0,1).toUpperCase() + this.gunParts.get(i).substring(1).toLowerCase();
            GuiMWButton button = new GuiMWButton(i, this.posX + this.width/2 - 8, this.posY + 15 * i + 4, buttonWidth, buttonHeight, displayName);

            this.addButton(button);
        }
    }

    public void actionPerformed(GuiButton givenButton) {
        partsSets.put(gunParts.get(givenButton.id), !partsSets.get(gunParts.get(givenButton.id)));
        if(givenButton instanceof GuiMWButton){
            ((GuiMWButton)givenButton).colorText = partsSets.get(gunParts.get(givenButton.id)) ? 0xFFFFFFFF : 0xFFFF5555;
        }
    }

    public void drawBackground() {
        GuiUtils.renderRectWithOutline(this.posX, this.posY, this.width, this.height, colorTheme - 0x22000000, colorTheme - 0x22000000, 1);
    }

}
