package com.modularwarfare.mcgltf;

public class DefaultMaterialHandler implements IMaterialHandler {
	
	public class TextureInfo {
		public int index;
	}
	
	public TextureInfo baseColorTexture;
	public TextureInfo normalTexture;
	public TextureInfo specularTexture;
	public float[] baseColorFactor = {1.0F, 1.0F, 1.0F, 1.0F};
	public boolean doubleSided;
	
	public Runnable preMeshDrawCommand;

	@Override
	public boolean hasNormalMap() {
		return normalTexture != null;
	}

	@Override
	public Runnable getPreMeshDrawCommand() {
		return preMeshDrawCommand;
	}

}
