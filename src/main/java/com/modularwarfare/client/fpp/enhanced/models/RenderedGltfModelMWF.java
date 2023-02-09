package com.modularwarfare.client.fpp.enhanced.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.type.BaseType;

import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.MathUtils;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SceneModel;
import de.javagl.jgltf.model.SkinModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class RenderedGltfModelMWF extends RenderedGltfModel implements IRenderedGltfModelMWF {

	public static BaseType CURRENT_BASE_TYPE;

	/**
	 * When "extras" of "materials" in glTF does not exist, using material provided by MWF.
	 */
	public static final Runnable mwfVanillaMaterialCommand = () -> {
		if(CURRENT_BASE_TYPE instanceof GunType){
			// A workaround to prevent renderEngine.bindTexture() in the next mess up by glBindTexture() and glActiveTexture() elsewhere in RenderedGltfModel.(Also glPushAttrib() and glPopAttrib() in EnhancedModel)
			// Because after glBindTexture() and glActiveTexture() change OpenGL texture binding,
			// the GlStateManager call by renderEngine still think the current texture binding remain the same and not willing to bind the same texture to OpenGL again.
			Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);

			Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.gunEnhancedRenderer.bindingTexture);
			GL11.glColor4f(ClientProxy.gunEnhancedRenderer.r,
					ClientProxy.gunEnhancedRenderer.g,
					ClientProxy.gunEnhancedRenderer.b,
					ClientProxy.gunEnhancedRenderer.a);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		else if(CURRENT_BASE_TYPE != null) {
			// A workaround to prevent renderEngine.bindTexture() in the next mess up by glBindTexture() and glActiveTexture() elsewhere in RenderedGltfModel.(Also glPushAttrib() and glPopAttrib() in EnhancedModel)
			// Because after glBindTexture() and glActiveTexture() change OpenGL texture binding,
			// the GlStateManager call by renderEngine still think the current texture binding remain the same and not willing to bind the same texture to OpenGL again.
			Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);

			Minecraft.getMinecraft().renderEngine.bindTexture(ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].bindingTexture);
			GL11.glColor4f(ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].r,
					ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].g,
					ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].b,
					ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].a);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		else {
			vanillaDefaultMaterialCommand.run();
		}
	};

	/**
	 * When "extras" of "materials" in glTF does not exist, using material provided by MWF.
	 */
	public static final Runnable mwfShaderModMaterialCommand = () -> {
		if(CURRENT_BASE_TYPE instanceof GunType){
			// A workaround to prevent renderEngine.bindTexture() in the next mess up by glBindTexture() and glActiveTexture() elsewhere in RenderedGltfModel.(Also glPushAttrib() and glPopAttrib() in EnhancedModel)
			// Because after glBindTexture() and glActiveTexture() change OpenGL texture binding,
			// the GlStateManager call by renderEngine still think the current texture binding remain the same and not willing to bind the same texture to OpenGL again.
			Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);

			Minecraft.getMinecraft().renderEngine.bindTexture(ClientProxy.gunEnhancedRenderer.bindingTexture);
			GL11.glColor4f(ClientProxy.gunEnhancedRenderer.r,
					ClientProxy.gunEnhancedRenderer.g,
					ClientProxy.gunEnhancedRenderer.b,
					ClientProxy.gunEnhancedRenderer.a);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		else if(CURRENT_BASE_TYPE != null) {
			// A workaround to prevent renderEngine.bindTexture() in the next mess up by glBindTexture() and glActiveTexture() elsewhere in RenderedGltfModel.(Also glPushAttrib() and glPopAttrib() in EnhancedModel)
			// Because after glBindTexture() and glActiveTexture() change OpenGL texture binding,
			// the GlStateManager call by renderEngine still think the current texture binding remain the same and not willing to bind the same texture to OpenGL again.
			Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);

			Minecraft.getMinecraft().renderEngine.bindTexture(ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].bindingTexture);
			GL11.glColor4f(ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].r,
					ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].g,
					ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].b,
					ClientRenderHooks.customRenderers[CURRENT_BASE_TYPE.id].a);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		else {
			shaderModDefaultMaterialCommand.run();
		}
	};

	/**
	 * Unlike set scale of node to zero to hide the node itself and its children, this will not affect children node.
	 */
	protected List<Pair<NodeModel, MutableBoolean>> singleNodeVisibleToggles;

	public RenderedGltfModelMWF(List<Runnable> gltfRenderData, GltfModel gltfModel) {
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
	protected void processNodeModel(List<Runnable> gltfRenderData, NodeModel nodeModel, List<Runnable> skinningCommands, List<Runnable> vanillaRenderCommands, List<Runnable> shaderModRenderCommands) {
		MutableBoolean visibleToggle = new MutableBoolean(true);
		singleNodeVisibleToggles.add(Pair.of(nodeModel, visibleToggle));
		
		ArrayList<Runnable> nodeSkinningCommands = new ArrayList<Runnable>();
		ArrayList<Runnable> vanillaNodeRenderCommands = new ArrayList<Runnable>();
		ArrayList<Runnable> shaderModNodeRenderCommands = new ArrayList<Runnable>();
		SkinModel skinModel = nodeModel.getSkinModel();
		if(skinModel != null) {
			//TODO: These part of code will need to optimized once MCglTF with SIMD comes out.
			boolean canHaveHardwareSkinning;
			checkHardwareSkinning: {
				for(MeshModel meshModel : nodeModel.getMeshModels()) {
					for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
						if(!meshPrimitiveModel.getAttributes().containsKey("JOINTS_1")) {
							canHaveHardwareSkinning = true;
							break checkHardwareSkinning;
						}
					}
				}
				canHaveHardwareSkinning = false;
			}
			
			int jointCount = skinModel.getJoints().size();
			
			float[][] transforms = new float[jointCount][];
			float[] invertNodeTransform = new float[16];
			float[] bindShapeMatrix = new float[16];
			
			List<Runnable> vanillaSingleNodeRenderCommands = new ArrayList<Runnable>();
			List<Runnable> shaderModSingleNodeRenderCommands = new ArrayList<Runnable>();
			
			if(canHaveHardwareSkinning) {
				List<Runnable> skinningSingleNodeRenderCommands = new ArrayList<Runnable>();
				
				int jointMatrixSize = jointCount * 16;
				float[] jointMatrices = new float[jointMatrixSize];
				
				int jointMatrixBuffer = GL15.glGenBuffers();
				gltfRenderData.add(() -> GL15.glDeleteBuffers(jointMatrixBuffer));
				GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer);
				GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixSize * Float.BYTES, GL15.GL_STATIC_DRAW);
				
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
						System.arraycopy(transform, 0, jointMatrices, i * 16, 16);
					});
				}
				
				skinningSingleNodeRenderCommands.add(() -> {
					for(int i = 0; i < transforms.length; i++) {
						System.arraycopy(findGlobalTransform(skinModel.getJoints().get(i)), 0, transforms[i], 0, 16);
					}
					MathUtils.invert4x4(findGlobalTransform(nodeModel), invertNodeTransform);
					skinModel.getBindShapeMatrix(bindShapeMatrix);
					jointMatricesTransformCommands.parallelStream().forEach(Runnable::run);

					GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer);
					GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, putFloatBuffer(jointMatrices));

					GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, jointMatrixBuffer);
				});
				
				Runnable transformCommand = createTransformCommand(nodeModel);
				vanillaSingleNodeRenderCommands.add(transformCommand);
				shaderModSingleNodeRenderCommands.add(transformCommand);
				for(MeshModel meshModel : nodeModel.getMeshModels()) {
					for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
						processMeshPrimitiveModel(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, transforms, skinningSingleNodeRenderCommands, vanillaSingleNodeRenderCommands, shaderModSingleNodeRenderCommands);
					}
				}
				vanillaSingleNodeRenderCommands.add(GL11::glPopMatrix);
				shaderModSingleNodeRenderCommands.add(GL11::glPopMatrix);
				
				nodeSkinningCommands.add(() -> {
					if(visibleToggle.booleanValue()) skinningSingleNodeRenderCommands.forEach(Runnable::run);
				});
			}
			else {
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
			}
			
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
		nodeModel.getChildren().forEach((childNode) -> processNodeModel(gltfRenderData, childNode, nodeSkinningCommands, vanillaNodeRenderCommands, shaderModNodeRenderCommands));
		if(!nodeSkinningCommands.isEmpty()) {
			// Zero-scale meshes visibility optimization
			// https://github.com/KhronosGroup/glTF/pull/2059
			skinningCommands.add(() -> {
				float[] scale = nodeModel.getScale();
				if(scale == null || scale[0] != 0.0F || scale[1] != 0.0F || scale[2] != 0.0F) {
					nodeSkinningCommands.forEach(Runnable::run);
				}
			});
		}
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
							vanillaRenderCommands.add(mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(mwfShaderModMaterialCommand);
						}
					}
					else {
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
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
							vanillaRenderCommands.add(mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(mwfShaderModMaterialCommand);
							processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
						}
					}
					else {
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
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
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
						processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
					}
				}
				else {
					vanillaRenderCommands.add(mwfVanillaMaterialCommand);
					shaderModRenderCommands.add(mwfShaderModMaterialCommand);
					processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
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
	protected void processMeshPrimitiveModel(List<Runnable> gltfRenderData, NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel, float[][] jointMatrices, List<Runnable> skinningCommand, List<Runnable> vanillaRenderCommands, List<Runnable> shaderModRenderCommands) {
		Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
		AccessorModel positionsAccessorModel = attributes.get("POSITION");
		if(positionsAccessorModel != null) {
			List<Runnable> renderCommand = new ArrayList<Runnable>();
			AccessorModel normalsAccessorModel = attributes.get("NORMAL");
			if(normalsAccessorModel != null) {
				AccessorModel tangentsAccessorModel = attributes.get("TANGENT");
				if(tangentsAccessorModel != null) {
					if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelIncludedTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices, attributes, positionsAccessorModel, normalsAccessorModel, tangentsAccessorModel);
					else processMeshPrimitiveModelIncludedTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand, attributes, positionsAccessorModel, normalsAccessorModel, tangentsAccessorModel);
					MaterialModel materialModel = meshPrimitiveModel.getMaterialModel();
					if(materialModel != null) {
						Object extras = materialModel.getExtras();
						if(extras != null) {
							Material renderedMaterial = obtainMaterial(gltfRenderData, extras);
							vanillaRenderCommands.add(renderedMaterial.vanillaMaterialCommand);
							shaderModRenderCommands.add(renderedMaterial.shaderModMaterialCommand);
						}
						else {
							vanillaRenderCommands.add(mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(mwfShaderModMaterialCommand);
						}
					}
					else {
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
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
								if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
								else processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand);
							}
							else {
								if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices, attributes, positionsAccessorModel, normalsAccessorModel);
								else processMeshPrimitiveModelSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand, attributes, positionsAccessorModel, normalsAccessorModel);
							}
						}
						else {
							vanillaRenderCommands.add(mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(mwfShaderModMaterialCommand);
							//These was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
							if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
							else processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand);
						}
					}
					else {
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
						//These was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
						if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
						else processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand);
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
							if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
							else processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand);
						}
						else {
							if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelFlatNormalSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
							else processMeshPrimitiveModelFlatNormalSimpleTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand);
						}
					}
					else {
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
						//These was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
						if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
						else processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand);
					}
				}
				else {
					vanillaRenderCommands.add(mwfVanillaMaterialCommand);
					shaderModRenderCommands.add(mwfShaderModMaterialCommand);
					//These was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
					if(attributes.containsKey("JOINTS_1")) processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices);
					else processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, skinningCommand);
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
							vanillaRenderCommands.add(mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(mwfShaderModMaterialCommand);
						}
					}
					else {
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
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
							vanillaRenderCommands.add(mwfVanillaMaterialCommand);
							shaderModRenderCommands.add(mwfShaderModMaterialCommand);
							processMeshPrimitiveModelMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
						}
					}
					else {
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
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
						vanillaRenderCommands.add(mwfVanillaMaterialCommand);
						shaderModRenderCommands.add(mwfShaderModMaterialCommand);
						processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
					}
				}
				else {
					vanillaRenderCommands.add(mwfVanillaMaterialCommand);
					shaderModRenderCommands.add(mwfShaderModMaterialCommand);
					processMeshPrimitiveModelFlatNormalMikkTangent(gltfRenderData, nodeModel, meshModel, meshPrimitiveModel, renderCommand, jointMatrices); //This was originally generate simple tangent, but TextureManager.bindTexture() may also bind normal map as well.
				}
			}
			vanillaRenderCommands.addAll(renderCommand);
			shaderModRenderCommands.addAll(renderCommand);
		}
	}

}
