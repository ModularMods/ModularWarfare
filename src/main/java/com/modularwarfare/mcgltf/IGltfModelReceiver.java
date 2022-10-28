package com.modularwarfare.mcgltf;

import net.minecraft.util.ResourceLocation;

public interface IGltfModelReceiver {

	ResourceLocation getModelLocation();
	
	void onModelLoaded(RenderedGltfModel renderedModel);
}
