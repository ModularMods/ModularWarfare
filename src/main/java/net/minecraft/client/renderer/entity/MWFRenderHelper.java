package net.minecraft.client.renderer.entity;

import java.util.List;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;

public class MWFRenderHelper {
    public RenderLivingBase renderLivingBase;

    public MWFRenderHelper(RenderLivingBase renderLivingBase) {
        super();
        this.renderLivingBase = renderLivingBase;
    }

    public List<LayerRenderer> getLayerRenderers() {
        return renderLivingBase.layerRenderers;
    }

    public boolean setBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks, boolean combineTextures)
    {
        return renderLivingBase.setBrightness(entitylivingbaseIn, partialTicks, combineTextures);
    }

    public void unsetBrightness() {
        renderLivingBase.unsetBrightness();
    }
}