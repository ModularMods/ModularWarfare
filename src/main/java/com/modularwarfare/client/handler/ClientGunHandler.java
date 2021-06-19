package com.modularwarfare.client.handler;

import com.modularwarfare.api.WeaponFireEvent;
import com.modularwarfare.api.WeaponReloadEvent;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientGunHandler extends ForgeEvent {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWeaponFire(WeaponFireEvent.Post event) {
    }

    @SubscribeEvent
    public void onWeaponReload(WeaponReloadEvent.Pre event) {

    }

}

