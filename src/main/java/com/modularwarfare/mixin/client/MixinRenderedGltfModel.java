package com.modularwarfare.mixin.client;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import de.javagl.jgltf.model.*;
import net.minecraft.client.renderer.GlStateManager;

import com.modularwarfare.api.IProcessNodeModelHandler;
import com.modularwarfare.mixin.client.accessor.IGltfRenderData;
import com.timlee9024.mcgltf.GltfRenderData;
import com.timlee9024.mcgltf.IMaterialHandler;
import com.timlee9024.mcgltf.RenderedGltfModel;

@Mixin(RenderedGltfModel.class)
public abstract class MixinRenderedGltfModel implements IProcessNodeModelHandler{
    
    @Shadow
    @Final
    public GltfRenderData gltfRenderData;
    
    @Shadow
    @Final
    private static FloatBuffer BUF_FLOAT_16;
    
    @Shadow
    private Runnable createTransformCommand(NodeModel nodeModel) {
        return null;
    }
    
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
    
    @Shadow
    private static float[] findGlobalTransform(NodeModel nodeModel) {
        return null;
    }
    
    @Shadow
    private static FloatBuffer putFloatBuffer(float value[]) {
        return null;
    }
    
    @Shadow
    private void processMeshPrimitiveModel(NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel, List<Runnable> renderCommand) {}
    @Shadow
    private void processMeshPrimitiveModel(NodeModel nodeModel, MeshModel meshModel, MeshPrimitiveModel meshPrimitiveModel, List<Runnable> renderCommand, List<Runnable> skinningCommand) {}
    
    @Override
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
    
    @Override
    public void processSingleNodeModel(NodeModel nodeModel, List<Runnable> renderCommands,
            List<Runnable> skinningCommands,List<Runnable> transformCommands,List<Runnable> transformInverseCommands) {
        ArrayList<Runnable> nodeSkinningCommands = new ArrayList<Runnable>();
        ArrayList<Runnable> nodeRenderCommands = new ArrayList<Runnable>();
        SkinModel skinModel = nodeModel.getSkinModel();
        if(skinModel != null) {
            int jointSize = skinModel.getJoints().size();
            int jointMatrixSize = jointSize * 16;
            
            int jointMatrixBuffer = GL15.glGenBuffers();
            ((IGltfRenderData)gltfRenderData).invokeAddGlBufferView(jointMatrixBuffer);
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
    
    @Accessor("nodeGlobalTransformLookup")
    public abstract Map<NodeModel, float[]> getNodeGlobalTransformLookup() ;
    @Accessor("materialModelToMaterialHandler")
    public abstract Map<MaterialModel, IMaterialHandler> getMaterialModelToMaterialHandler();
}
