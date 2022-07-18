package com.modularwarfare;

import com.modularwarfare.client.ClientRenderHooks;
import net.minecraft.client.Minecraft;

public class InjectMethods {

    public static boolean isReloading() {
        return ClientRenderHooks.getAnimMachine(Minecraft.getMinecraft().player).reloading;
    }

}
