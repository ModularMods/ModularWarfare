package com.modularwarfare.client.model.layers;

import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;

public class ResetHiddenModelLayer implements LayerRenderer<EntityPlayer>{
    RenderPlayer renderPlayer;
    public ResetHiddenModelLayer(RenderPlayer renderPlayer) {
        this.renderPlayer=renderPlayer;
    }

    @Override
    public void doRenderLayer(EntityPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount,
            float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        renderPlayer.getMainModel().bipedHead.isHidden = false;
        renderPlayer.getMainModel().bipedBody.isHidden = false;
        renderPlayer.getMainModel().bipedLeftArm.isHidden = false;
        renderPlayer.getMainModel().bipedRightArm.isHidden = false;
        renderPlayer.getMainModel().bipedLeftLeg.isHidden = false;
        renderPlayer.getMainModel().bipedRightLeg.isHidden = false;
        renderPlayer.getMainModel().bipedHead.showModel = true;
        renderPlayer.getMainModel().bipedBody.showModel = true;
        renderPlayer.getMainModel().bipedLeftArm.showModel = true;
        renderPlayer.getMainModel().bipedRightArm.showModel = true;
        renderPlayer.getMainModel().bipedLeftLeg.showModel = true;
        renderPlayer.getMainModel().bipedRightLeg.showModel = true;
    }

    @Override
    public boolean shouldCombineTextures() {
        // TODO Auto-generated method stub
        return false;
    }

}