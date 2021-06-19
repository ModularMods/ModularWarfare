package com.modularwarfare.client.killchat;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.utility.RenderHelperMW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class KillFeedRender {
    private KillFeedManager manager;
    private Minecraft mc;

    public KillFeedRender(final KillFeedManager manager) {
        this.mc = Minecraft.getMinecraft();
        this.manager = manager;
    }

    public static int calculateChatboxHeight(final float scale) {
        return MathHelper.floor(scale * 160.0f + 20.0f);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onTick(final TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            this.manager.getEntries().stream().filter(entry -> entry.incrementTimeLived() > entry.getTimeLiving()).forEach(entry -> this.manager.remove(entry));
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRender(final RenderGameOverlayEvent.Post event) {
        if (this.mc.ingameGUI.getChatGUI().getChatOpen()) {
            return;
        }
        if (this.manager.getEntries().isEmpty()) {
            return;
        }
        GlStateManager.pushMatrix();
        final float scale = 1f;
        GlStateManager.scale(scale, scale, scale);
        final List<KillFeedEntry> entries = this.manager.getEntries().stream().sorted(Comparator.comparingInt(KillFeedEntry::getTimeLived).reversed()).collect(Collectors.toList());
        final int chatHeight = calculateChatboxHeight(this.mc.gameSettings.chatHeightFocused);
        int bottom = event.getResolution().getScaledHeight() - chatHeight - 10;
        final int left = 5;
        final int messagesHeight = entries.size() * this.mc.fontRenderer.FONT_HEIGHT + entries.size();
        bottom -= messagesHeight;
        int messagesWidth = 0;
        for (final KillFeedEntry entry : entries) {
            messagesWidth = (int) Math.max(messagesWidth, this.mc.fontRenderer.getStringWidth(entry.getText()) * scale);
        }

        int msgY = bottom;
        for (final KillFeedEntry entry2 : entries) {
            this.mc.fontRenderer.drawString(entry2.getText(), (int) (left / scale) + 12, (int) (msgY / scale), Integer.MAX_VALUE);
            msgY += this.mc.fontRenderer.FONT_HEIGHT + 1;

            if (entry2.isCausedByGun()) {
                GlStateManager.pushMatrix();
                final float scaleGun = scale * 0.6f;
                GlStateManager.scale(0.6f, 0.6f, 0.6f);
                RenderHelperMW.renderItemStack(new ItemStack(ModularWarfare.gunTypes.get(entry2.getWeaponInternalName())), (int) (left / scaleGun), (int) (msgY / scaleGun) - 17, 0, true);
                GlStateManager.popMatrix();
            }
        }
        GlStateManager.popMatrix();
    }
}
