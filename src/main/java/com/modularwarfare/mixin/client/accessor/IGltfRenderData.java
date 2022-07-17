package com.modularwarfare.mixin.client.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.timlee9024.mcgltf.GltfRenderData;

@Mixin(value = GltfRenderData.class,priority = 1001)
public interface IGltfRenderData {
    @Invoker("addGlBufferView")
    public void invokeAddGlBufferView(int glBufferView);
}
