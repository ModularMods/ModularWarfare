package com.modularwarfare.mixin.client;

import com.modularwarfare.client.ClientRenderHooks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

    @Shadow
    public EntityPlayer player;

    @Inject(method = "changeCurrentItem", at = @At("HEAD"), cancellable = true)
    public void inject_changeCurrentItem(CallbackInfo ci) {
        if(ClientRenderHooks.getAnimMachine(player).reloading){
            ci.cancel();
        }
    }

}
