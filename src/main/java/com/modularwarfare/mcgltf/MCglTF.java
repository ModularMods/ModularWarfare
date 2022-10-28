package com.modularwarfare.mcgltf;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;

import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfModelReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.client.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;

public class MCglTF {

	public static final String MODID = "mcgltf";
	public static final String RESOURCE_LOCATION = "resourceLocation";
	public static final String MATERIAL_HANDLER = "materialHandler";
	
	public static final Logger logger = LogManager.getLogger(MODID);
	
	private static MCglTF INSTANCE;
	
	private final int glProgramSkinnig;
	private final int defaultColorMap;
	private final int defaultNormalMap;
	
	private final Map<ResourceLocation, ByteBuffer> loadedBufferResources = new HashMap<ResourceLocation, ByteBuffer>();
	private final Map<ResourceLocation, ByteBuffer> loadedImageResources = new HashMap<ResourceLocation, ByteBuffer>();
	private final Map<ResourceLocation, RenderedGltfModel> renderedGltfModels = new HashMap<ResourceLocation, RenderedGltfModel>();
	private final List<IGltfModelReceiver> gltfModelReceivers = new ArrayList<IGltfModelReceiver>();
	private final List<GltfRenderData> gltfRenderDatas = new ArrayList<GltfRenderData>();
	private final Map<ResourceLocation, BiFunction<RenderedGltfModel, MaterialModel, IMaterialHandler>> materialHandlerFactories = new HashMap<ResourceLocation, BiFunction<RenderedGltfModel, MaterialModel, IMaterialHandler>>();
	
	public MCglTF() {
		INSTANCE = this;

		logger.info("Loading MCglTF");
		ContextCapabilities contextcapabilities = GLContext.getCapabilities();
		if(!(contextcapabilities.OpenGL40)) {
			try {
				Field errorMessage = FMLClientHandler.class.getDeclaredField("errorToDisplay");
				errorMessage.setAccessible(true);
				errorMessage.set(FMLClientHandler.instance(), new OpenGLNotSupportedException());
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		int glShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		GL20.glShaderSource(glShader,
				  "#version 430\r\n"
				+ "layout(location = 0) in vec4 joint;"
				+ "layout(location = 1) in vec4 weight;"
				+ "layout(location = 2) in vec3 position;"
				+ "layout(location = 3) in vec3 normal;"
				+ "layout(location = 4) in vec4 tangent;"
				+ "layout(std430, binding = 0) readonly buffer jointMatrixBuffer {mat4 jointMatrix[];};"
				+ "out vec3 outPosition;"
				+ "out vec3 outNormal;"
				+ "out vec4 outTangent;"
				+ "void main() {"
				+ "mat4 skinMatrix ="
				+ " weight.x * jointMatrix[int(joint.x)] +"
				+ " weight.y * jointMatrix[int(joint.y)] +"
				+ " weight.z * jointMatrix[int(joint.z)] +"
				+ " weight.w * jointMatrix[int(joint.w)];"
				+ "outPosition = (skinMatrix * vec4(position, 1.0)).xyz;"
				+ "mat3 upperLeft = mat3(skinMatrix);"
				+ "outNormal = upperLeft * normal;"
				+ "outTangent.xyz = upperLeft * tangent.xyz;"
				+ "outTangent.w = tangent.w;"
				+ "}");
		GL20.glCompileShader(glShader);
		
		glProgramSkinnig = GL20.glCreateProgram();
		GL20.glAttachShader(glProgramSkinnig, glShader);
		GL20.glDeleteShader(glShader);
		GL30.glTransformFeedbackVaryings(glProgramSkinnig, new CharSequence[]{"outPosition", "outNormal", "outTangent"}, GL30.GL_SEPARATE_ATTRIBS);
		GL20.glLinkProgram(glProgramSkinnig);
		
		GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
		
		defaultColorMap = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, defaultColorMap);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
		
		defaultNormalMap = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, defaultNormalMap);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1}));
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
		
		GL11.glPopAttrib();
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEvent(FMLLoadCompleteEvent event) {
		SplashProgress.pause(); //This prevent the container object generated by glGenVertexArrays and glGenTransformFeedbacks during the game startup become invalid after SplashProgress#finish().
		
		((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new ISelectiveResourceReloadListener() {
			@Override
			public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
				logger.info("Loading MCglTF models ...");
				if(resourcePredicate.test(VanillaResourceType.MODELS)) {
					gltfRenderDatas.forEach((gltfRenderData) -> gltfRenderData.delete());
					gltfRenderDatas.clear();
					gltfModelReceivers.forEach((receiver) -> {
						try {
							ResourceLocation modelLocation = receiver.getModelLocation();
							logger.info("Loading MCglTF model: "+modelLocation.getResourcePath());
							RenderedGltfModel renderedModel = renderedGltfModels.get(modelLocation);
							if(renderedModel == null) {
								renderedModel = new RenderedGltfModel(new GltfModelReader().readWithoutReferences(Minecraft.getMinecraft().getResourceManager().getResource(modelLocation).getInputStream()));
								renderedGltfModels.put(modelLocation, renderedModel);
								gltfRenderDatas.add(renderedModel.gltfRenderData);
							}
							receiver.onModelLoaded(renderedModel);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
					renderedGltfModels.clear();
					loadedBufferResources.clear();
					loadedImageResources.clear();
				}
			}
			
		});
		
		SplashProgress.resume();
	}

	public int getGlProgramSkinnig() {
		return glProgramSkinnig;
	}
	
	public int getDefaultColorMap() {
		return defaultColorMap;
	}
	
	public int getDefaultNormalMap() {
		return defaultNormalMap;
	}
	
	public int getDefaultSpecularMap() {
		return 0;
	}
	
	public ByteBuffer getBufferResource(ResourceLocation location) {
		ByteBuffer bufferData = loadedBufferResources.get(location);
		if(bufferData == null) {
			try {
				bufferData = Buffers.create(IOUtils.toByteArray(Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()));
				loadedBufferResources.put(location, bufferData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bufferData;
	}
	
	public ByteBuffer getImageResource(ResourceLocation location) {
		ByteBuffer bufferData = loadedImageResources.get(location);
		if(bufferData == null) {
			try {
				bufferData = Buffers.create(IOUtils.toByteArray(Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()));
				loadedImageResources.put(location, bufferData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bufferData;
	}
	
	public void addGltfModelReceiver(IGltfModelReceiver receiver) {
		gltfModelReceivers.add(receiver);
	}
	
	public boolean removeGltfModelReceiver(IGltfModelReceiver receiver) {
		return gltfModelReceivers.remove(receiver);
	}
	
	public void registerMaterialHandlerFactory(ResourceLocation location, BiFunction<RenderedGltfModel, MaterialModel, IMaterialHandler> materialHandlerFactory) {
		materialHandlerFactories.put(location, materialHandlerFactory);
	}
	
	public BiFunction<RenderedGltfModel, MaterialModel, IMaterialHandler> getMaterialHandlerFactory(ResourceLocation location) {
		return materialHandlerFactories.get(location);
	}
	
	public static MCglTF getInstance() {
		return INSTANCE;
	}

}
