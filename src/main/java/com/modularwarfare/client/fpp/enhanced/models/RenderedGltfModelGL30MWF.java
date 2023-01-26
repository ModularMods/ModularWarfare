package com.modularwarfare.client.fpp.enhanced.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.modularmods.mcgltf.RenderedGltfModelGL30;
import com.modularmods.mcgltf.RenderedGltfScene;

import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.MathUtils;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SceneModel;
import de.javagl.jgltf.model.SkinModel;

public class RenderedGltfModelGL30MWF extends RenderedGltfModelGL30 implements IRenderedGltfModelMWF {

	/**
	 * Unlike set scale of node to zero to hide the node itself and its children, this will not affect children node.
	 */
	protected List<Pair<NodeModel, MutableBoolean>> singleNodeVisibleToggles;
	
	public RenderedGltfModelGL30MWF(List<Runnable> gltfRenderData, GltfModel gltfModel) {
		super(gltfRenderData, gltfModel);
	}

	@Override
	public GltfModel getGltfModel() {
		return gltfModel;
	}

	@Override
	public List<RenderedGltfScene> getRenderedGltfScenes() {
		return renderedGltfScenes;
	}

	@Override
	public List<Pair<NodeModel, MutableBoolean>> getSingleNodeVisibleToggles() {
		return singleNodeVisibleToggles;
	}

	@Override
	protected void processSceneModels(List<Runnable> gltfRenderData, List<SceneModel> sceneModels) {
		singleNodeVisibleToggles = new ArrayList<Pair<NodeModel, MutableBoolean>>(); //If the instance is creating at constructor or field, it will cause NPE. Because it will only create after processSceneModels().
		super.processSceneModels(gltfRenderData, sceneModels);
	}

