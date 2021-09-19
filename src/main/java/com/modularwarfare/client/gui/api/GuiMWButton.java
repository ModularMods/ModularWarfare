package com.modularwarfare.client.gui.api;

import com.modularwarfare.common.init.ModSounds;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class GuiMWButton extends GuiButton {

    public int isOver = 2;
    public boolean drawBackground = false;
    public boolean drawShadow = true;
    public int buttonColor = 0x0;
    public boolean centeredText = true;
    public boolean soundPlayed = true;
    public int colorText = 0xFFFF5555;
    private ResourceLocation iconTexture = null;
    private float fade = 0;
    private int xMovement;
    private boolean animationStarted = true;
    private int toolTipY;
    private String toolTip;
    private boolean showToolTip = false;

    public int colorTheme = 0x55000000;

    public GuiMWButton(int buttonId, int x, int y, int givenWidth, int givenHeight, String givenText) {
        this(buttonId, x, y, givenText);
        this.width = givenWidth;
        this.height = givenHeight;
    }

    public GuiMWButton(int buttonId, int x, int y, int givenWidth, int givenHeight, String givenText, int colorText) {
        this(buttonId, x, y, givenText);
        this.width = givenWidth;
        this.height = givenHeight;
        this.colorText = colorText;
    }

    public GuiMWButton(int buttonID, int x, int y, ResourceLocation iconTexture) {
        this(buttonID, x, y, 30, 20, "");
        this.iconTexture = iconTexture;
    }

    public GuiMWButton(int id, int x, int y, int width, int height, String displayString, String givenToolTip, Color givenColor, ResourceLocation iconTexture) {
        this(id, x, y, width, height, displayString);
        this.toolTip = givenToolTip;
        this.showToolTip = true;
        this.iconTexture = iconTexture;
    }

    public GuiMWButton(int id, int x, int y, int width, int height, String displayString, ResourceLocation iconTexture) {
        this(id, x, y, width, height, displayString);
        this.iconTexture = iconTexture;
    }

    public GuiMWButton(int id, int x, int y, String par6, int buttonColor, String givenToolTip) {
        this(id, x, y, 69, 20, par6);
        this.buttonColor = buttonColor;
        this.toolTip = givenToolTip;
        this.showToolTip = true;
    }

    public GuiMWButton(int id, int x, int y, int width, int height, String displayString, String givenToolTip, int givenColor) {
        this(id, x, y, width, height, displayString);
        this.toolTip = givenToolTip;
        this.showToolTip = true;
        this.buttonColor = givenColor;
    }

    public GuiMWButton(int buttonId, int x, int y, String givenText) {
        super(buttonId, x, y, givenText);
    }

    public GuiMWButton addToolTip(String givenToolTip) {
        this.showToolTip = true;
        this.toolTip = givenToolTip;
        return this;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {

        GlStateManager.pushMatrix();

        if (visible) {

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            isOver = getHoverState(hovered);

            if (drawBackground) {
                if (drawShadow) {
                    GuiUtils.renderRectWithOutline(x, y, width, height, colorTheme, colorTheme, 1);
                } else {
                    GuiUtils.renderRect(x, y, width, height, buttonColor);
                }
            }
            mouseDragged(mc, mouseX, mouseY);
            String displayText = displayString;
            if (isOver == 2) {

                if (!soundPlayed) {
                    //Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(ModSounds.CLICK, 1.0F));
                    soundPlayed = true;
                }

                if (!animationStarted) {
                    animationStarted = true;
                }

                if (fade <= 0) {
                    fade = 0;
                } else {
                    fade -= .2;
                }

                if (xMovement < 3) {
                    xMovement++;
                }

                GlStateManager.pushMatrix();
                //GuiUtils.renderRectWithFade(x, y, width, height, Reference.colorTheme - 0x22000000, fade);
                GlStateManager.popMatrix();

            } else {
                toolTipY = 0;
                fade = 1;
                soundPlayed = false;
                animationStarted = false;
                if (xMovement > 0) {
                    xMovement--;
                }
            }
            if (iconTexture != null) {
                if (isOver == 2) {
                    GuiUtils.renderColor(0x999999);
                } else {
                    GuiUtils.renderColor(0xFFFFFF);
                }
                GuiUtils.renderImage(x + width / 2 - 8, y + (height - 8) - 10, iconTexture, 16.0D, 16.0D);
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
            }
            if (!enabled) {
                displayText = ChatFormatting.GRAY + displayText;
            }
            if (!centeredText) {
                GuiUtils.renderTextWithShadow(displayText, x + 2, y + (height - 8) / 2, isOver == 2 ? 0xFFFFFFFF : 0xFFFFFFFF);
                return;
            }

            if (showToolTip && isOver == 2) {
                int toolTipWidth = mc.fontRenderer.getStringWidth(this.toolTip);
                GuiUtils.renderRectWithOutline(mouseX, mouseY - 10, toolTipY, 10, colorTheme, colorTheme, 1);
                if (toolTipY < (toolTipWidth + 2)) {

                    int toolTipGap = (toolTipWidth + 2) - toolTipY;

                    if (toolTipGap >= 10) {
                        toolTipY = toolTipY + 10;
                    } else {
                        toolTipY = toolTipY + 1;
                    }

                } else if (toolTipY > (toolTipWidth + 2)) {
                    toolTipY--;
                }

                if (toolTipY >= (toolTipWidth + 2)) {
                    GuiUtils.renderText(this.toolTip, mouseX + 1, mouseY - 9, 0xFFFFFF);
                }

            }

            GuiUtils.renderCenteredTextWithShadow(displayText, (x + width / 2) + xMovement, y + (height - 8) / 2, isOver == 2 ? 0xFFFFFFFF : colorText, 0x000000);
        }

        GlStateManager.popMatrix();

    }

}