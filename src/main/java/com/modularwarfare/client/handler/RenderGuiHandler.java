package com.modularwarfare.client.handler;

import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.utility.DevGui;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderGuiHandler extends ForgeEvent {

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() != ElementType.EXPERIENCE) return;

        if (Minecraft.getMinecraft().player != null) {
            EntityPlayerSP entityPlayer = Minecraft.getMinecraft().player;
            if (entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
                new DevGui(Minecraft.getMinecraft(), entityPlayer.getHeldItemMainhand(), ((ItemGun) entityPlayer.getHeldItemMainhand().getItem()), ClientProxy.gunStaticRenderer, ClientRenderHooks.getAnimMachine(entityPlayer));
            }
        }
    }

}