	/**
	 * Copy-paste from parent class, add single node visible toggle.
	 */
	@Override
	protected void processNodeModel(List<Runnable> gltfRenderData, NodeModel nodeModel, List<Runnable> vanillaRenderCommands, List<Runnable> shaderModRenderCommands) {
		MutableBoolean visibleToggle = new MutableBoolean(true);
		singleNodeVisibleToggles.add(Pair.of(nodeModel, visibleToggle));
		
		ArrayList<Runnable> vanillaNodeRenderCommands = new ArrayList<Runnable>();
		ArrayList<Runnable> shaderModNodeRenderCommands = new ArrayList<Runnable>();
		SkinModel skinModel = nodeModel.getSkinModel();
		if(skinModel != null) {
			//TODO: These part of code will need to optimized once MCglTF with SIMD comes out.
			int jointCount = skinModel.getJoints().size();
			
			float[][] transforms = new float[jointCount][];
			float[] invertNodeTransform = new float[16];
			float[] bindShapeMatrix = new float[16];
			
			List<Runnable> jointMatricesTransformCommands = new ArrayList<Runnable>(jointCount);
			for(int joint = 0; joint < jointCount; joint++) {
				int i = joint;
				float[] transform = transforms[i] = new float[16];
				float[] inverseBindMatrix = new float[16];
				jointMatricesTransformCommands.add(() -> {
					MathUtils.mul4x4(invertNodeTransform, transform, transform);
					skinModel.getInverseBindMatrix(i, inverseBindMatrix);
					MathUtils.mul4x4(transform, inverseBindMatrix, transform);
					MathUtils.mul4x4(transform, bindShapeMatrix, transform);
				});
			}
			
			List<Runnable> vanillaSingleNodeRenderCommands = new ArrayList<Runnable>();
			List<Runnable> shaderModSingleNodeRenderCommands = new ArrayList<Runnable>();
			
			Runnable jointMatricesTransformCommand = () -> {
				for(int i = 0; i < transforms.length; i++) {
					System.arraycopy(findGlobalTransform(skinModel.getJoints().get(i)), 0, transforms[i], 0, 16);
				}
				MathUtils.invert4x4(findGlobalTransform(nodeModel), invertNodeTransform);
				skinModel.getBindShapeMatrix(bindShapeMatrix);
				jointMatricesTransformCommands.parallelStream().forEach(Runnable::run);
			};
			vanillaSingleNodeRenderCommands.add(jointMatricesTransformCommand);
			shaderModSingleNodeRenderCommands.add(jointMatricesTransformCommand);
			
			Runnable transformCommand = createTransformCommand(nodeModel);
			vanillaSingleNodeRenderCommands.add(transformCommand);
			shaderModSingleNodeRenderCommands.add(transformCommand);
			for(MeshModel meshModel : nodeModel.getMeshModels()) {
				for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
					processMeshPrimitiveModel(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, transforms, vanillaSingleNodeRenderCommands, shaderModSingleNodeRenderCommands);
				}
			}
			vanillaSingleNodeRenderCommands.add(GL11::glPopMatrix);
			shaderModSingleNodeRenderCommands.add(GL11::glPopMatrix);
			
			vanillaNodeRenderCommands.add(() -> {
				if(visibleToggle.booleanValue()) vanillaSingleNodeRenderCommands.forEach(Runnable::run);
			});
			shaderModNodeRenderCommands.add(() -> {
				if(visibleToggle.booleanValue()) shaderModSingleNodeRenderCommands.forEach(Runnable::run);
			});
		}
		else {
			if(!nodeModel.getMeshModels().isEmpty()) {
				List<Runnable> vanillaSingleNodeRenderCommands = new ArrayList<Runnable>();
				List<Runnable> shaderModSingleNodeRenderCommands = new ArrayList<Runnable>();
				
				Runnable transformCommand = createTransformCommand(nodeModel);
				vanillaSingleNodeRenderCommands.add(transformCommand);
				shaderModSingleNodeRenderCommands.add(transformCommand);
				for(MeshModel meshModel : nodeModel.getMeshModels()) {
					for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
						processMeshPrimitiveModel(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, vanillaSingleNodeRenderCommands, shaderModSingleNodeRenderCommands);
					}
				}
				vanillaSingleNodeRenderCommands.add(GL11::glPopMatrix);
				shaderModSingleNodeRenderCommands.add(GL11::glPopMatrix);
				
				vanillaNodeRenderCommands.add(() -> {
					if(visibleToggle.booleanValue()) vanillaSingleNodeRenderCommands.forEach(Runnable::run);
				});
				shaderModNodeRenderCommands.add(() -> {
					if(visibleToggle.booleanValue()) shaderModSingleNodeRenderCommands.forEach(Runnable::run);
				});
			}
		}
		nodeModel.getChildren().forEach((childNode) -> processNodeModel(gltfRenderData, childNode, vanillaNodeRenderCommands, shaderModNodeRenderCommands));
		if(!vanillaNodeRenderCommands.isEmpty()) {
			vanillaRenderCommands.add(() -> {
				float[] scale = nodeModel.getScale();
				if(scale == null || scale[0] != 0.0F || scale[1] != 0.0F || scale[2] != 0.0F) {
					vanillaNodeRenderCommands.forEach(Runnable::run);
				}
			});
			shaderModRenderCommands.add(() -> {
				float[] scale = nodeModel.getScale();
				if(scale == null || scale[0] != 0.0F || scale[1] != 0.0F || scale[2] != 0.0F) {
					shaderModNodeRenderCommands.forEach(Runnable::run);
				}
			});
		}
	}

	/**
	 * Copy-paste from parent class, replace default material command with MWF material command and force generate Mikk tangent with it.
	 */
	@Override
	protected void processMeshPrimitiveModel(List<Runnable> gltfRenderData, NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel, List<Runnable> vanillaRenderCommands, List<Runnable> shaderModRenderCommands) {
		Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
		AccessorModel positionsAccessorModel = attributes.get("POSITION");
		if(positionsAccessorModel != null) {
			List<Runnable> renderCommand = new ArrayList<Runnable>();
			AccessorModel normalsAccessorModel = attributes.get("NORMAL");
			if(normalsAccessorModel != null) {
				AccessorModel tangentsAccessorModel = attributes.get("TANGENT");
				if(tangentsAccessorModel != null) {
					processMeshPrimitiveModelIncludedTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, attributes, positionsAccessorModel, normalsAccessorModel, tangentsAccessorModel);
					MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
					if(materialModel != null) {
						Object extras = materialModel.getExtras();
						if(extras != null) {
							Material renderedMaterial = obtainMaterial(gltfRenderData, extras);
							vanillaRenderCommands.add(renderedMaterial.vanillaMaterialCommand);
							shaderModRenderCommands.add(renderedMaterial.shaderModMaterialCommand);
						}
						else {
							vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
						}
					}
					else {
						vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
					}
				}
				else {
					MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
					if(materialModel != null) {
						Object extras = materialModel.getExtras();
						if(extras != null) {
							Material renderedMaterial = obtainMaterial(gltfRenderData, extras);
							vanillaRenderCommands.add(renderedMaterial.vanillaMaterialCommand);
							shaderModRenderCommands.add(renderedMaterial.shaderModMaterialCommand);
							if(renderedMaterial.normalTexture != null) {
								processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand);
							}
							else {
								processMeshPrimitiveModelSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, attributes, positionsAccessorModel, normalsAccessorModel);
							}
						}
						else {
							vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
							processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
						}
					}
					else {
						vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
						processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
					}
				}
			}
			else {
				MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
				if(materialModel != null) {
					Object extras = materialModel.getExtras();
					if(extras != null) {
						Material renderedMaterial = obtainMaterial(gltfRenderData, extras);
						vanillaRenderCommands.add(renderedMaterial.vanillaMaterialCommand);
						shaderModRenderCommands.add(renderedMaterial.shaderModMaterialCommand);
						if(renderedMaterial.normalTexture != null) {
							processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand);
						}
						else {
							processMeshPrimitiveModelFlatNormalSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand);
						}
					}
					else {
						vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
						processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand);
					}
				}
				else {
					vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
					shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
					processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand);
				}
			}
			vanillaRenderCommands.addAll(renderCommand);
			shaderModRenderCommands.addAll(renderCommand);
		}
	}

	/**
	 * Copy-paste from parent class, replace default material command with MWF material command and force generate Mikk tangent with it.
	 */
	@Override
	protected void processMeshPrimitiveModel(List<Runnable> gltfRenderData, NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel, float[][] jointMatrices, List<Runnable> vanillaRenderCommands, List<Runnable> shaderModRenderCommands) {
		Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
		AccessorModel positionsAccessorModel = attributes.get("POSITION");
		if(positionsAccessorModel != null) {
			List<Runnable> renderCommand = new ArrayList<Runnable>();
			AccessorModel normalsAccessorModel = attributes.get("NORMAL");
			if(normalsAccessorModel != null) {
				AccessorModel tangentsAccessorModel = attributes.get("TANGENT");
				if(tangentsAccessorModel != null) {
					processMeshPrimitiveModelIncludedTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices, attributes, positionsAccessorModel, normalsAccessorModel, tangentsAccessorModel);
					MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
					if(materialModel != null) {
						Object extras = materialModel.getExtras();
						if(extras != null) {
							Material renderedMaterial = obtainMaterial(gltfRenderData, extras);
							vanillaRenderCommands.add(renderedMaterial.vanillaMaterialCommand);
							shaderModRenderCommands.add(renderedMaterial.shaderModMaterialCommand);
						}
						else {
							vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
						}
					}
					else {
						vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
					}
				}
				else {
					MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
					if(materialModel != null) {
						Object extras = materialModel.getExtras();
						if(extras != null) {
							Material renderedMaterial = obtainMaterial(gltfRenderData, extras);
							vanillaRenderCommands.add(renderedMaterial.vanillaMaterialCommand);
							shaderModRenderCommands.add(renderedMaterial.shaderModMaterialCommand);
							if(renderedMaterial.normalTexture != null) {
								processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
							}
							else {
								processMeshPrimitiveModelSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices, attributes, positionsAccessorModel, normalsAccessorModel);
							}
						}
						else {
							vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
							processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
						}
					}
					else {
						vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
						processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
					}
				}
			}
			else {
				MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
				if(materialModel != null) {
					Object extras = materialModel.getExtras();
					if(extras != null) {
						Material renderedMaterial = obtainMaterial(gltfRenderData, extras);
						vanillaRenderCommands.add(renderedMaterial.vanillaMaterialCommand);
						shaderModRenderCommands.add(renderedMaterial.shaderModMaterialCommand);
						if(renderedMaterial.normalTexture != null) {
							processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
						}
						else {
							processMeshPrimitiveModelFlatNormalSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
						}
					}
					else {
						vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
						processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
					}
				}
				else {
					vanillaRenderCommands.add(RenderedGltfModelMWF.mwfVanillaMaterialCommand);
					shaderModRenderCommands.add(RenderedGltfModelMWF.mwfShaderModMaterialCommand);
					processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
				}
			}
			vanillaRenderCommands.addAll(renderCommand);
			shaderModRenderCommands.addAll(renderCommand);
		}
	}

}
