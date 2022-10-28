package com.modularwarfare.mcgltf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.jme3.util.mikktspace.MikkTSpaceContext;
import com.jme3.util.mikktspace.MikktspaceTangentGenerator;

import de.javagl.jgltf.model.AccessorByteData;
import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorDatas;
import de.javagl.jgltf.model.AccessorFloatData;
import de.javagl.jgltf.model.AccessorIntData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.AccessorShortData;
import de.javagl.jgltf.model.BufferViewModel;
import de.javagl.jgltf.model.ElementType;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.MathUtils;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.MeshPrimitiveModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.Optionals;
import de.javagl.jgltf.model.SceneModel;
import de.javagl.jgltf.model.SkinModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.image.PixelData;
import de.javagl.jgltf.model.image.PixelDatas;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import net.minecraft.util.ResourceLocation;

public class RenderedGltfModel {

	/**
	 * ShaderMod attribute location for middle UV coordinates, used for parallax occlusion mapping.</br>
	 * This may change in different Minecraft version.</br>
	 * <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt">optifine/shaders.txt</a>
	 */
	public static final int mc_midTexCoord = 11;
	
	/**
	 * ShaderMod attribute location for Tangent.</br>
	 * This may change in different Minecraft version.</br>
	 * <a href="https://github.com/sp614x/optifine/blob/master/OptiFineDoc/doc/shaders.txt">optifine/shaders.txt</a>
	 */
	public static final int at_tangent = 12;
	
	private static final int skinning_joint = 0;
	private static final int skinning_weight = 1;
	private static final int skinning_position = 2;
	private static final int skinning_normal = 3;
	private static final int skinning_tangent = 4;
	
	private static final int skinning_out_position = 0;
	private static final int skinning_out_normal = 1;
	private static final int skinning_out_tangent = 2;
	
	private static FloatBuffer uniformFloatBuffer = null;
    
	private static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
	
	public static final Map<NodeModel, float[]> nodeGlobalTransformLookup = new IdentityHashMap<NodeModel, float[]>();
	
	public final GltfModel gltfModel;
	
	public final List<List<Runnable>> sceneCommands;
	
	public final GltfRenderData gltfRenderData = new GltfRenderData();
	
	private final Map<NodeModel, List<Runnable>> nodeModelToRootRenderCommands = new IdentityHashMap<NodeModel, List<Runnable>>();
	private final Map<NodeModel, List<Runnable>> nodeModelToRootSkinningCommands = new IdentityHashMap<NodeModel, List<Runnable>>();
	public final Map<MaterialModel, IMaterialHandler> materialModelToMaterialHandler = new IdentityHashMap<MaterialModel, IMaterialHandler>();
	private final Map<AccessorModel, AccessorModel> positionsAccessorModelToNormalsAccessorModel = new IdentityHashMap<AccessorModel, AccessorModel>();
	private final Map<AccessorModel, AccessorModel> normalsAccessorModelToTangentsAccessorModel = new IdentityHashMap<AccessorModel, AccessorModel>();
	private final Map<AccessorModel, AccessorModel> colorsAccessorModelVec3ToVec4 = new IdentityHashMap<AccessorModel, AccessorModel>();
	private final Map<AccessorModel, AccessorFloatData> colorsMorphTargetAccessorModelToAccessorData = new IdentityHashMap<AccessorModel, AccessorFloatData>();
	private final Map<AccessorModel, AccessorFloatData> texcoordsMorphTargetAccessorModelToAccessorData = new IdentityHashMap<AccessorModel, AccessorFloatData>();
	private final Map<MeshPrimitiveModel, AccessorModel> meshPrimitiveModelToTangentsAccessorModel = new IdentityHashMap<MeshPrimitiveModel, AccessorModel>();
	private final Map<MeshPrimitiveModel, Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>>> meshPrimitiveModelToUnindexed = new IdentityHashMap<MeshPrimitiveModel, Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>>>();
	private final Map<BufferViewModel, Integer> bufferViewModelToGlBufferView = new IdentityHashMap<BufferViewModel, Integer>();
	private final Map<TextureModel, Integer> textureModelToGlTexture = new IdentityHashMap<TextureModel, Integer>();
	
	public RenderedGltfModel(GltfModel gltfModel) {
		this.gltfModel = gltfModel;
		List<SceneModel> sceneModels = gltfModel.getSceneModels();
		sceneCommands = new ArrayList<List<Runnable>>(sceneModels.size());
		for(SceneModel sceneModel : sceneModels) {
			List<Runnable> renderCommands = new ArrayList<Runnable>();
			List<Runnable> skinningCommands = new ArrayList<Runnable>();
			
			for(NodeModel nodeModel : sceneModel.getNodeModels()) {
				List<Runnable> rootRenderCommands = nodeModelToRootRenderCommands.get(nodeModel);
				List<Runnable> rootSkinningCommands;
				if(rootRenderCommands == null) {
					rootRenderCommands = new ArrayList<Runnable>();
					rootSkinningCommands = new ArrayList<Runnable>();
					processNodeModel(nodeModel, rootRenderCommands, rootSkinningCommands);
					nodeModelToRootRenderCommands.put(nodeModel, rootRenderCommands);
					nodeModelToRootSkinningCommands.put(nodeModel, rootSkinningCommands);
				}
				else {
					rootSkinningCommands = nodeModelToRootSkinningCommands.get(nodeModel);
				}
				renderCommands.addAll(rootRenderCommands);
				skinningCommands.addAll(rootSkinningCommands);
			}
			
			List<Runnable> commands = new ArrayList<Runnable>();
			if(!skinningCommands.isEmpty()) {
				commands.add(() -> {
					int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
					GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
					GL20.glUseProgram(MCglTF.getInstance().getGlProgramSkinnig());
					skinningCommands.forEach((command) -> command.run());
					GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
					GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
					GL20.glUseProgram(currentProgram);
					GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
				});
			}
			commands.add(() -> GL20.glVertexAttrib2f(mc_midTexCoord, 1.0F, 1.0F));
			commands.addAll(renderCommands);
			commands.add(() -> {
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
				GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
				GL30.glBindVertexArray(0);
				nodeGlobalTransformLookup.clear();
			});
			sceneCommands.add(commands);
		}
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0);
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
		GL30.glBindVertexArray(0);
		GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
	}
	
	private void processNodeModel(NodeModel nodeModel, List<Runnable> renderCommands, List<Runnable> skinningCommands) {
		ArrayList<Runnable> nodeSkinningCommands = new ArrayList<Runnable>();
		ArrayList<Runnable> nodeRenderCommands = new ArrayList<Runnable>();
		SkinModel skinModel = nodeModel.getSkinModel();
		if(skinModel != null) {
			int jointSize = skinModel.getJoints().size();
			int jointMatrixSize = jointSize * 16;
			
			int jointMatrixBuffer = GL15.glGenBuffers();
			gltfRenderData.addGlBufferView(jointMatrixBuffer);
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixSize * Float.BYTES, GL15.GL_STATIC_DRAW);
			
			float[] invertNodeTransform = new float[16];
			float[] inverseBindMatrix = new float[16];
			float[] bindShapeMatrix = skinModel.getBindShapeMatrix(null);
			float[] result = new float[16];
			float[] jointMatrices = new float[jointMatrixSize];
			
			nodeSkinningCommands.add(() -> {
				MathUtils.invert4x4(findGlobalTransform(nodeModel), invertNodeTransform);
				for(int i = 0; i < jointSize; i++) {
					MathUtils.mul4x4(invertNodeTransform, findGlobalTransform(skinModel.getJoints().get(i)), result);
					skinModel.getInverseBindMatrix(i, inverseBindMatrix);
					MathUtils.mul4x4(result, inverseBindMatrix, result);
					MathUtils.mul4x4(result, bindShapeMatrix, result);
					System.arraycopy(result, 0, jointMatrices, i * 16, 16);
				}
				
				GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer);
				GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, putFloatBuffer(jointMatrices));
				
				GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, jointMatrixBuffer);
			});
			
			nodeRenderCommands.add(createTransformCommand(nodeModel));
			for(MeshModel meshModel : nodeModel.getMeshModels()) {
				for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
					processMeshPrimitiveModel(nodeModel, meshModel, meshPrimitiveModel, nodeRenderCommands, nodeSkinningCommands);
				}
			}
			nodeRenderCommands.add(GL11::glPopMatrix);
		}
		else {
			if(!nodeModel.getMeshModels().isEmpty()) {
				nodeRenderCommands.add(createTransformCommand(nodeModel));
				for(MeshModel meshModel : nodeModel.getMeshModels()) {
					for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
						processMeshPrimitiveModel(nodeModel, meshModel, meshPrimitiveModel, nodeRenderCommands);
					}
				}
				nodeRenderCommands.add(GL11::glPopMatrix);
			}
		}
		nodeModel.getChildren().forEach((childNode) -> processNodeModel(childNode, nodeRenderCommands, nodeSkinningCommands));
		if(!nodeSkinningCommands.isEmpty()) {
			skinningCommands.add(() -> {
				// Zero-scale meshes visibility optimization
				// https://github.com/KhronosGroup/glTF/pull/2059
				float[] scale = nodeModel.getScale();
				if(scale == null || scale[0] != 0.0F || scale[1] != 0.0F || scale[2] != 0.0F) {
					nodeSkinningCommands.forEach((command) -> command.run());
				}
			});
		}
		if(!nodeRenderCommands.isEmpty()) {
			renderCommands.add(() -> {
				float[] scale = nodeModel.getScale();
				if(scale == null || scale[0] != 0.0F || scale[1] != 0.0F || scale[2] != 0.0F) {
					nodeRenderCommands.forEach((command) -> command.run());
				}
			});
		}
	}
	
	private void processMeshPrimitiveModel(NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel, List<Runnable> renderCommand) {
		Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
		AccessorModel positionsAccessorModel = attributes.get("POSITION");
		if(positionsAccessorModel != null) {
			IMaterialHandler materialHandler = obtainMaterialHandler(meshPrimitiveModel.getMaterialModel());
			Runnable materialCommand = materialHandler.getPreMeshDrawCommand();
			if(materialCommand != null) renderCommand.add(materialCommand);
			
			int glVertexArray = GL30.glGenVertexArrays();
			gltfRenderData.addGlVertexArray(glVertexArray);
			GL30.glBindVertexArray(glVertexArray);
			AccessorModel normalsAccessorModel = attributes.get("NORMAL");
			if(normalsAccessorModel == null) {
				Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>> unindexed = obtainUnindexed(meshPrimitiveModel);
				attributes = unindexed.getLeft();
				List<Map<String, AccessorModel>> morphTargets = unindexed.getRight();
				positionsAccessorModel = attributes.get("POSITION");
				normalsAccessorModel = obtainNormalsAccessorModel(positionsAccessorModel);
				if(materialHandler.hasNormalMap()) {
					AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
					List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					List<AccessorFloatData> normalTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createPositionNormalMorphTarget(morphTargets, positionsAccessorModel, normalsAccessorModel, targetAccessorDatas, normalTargetAccessorDatas)) {
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, positionsAccessorModel, targetAccessorDatas);
						GL11.glVertexPointer(
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, normalsAccessorModel, normalTargetAccessorDatas);
						GL11.glNormalPointer(
								normalsAccessorModel.getComponentType(),
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
					}
					else {
						bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						GL11.glVertexPointer(
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
						GL11.glNormalPointer(
								normalsAccessorModel.getComponentType(),
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
					}
					
					AccessorModel tangentsAccessorModel = obtainTangentsAccessorModel(normalsAccessorModel);
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createTangentMorphTarget(morphTargets, targetAccessorDatas, positionsAccessorModel, normalsAccessorModel, texcoordsAccessorModel, tangentsAccessorModel, normalTargetAccessorDatas)) {
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, tangentsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
					}
					GL20.glVertexAttribPointer(
							at_tangent,
							tangentsAccessorModel.getElementType().getNumComponents(),
							tangentsAccessorModel.getComponentType(),
							false,
							tangentsAccessorModel.getByteStride(),
							tangentsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(at_tangent);
					
					AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
					if(colorsAccessorModel != null) {
						colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
							colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
						}
						GL11.glColorPointer(
								colorsAccessorModel.getElementType().getNumComponents(),
								colorsAccessorModel.getComponentType(),
								colorsAccessorModel.getByteStride(),
								colorsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					}
					
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
						texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
					}
					GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
					GL11.glTexCoordPointer(
							texcoordsAccessorModel.getElementType().getNumComponents(),
							texcoordsAccessorModel.getComponentType(),
							texcoordsAccessorModel.getByteStride(),
							texcoordsAccessorModel.getByteOffset());
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					
					int mode = meshPrimitiveModel.getMode();
					int count = positionsAccessorModel.getCount();
					renderCommand.add(() -> {
						GL30.glBindVertexArray(glVertexArray);
						GL11.glDrawArrays(mode, 0, count);
					});
				}
				else {
					AccessorModel tangentsAccessorModel = obtainTangentsAccessorModel(normalsAccessorModel);
					List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					List<AccessorFloatData> normalTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					List<AccessorFloatData> tangentTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createPositionNormalTangentMorphTarget(morphTargets, positionsAccessorModel, normalsAccessorModel, tangentsAccessorModel, targetAccessorDatas, normalTargetAccessorDatas, tangentTargetAccessorDatas)) {
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, positionsAccessorModel, targetAccessorDatas);
						GL11.glVertexPointer(
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, normalsAccessorModel, normalTargetAccessorDatas);
						GL11.glNormalPointer(
								normalsAccessorModel.getComponentType(),
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
						
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, tangentsAccessorModel, tangentTargetAccessorDatas);
						GL20.glVertexAttribPointer(
								at_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								tangentsAccessorModel.getByteStride(),
								tangentsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(at_tangent);
					}
					else {
						bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						GL11.glVertexPointer(
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
						GL11.glNormalPointer(
								normalsAccessorModel.getComponentType(),
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
						
						bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								at_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								tangentsAccessorModel.getByteStride(),
								tangentsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(at_tangent);
					}

					AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
					if(colorsAccessorModel != null) {
						colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
							colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
						}
						GL11.glColorPointer(
								colorsAccessorModel.getElementType().getNumComponents(),
								colorsAccessorModel.getComponentType(),
								colorsAccessorModel.getByteStride(),
								colorsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					}
					
					AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
					if(texcoordsAccessorModel != null) {
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
							texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
						}
						GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
						GL11.glTexCoordPointer(
								texcoordsAccessorModel.getElementType().getNumComponents(),
								texcoordsAccessorModel.getComponentType(),
								texcoordsAccessorModel.getByteStride(),
								texcoordsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					}
					
					int mode = meshPrimitiveModel.getMode();
					int count = positionsAccessorModel.getCount();
					renderCommand.add(() -> {
						GL30.glBindVertexArray(glVertexArray);
						GL11.glDrawArrays(mode, 0, count);
					});
				}
			}
			else {
				AccessorModel tangentsAccessorModel = attributes.get("TANGENT");
				if(tangentsAccessorModel == null) {
					if(materialHandler.hasNormalMap()) {
						Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>> unindexed = obtainUnindexed(meshPrimitiveModel);
						attributes = unindexed.getLeft();
						List<Map<String, AccessorModel>> morphTargets = unindexed.getRight();
						AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
						
						positionsAccessorModel = attributes.get("POSITION");
						List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createMorphTarget(morphTargets, targetAccessorDatas, "POSITION")) {
							bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, positionsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						}
						GL11.glVertexPointer(
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						normalsAccessorModel = attributes.get("NORMAL");
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createMorphTarget(morphTargets, targetAccessorDatas, "NORMAL")) {
							bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, normalsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
						}
						GL11.glNormalPointer(
								normalsAccessorModel.getComponentType(),
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
						
						tangentsAccessorModel = obtainTangentsAccessorModel(meshPrimitiveModel, positionsAccessorModel, normalsAccessorModel, texcoordsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTangentMorphTarget(morphTargets, targetAccessorDatas, positionsAccessorModel, normalsAccessorModel, texcoordsAccessorModel, tangentsAccessorModel)) {
							bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, tangentsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
						}
						GL20.glVertexAttribPointer(
								at_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								tangentsAccessorModel.getByteStride(),
								tangentsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(at_tangent);
						
						AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
						if(colorsAccessorModel != null) {
							colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
							targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
							if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
								colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
							}
							else {
								bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
							}
							GL11.glColorPointer(
									colorsAccessorModel.getElementType().getNumComponents(),
									colorsAccessorModel.getComponentType(),
									colorsAccessorModel.getByteStride(),
									colorsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
						}
						
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
							texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
						}
						GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
						GL11.glTexCoordPointer(
								texcoordsAccessorModel.getElementType().getNumComponents(),
								texcoordsAccessorModel.getComponentType(),
								texcoordsAccessorModel.getByteStride(),
								texcoordsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						
						int mode = meshPrimitiveModel.getMode();
						int count = positionsAccessorModel.getCount();
						renderCommand.add(() -> {
							GL30.glBindVertexArray(glVertexArray);
							GL11.glDrawArrays(mode, 0, count);
						});
					}
					else {
						List<Map<String, AccessorModel>> morphTargets = meshPrimitiveModel.getTargets();
						
						List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createMorphTarget(morphTargets, targetAccessorDatas, "POSITION")) {
							bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, positionsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						}
						GL11.glVertexPointer(
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						tangentsAccessorModel = obtainTangentsAccessorModel(normalsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						List<AccessorFloatData> tangentTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createNormalTangentMorphTarget(morphTargets, normalsAccessorModel, tangentsAccessorModel, targetAccessorDatas, tangentTargetAccessorDatas)) {
							bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, normalsAccessorModel, targetAccessorDatas);
							GL11.glNormalPointer(
									normalsAccessorModel.getComponentType(),
									normalsAccessorModel.getByteStride(),
									normalsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
							
							bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, tangentsAccessorModel, tangentTargetAccessorDatas);
							GL20.glVertexAttribPointer(
									at_tangent,
									tangentsAccessorModel.getElementType().getNumComponents(),
									tangentsAccessorModel.getComponentType(),
									false,
									tangentsAccessorModel.getByteStride(),
									tangentsAccessorModel.getByteOffset());
							GL20.glEnableVertexAttribArray(at_tangent);
						}
						else {
							bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
							GL11.glNormalPointer(
									normalsAccessorModel.getComponentType(),
									normalsAccessorModel.getByteStride(),
									normalsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
							
							bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
							GL20.glVertexAttribPointer(
									at_tangent,
									tangentsAccessorModel.getElementType().getNumComponents(),
									tangentsAccessorModel.getComponentType(),
									false,
									tangentsAccessorModel.getByteStride(),
									tangentsAccessorModel.getByteOffset());
							GL20.glEnableVertexAttribArray(at_tangent);
						}
						
						AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
						if(colorsAccessorModel != null) {
							colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
							targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
							if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
								colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
							}
							else {
								bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
							}
							GL11.glColorPointer(
									colorsAccessorModel.getElementType().getNumComponents(),
									colorsAccessorModel.getComponentType(),
									colorsAccessorModel.getByteStride(),
									colorsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
						}
						
						AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
						if(texcoordsAccessorModel != null) {
							targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
							if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
								texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
							}
							else {
								bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
							}
							GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
							GL11.glTexCoordPointer(
									texcoordsAccessorModel.getElementType().getNumComponents(),
									texcoordsAccessorModel.getComponentType(),
									texcoordsAccessorModel.getByteStride(),
									texcoordsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						}
						
						int mode = meshPrimitiveModel.getMode();
						AccessorModel indices = meshPrimitiveModel.getIndices();
						if(indices != null) {
							int glIndicesBufferView = obtainElementArrayBuffer(indices.getBufferViewModel());
							int count = indices.getCount();
							int type = indices.getComponentType();
							int offset = indices.getByteOffset();
							renderCommand.add(() -> {
								GL30.glBindVertexArray(glVertexArray);
								GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glIndicesBufferView);
								GL11.glDrawElements(mode, count, type, offset);
							});
						}
						else {
							int count = positionsAccessorModel.getCount();
							renderCommand.add(() -> {
								GL30.glBindVertexArray(glVertexArray);
								GL11.glDrawArrays(mode, 0, count);
							});
						}
					}
				}
				else {
					List<Map<String, AccessorModel>> morphTargets = meshPrimitiveModel.getTargets();
					
					List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createMorphTarget(morphTargets, targetAccessorDatas, "POSITION")) {
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, positionsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
					}
					GL11.glVertexPointer(
							positionsAccessorModel.getElementType().getNumComponents(),
							positionsAccessorModel.getComponentType(),
							positionsAccessorModel.getByteStride(),
							positionsAccessorModel.getByteOffset());
					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createMorphTarget(morphTargets, targetAccessorDatas, "NORMAL")) {
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, normalsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
					}
					GL11.glNormalPointer(
							normalsAccessorModel.getComponentType(),
							normalsAccessorModel.getByteStride(),
							normalsAccessorModel.getByteOffset());
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
					
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createMorphTarget(morphTargets, targetAccessorDatas, "TANGENT")) {
						bindVec3FloatMorphed(nodeModel, meshModel, renderCommand, tangentsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
					}
					GL20.glVertexAttribPointer(
							at_tangent,
							tangentsAccessorModel.getElementType().getNumComponents(),
							tangentsAccessorModel.getComponentType(),
							false,
							tangentsAccessorModel.getByteStride(),
							tangentsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(at_tangent);
					
					AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
					if(colorsAccessorModel != null) {
						colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
							colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
						}
						GL11.glColorPointer(
								colorsAccessorModel.getElementType().getNumComponents(),
								colorsAccessorModel.getComponentType(),
								colorsAccessorModel.getByteStride(),
								colorsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					}
					
					AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
					if(texcoordsAccessorModel != null) {
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
							texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
						}
						GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
						GL11.glTexCoordPointer(
								texcoordsAccessorModel.getElementType().getNumComponents(),
								texcoordsAccessorModel.getComponentType(),
								texcoordsAccessorModel.getByteStride(),
								texcoordsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					}
					
					int mode = meshPrimitiveModel.getMode();
					AccessorModel indices = meshPrimitiveModel.getIndices();
					if(indices != null) {
						int glIndicesBufferView = obtainElementArrayBuffer(indices.getBufferViewModel());
						int count = indices.getCount();
						int type = indices.getComponentType();
						int offset = indices.getByteOffset();
						renderCommand.add(() -> {
							GL30.glBindVertexArray(glVertexArray);
							GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glIndicesBufferView);
							GL11.glDrawElements(mode, count, type, offset);
						});
					}
					else {
						int count = positionsAccessorModel.getCount();
						renderCommand.add(() -> {
							GL30.glBindVertexArray(glVertexArray);
							GL11.glDrawArrays(mode, 0, count);
						});
					}
				}
			}
			
			materialCommand = materialHandler.getPostMeshDrawCommand();
			if(materialCommand != null) renderCommand.add(materialCommand);
		}
	}
	
	private void processMeshPrimitiveModel(NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel, List<Runnable> renderCommand, List<Runnable> skinningCommand) {
		Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
		AccessorModel positionsAccessorModel = attributes.get("POSITION");
		if(positionsAccessorModel != null) {
			IMaterialHandler materialHandler = obtainMaterialHandler(meshPrimitiveModel.getMaterialModel());
			Runnable materialCommand = materialHandler.getPreMeshDrawCommand();
			if(materialCommand != null) renderCommand.add(materialCommand);
			
			int glTransformFeedback = GL40.glGenTransformFeedbacks();
			gltfRenderData.addGlTransformFeedback(glTransformFeedback);
			GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, glTransformFeedback);
			
			int glVertexArraySkinning = GL30.glGenVertexArrays();
			gltfRenderData.addGlVertexArray(glVertexArraySkinning);
			GL30.glBindVertexArray(glVertexArraySkinning);
			
			AccessorModel normalsAccessorModel = attributes.get("NORMAL");
			if(normalsAccessorModel == null) {
				Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>> unindexed = obtainUnindexed(meshPrimitiveModel);
				attributes = unindexed.getLeft();
				
				AccessorModel jointsAccessorModel = attributes.get("JOINTS_0");
				bindArrayBufferViewModel(jointsAccessorModel.getBufferViewModel());
				GL20.glVertexAttribPointer(
						skinning_joint,
						jointsAccessorModel.getElementType().getNumComponents(),
						jointsAccessorModel.getComponentType(),
						false,
						jointsAccessorModel.getByteStride(),
						jointsAccessorModel.getByteOffset());
				GL20.glEnableVertexAttribArray(skinning_joint);
				
				AccessorModel weightsAccessorModel = attributes.get("WEIGHTS_0");
				bindArrayBufferViewModel(weightsAccessorModel.getBufferViewModel());
				GL20.glVertexAttribPointer(
						skinning_weight,
						weightsAccessorModel.getElementType().getNumComponents(),
						weightsAccessorModel.getComponentType(),
						false,
						weightsAccessorModel.getByteStride(),
						weightsAccessorModel.getByteOffset());
				GL20.glEnableVertexAttribArray(skinning_weight);
				
				List<Map<String, AccessorModel>> morphTargets = unindexed.getRight();
				positionsAccessorModel = attributes.get("POSITION");
				normalsAccessorModel = obtainNormalsAccessorModel(positionsAccessorModel);
				if(materialHandler.hasNormalMap()) {
					AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
					List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					List<AccessorFloatData> normalTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createPositionNormalMorphTarget(morphTargets, positionsAccessorModel, normalsAccessorModel, targetAccessorDatas, normalTargetAccessorDatas)) {
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, positionsAccessorModel, targetAccessorDatas);
						GL20.glVertexAttribPointer(
								skinning_position,
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								false,
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_position);
						
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, normalsAccessorModel, normalTargetAccessorDatas);
						GL20.glVertexAttribPointer(
								skinning_normal,
								normalsAccessorModel.getElementType().getNumComponents(),
								normalsAccessorModel.getComponentType(),
								false,
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_normal);
					}
					else {
						bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_position,
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								false,
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_position);
						
						bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_normal,
								normalsAccessorModel.getElementType().getNumComponents(),
								normalsAccessorModel.getComponentType(),
								false,
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_normal);
					}
					
					AccessorModel tangentsAccessorModel = obtainTangentsAccessorModel(normalsAccessorModel);
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createTangentMorphTarget(morphTargets, targetAccessorDatas, positionsAccessorModel, normalsAccessorModel, texcoordsAccessorModel, tangentsAccessorModel, normalTargetAccessorDatas)) {
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, tangentsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
					}
					GL20.glVertexAttribPointer(
							skinning_tangent,
							tangentsAccessorModel.getElementType().getNumComponents(),
							tangentsAccessorModel.getComponentType(),
							false,
							tangentsAccessorModel.getByteStride(),
							tangentsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(skinning_tangent);
					
					int pointCount = positionsAccessorModel.getCount();
					skinningCommand.add(() -> {
						GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, glTransformFeedback);
						GL30.glBeginTransformFeedback(GL11.GL_POINTS);
						GL30.glBindVertexArray(glVertexArraySkinning);
						GL11.glDrawArrays(GL11.GL_POINTS, 0, pointCount);
						GL30.glEndTransformFeedback();
					});
					
					int outputPosition = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputPosition);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputPosition);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, positionsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_position, outputPosition);
					
					int outputNormal = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputNormal);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputNormal);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, normalsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_normal, outputNormal);
					
					int outputTangent = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputTangent);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputTangent);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, tangentsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_tangent, outputTangent);
					
					int glVertexArray = GL30.glGenVertexArrays();
					gltfRenderData.addGlVertexArray(glVertexArray);
					GL30.glBindVertexArray(glVertexArray);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputPosition);
					GL11.glVertexPointer(positionsAccessorModel.getElementType().getNumComponents(), positionsAccessorModel.getComponentType(), 0, 0);
					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputNormal);
					GL11.glNormalPointer(normalsAccessorModel.getComponentType(), 0, 0);
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputTangent);
					GL20.glVertexAttribPointer(
							at_tangent,
							tangentsAccessorModel.getElementType().getNumComponents(),
							tangentsAccessorModel.getComponentType(),
							false,
							0,
							0);
					GL20.glEnableVertexAttribArray(at_tangent);
					
					AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
					if(colorsAccessorModel != null) {
						colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
							colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
						}
						GL11.glColorPointer(
								colorsAccessorModel.getElementType().getNumComponents(),
								colorsAccessorModel.getComponentType(),
								colorsAccessorModel.getByteStride(),
								colorsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					}
					
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
						texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
					}
					GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
					GL11.glTexCoordPointer(
							texcoordsAccessorModel.getElementType().getNumComponents(),
							texcoordsAccessorModel.getComponentType(),
							texcoordsAccessorModel.getByteStride(),
							texcoordsAccessorModel.getByteOffset());
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					
					int mode = meshPrimitiveModel.getMode();
					renderCommand.add(() -> {
						GL30.glBindVertexArray(glVertexArray);
						GL40.glDrawTransformFeedback(mode, glTransformFeedback);
					});
				}
				else {
					AccessorModel tangentsAccessorModel = obtainTangentsAccessorModel(normalsAccessorModel);
					List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					List<AccessorFloatData> normalTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					List<AccessorFloatData> tangentTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createPositionNormalTangentMorphTarget(morphTargets, positionsAccessorModel, normalsAccessorModel, tangentsAccessorModel, targetAccessorDatas, normalTargetAccessorDatas, tangentTargetAccessorDatas)) {
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, positionsAccessorModel, targetAccessorDatas);
						GL20.glVertexAttribPointer(
								skinning_position,
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								false,
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_position);
						
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, normalsAccessorModel, normalTargetAccessorDatas);
						GL20.glVertexAttribPointer(
								skinning_normal,
								normalsAccessorModel.getElementType().getNumComponents(),
								normalsAccessorModel.getComponentType(),
								false,
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_normal);
						
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, tangentsAccessorModel, tangentTargetAccessorDatas);
						GL20.glVertexAttribPointer(
								skinning_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								tangentsAccessorModel.getByteStride(),
								tangentsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_tangent);
					}
					else {
						bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_position,
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								false,
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_position);
						
						bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_normal,
								normalsAccessorModel.getElementType().getNumComponents(),
								normalsAccessorModel.getComponentType(),
								false,
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_normal);
						
						bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								tangentsAccessorModel.getByteStride(),
								tangentsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_tangent);
					}
					
					int pointCount = positionsAccessorModel.getCount();
					skinningCommand.add(() -> {
						GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, glTransformFeedback);
						GL30.glBeginTransformFeedback(GL11.GL_POINTS);
						GL30.glBindVertexArray(glVertexArraySkinning);
						GL11.glDrawArrays(GL11.GL_POINTS, 0, pointCount);
						GL30.glEndTransformFeedback();
					});
					
					int outputPosition = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputPosition);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputPosition);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, positionsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_position, outputPosition);
					
					int outputNormal = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputNormal);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputNormal);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, normalsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_normal, outputNormal);
					
					int outputTangent = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputTangent);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputTangent);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, tangentsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_tangent, outputTangent);
					
					int glVertexArray = GL30.glGenVertexArrays();
					gltfRenderData.addGlVertexArray(glVertexArray);
					GL30.glBindVertexArray(glVertexArray);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputPosition);
					GL11.glVertexPointer(positionsAccessorModel.getElementType().getNumComponents(), positionsAccessorModel.getComponentType(), 0, 0);
					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputNormal);
					GL11.glNormalPointer(normalsAccessorModel.getComponentType(), 0, 0);
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputTangent);
					GL20.glVertexAttribPointer(
							at_tangent,
							tangentsAccessorModel.getElementType().getNumComponents(),
							tangentsAccessorModel.getComponentType(),
							false,
							0,
							0);
					GL20.glEnableVertexAttribArray(at_tangent);
					
					AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
					if(colorsAccessorModel != null) {
						colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
							colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
						}
						GL11.glColorPointer(
								colorsAccessorModel.getElementType().getNumComponents(),
								colorsAccessorModel.getComponentType(),
								colorsAccessorModel.getByteStride(),
								colorsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					}
					
					AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
					if(texcoordsAccessorModel != null) {
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
							texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
						}
						GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
						GL11.glTexCoordPointer(
								texcoordsAccessorModel.getElementType().getNumComponents(),
								texcoordsAccessorModel.getComponentType(),
								texcoordsAccessorModel.getByteStride(),
								texcoordsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					}
					
					int mode = meshPrimitiveModel.getMode();
					renderCommand.add(() -> {
						GL30.glBindVertexArray(glVertexArray);
						GL40.glDrawTransformFeedback(mode, glTransformFeedback);
					});
				}
			}
			else {
				AccessorModel tangentsAccessorModel = attributes.get("TANGENT");
				if(tangentsAccessorModel == null) {
					if(materialHandler.hasNormalMap()) {
						Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>> unindexed = obtainUnindexed(meshPrimitiveModel);
						attributes = unindexed.getLeft();
						
						AccessorModel jointsAccessorModel = attributes.get("JOINTS_0");
						bindArrayBufferViewModel(jointsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_joint,
								jointsAccessorModel.getElementType().getNumComponents(),
								jointsAccessorModel.getComponentType(),
								false,
								jointsAccessorModel.getByteStride(),
								jointsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_joint);
						
						AccessorModel weightsAccessorModel = attributes.get("WEIGHTS_0");
						bindArrayBufferViewModel(weightsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_weight,
								weightsAccessorModel.getElementType().getNumComponents(),
								weightsAccessorModel.getComponentType(),
								false,
								weightsAccessorModel.getByteStride(),
								weightsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_weight);
						
						List<Map<String, AccessorModel>> morphTargets = unindexed.getRight();
						AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
						
						positionsAccessorModel = attributes.get("POSITION");
						List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createMorphTarget(morphTargets, targetAccessorDatas, "POSITION")) {
							bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, positionsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						}
						GL20.glVertexAttribPointer(
								skinning_position,
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								false,
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_position);
						
						normalsAccessorModel = attributes.get("NORMAL");
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createMorphTarget(morphTargets, targetAccessorDatas, "NORMAL")) {
							bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, normalsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
						}
						GL20.glVertexAttribPointer(
								skinning_normal,
								normalsAccessorModel.getElementType().getNumComponents(),
								normalsAccessorModel.getComponentType(),
								false,
								normalsAccessorModel.getByteStride(),
								normalsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_normal);
						
						tangentsAccessorModel = obtainTangentsAccessorModel(meshPrimitiveModel, positionsAccessorModel, normalsAccessorModel, texcoordsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTangentMorphTarget(morphTargets, targetAccessorDatas, positionsAccessorModel, normalsAccessorModel, texcoordsAccessorModel, tangentsAccessorModel)) {
							bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, tangentsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
						}
						GL20.glVertexAttribPointer(
								skinning_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								tangentsAccessorModel.getByteStride(),
								tangentsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_tangent);
						
						int pointCount = positionsAccessorModel.getCount();
						skinningCommand.add(() -> {
							GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, glTransformFeedback);
							GL30.glBeginTransformFeedback(GL11.GL_POINTS);
							GL30.glBindVertexArray(glVertexArraySkinning);
							GL11.glDrawArrays(GL11.GL_POINTS, 0, pointCount);
							GL30.glEndTransformFeedback();
						});
						
						int outputPosition = GL15.glGenBuffers();
						gltfRenderData.addGlBufferView(outputPosition);
						GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputPosition);
						GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, positionsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
						GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_position, outputPosition);
						
						int outputNormal = GL15.glGenBuffers();
						gltfRenderData.addGlBufferView(outputNormal);
						GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputNormal);
						GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, normalsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
						GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_normal, outputNormal);
						
						int outputTangent = GL15.glGenBuffers();
						gltfRenderData.addGlBufferView(outputTangent);
						GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputTangent);
						GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, tangentsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
						GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_tangent, outputTangent);
						
						int glVertexArray = GL30.glGenVertexArrays();
						gltfRenderData.addGlVertexArray(glVertexArray);
						GL30.glBindVertexArray(glVertexArray);
						
						GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputPosition);
						GL11.glVertexPointer(positionsAccessorModel.getElementType().getNumComponents(), positionsAccessorModel.getComponentType(), 0, 0);
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputNormal);
						GL11.glNormalPointer(normalsAccessorModel.getComponentType(), 0, 0);
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
						
						GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputTangent);
						GL20.glVertexAttribPointer(
								at_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								0,
								0);
						GL20.glEnableVertexAttribArray(at_tangent);
						
						AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
						if(colorsAccessorModel != null) {
							colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
							targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
							if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
								colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
							}
							else {
								bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
							}
							GL11.glColorPointer(
									colorsAccessorModel.getElementType().getNumComponents(),
									colorsAccessorModel.getComponentType(),
									colorsAccessorModel.getByteStride(),
									colorsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
						}
						
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
							texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
						}
						GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
						GL11.glTexCoordPointer(
								texcoordsAccessorModel.getElementType().getNumComponents(),
								texcoordsAccessorModel.getComponentType(),
								texcoordsAccessorModel.getByteStride(),
								texcoordsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						
						int mode = meshPrimitiveModel.getMode();
						renderCommand.add(() -> {
							GL30.glBindVertexArray(glVertexArray);
							GL40.glDrawTransformFeedback(mode, glTransformFeedback);
						});
					}
					else {
						AccessorModel jointsAccessorModel = attributes.get("JOINTS_0");
						bindArrayBufferViewModel(jointsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_joint,
								jointsAccessorModel.getElementType().getNumComponents(),
								jointsAccessorModel.getComponentType(),
								false,
								jointsAccessorModel.getByteStride(),
								jointsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_joint);
						
						AccessorModel weightsAccessorModel = attributes.get("WEIGHTS_0");
						bindArrayBufferViewModel(weightsAccessorModel.getBufferViewModel());
						GL20.glVertexAttribPointer(
								skinning_weight,
								weightsAccessorModel.getElementType().getNumComponents(),
								weightsAccessorModel.getComponentType(),
								false,
								weightsAccessorModel.getByteStride(),
								weightsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_weight);
						
						List<Map<String, AccessorModel>> morphTargets = meshPrimitiveModel.getTargets();
						
						List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createMorphTarget(morphTargets, targetAccessorDatas, "POSITION")) {
							bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, positionsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
						}
						GL20.glVertexAttribPointer(
								skinning_position,
								positionsAccessorModel.getElementType().getNumComponents(),
								positionsAccessorModel.getComponentType(),
								false,
								positionsAccessorModel.getByteStride(),
								positionsAccessorModel.getByteOffset());
						GL20.glEnableVertexAttribArray(skinning_position);
						
						tangentsAccessorModel = obtainTangentsAccessorModel(normalsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						List<AccessorFloatData> tangentTargetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createNormalTangentMorphTarget(morphTargets, normalsAccessorModel, tangentsAccessorModel, targetAccessorDatas, tangentTargetAccessorDatas)) {
							bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, normalsAccessorModel, targetAccessorDatas);
							GL20.glVertexAttribPointer(
									skinning_normal,
									normalsAccessorModel.getElementType().getNumComponents(),
									normalsAccessorModel.getComponentType(),
									false,
									normalsAccessorModel.getByteStride(),
									normalsAccessorModel.getByteOffset());
							GL20.glEnableVertexAttribArray(skinning_normal);
							
							bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, tangentsAccessorModel, tangentTargetAccessorDatas);
							GL20.glVertexAttribPointer(
									skinning_tangent,
									tangentsAccessorModel.getElementType().getNumComponents(),
									tangentsAccessorModel.getComponentType(),
									false,
									tangentsAccessorModel.getByteStride(),
									tangentsAccessorModel.getByteOffset());
							GL20.glEnableVertexAttribArray(skinning_tangent);
						}
						else {
							bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
							GL20.glVertexAttribPointer(
									skinning_normal,
									normalsAccessorModel.getElementType().getNumComponents(),
									normalsAccessorModel.getComponentType(),
									false,
									normalsAccessorModel.getByteStride(),
									normalsAccessorModel.getByteOffset());
							GL20.glEnableVertexAttribArray(skinning_normal);
							
							bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
							GL20.glVertexAttribPointer(
									skinning_tangent,
									tangentsAccessorModel.getElementType().getNumComponents(),
									tangentsAccessorModel.getComponentType(),
									false,
									tangentsAccessorModel.getByteStride(),
									tangentsAccessorModel.getByteOffset());
							GL20.glEnableVertexAttribArray(skinning_tangent);
						}
						
						int pointCount = positionsAccessorModel.getCount();
						skinningCommand.add(() -> {
							GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, glTransformFeedback);
							GL30.glBeginTransformFeedback(GL11.GL_POINTS);
							GL30.glBindVertexArray(glVertexArraySkinning);
							GL11.glDrawArrays(GL11.GL_POINTS, 0, pointCount);
							GL30.glEndTransformFeedback();
						});
						
						int outputPosition = GL15.glGenBuffers();
						gltfRenderData.addGlBufferView(outputPosition);
						GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputPosition);
						GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, positionsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
						GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_position, outputPosition);
						
						int outputNormal = GL15.glGenBuffers();
						gltfRenderData.addGlBufferView(outputNormal);
						GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputNormal);
						GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, normalsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
						GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_normal, outputNormal);
						
						int outputTangent = GL15.glGenBuffers();
						gltfRenderData.addGlBufferView(outputTangent);
						GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputTangent);
						GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, tangentsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
						GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_tangent, outputTangent);
						
						int glVertexArray = GL30.glGenVertexArrays();
						gltfRenderData.addGlVertexArray(glVertexArray);
						GL30.glBindVertexArray(glVertexArray);
						
						GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputPosition);
						GL11.glVertexPointer(positionsAccessorModel.getElementType().getNumComponents(), positionsAccessorModel.getComponentType(), 0, 0);
						GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
						
						GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputNormal);
						GL11.glNormalPointer(normalsAccessorModel.getComponentType(), 0, 0);
						GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
						
						GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputTangent);
						GL20.glVertexAttribPointer(
								at_tangent,
								tangentsAccessorModel.getElementType().getNumComponents(),
								tangentsAccessorModel.getComponentType(),
								false,
								0,
								0);
						GL20.glEnableVertexAttribArray(at_tangent);
						
						AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
						if(colorsAccessorModel != null) {
							colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
							targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
							if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
								colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
							}
							else {
								bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
							}
							GL11.glColorPointer(
									colorsAccessorModel.getElementType().getNumComponents(),
									colorsAccessorModel.getComponentType(),
									colorsAccessorModel.getByteStride(),
									colorsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
						}
						
						AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
						if(texcoordsAccessorModel != null) {
							targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
							if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
								texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
							}
							else {
								bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
							}
							GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
							GL11.glTexCoordPointer(
									texcoordsAccessorModel.getElementType().getNumComponents(),
									texcoordsAccessorModel.getComponentType(),
									texcoordsAccessorModel.getByteStride(),
									texcoordsAccessorModel.getByteOffset());
							GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
						}
						
						int mode = meshPrimitiveModel.getMode();
						AccessorModel indices = meshPrimitiveModel.getIndices();
						if(indices != null) {
							int glIndicesBufferView = obtainElementArrayBuffer(indices.getBufferViewModel());
							int count = indices.getCount();
							int type = indices.getComponentType();
							int offset = indices.getByteOffset();
							renderCommand.add(() -> {
								GL30.glBindVertexArray(glVertexArray);
								GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glIndicesBufferView);
								GL11.glDrawElements(mode, count, type, offset);
							});
						}
						else {
							renderCommand.add(() -> {
								GL30.glBindVertexArray(glVertexArray);
								GL40.glDrawTransformFeedback(mode, glTransformFeedback);
							});
						}
					}
				}
				else {
					AccessorModel jointsAccessorModel = attributes.get("JOINTS_0");
					bindArrayBufferViewModel(jointsAccessorModel.getBufferViewModel());
					GL20.glVertexAttribPointer(
							skinning_joint,
							jointsAccessorModel.getElementType().getNumComponents(),
							jointsAccessorModel.getComponentType(),
							false,
							jointsAccessorModel.getByteStride(),
							jointsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(skinning_joint);
					
					AccessorModel weightsAccessorModel = attributes.get("WEIGHTS_0");
					bindArrayBufferViewModel(weightsAccessorModel.getBufferViewModel());
					GL20.glVertexAttribPointer(
							skinning_weight,
							weightsAccessorModel.getElementType().getNumComponents(),
							weightsAccessorModel.getComponentType(),
							false,
							weightsAccessorModel.getByteStride(),
							weightsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(skinning_weight);
					
					List<Map<String, AccessorModel>> morphTargets = meshPrimitiveModel.getTargets();
					
					List<AccessorFloatData> targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createMorphTarget(morphTargets, targetAccessorDatas, "POSITION")) {
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, positionsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(positionsAccessorModel.getBufferViewModel());
					}
					GL20.glVertexAttribPointer(
							skinning_position,
							positionsAccessorModel.getElementType().getNumComponents(),
							positionsAccessorModel.getComponentType(),
							false,
							positionsAccessorModel.getByteStride(),
							positionsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(skinning_position);
					
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createMorphTarget(morphTargets, targetAccessorDatas, "NORMAL")) {
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, normalsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(normalsAccessorModel.getBufferViewModel());
					}
					GL20.glVertexAttribPointer(
							skinning_normal,
							normalsAccessorModel.getElementType().getNumComponents(),
							normalsAccessorModel.getComponentType(),
							false,
							normalsAccessorModel.getByteStride(),
							normalsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(skinning_normal);
					
					targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
					if(createMorphTarget(morphTargets, targetAccessorDatas, "TANGENT")) {
						bindVec3FloatMorphed(nodeModel, meshModel, skinningCommand, tangentsAccessorModel, targetAccessorDatas);
					}
					else {
						bindArrayBufferViewModel(tangentsAccessorModel.getBufferViewModel());
					}
					GL20.glVertexAttribPointer(
							skinning_tangent,
							tangentsAccessorModel.getElementType().getNumComponents(),
							tangentsAccessorModel.getComponentType(),
							false,
							tangentsAccessorModel.getByteStride(),
							tangentsAccessorModel.getByteOffset());
					GL20.glEnableVertexAttribArray(skinning_tangent);
					
					int pointCount = positionsAccessorModel.getCount();
					skinningCommand.add(() -> {
						GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, glTransformFeedback);
						GL30.glBeginTransformFeedback(GL11.GL_POINTS);
						GL30.glBindVertexArray(glVertexArraySkinning);
						GL11.glDrawArrays(GL11.GL_POINTS, 0, pointCount);
						GL30.glEndTransformFeedback();
					});
					
					int outputPosition = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputPosition);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputPosition);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, positionsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_position, outputPosition);
					
					int outputNormal = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputNormal);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputNormal);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, normalsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_normal, outputNormal);
					
					int outputTangent = GL15.glGenBuffers();
					gltfRenderData.addGlBufferView(outputTangent);
					GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, outputTangent);
					GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, tangentsAccessorModel.getBufferViewModel().getByteLength(), GL15.GL_STATIC_DRAW);
					GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, skinning_out_tangent, outputTangent);
					
					int glVertexArray = GL30.glGenVertexArrays();
					gltfRenderData.addGlVertexArray(glVertexArray);
					GL30.glBindVertexArray(glVertexArray);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputPosition);
					GL11.glVertexPointer(positionsAccessorModel.getElementType().getNumComponents(), positionsAccessorModel.getComponentType(), 0, 0);
					GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputNormal);
					GL11.glNormalPointer(normalsAccessorModel.getComponentType(), 0, 0);
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
					
					GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, outputTangent);
					GL20.glVertexAttribPointer(
							at_tangent,
							tangentsAccessorModel.getElementType().getNumComponents(),
							tangentsAccessorModel.getComponentType(),
							false,
							0,
							0);
					GL20.glEnableVertexAttribArray(at_tangent);
					
					AccessorModel colorsAccessorModel = attributes.get("COLOR_0");
					if(colorsAccessorModel != null) {
						colorsAccessorModel = obtainVec4ColorsAccessorModel(colorsAccessorModel);
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createColorMorphTarget(morphTargets, targetAccessorDatas)) {
							colorsAccessorModel = bindColorMorphed(nodeModel, meshModel, renderCommand, colorsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(colorsAccessorModel.getBufferViewModel());
						}
						GL11.glColorPointer(
								colorsAccessorModel.getElementType().getNumComponents(),
								colorsAccessorModel.getComponentType(),
								colorsAccessorModel.getByteStride(),
								colorsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
					}
					
					AccessorModel texcoordsAccessorModel = attributes.get("TEXCOORD_0");
					if(texcoordsAccessorModel != null) {
						targetAccessorDatas = new ArrayList<AccessorFloatData>(morphTargets.size());
						if(createTexcoordMorphTarget(morphTargets, targetAccessorDatas)) {
							texcoordsAccessorModel = bindTexcoordMorphed(nodeModel, meshModel, renderCommand, texcoordsAccessorModel, targetAccessorDatas);
						}
						else {
							bindArrayBufferViewModel(texcoordsAccessorModel.getBufferViewModel());
						}
						GL13.glClientActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
						GL11.glTexCoordPointer(
								texcoordsAccessorModel.getElementType().getNumComponents(),
								texcoordsAccessorModel.getComponentType(),
								texcoordsAccessorModel.getByteStride(),
								texcoordsAccessorModel.getByteOffset());
						GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					}
					
					int mode = meshPrimitiveModel.getMode();
					AccessorModel indices = meshPrimitiveModel.getIndices();
					if(indices != null) {
						int glIndicesBufferView = obtainElementArrayBuffer(indices.getBufferViewModel());
						int count = indices.getCount();
						int type = indices.getComponentType();
						int offset = indices.getByteOffset();
						renderCommand.add(() -> {
							GL30.glBindVertexArray(glVertexArray);
							GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glIndicesBufferView);
							GL11.glDrawElements(mode, count, type, offset);
						});
					}
					else {
						renderCommand.add(() -> {
							GL30.glBindVertexArray(glVertexArray);
							GL40.glDrawTransformFeedback(mode, glTransformFeedback);
						});
					}
				}
			}
			
			materialCommand = materialHandler.getPostMeshDrawCommand();
			if(materialCommand != null) renderCommand.add(materialCommand);
		}
	}
	
	private void bindArrayBufferViewModel(BufferViewModel bufferViewModel) {
		Integer glBufferView = bufferViewModelToGlBufferView.get(bufferViewModel);
		if(glBufferView == null) {
			glBufferView = GL15.glGenBuffers();
			gltfRenderData.addGlBufferView(glBufferView);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufferViewModel.getBufferViewData(), GL15.GL_STATIC_DRAW);
			bufferViewModelToGlBufferView.put(bufferViewModel, glBufferView);
		}
		else GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
	}
	
	private Runnable createTransformCommand(NodeModel nodeModel) {
		return () -> {
			GL11.glPushMatrix();
			BUF_FLOAT_16.clear();
			BUF_FLOAT_16.put(findGlobalTransform(nodeModel));
			BUF_FLOAT_16.rewind();
			GL11.glMultMatrix(BUF_FLOAT_16);
		};
	}
	
	public int obtainGlTexture(TextureModel textureModel) {
		Integer glTexture = textureModelToGlTexture.get(textureModel);
		if(glTexture == null) {
			PixelData pixelData = PixelDatas.create(textureModel.getImageModel().getImageData());
			if (pixelData == null)
			{
				MCglTF.logger.warn("Could not extract pixel data from image");
				pixelData = PixelDatas.createErrorPixelData();
			}
			
			glTexture = GL11.glGenTextures();
			gltfRenderData.addGlTexture(glTexture);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTexture);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, pixelData.getWidth(), pixelData.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelData.getPixelsRGBA());
		
			int minFilter = Optionals.of(
				textureModel.getMinFilter(), 
				GL11.GL_NEAREST_MIPMAP_LINEAR);
			int magFilter = Optionals.of(
				textureModel.getMagFilter(),
				GL11.GL_LINEAR);
			int wrapS = Optionals.of(
				textureModel.getWrapS(),
				GL11.GL_REPEAT);
			int wrapT = Optionals.of(
				textureModel.getWrapT(),
				GL11.GL_REPEAT);
			
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapS);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapT);
			
			textureModelToGlTexture.put(textureModel, glTexture);
		}
		return glTexture;
	}
	
	private IMaterialHandler obtainMaterialHandler(MaterialModel materialModel) {
		IMaterialHandler materialHandler = materialModelToMaterialHandler.get(materialModel);
		if(materialHandler == null) {
			Object extras = materialModel.getExtras();
			if(extras != null) {
				Gson gson = new Gson();
				JsonElement reference = gson.toJsonTree(extras).getAsJsonObject().get(MCglTF.MATERIAL_HANDLER);
				if(reference != null) {
					BiFunction<RenderedGltfModel, MaterialModel, IMaterialHandler> materialHandlerFactory = MCglTF.getInstance().getMaterialHandlerFactory(new ResourceLocation(reference.getAsString()));
					if(materialHandlerFactory == null) {
						MCglTF.logger.error("Can not find Material Handler {}, the materials for this model will not display properly!", reference.getAsString());
						materialHandler = IMaterialHandler.DEFAULT_INSTANCE;
					}
					else {
						materialHandler = materialHandlerFactory.apply(this, materialModel);
					}
				}
				else {
					DefaultMaterialHandler defaultMaterialHandler = gson.fromJson(gson.toJsonTree(extras), DefaultMaterialHandler.class);
					List<TextureModel> textureModels = gltfModel.getTextureModels();
					int colorMap = defaultMaterialHandler.baseColorTexture == null ? MCglTF.getInstance().getDefaultColorMap() : obtainGlTexture(textureModels.get(defaultMaterialHandler.baseColorTexture.index));
					int normalMap = defaultMaterialHandler.normalTexture == null ? MCglTF.getInstance().getDefaultNormalMap() : obtainGlTexture(textureModels.get(defaultMaterialHandler.normalTexture.index));
					int specularMap = defaultMaterialHandler.specularTexture == null ? MCglTF.getInstance().getDefaultSpecularMap() : obtainGlTexture(textureModels.get(defaultMaterialHandler.specularTexture.index));
					
					float r = defaultMaterialHandler.baseColorFactor[0];
					float g = defaultMaterialHandler.baseColorFactor[1];
					float b = defaultMaterialHandler.baseColorFactor[2];
					float a = defaultMaterialHandler.baseColorFactor[3];
					
					if(defaultMaterialHandler.doubleSided) {
						defaultMaterialHandler.preMeshDrawCommand = () -> {
							GL13.glActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
							GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorMap);
							GL13.glActiveTexture(IMaterialHandler.NORMAL_MAP_INDEX);
							GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMap);
							GL13.glActiveTexture(IMaterialHandler.SPECULAR_MAP_INDEX);
							GL11.glBindTexture(GL11.GL_TEXTURE_2D, specularMap);
							GL11.glColor4f(r, g, b, a);
							GL11.glDisable(GL11.GL_CULL_FACE);
						};
					}
					else {
						defaultMaterialHandler.preMeshDrawCommand = () -> {
							GL13.glActiveTexture(IMaterialHandler.COLOR_MAP_INDEX);
							GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorMap);
							GL13.glActiveTexture(IMaterialHandler.NORMAL_MAP_INDEX);
							GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMap);
							GL13.glActiveTexture(IMaterialHandler.SPECULAR_MAP_INDEX);
							GL11.glBindTexture(GL11.GL_TEXTURE_2D, specularMap);
							GL11.glColor4f(r, g, b, a);
							GL11.glEnable(GL11.GL_CULL_FACE);
						};
					}
					materialHandler = defaultMaterialHandler;
				}
			}
			else {
				materialHandler = IMaterialHandler.DEFAULT_INSTANCE;
			}
			materialModelToMaterialHandler.put(materialModel, materialHandler);
		}
		return materialHandler;
	}
	
	private int obtainElementArrayBuffer(BufferViewModel bufferViewModel) {
		Integer glBufferView = bufferViewModelToGlBufferView.get(bufferViewModel);
		if(glBufferView == null) {
			glBufferView = GL15.glGenBuffers();
			gltfRenderData.addGlBufferView(glBufferView);
			GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, glBufferView);
			GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, bufferViewModel.getBufferViewData(), GL15.GL_STATIC_DRAW);
			bufferViewModelToGlBufferView.put(bufferViewModel, glBufferView);
		}
		return glBufferView;
	}
	
	private AccessorModel obtainNormalsAccessorModel(AccessorModel positionsAccessorModel) {
		AccessorModel normalsAccessorModel = positionsAccessorModelToNormalsAccessorModel.get(positionsAccessorModel);
		if(normalsAccessorModel == null) {
			int count = positionsAccessorModel.getCount();
			int numTriangles = count / 3;
			normalsAccessorModel = AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, "");
			positionsAccessorModelToNormalsAccessorModel.put(positionsAccessorModel, normalsAccessorModel);
			AccessorFloatData positionsAccessorData = AccessorDatas.createFloat(positionsAccessorModel);
			AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
			float vertex0[] = new float[3];
			float vertex1[] = new float[3];
			float vertex2[] = new float[3];
			float edge01[] = new float[3];
			float edge02[] = new float[3];
			float cross[] = new float[3];
			float normal[] = new float[3];
			for(int i = 0; i < numTriangles; i++) {
				int index0 = i * 3;
				int index1 = index0 + 1;
				int index2 = index0 + 2;
				
				vertex0[0] = positionsAccessorData.get(index0, 0);
				vertex0[1] = positionsAccessorData.get(index0, 1);
				vertex0[2] = positionsAccessorData.get(index0, 2);
				
				vertex1[0] = positionsAccessorData.get(index1, 0);
				vertex1[1] = positionsAccessorData.get(index1, 1);
				vertex1[2] = positionsAccessorData.get(index1, 2);
				
				vertex2[0] = positionsAccessorData.get(index2, 0);
				vertex2[1] = positionsAccessorData.get(index2, 1);
				vertex2[2] = positionsAccessorData.get(index2, 2);
				
				subtract(vertex1, vertex0, edge01);
				subtract(vertex2, vertex0, edge02);
				cross(edge01, edge02, cross);
				normalize(cross, normal);
				
				normalsAccessorData.set(index0, 0, normal[0]);
				normalsAccessorData.set(index0, 1, normal[1]);
				normalsAccessorData.set(index0, 2, normal[2]);
				
				normalsAccessorData.set(index1, 0, normal[0]);
				normalsAccessorData.set(index1, 1, normal[1]);
				normalsAccessorData.set(index1, 2, normal[2]);
				
				normalsAccessorData.set(index2, 0, normal[0]);
				normalsAccessorData.set(index2, 1, normal[1]);
				normalsAccessorData.set(index2, 2, normal[2]);
			}
		}
		return normalsAccessorModel;
	}
	
	/**
	 * Found this simple normals to tangent algorithm here:</br>
	 * <a href="https://stackoverflow.com/questions/55464852/how-to-find-a-randomic-vector-orthogonal-to-a-given-vector">How to find a randomic Vector orthogonal to a given Vector</a>
	 */
	private AccessorModel obtainTangentsAccessorModel(AccessorModel normalsAccessorModel) {
		AccessorModel tangentsAccessorModel = normalsAccessorModelToTangentsAccessorModel.get(normalsAccessorModel);
		if(tangentsAccessorModel == null) {
			int count = normalsAccessorModel.getCount();
			tangentsAccessorModel = AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC4, "");
			normalsAccessorModelToTangentsAccessorModel.put(normalsAccessorModel, tangentsAccessorModel);
			AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
			AccessorFloatData tangentsAccessorData = AccessorDatas.createFloat(tangentsAccessorModel);
			float[] normal0 = new float[3];
			float[] normal1 = new float[3];
			float[] cross = new float[3];
			float[] tangent = new float[3];
			
			for(int i = 0; i < count; i++) {
				normal0[0] = normalsAccessorData.get(i, 0);
				normal0[1] = normalsAccessorData.get(i, 1);
				normal0[2] = normalsAccessorData.get(i, 2);
				
				normal1[0] = -normal0[2];
				normal1[1] = normal0[0];
				normal1[2] = normal0[1];
				
				cross(normal0, normal1, cross);
				normalize(cross, tangent);
				
				tangentsAccessorData.set(i, 0, tangent[0]);
				tangentsAccessorData.set(i, 1, tangent[1]);
				tangentsAccessorData.set(i, 2, tangent[2]);
				tangentsAccessorData.set(i, 3, 1.0F);
			}
		}
		return tangentsAccessorModel;
	}
	
	private AccessorModel obtainTangentsAccessorModel(MeshPrimitiveModel meshPrimitiveModel, AccessorModel positionsAccessorModel, AccessorModel normalsAccessorModel, AccessorModel texcoordsAccessorModel) {
		AccessorModel tangentsAccessorModel = meshPrimitiveModelToTangentsAccessorModel.get(meshPrimitiveModel);
		if(tangentsAccessorModel == null) {
			int count = positionsAccessorModel.getCount();
			int numFaces = count / 3;
			tangentsAccessorModel = AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC4, "");
			meshPrimitiveModelToTangentsAccessorModel.put(meshPrimitiveModel, tangentsAccessorModel);
			AccessorFloatData positionsAccessorData = AccessorDatas.createFloat(positionsAccessorModel);
			AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
			AccessorData texcoordsAccessorData = AccessorDatas.create(texcoordsAccessorModel);
			AccessorFloatData tangentsAccessorData = AccessorDatas.createFloat(tangentsAccessorModel);
			
			MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {
	
				@Override
				public int getNumFaces() {
					return numFaces;
				}
	
				@Override
				public int getNumVerticesOfFace(int face) {
					return 3;
				}
	
				@Override
				public void getPosition(float[] posOut, int face, int vert) {
					int index = (face * 3) + vert;
					posOut[0] = positionsAccessorData.get(index, 0);
					posOut[1] = positionsAccessorData.get(index, 1);
					posOut[2] = positionsAccessorData.get(index, 2);
				}
	
				@Override
				public void getNormal(float[] normOut, int face, int vert) {
					int index = (face * 3) + vert;
					normOut[0] = normalsAccessorData.get(index, 0);
					normOut[1] = normalsAccessorData.get(index, 1);
					normOut[2] = normalsAccessorData.get(index, 2);
				}
	
				@Override
				public void getTexCoord(float[] texOut, int face, int vert) {
					int index = (face * 3) + vert;
					texOut[0] = texcoordsAccessorData.getFloat(index, 0);
					texOut[1] = texcoordsAccessorData.getFloat(index, 1);
				}
	
				@Override
				public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
					int index = (face * 3) + vert;
					tangentsAccessorData.set(index, 0, tangent[0]);
					tangentsAccessorData.set(index, 1, tangent[1]);
					tangentsAccessorData.set(index, 2, tangent[2]);
					tangentsAccessorData.set(index, 3, -sign);
				}
	
				@Override
				public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
					//Do nothing
				}
				
			});
		}
		return tangentsAccessorModel;
	}
	
	private AccessorModel obtainVec4ColorsAccessorModel(AccessorModel colorsAccessorModel) {
		if(colorsAccessorModel.getElementType() == ElementType.VEC3) {
			AccessorModel colorsVec4AccessorModel = colorsAccessorModelVec3ToVec4.get(colorsAccessorModel);
			if(colorsVec4AccessorModel == null) {
				int count = colorsAccessorModel.getCount();
				colorsVec4AccessorModel = AccessorModelCreation.createAccessorModel(colorsAccessorModel.getComponentType(), count, ElementType.VEC4, "");
				colorsAccessorModelVec3ToVec4.put(colorsAccessorModel, colorsVec4AccessorModel);
				AccessorData accessorData = AccessorDatas.create(colorsVec4AccessorModel);
				if(accessorData instanceof AccessorByteData) {
					AccessorByteData colorsVec4AccessorData = (AccessorByteData) accessorData;
					AccessorByteData colorsAccessorData = AccessorDatas.createByte(colorsAccessorModel);
					if(colorsAccessorData.isUnsigned()) {
						for(int i = 0; i < count; i++) {
							colorsVec4AccessorData.set(i, 0, colorsAccessorData.get(i, 0));
							colorsVec4AccessorData.set(i, 1, colorsAccessorData.get(i, 1));
							colorsVec4AccessorData.set(i, 2, colorsAccessorData.get(i, 2));
							colorsVec4AccessorData.set(i, 3, (byte) -1);
						}
					}
					else {
						for(int i = 0; i < count; i++) {
							colorsVec4AccessorData.set(i, 0, colorsAccessorData.get(i, 0));
							colorsVec4AccessorData.set(i, 1, colorsAccessorData.get(i, 1));
							colorsVec4AccessorData.set(i, 2, colorsAccessorData.get(i, 2));
							colorsVec4AccessorData.set(i, 3, Byte.MAX_VALUE);
						}
					}
				}
				else if(accessorData instanceof AccessorShortData) {
					AccessorShortData colorsVec4AccessorData = (AccessorShortData) accessorData;
					AccessorShortData colorsAccessorData = AccessorDatas.createShort(colorsAccessorModel);
					if(colorsAccessorData.isUnsigned()) {
						for(int i = 0; i < count; i++) {
							colorsVec4AccessorData.set(i, 0, colorsAccessorData.get(i, 0));
							colorsVec4AccessorData.set(i, 1, colorsAccessorData.get(i, 1));
							colorsVec4AccessorData.set(i, 2, colorsAccessorData.get(i, 2));
							colorsVec4AccessorData.set(i, 3, (short) -1);
						}
					}
					else {
						for(int i = 0; i < count; i++) {
							colorsVec4AccessorData.set(i, 0, colorsAccessorData.get(i, 0));
							colorsVec4AccessorData.set(i, 1, colorsAccessorData.get(i, 1));
							colorsVec4AccessorData.set(i, 2, colorsAccessorData.get(i, 2));
							colorsVec4AccessorData.set(i, 3, Short.MAX_VALUE);
						}
					}
				}
				else if(accessorData instanceof AccessorFloatData) {
					AccessorFloatData colorsVec4AccessorData = (AccessorFloatData) accessorData;
					AccessorFloatData colorsAccessorData = AccessorDatas.createFloat(colorsAccessorModel);
					for(int i = 0; i < count; i++) {
						colorsVec4AccessorData.set(i, 0, colorsAccessorData.get(i, 0));
						colorsVec4AccessorData.set(i, 1, colorsAccessorData.get(i, 1));
						colorsVec4AccessorData.set(i, 2, colorsAccessorData.get(i, 2));
						colorsVec4AccessorData.set(i, 3, 1.0F);
					}
				}
			}
			return colorsVec4AccessorModel;
		}
		return colorsAccessorModel;
	}
	
	private Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>> obtainUnindexed(MeshPrimitiveModel meshPrimitiveModel) {
		Pair<Map<String, AccessorModel>, List<Map<String, AccessorModel>>> unindexed;
		AccessorModel indicesAccessorModel = meshPrimitiveModel.getIndices();
		if(indicesAccessorModel != null) {
			unindexed = meshPrimitiveModelToUnindexed.get(meshPrimitiveModel);
			if(unindexed == null) {
				int indices[] = AccessorDataUtils.readInts(AccessorDatas.create(indicesAccessorModel));
				Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
				Map<String, AccessorModel> attributesUnindexed = new LinkedHashMap<String, AccessorModel>(attributes.size());
				attributes.forEach((name, attribute) -> {
					ElementType elementType = attribute.getElementType();
					int size = elementType.getNumComponents();
					AccessorModel accessorModel = AccessorModelCreation.createAccessorModel(attribute.getComponentType(), indices.length, elementType, "");
					attributesUnindexed.put(name, accessorModel);
					AccessorData accessorData = AccessorDatas.create(accessorModel);
					if(accessorData instanceof AccessorByteData) {
						AccessorByteData accessorDataUnindexed = (AccessorByteData) accessorData;
						AccessorByteData accessorDataIndexed = AccessorDatas.createByte(attribute);
						for(int i = 0; i < indices.length; i++) {
							int index = indices[i];
							for(int j = 0; j < size; j++) {
								accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
							}
						}
					}
					else if(accessorData instanceof AccessorShortData) {
						AccessorShortData accessorDataUnindexed = (AccessorShortData) accessorData;
						AccessorShortData accessorDataIndexed = AccessorDatas.createShort(attribute);
						for(int i = 0; i < indices.length; i++) {
							int index = indices[i];
							for(int j = 0; j < size; j++) {
								accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
							}
						}
					}
					else if(accessorData instanceof AccessorIntData) {
						AccessorIntData accessorDataUnindexed = (AccessorIntData) accessorData;
						AccessorIntData accessorDataIndexed = AccessorDatas.createInt(attribute);
						for(int i = 0; i < indices.length; i++) {
							int index = indices[i];
							for(int j = 0; j < size; j++) {
								accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
							}
						}
					}
					else if(accessorData instanceof AccessorFloatData) {
						AccessorFloatData accessorDataUnindexed = (AccessorFloatData) accessorData;
						AccessorFloatData accessorDataIndexed = AccessorDatas.createFloat(attribute);
						for(int i = 0; i < indices.length; i++) {
							int index = indices[i];
							for(int j = 0; j < size; j++) {
								accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
							}
						}
					}
				});
				
				List<Map<String, AccessorModel>> targets = meshPrimitiveModel.getTargets();
				List<Map<String, AccessorModel>> targetsUnindexed = new ArrayList<Map<String, AccessorModel>>(targets.size());
				targets.forEach((target) -> {
					Map<String, AccessorModel> targetUnindexed = new LinkedHashMap<String, AccessorModel>(target.size());
					targetsUnindexed.add(targetUnindexed);
					target.forEach((name, attribute) -> {
						ElementType elementType = attribute.getElementType();
						int size = elementType.getNumComponents();
						AccessorModel accessorModel = AccessorModelCreation.createAccessorModel(attribute.getComponentType(), indices.length, elementType, "");
						targetUnindexed.put(name, accessorModel);
						AccessorData accessorData = AccessorDatas.create(accessorModel);
						if(accessorData instanceof AccessorByteData) {
							AccessorByteData accessorDataUnindexed = (AccessorByteData) accessorData;
							AccessorByteData accessorDataIndexed = AccessorDatas.createByte(attribute);
							for(int i = 0; i < indices.length; i++) {
								int index = indices[i];
								for(int j = 0; j < size; j++) {
									accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
								}
							}
						}
						else if(accessorData instanceof AccessorShortData) {
							AccessorShortData accessorDataUnindexed = (AccessorShortData) accessorData;
							AccessorShortData accessorDataIndexed = AccessorDatas.createShort(attribute);
							for(int i = 0; i < indices.length; i++) {
								int index = indices[i];
								for(int j = 0; j < size; j++) {
									accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
								}
							}
						}
						else if(accessorData instanceof AccessorIntData) {
							AccessorIntData accessorDataUnindexed = (AccessorIntData) accessorData;
							AccessorIntData accessorDataIndexed = AccessorDatas.createInt(attribute);
							for(int i = 0; i < indices.length; i++) {
								int index = indices[i];
								for(int j = 0; j < size; j++) {
									accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
								}
							}
						}
						else if(accessorData instanceof AccessorFloatData) {
							AccessorFloatData accessorDataUnindexed = (AccessorFloatData) accessorData;
							AccessorFloatData accessorDataIndexed = AccessorDatas.createFloat(attribute);
							for(int i = 0; i < indices.length; i++) {
								int index = indices[i];
								for(int j = 0; j < size; j++) {
									accessorDataUnindexed.set(i, j, accessorDataIndexed.get(index, j));
								}
							}
						}
					});
				});
				unindexed = Pair.of(attributesUnindexed, targetsUnindexed);
				meshPrimitiveModelToUnindexed.put(meshPrimitiveModel, unindexed);
			}
		}
		else unindexed = Pair.of(meshPrimitiveModel.getAttributes(), meshPrimitiveModel.getTargets());
		return unindexed;
	}
	
	private boolean createMorphTarget(List<Map<String, AccessorModel>> morphTargets, List<AccessorFloatData> targetAccessorDatas, String attributeName) {
		boolean isMorphableAttribute = false;
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorModel accessorModel = morphTarget.get(attributeName);
			if(accessorModel != null) {
				isMorphableAttribute = true;
				targetAccessorDatas.add(AccessorDatas.createFloat(accessorModel));
			}
			else targetAccessorDatas.add(null);
		}
		return isMorphableAttribute;
	}
	
	private boolean createPositionNormalMorphTarget(List<Map<String, AccessorModel>> morphTargets, AccessorModel positionsAccessorModel, AccessorModel normalsAccessorModel, List<AccessorFloatData> positionTargetAccessorDatas, List<AccessorFloatData> normalTargetAccessorDatas) {
		boolean isMorphableAttribute = false;
		int count = positionsAccessorModel.getCount();
		int numTriangles = count / 3;
		AccessorFloatData positionsAccessorData = AccessorDatas.createFloat(positionsAccessorModel);
		AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorModel accessorModel = morphTarget.get("POSITION");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData deltaPositionsAccessorData = AccessorDatas.createFloat(accessorModel);
				positionTargetAccessorDatas.add(deltaPositionsAccessorData);
				AccessorFloatData normalTargetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				normalTargetAccessorDatas.add(normalTargetAccessorData);
				float[] vertex0 = new float[3];
				float[] vertex1 = new float[3];
				float[] vertex2 = new float[3];
				float[] edge01 = new float[3];
				float[] edge02 = new float[3];
				float[] cross = new float[3];
				float[] normal0 = new float[3];
				float[] normal1 = new float[3];
				for(int i = 0; i < numTriangles; i++) {
					int index0 = i * 3;
					int index1 = index0 + 1;
					int index2 = index0 + 2;
					
					vertex0[0] = positionsAccessorData.get(index0, 0) + deltaPositionsAccessorData.get(index0, 0);
					vertex0[1] = positionsAccessorData.get(index0, 1) + deltaPositionsAccessorData.get(index0, 1);
					vertex0[2] = positionsAccessorData.get(index0, 2) + deltaPositionsAccessorData.get(index0, 2);
					
					vertex1[0] = positionsAccessorData.get(index1, 0) + deltaPositionsAccessorData.get(index1, 0);
					vertex1[1] = positionsAccessorData.get(index1, 1) + deltaPositionsAccessorData.get(index1, 1);
					vertex1[2] = positionsAccessorData.get(index1, 2) + deltaPositionsAccessorData.get(index1, 2);
					
					vertex2[0] = positionsAccessorData.get(index2, 0) + deltaPositionsAccessorData.get(index2, 0);
					vertex2[1] = positionsAccessorData.get(index2, 1) + deltaPositionsAccessorData.get(index2, 1);
					vertex2[2] = positionsAccessorData.get(index2, 2) + deltaPositionsAccessorData.get(index2, 2);
					
					normal0[0] = normalsAccessorData.get(index0, 0);
					normal0[1] = normalsAccessorData.get(index0, 1);
					normal0[2] = normalsAccessorData.get(index0, 2);
					
					subtract(vertex1, vertex0, edge01);
					subtract(vertex2, vertex0, edge02);
					cross(edge01, edge02, cross);
					normalize(cross, normal1);
					
					subtract(normal1, normal0, normal1);
					
					normalTargetAccessorData.set(index0, 0, normal1[0]);
					normalTargetAccessorData.set(index0, 1, normal1[1]);
					normalTargetAccessorData.set(index0, 2, normal1[2]);
					
					normalTargetAccessorData.set(index1, 0, normal1[0]);
					normalTargetAccessorData.set(index1, 1, normal1[1]);
					normalTargetAccessorData.set(index1, 2, normal1[2]);
					
					normalTargetAccessorData.set(index2, 0, normal1[0]);
					normalTargetAccessorData.set(index2, 1, normal1[1]);
					normalTargetAccessorData.set(index2, 2, normal1[2]);
				}
			}
			else {
				positionTargetAccessorDatas.add(null);
				normalTargetAccessorDatas.add(null);
			}
		}
		return isMorphableAttribute;
	}
	
	private boolean createPositionNormalTangentMorphTarget(List<Map<String, AccessorModel>> morphTargets, AccessorModel positionsAccessorModel, AccessorModel normalsAccessorModel, AccessorModel tangentsAccessorModel, List<AccessorFloatData> positionTargetAccessorDatas, List<AccessorFloatData> normalTargetAccessorDatas, List<AccessorFloatData> tangentTargetAccessorDatas) {
		boolean isMorphableAttribute = false;
		int count = positionsAccessorModel.getCount();
		int numTriangles = count / 3;
		AccessorFloatData positionsAccessorData = AccessorDatas.createFloat(positionsAccessorModel);
		AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
		AccessorFloatData tangentsAccessorData = AccessorDatas.createFloat(tangentsAccessorModel);
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorModel accessorModel = morphTarget.get("POSITION");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData deltaPositionsAccessorData = AccessorDatas.createFloat(accessorModel);
				positionTargetAccessorDatas.add(deltaPositionsAccessorData);
				AccessorFloatData normalTargetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				normalTargetAccessorDatas.add(normalTargetAccessorData);
				AccessorFloatData tangentTargetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				tangentTargetAccessorDatas.add(tangentTargetAccessorData);
				float[] vertex0 = new float[3];
				float[] vertex1 = new float[3];
				float[] vertex2 = new float[3];
				float[] edge01 = new float[3];
				float[] edge02 = new float[3];
				float[] cross = new float[3];
				float[] normal0 = new float[3];
				float[] normal1 = new float[3];
				float[] normal2 = new float[3];
				float[] tangent0 = new float[3];
				float[] tangent1 = new float[3];
				for(int i = 0; i < numTriangles; i++) {
					int index0 = i * 3;
					int index1 = index0 + 1;
					int index2 = index0 + 2;
					
					vertex0[0] = positionsAccessorData.get(index0, 0) + deltaPositionsAccessorData.get(index0, 0);
					vertex0[1] = positionsAccessorData.get(index0, 1) + deltaPositionsAccessorData.get(index0, 1);
					vertex0[2] = positionsAccessorData.get(index0, 2) + deltaPositionsAccessorData.get(index0, 2);
					
					vertex1[0] = positionsAccessorData.get(index1, 0) + deltaPositionsAccessorData.get(index1, 0);
					vertex1[1] = positionsAccessorData.get(index1, 1) + deltaPositionsAccessorData.get(index1, 1);
					vertex1[2] = positionsAccessorData.get(index1, 2) + deltaPositionsAccessorData.get(index1, 2);
					
					vertex2[0] = positionsAccessorData.get(index2, 0) + deltaPositionsAccessorData.get(index2, 0);
					vertex2[1] = positionsAccessorData.get(index2, 1) + deltaPositionsAccessorData.get(index2, 1);
					vertex2[2] = positionsAccessorData.get(index2, 2) + deltaPositionsAccessorData.get(index2, 2);
					
					normal0[0] = normalsAccessorData.get(index0, 0);
					normal0[1] = normalsAccessorData.get(index0, 1);
					normal0[2] = normalsAccessorData.get(index0, 2);
					
					tangent0[0] = tangentsAccessorData.get(index0, 0);
					tangent0[1] = tangentsAccessorData.get(index0, 1);
					tangent0[2] = tangentsAccessorData.get(index0, 2);
					
					subtract(vertex1, vertex0, edge01);
					subtract(vertex2, vertex0, edge02);
					cross(edge01, edge02, cross);
					normalize(cross, normal1);
					
					normal2[0] = -normal1[2];
					normal2[1] = normal1[0];
					normal2[2] = normal1[1];
					
					cross(normal1, normal2, cross);
					normalize(cross, tangent1);
					
					subtract(normal1, normal0, normal1);
					subtract(tangent1, tangent0, tangent1);
					
					normalTargetAccessorData.set(index0, 0, normal1[0]);
					normalTargetAccessorData.set(index0, 1, normal1[1]);
					normalTargetAccessorData.set(index0, 2, normal1[2]);
					
					tangentTargetAccessorData.set(index0, 0, tangent1[0]);
					tangentTargetAccessorData.set(index0, 1, tangent1[1]);
					tangentTargetAccessorData.set(index0, 2, tangent1[2]);
					
					normalTargetAccessorData.set(index1, 0, normal1[0]);
					normalTargetAccessorData.set(index1, 1, normal1[1]);
					normalTargetAccessorData.set(index1, 2, normal1[2]);
					
					tangentTargetAccessorData.set(index1, 0, tangent1[0]);
					tangentTargetAccessorData.set(index1, 1, tangent1[1]);
					tangentTargetAccessorData.set(index1, 2, tangent1[2]);
					
					normalTargetAccessorData.set(index2, 0, normal1[0]);
					normalTargetAccessorData.set(index2, 1, normal1[1]);
					normalTargetAccessorData.set(index2, 2, normal1[2]);
					
					tangentTargetAccessorData.set(index2, 0, tangent1[0]);
					tangentTargetAccessorData.set(index2, 1, tangent1[1]);
					tangentTargetAccessorData.set(index2, 2, tangent1[2]);
				}
			}
			else {
				positionTargetAccessorDatas.add(null);
				normalTargetAccessorDatas.add(null);
				tangentTargetAccessorDatas.add(null);
			}
		}
		return isMorphableAttribute;
	}
	
	private boolean createNormalTangentMorphTarget(List<Map<String, AccessorModel>> morphTargets, AccessorModel normalsAccessorModel, AccessorModel tangentsAccessorModel, List<AccessorFloatData> normalTargetAccessorDatas, List<AccessorFloatData> tangentTargetAccessorDatas) {
		boolean isMorphableAttribute = false;
		int count = normalsAccessorModel.getCount();
		AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
		AccessorFloatData tangentsAccessorData = AccessorDatas.createFloat(tangentsAccessorModel);
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorModel accessorModel = morphTarget.get("NORMAL");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData deltaNormalsAccessorData = AccessorDatas.createFloat(accessorModel);
				normalTargetAccessorDatas.add(deltaNormalsAccessorData);
				AccessorFloatData tangentTargetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				tangentTargetAccessorDatas.add(tangentTargetAccessorData);
				float[] normal0 = new float[3];
				float[] normal1 = new float[3];
				float[] cross = new float[3];
				float[] tangent = new float[3];
				
				for(int i = 0; i < count; i++) {
					normal0[0] = normalsAccessorData.get(i, 0) + deltaNormalsAccessorData.get(i, 0);
					normal0[1] = normalsAccessorData.get(i, 1) + deltaNormalsAccessorData.get(i, 1);
					normal0[2] = normalsAccessorData.get(i, 2) + deltaNormalsAccessorData.get(i, 2);
					
					normal1[0] = -normal0[2];
					normal1[1] = normal0[0];
					normal1[2] = normal0[1];
					
					cross(normal0, normal1, cross);
					normalize(cross, tangent);
					
					tangentTargetAccessorData.set(i, 0, tangent[0] - tangentsAccessorData.get(i, 0));
					tangentTargetAccessorData.set(i, 1, tangent[1] - tangentsAccessorData.get(i, 1));
					tangentTargetAccessorData.set(i, 2, tangent[2] - tangentsAccessorData.get(i, 2));
				}
			}
			else {
				normalTargetAccessorDatas.add(null);
				tangentTargetAccessorDatas.add(null);
			}
		}
		return isMorphableAttribute;
	}
	
	private boolean createTangentMorphTarget(List<Map<String, AccessorModel>> morphTargets, List<AccessorFloatData> targetAccessorDatas, AccessorModel positionsAccessorModel, AccessorModel normalsAccessorModel, AccessorModel texcoordsAccessorModel, AccessorModel tangentsAccessorModel) {
		boolean isMorphableAttribute = false;
		int count = positionsAccessorModel.getCount();
		int numFaces = count / 3;
		AccessorFloatData positionsAccessorData = AccessorDatas.createFloat(positionsAccessorModel);
		AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
		AccessorFloatData tangentsAccessorData = AccessorDatas.createFloat(tangentsAccessorModel);
		AccessorData texcoordsAccessorData = AccessorDatas.create(texcoordsAccessorModel);
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorModel accessorModel = morphTarget.get("POSITION");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData targetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				targetAccessorDatas.add(targetAccessorData);
				AccessorFloatData deltaPositionsAccessorData = AccessorDatas.createFloat(accessorModel);
				accessorModel = morphTarget.get("NORMAL");
				if(accessorModel != null) {
					AccessorFloatData deltaNormalsAccessorData = AccessorDatas.createFloat(accessorModel);
					accessorModel = morphTarget.get("TEXCOORD_0");
					if(accessorModel != null) {
						AccessorData deltaTexcoordsAccessorData = AccessorDatas.create(accessorModel);
						MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

							@Override
							public int getNumFaces() {
								return numFaces;
							}

							@Override
							public int getNumVerticesOfFace(int face) {
								return 3;
							}

							@Override
							public void getPosition(float[] posOut, int face, int vert) {
								int index = (face * 3) + vert;
								posOut[0] = positionsAccessorData.get(index, 0) + deltaPositionsAccessorData.get(index, 0);
								posOut[1] = positionsAccessorData.get(index, 1) + deltaPositionsAccessorData.get(index, 1);
								posOut[2] = positionsAccessorData.get(index, 2) + deltaPositionsAccessorData.get(index, 2);
							}

							@Override
							public void getNormal(float[] normOut, int face, int vert) {
								int index = (face * 3) + vert;
								normOut[0] = normalsAccessorData.get(index, 0) + deltaNormalsAccessorData.get(index, 0);
								normOut[1] = normalsAccessorData.get(index, 1) + deltaNormalsAccessorData.get(index, 1);
								normOut[2] = normalsAccessorData.get(index, 2) + deltaNormalsAccessorData.get(index, 2);
							}

							@Override
							public void getTexCoord(float[] texOut, int face, int vert) {
								int index = (face * 3) + vert;
								texOut[0] = texcoordsAccessorData.getFloat(index, 0) + deltaTexcoordsAccessorData.getFloat(index, 0);
								texOut[1] = texcoordsAccessorData.getFloat(index, 1) + deltaTexcoordsAccessorData.getFloat(index, 1);
							}

							@Override
							public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
								int index = (face * 3) + vert;
								tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
								tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
								tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
							}

							@Override
							public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
								//Do nothing
							}
							
						});
					}
					else {
						MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

							@Override
							public int getNumFaces() {
								return numFaces;
							}

							@Override
							public int getNumVerticesOfFace(int face) {
								return 3;
							}

							@Override
							public void getPosition(float[] posOut, int face, int vert) {
								int index = (face * 3) + vert;
								posOut[0] = positionsAccessorData.get(index, 0) + deltaPositionsAccessorData.get(index, 0);
								posOut[1] = positionsAccessorData.get(index, 1) + deltaPositionsAccessorData.get(index, 1);
								posOut[2] = positionsAccessorData.get(index, 2) + deltaPositionsAccessorData.get(index, 2);
							}

							@Override
							public void getNormal(float[] normOut, int face, int vert) {
								int index = (face * 3) + vert;
								normOut[0] = normalsAccessorData.get(index, 0) + deltaNormalsAccessorData.get(index, 0);
								normOut[1] = normalsAccessorData.get(index, 1) + deltaNormalsAccessorData.get(index, 1);
								normOut[2] = normalsAccessorData.get(index, 2) + deltaNormalsAccessorData.get(index, 2);
							}

							@Override
							public void getTexCoord(float[] texOut, int face, int vert) {
								int index = (face * 3) + vert;
								texOut[0] = texcoordsAccessorData.getFloat(index, 0);
								texOut[1] = texcoordsAccessorData.getFloat(index, 1);
							}

							@Override
							public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
								int index = (face * 3) + vert;
								tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
								tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
								tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
							}

							@Override
							public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
								//Do nothing
							}
							
						});
					}
				}
				else {
					accessorModel = morphTarget.get("TEXCOORD_0");
					if(accessorModel != null) {
						AccessorData deltaTexcoordsAccessorData = AccessorDatas.create(accessorModel);
						MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

							@Override
							public int getNumFaces() {
								return numFaces;
							}

							@Override
							public int getNumVerticesOfFace(int face) {
								return 3;
							}

							@Override
							public void getPosition(float[] posOut, int face, int vert) {
								int index = (face * 3) + vert;
								posOut[0] = positionsAccessorData.get(index, 0) + deltaPositionsAccessorData.get(index, 0);
								posOut[1] = positionsAccessorData.get(index, 1) + deltaPositionsAccessorData.get(index, 1);
								posOut[2] = positionsAccessorData.get(index, 2) + deltaPositionsAccessorData.get(index, 2);
							}

							@Override
							public void getNormal(float[] normOut, int face, int vert) {
								int index = (face * 3) + vert;
								normOut[0] = normalsAccessorData.get(index, 0);
								normOut[1] = normalsAccessorData.get(index, 1);
								normOut[2] = normalsAccessorData.get(index, 2);
							}

							@Override
							public void getTexCoord(float[] texOut, int face, int vert) {
								int index = (face * 3) + vert;
								texOut[0] = texcoordsAccessorData.getFloat(index, 0) + deltaTexcoordsAccessorData.getFloat(index, 0);
								texOut[1] = texcoordsAccessorData.getFloat(index, 1) + deltaTexcoordsAccessorData.getFloat(index, 1);
							}

							@Override
							public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
								int index = (face * 3) + vert;
								tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
								tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
								tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
							}

							@Override
							public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
								//Do nothing
							}
							
						});
					}
					else {
						MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

							@Override
							public int getNumFaces() {
								return numFaces;
							}

							@Override
							public int getNumVerticesOfFace(int face) {
								return 3;
							}

							@Override
							public void getPosition(float[] posOut, int face, int vert) {
								int index = (face * 3) + vert;
								posOut[0] = positionsAccessorData.get(index, 0) + deltaPositionsAccessorData.get(index, 0);
								posOut[1] = positionsAccessorData.get(index, 1) + deltaPositionsAccessorData.get(index, 1);
								posOut[2] = positionsAccessorData.get(index, 2) + deltaPositionsAccessorData.get(index, 2);
							}

							@Override
							public void getNormal(float[] normOut, int face, int vert) {
								int index = (face * 3) + vert;
								normOut[0] = normalsAccessorData.get(index, 0);
								normOut[1] = normalsAccessorData.get(index, 1);
								normOut[2] = normalsAccessorData.get(index, 2);
							}

							@Override
							public void getTexCoord(float[] texOut, int face, int vert) {
								int index = (face * 3) + vert;
								texOut[0] = texcoordsAccessorData.getFloat(index, 0);
								texOut[1] = texcoordsAccessorData.getFloat(index, 1);
							}

							@Override
							public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
								int index = (face * 3) + vert;
								tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
								tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
								tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
							}

							@Override
							public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
								//Do nothing
							}
							
						});
					}
				}
				continue;
			}
			accessorModel = morphTarget.get("NORMAL");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData targetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				targetAccessorDatas.add(targetAccessorData);
				AccessorFloatData deltaNormalsAccessorData = AccessorDatas.createFloat(accessorModel);
				accessorModel = morphTarget.get("TEXCOORD_0");
				if(accessorModel != null) {
					AccessorData deltaTexcoordsAccessorData = AccessorDatas.create(accessorModel);
					MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

						@Override
						public int getNumFaces() {
							return numFaces;
						}

						@Override
						public int getNumVerticesOfFace(int face) {
							return 3;
						}

						@Override
						public void getPosition(float[] posOut, int face, int vert) {
							int index = (face * 3) + vert;
							posOut[0] = positionsAccessorData.get(index, 0);
							posOut[1] = positionsAccessorData.get(index, 1);
							posOut[2] = positionsAccessorData.get(index, 2);
						}

						@Override
						public void getNormal(float[] normOut, int face, int vert) {
							int index = (face * 3) + vert;
							normOut[0] = normalsAccessorData.get(index, 0) + deltaNormalsAccessorData.get(index, 0);
							normOut[1] = normalsAccessorData.get(index, 1) + deltaNormalsAccessorData.get(index, 1);
							normOut[2] = normalsAccessorData.get(index, 2) + deltaNormalsAccessorData.get(index, 2);
						}

						@Override
						public void getTexCoord(float[] texOut, int face, int vert) {
							int index = (face * 3) + vert;
							texOut[0] = texcoordsAccessorData.getFloat(index, 0) + deltaTexcoordsAccessorData.getFloat(index, 0);
							texOut[1] = texcoordsAccessorData.getFloat(index, 1) + deltaTexcoordsAccessorData.getFloat(index, 1);
						}

						@Override
						public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
							int index = (face * 3) + vert;
							tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
							tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
							tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
						}

						@Override
						public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
							//Do nothing
						}
						
					});
				}
				else {
					MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

						@Override
						public int getNumFaces() {
							return numFaces;
						}

						@Override
						public int getNumVerticesOfFace(int face) {
							return 3;
						}

						@Override
						public void getPosition(float[] posOut, int face, int vert) {
							int index = (face * 3) + vert;
							posOut[0] = positionsAccessorData.get(index, 0);
							posOut[1] = positionsAccessorData.get(index, 1);
							posOut[2] = positionsAccessorData.get(index, 2);
						}

						@Override
						public void getNormal(float[] normOut, int face, int vert) {
							int index = (face * 3) + vert;
							normOut[0] = normalsAccessorData.get(index, 0) + deltaNormalsAccessorData.get(index, 0);
							normOut[1] = normalsAccessorData.get(index, 1) + deltaNormalsAccessorData.get(index, 1);
							normOut[2] = normalsAccessorData.get(index, 2) + deltaNormalsAccessorData.get(index, 2);
						}

						@Override
						public void getTexCoord(float[] texOut, int face, int vert) {
							int index = (face * 3) + vert;
							texOut[0] = texcoordsAccessorData.getFloat(index, 0);
							texOut[1] = texcoordsAccessorData.getFloat(index, 1);
						}

						@Override
						public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
							int index = (face * 3) + vert;
							tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
							tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
							tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
						}

						@Override
						public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
							//Do nothing
						}
						
					});
				}
				continue;
			}
			accessorModel = morphTarget.get("TEXCOORD_0");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData targetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				targetAccessorDatas.add(targetAccessorData);
				AccessorData deltaTexcoordsAccessorData = AccessorDatas.create(accessorModel);
				MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

					@Override
					public int getNumFaces() {
						return numFaces;
					}

					@Override
					public int getNumVerticesOfFace(int face) {
						return 3;
					}

					@Override
					public void getPosition(float[] posOut, int face, int vert) {
						int index = (face * 3) + vert;
						posOut[0] = positionsAccessorData.get(index, 0);
						posOut[1] = positionsAccessorData.get(index, 1);
						posOut[2] = positionsAccessorData.get(index, 2);
					}

					@Override
					public void getNormal(float[] normOut, int face, int vert) {
						int index = (face * 3) + vert;
						normOut[0] = normalsAccessorData.get(index, 0);
						normOut[1] = normalsAccessorData.get(index, 1);
						normOut[2] = normalsAccessorData.get(index, 2);
					}

					@Override
					public void getTexCoord(float[] texOut, int face, int vert) {
						int index = (face * 3) + vert;
						texOut[0] = texcoordsAccessorData.getFloat(index, 0) + deltaTexcoordsAccessorData.getFloat(index, 0);
						texOut[1] = texcoordsAccessorData.getFloat(index, 1) + deltaTexcoordsAccessorData.getFloat(index, 1);
					}

					@Override
					public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
						int index = (face * 3) + vert;
						tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
						tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
						tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
					}

					@Override
					public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
						//Do nothing
					}
					
				});
				continue;
			}
			targetAccessorDatas.add(null);
		}
		return isMorphableAttribute;
	}
	
	private boolean createTangentMorphTarget(List<Map<String, AccessorModel>> morphTargets, List<AccessorFloatData> targetAccessorDatas, AccessorModel positionsAccessorModel, AccessorModel normalsAccessorModel, AccessorModel texcoordsAccessorModel, AccessorModel tangentsAccessorModel, List<AccessorFloatData> normalTargetAccessorDatas) {
		boolean isMorphableAttribute = false;
		int count = positionsAccessorModel.getCount();
		int numFaces = count / 3;
		AccessorFloatData positionsAccessorData = AccessorDatas.createFloat(positionsAccessorModel);
		AccessorFloatData normalsAccessorData = AccessorDatas.createFloat(normalsAccessorModel);
		AccessorFloatData tangentsAccessorData = AccessorDatas.createFloat(tangentsAccessorModel);
		AccessorData texcoordsAccessorData = AccessorDatas.create(texcoordsAccessorModel);
		Iterator<AccessorFloatData> iterator = normalTargetAccessorDatas.iterator();
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorFloatData deltaNormalsAccessorData = iterator.next();
			AccessorModel accessorModel = morphTarget.get("POSITION");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData targetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				targetAccessorDatas.add(targetAccessorData);
				AccessorFloatData deltaPositionsAccessorData = AccessorDatas.createFloat(accessorModel);
				accessorModel = morphTarget.get("TEXCOORD_0");
				if(accessorModel != null) {
					AccessorData deltaTexcoordsAccessorData = AccessorDatas.create(accessorModel);
					MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

						@Override
						public int getNumFaces() {
							return numFaces;
						}

						@Override
						public int getNumVerticesOfFace(int face) {
							return 3;
						}

						@Override
						public void getPosition(float[] posOut, int face, int vert) {
							int index = (face * 3) + vert;
							posOut[0] = positionsAccessorData.get(index, 0) + deltaPositionsAccessorData.get(index, 0);
							posOut[1] = positionsAccessorData.get(index, 1) + deltaPositionsAccessorData.get(index, 1);
							posOut[2] = positionsAccessorData.get(index, 2) + deltaPositionsAccessorData.get(index, 2);
						}

						@Override
						public void getNormal(float[] normOut, int face, int vert) {
							int index = (face * 3) + vert;
							normOut[0] = normalsAccessorData.get(index, 0) + deltaNormalsAccessorData.get(index, 0);
							normOut[1] = normalsAccessorData.get(index, 1) + deltaNormalsAccessorData.get(index, 1);
							normOut[2] = normalsAccessorData.get(index, 2) + deltaNormalsAccessorData.get(index, 2);
						}

						@Override
						public void getTexCoord(float[] texOut, int face, int vert) {
							int index = (face * 3) + vert;
							texOut[0] = texcoordsAccessorData.getFloat(index, 0) + deltaTexcoordsAccessorData.getFloat(index, 0);
							texOut[1] = texcoordsAccessorData.getFloat(index, 1) + deltaTexcoordsAccessorData.getFloat(index, 1);
						}

						@Override
						public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
							int index = (face * 3) + vert;
							tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
							tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
							tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
						}

						@Override
						public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
							//Do nothing
						}
						
					});
				}
				else {
					MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

						@Override
						public int getNumFaces() {
							return numFaces;
						}

						@Override
						public int getNumVerticesOfFace(int face) {
							return 3;
						}

						@Override
						public void getPosition(float[] posOut, int face, int vert) {
							int index = (face * 3) + vert;
							posOut[0] = positionsAccessorData.get(index, 0) + deltaPositionsAccessorData.get(index, 0);
							posOut[1] = positionsAccessorData.get(index, 1) + deltaPositionsAccessorData.get(index, 1);
							posOut[2] = positionsAccessorData.get(index, 2) + deltaPositionsAccessorData.get(index, 2);
						}

						@Override
						public void getNormal(float[] normOut, int face, int vert) {
							int index = (face * 3) + vert;
							normOut[0] = normalsAccessorData.get(index, 0) + deltaNormalsAccessorData.get(index, 0);
							normOut[1] = normalsAccessorData.get(index, 1) + deltaNormalsAccessorData.get(index, 1);
							normOut[2] = normalsAccessorData.get(index, 2) + deltaNormalsAccessorData.get(index, 2);
						}

						@Override
						public void getTexCoord(float[] texOut, int face, int vert) {
							int index = (face * 3) + vert;
							texOut[0] = texcoordsAccessorData.getFloat(index, 0);
							texOut[1] = texcoordsAccessorData.getFloat(index, 1);
						}

						@Override
						public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
							int index = (face * 3) + vert;
							tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
							tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
							tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
						}

						@Override
						public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
							//Do nothing
						}
						
					});
				}
				continue;
			}
			accessorModel = morphTarget.get("TEXCOORD_0");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData targetAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC3, ""));
				targetAccessorDatas.add(targetAccessorData);
				AccessorData deltaTexcoordsAccessorData = AccessorDatas.create(accessorModel);
				MikktspaceTangentGenerator.genTangSpaceDefault(new MikkTSpaceContext() {

					@Override
					public int getNumFaces() {
						return numFaces;
					}

					@Override
					public int getNumVerticesOfFace(int face) {
						return 3;
					}

					@Override
					public void getPosition(float[] posOut, int face, int vert) {
						int index = (face * 3) + vert;
						posOut[0] = positionsAccessorData.get(index, 0);
						posOut[1] = positionsAccessorData.get(index, 1);
						posOut[2] = positionsAccessorData.get(index, 2);
					}

					@Override
					public void getNormal(float[] normOut, int face, int vert) {
						int index = (face * 3) + vert;
						normOut[0] = normalsAccessorData.get(index, 0);
						normOut[1] = normalsAccessorData.get(index, 1);
						normOut[2] = normalsAccessorData.get(index, 2);
					}

					@Override
					public void getTexCoord(float[] texOut, int face, int vert) {
						int index = (face * 3) + vert;
						texOut[0] = texcoordsAccessorData.getFloat(index, 0) + deltaTexcoordsAccessorData.getFloat(index, 0);
						texOut[1] = texcoordsAccessorData.getFloat(index, 1) + deltaTexcoordsAccessorData.getFloat(index, 1);
					}

					@Override
					public void setTSpaceBasic(float[] tangent, float sign, int face, int vert) {
						int index = (face * 3) + vert;
						tangentsAccessorData.set(index, 0, tangent[0] - tangentsAccessorData.get(index, 0));
						tangentsAccessorData.set(index, 1, tangent[1] - tangentsAccessorData.get(index, 1));
						tangentsAccessorData.set(index, 2, tangent[2] - tangentsAccessorData.get(index, 2));
					}

					@Override
					public void setTSpace(float[] tangent, float[] biTangent, float magS, float magT, boolean isOrientationPreserving, int face, int vert) {
						//Do nothing
					}
					
				});
				continue;
			}
			targetAccessorDatas.add(null);
		}
		return isMorphableAttribute;
	}
	
	private boolean createColorMorphTarget(List<Map<String, AccessorModel>> morphTargets, List<AccessorFloatData> targetAccessorDatas) {
		boolean isMorphableAttribute = false;
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorModel accessorModel = morphTarget.get("COLOR_0");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData morphAccessorData = colorsMorphTargetAccessorModelToAccessorData.get(accessorModel);
				if(morphAccessorData == null) {
					if(accessorModel.getElementType() == ElementType.VEC3) {
						int count = accessorModel.getCount();
						morphAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC4, ""));
						AccessorData accessorData = AccessorDatas.create(accessorModel);
						for(int i = 0; i < count; i++) {
							morphAccessorData.set(i, 0, accessorData.getFloat(i, 0));
							morphAccessorData.set(i, 1, accessorData.getFloat(i, 1));
							morphAccessorData.set(i, 2, accessorData.getFloat(i, 2));
							morphAccessorData.set(i, 3, 0.0F);
						}
					}
					else if(accessorModel.getComponentDataType() != float.class) {
						int count = accessorModel.getCount();
						morphAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC4, ""));
						AccessorData accessorData = AccessorDatas.create(accessorModel);
						for(int i = 0; i < count; i++) {
							morphAccessorData.set(i, 0, accessorData.getFloat(i, 0));
							morphAccessorData.set(i, 1, accessorData.getFloat(i, 1));
							morphAccessorData.set(i, 2, accessorData.getFloat(i, 2));
							morphAccessorData.set(i, 3, accessorData.getFloat(i, 3));
						}
					}
					else {
						morphAccessorData = AccessorDatas.createFloat(accessorModel);
					}
					colorsMorphTargetAccessorModelToAccessorData.put(accessorModel, morphAccessorData);
				}
				targetAccessorDatas.add(morphAccessorData);
			}
			else targetAccessorDatas.add(null);
		}
		return isMorphableAttribute;
	}
	
	private boolean createTexcoordMorphTarget(List<Map<String, AccessorModel>> morphTargets, List<AccessorFloatData> targetAccessorDatas) {
		boolean isMorphableAttribute = false;
		for(Map<String, AccessorModel> morphTarget : morphTargets) {
			AccessorModel accessorModel = morphTarget.get("TEXCOORD_0");
			if(accessorModel != null) {
				isMorphableAttribute = true;
				AccessorFloatData morphAccessorData = texcoordsMorphTargetAccessorModelToAccessorData.get(accessorModel);
				if(morphAccessorData == null) {
					if(accessorModel.getComponentDataType() != float.class) {
						int count = accessorModel.getCount();
						morphAccessorData = AccessorDatas.createFloat(AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC2, ""));
						AccessorData accessorData = AccessorDatas.create(accessorModel);
						for(int i = 0; i < count; i++) {
							morphAccessorData.set(i, 0, accessorData.getFloat(i, 0));
							morphAccessorData.set(i, 1, accessorData.getFloat(i, 1));
						}
					}
					else {
						morphAccessorData = AccessorDatas.createFloat(accessorModel);
					}
					texcoordsMorphTargetAccessorModelToAccessorData.put(accessorModel, morphAccessorData);
				}
				targetAccessorDatas.add(morphAccessorData);
			}
			else targetAccessorDatas.add(null);
		}
		return isMorphableAttribute;
	}
	
	private void bindVec3FloatMorphed(NodeModel nodeModel, MeshModel meshModel, List<Runnable> command, AccessorModel baseAccessorModel, List<AccessorFloatData> targetAccessorDatas) {
		AccessorModel morphedAccessorModel = AccessorModelCreation.instantiate(baseAccessorModel, "");
		AccessorFloatData baseAccessorData = AccessorDatas.createFloat(baseAccessorModel);
		AccessorFloatData morphedAccessorData = AccessorDatas.createFloat(morphedAccessorModel);
		ByteBuffer morphedBufferViewData = morphedAccessorModel.getBufferViewModel().getBufferViewData();
		
		int glBufferView = GL15.glGenBuffers();
		gltfRenderData.addGlBufferView(glBufferView);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, morphedBufferViewData, GL15.GL_STATIC_DRAW);
		
		float weights[] = new float[targetAccessorDatas.size()];
		int numComponents = 3;
		int numElements = morphedAccessorData.getNumElements();
		command.add(() -> {
			if(nodeModel.getWeights() != null) System.arraycopy(nodeModel.getWeights(), 0, weights, 0, weights.length);
			else if(meshModel.getWeights() != null) System.arraycopy(meshModel.getWeights(), 0, weights, 0, weights.length);
			
			for(int e = 0; e < numElements; e++) {
				for(int c = 0; c < numComponents; c++) {
					float r = baseAccessorData.get(e, c);
					for(int i = 0; i < weights.length; i++) {
						AccessorFloatData target = targetAccessorDatas.get(i);
						if(target != null) {
							r += weights[i] * target.get(e, c);
						}
					}
					morphedAccessorData.set(e, c, r);
				}
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, morphedBufferViewData);
		});
	}
	
	private AccessorModel bindColorMorphed(NodeModel nodeModel, MeshModel meshModel, List<Runnable> command, AccessorModel baseAccessorModel, List<AccessorFloatData> targetAccessorDatas) {
		AccessorFloatData baseAccessorData;
		AccessorFloatData morphedAccessorData;
		ByteBuffer morphedBufferViewData;
		
		if(baseAccessorModel.getComponentDataType() != float.class) {
			int count = baseAccessorModel.getCount();
			AccessorData accessorData = AccessorDatas.create(baseAccessorModel);
			baseAccessorModel = AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC4, "");
			baseAccessorData = AccessorDatas.createFloat(baseAccessorModel);
			for(int i = 0; i < count; i++) {
				baseAccessorData.set(i, 0, accessorData.getFloat(i, 0));
				baseAccessorData.set(i, 1, accessorData.getFloat(i, 1));
				baseAccessorData.set(i, 2, accessorData.getFloat(i, 2));
				baseAccessorData.set(i, 3, accessorData.getFloat(i, 3));
			}
			AccessorModel morphedAccessorModel = AccessorModelCreation.instantiate(baseAccessorModel, "");
			morphedAccessorData = AccessorDatas.createFloat(morphedAccessorModel);
			morphedBufferViewData = morphedAccessorModel.getBufferViewModel().getBufferViewData();
		}
		else {
			baseAccessorData = AccessorDatas.createFloat(baseAccessorModel);
			AccessorModel morphedAccessorModel = AccessorModelCreation.instantiate(baseAccessorModel, "");
			morphedAccessorData = AccessorDatas.createFloat(morphedAccessorModel);
			morphedBufferViewData = morphedAccessorModel.getBufferViewModel().getBufferViewData();
		}
		
		int glBufferView = GL15.glGenBuffers();
		gltfRenderData.addGlBufferView(glBufferView);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, morphedBufferViewData, GL15.GL_STATIC_DRAW);
		
		float weights[] = new float[targetAccessorDatas.size()];
		int numComponents = 4;
		int numElements = morphedAccessorData.getNumElements();
		command.add(() -> {
			if(nodeModel.getWeights() != null) System.arraycopy(nodeModel.getWeights(), 0, weights, 0, weights.length);
			else if(meshModel.getWeights() != null) System.arraycopy(meshModel.getWeights(), 0, weights, 0, weights.length);
			
			for(int e = 0; e < numElements; e++) {
				for(int c = 0; c < numComponents; c++) {
					float r = baseAccessorData.get(e, c);
					for(int i = 0; i < weights.length; i++) {
						AccessorFloatData target = targetAccessorDatas.get(i);
						if(target != null) {
							r += weights[i] * target.get(e, c);
						}
					}
					morphedAccessorData.set(e, c, r);
				}
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, morphedBufferViewData);
		});
		return baseAccessorModel;
	}
	
	private AccessorModel bindTexcoordMorphed(NodeModel nodeModel, MeshModel meshModel, List<Runnable> command, AccessorModel baseAccessorModel, List<AccessorFloatData> targetAccessorDatas) {
		AccessorFloatData baseAccessorData;
		AccessorFloatData morphedAccessorData;
		ByteBuffer morphedBufferViewData;
		
		if(baseAccessorModel.getComponentDataType() != float.class) {
			int count = baseAccessorModel.getCount();
			AccessorData accessorData = AccessorDatas.create(baseAccessorModel);
			baseAccessorModel = AccessorModelCreation.createAccessorModel(GL11.GL_FLOAT, count, ElementType.VEC2, "");
			baseAccessorData = AccessorDatas.createFloat(baseAccessorModel);
			for(int i = 0; i < count; i++) {
				baseAccessorData.set(i, 0, accessorData.getFloat(i, 0));
				baseAccessorData.set(i, 1, accessorData.getFloat(i, 1));
			}
			AccessorModel morphedAccessorModel = AccessorModelCreation.instantiate(baseAccessorModel, "");
			morphedAccessorData = AccessorDatas.createFloat(morphedAccessorModel);
			morphedBufferViewData = morphedAccessorModel.getBufferViewModel().getBufferViewData();
		}
		else {
			baseAccessorData = AccessorDatas.createFloat(baseAccessorModel);
			AccessorModel morphedAccessorModel = AccessorModelCreation.instantiate(baseAccessorModel, "");
			morphedAccessorData = AccessorDatas.createFloat(morphedAccessorModel);
			morphedBufferViewData = morphedAccessorModel.getBufferViewModel().getBufferViewData();
		}
		
		int glBufferView = GL15.glGenBuffers();
		gltfRenderData.addGlBufferView(glBufferView);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, morphedBufferViewData, GL15.GL_STATIC_DRAW);
		
		float weights[] = new float[targetAccessorDatas.size()];
		int numComponents = 2;
		int numElements = morphedAccessorData.getNumElements();
		command.add(() -> {
			if(nodeModel.getWeights() != null) System.arraycopy(nodeModel.getWeights(), 0, weights, 0, weights.length);
			else if(meshModel.getWeights() != null) System.arraycopy(meshModel.getWeights(), 0, weights, 0, weights.length);
			
			for(int e = 0; e < numElements; e++) {
				for(int c = 0; c < numComponents; c++) {
					float r = baseAccessorData.get(e, c);
					for(int i = 0; i < weights.length; i++) {
						AccessorFloatData target = targetAccessorDatas.get(i);
						if(target != null) {
							r += weights[i] * target.get(e, c);
						}
					}
					morphedAccessorData.set(e, c, r);
				}
			}
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, glBufferView);
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, morphedBufferViewData);
		});
		return baseAccessorModel;
	}
	
	/**
	 * Put the given values into a direct FloatBuffer and return it.
	 * The returned buffer may always be a slice of the same instance.
	 * This method is supposed to be called only from the OpenGL thread.
	 *
	 * @param value The value
	 * @return The FloatBuffer
	 */
	private static FloatBuffer putFloatBuffer(float value[])
	{
		int total = value.length;
		if (uniformFloatBuffer == null || uniformFloatBuffer.capacity() < total)
		{
		    uniformFloatBuffer = ByteBuffer
	    		.allocateDirect(total * Float.BYTES)
	    		.order(ByteOrder.nativeOrder())
	    		.asFloatBuffer();
		}
		uniformFloatBuffer.position(0);
		uniformFloatBuffer.limit(uniformFloatBuffer.capacity());
		uniformFloatBuffer.put(value);
		uniformFloatBuffer.flip();
		return uniformFloatBuffer;
	}
	
	private static float[] findGlobalTransform(NodeModel nodeModel) {
		float[] found = nodeGlobalTransformLookup.get(nodeModel);
		if(found != null) {
			return found;
		}
		else {
			List<NodeModel> pathToNode = new ArrayList<NodeModel>();
			pathToNode.add(nodeModel);
			nodeModel = nodeModel.getParent();
			while(nodeModel != null) {
	    		found = nodeGlobalTransformLookup.get(nodeModel);
	        	if(found != null) {
	        		int i = pathToNode.size() - 1;
	        		do {
	        			nodeModel = pathToNode.get(i);
	        			float[] transform = DefaultNodeModel.computeLocalTransform(nodeModel, null);
	        			MathUtils.mul4x4(found, transform, transform);
	        			nodeGlobalTransformLookup.put(nodeModel, transform);
	        			found = transform;
	        		}
	        		while(--i >= 0);
	        		return found;
	        	}
	        	else {
	        		pathToNode.add(nodeModel);
	        		nodeModel = nodeModel.getParent();
	        	}
	    	}
			int i = pathToNode.size() - 1;
			nodeModel = pathToNode.get(i);
			found = DefaultNodeModel.computeLocalTransform(nodeModel, null);
			while(--i >= 0) {
				nodeModel = pathToNode.get(i);
				float[] transform = DefaultNodeModel.computeLocalTransform(nodeModel, null);
				MathUtils.mul4x4(found, transform, transform);
				nodeGlobalTransformLookup.put(nodeModel, transform);
				found = transform;
			}
			return found;
		}
	}
	
	/**
	 * Computes a0-a1, and stores the result in the given array.
	 * 
	 * This assumes that the given arrays are non-<code>null</code> 
	 * and have equal lengths.
	 *   
	 * @param a0 The first array
	 * @param a1 The second array
	 * @param result The array that stores the result
	 */
	private static void subtract(float[] a0, float[] a1, float[] result)
	{
		for (int i = 0; i < a0.length; i++)
		{
			result[i] = a0[i] - a1[i];
		}
	} 
	
	/**
	 * Computes the cross product of a0 and a1, and stores the result in 
	 * the given array.
	 * 
	 * This assumes that the given arrays are non-<code>null</code> 
	 * and have length 3.
	 *   
	 * @param a0 The first array
	 * @param a1 The second array
	 * @param result The array that stores the result
	 */
	private static void cross(float a0[], float a1[], float result[])
	{
		result[0] = a0[1] * a1[2] - a0[2] * a1[1];
		result[1] = a0[2] * a1[0] - a0[0] * a1[2];
		result[2] = a0[0] * a1[1] - a0[1] * a1[0];
	}
	
	/**
	 * Compute the length of the given vector
	 * 
	 * @param a The vector
	 * @return The length
	 */
	private static float computeLength(float a[])
	{
		float sum = 0;
		for (int i=0; i<a.length; i++)
		{
			sum += a[i] * a[i];
		}
		float r = (float) Math.sqrt(sum);
		return r;
	}
	
	/**
	 * Normalizes the given array, and stores the result in the given array.
	 * 
	 * This assumes that the given arrays are non-<code>null</code> 
	 * and have the same length.
	 *   
	 * @param a The array
	 * @param result The array that stores the result
	 */
	private static void normalize(float a[], float result[])
	{
		float scaling = 1.0f / computeLength(a);
		scale(a, scaling, result);
	}
	
	/**
	 * Scales the given vector with the given factor, and stores the result
	 * in the given array.
	 * 
	 * This assumes that the given arrays are non-<code>null</code> 
	 * and have equal lengths.
	 * 
	 * @param a The vector
	 * @param factor The scaling factor
	 * @param result The array that will store the result
	 */
	private static void scale(float a[], float factor, float result[])
	{
		for (int i = 0; i < a.length; i++)
		{
			result[i] = a[i] * factor;
		}
	}

	/**
	 * MODULARWARFARE
	 */

	private Runnable createTransformInverseCommand(NodeModel nodeModel) {
		return () -> {
			GL11.glPushMatrix();
			BUF_FLOAT_16.clear();
			float[] invertTransform = new float[16];
			MathUtils.invert4x4(findGlobalTransform(nodeModel), invertTransform);
			BUF_FLOAT_16.put(invertTransform);
			BUF_FLOAT_16.rewind();
			GL11.glMultMatrix(BUF_FLOAT_16);
		};
	}

	public float[] getGlobalTransform(NodeModel nodeModel) {
		if(nodeModel==null) {
			return new float[] {
					1,0,0,0,
					0,1,0,0,
					0,0,1,0,
					0,0,0,1
			};
		}
		return findGlobalTransform(nodeModel);
	}

	public void processSingleNodeModel(NodeModel nodeModel, List<Runnable> renderCommands,
									   List<Runnable> skinningCommands,List<Runnable> transformCommands,List<Runnable> transformInverseCommands) {
		ArrayList<Runnable> nodeSkinningCommands = new ArrayList<Runnable>();
		ArrayList<Runnable> nodeRenderCommands = new ArrayList<Runnable>();
		SkinModel skinModel = nodeModel.getSkinModel();
		if(skinModel != null) {
			int jointSize = skinModel.getJoints().size();
			int jointMatrixSize = jointSize * 16;

			int jointMatrixBuffer = GL15.glGenBuffers();
			gltfRenderData.addGlBufferView(jointMatrixBuffer);
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixSize * Float.BYTES, GL15.GL_STATIC_DRAW);

			float[] invertNodeTransform = new float[16];
			float[] inverseBindMatrix = new float[16];
			float[] bindShapeMatrix = skinModel.getBindShapeMatrix(null);
			float[] result = new float[16];
			float[] jointMatrices = new float[jointMatrixSize];

			nodeSkinningCommands.add(() -> {
				MathUtils.invert4x4(findGlobalTransform(nodeModel), invertNodeTransform);
				for(int i = 0; i < jointSize; i++) {
					MathUtils.mul4x4(invertNodeTransform, findGlobalTransform(skinModel.getJoints().get(i)), result);
					skinModel.getInverseBindMatrix(i, inverseBindMatrix);
					MathUtils.mul4x4(result, inverseBindMatrix, result);
					MathUtils.mul4x4(result, bindShapeMatrix, result);
					System.arraycopy(result, 0, jointMatrices, i * 16, 16);
				}

				GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, jointMatrixBuffer);
				GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, putFloatBuffer(jointMatrices));

				GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, jointMatrixBuffer);
			});

			nodeRenderCommands.add(createTransformCommand(nodeModel));
			transformCommands.add(createTransformCommand(nodeModel));
			transformInverseCommands.add(createTransformInverseCommand(nodeModel));
			for(MeshModel meshModel : nodeModel.getMeshModels()) {
				for(MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
					processMeshPrimitiveModel(nodeModel, meshModel, meshPrimitiveModel, nodeRenderCommands, nodeSkinningCommands);
				}
			}
			//nodeRenderCommands.add(GL11::glPopMatrix);
		}
		else {
			nodeRenderCommands.add(createTransformCommand(nodeModel));
			transformCommands.add(createTransformCommand(nodeModel));
			transformInverseCommands.add(createTransformInverseCommand(nodeModel));
			for (MeshModel meshModel : nodeModel.getMeshModels()) {
				for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
					processMeshPrimitiveModel(nodeModel, meshModel, meshPrimitiveModel, nodeRenderCommands);
				}
			}
			//nodeRenderCommands.add(GL11::glPopMatrix);
		}
		//nodeModel.getChildren().forEach((childNode) -> processNodeModel(childNode, nodeRenderCommands, nodeSkinningCommands));
		if(!nodeSkinningCommands.isEmpty()) {
			skinningCommands.add(() -> {
				// Zero-scale meshes visibility optimization
				// https://github.com/KhronosGroup/glTF/pull/2059
				float[] scale = nodeModel.getScale();
				if(scale == null || scale[0] != 0.0F || scale[1] != 0.0F || scale[2] != 0.0F) {
					nodeSkinningCommands.forEach((command) -> command.run());
				}
			});
		}
		if(!nodeRenderCommands.isEmpty()) {
			renderCommands.add(() -> {
				float[] scale = nodeModel.getScale();
				if(scale == null || scale[0] != 0.0F || scale[1] != 0.0F || scale[2] != 0.0F) {
					nodeRenderCommands.forEach((command) -> command.run());
				}else {
					GlStateManager.pushMatrix();
				}
			});
		}else {
			renderCommands.add(()->{
				GlStateManager.pushMatrix();
			});
		}
	}

}
