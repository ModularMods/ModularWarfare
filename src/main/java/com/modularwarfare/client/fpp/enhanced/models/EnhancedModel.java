package com.modularwarfare.client.fpp.enhanced.models;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.IMWModel;
import com.modularwarfare.client.fpp.enhanced.configs.EnhancedRenderConfig;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.utility.maths.MathUtils;
import com.modularmods.mcgltf.IGltfModelReceiver;
import com.modularmods.mcgltf.MCglTF;
import com.modularmods.mcgltf.RenderedGltfModel;
import com.modularmods.mcgltf.RenderedGltfScene;
import com.modularmods.mcgltf.animation.GltfAnimationCreator;
import com.modularmods.mcgltf.animation.InterpolatedChannel;

import de.javagl.jgltf.model.AnimationModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.NodeModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Quaternion;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Optional.Interface(iface="com.modularmods.mcgltf.IGltfModelReceiver",modid="mcgltf")
public class EnhancedModel implements IGltfModelReceiver,IMWModel{

	protected static final FloatBuffer BUF_FLOAT_16 = BufferUtils.createFloatBuffer(16);
	
	protected RenderedGltfScene renderedScene;

    /**
     * List of all .gltf animations loaded with animation name;
     */
	protected List<Pair<String, List<InterpolatedChannel>>> animations;

    private boolean isInit = false;

    /**
     * .gltf Model
     */
    public GltfModel gltfModel;
    
    /**
	 * Unlike set scale of node to zero to hide the node itself and its children, this will not affect children node.</br>
	 * This was exist because it need to compatible with the behavior of renderPart() and renderPartExcept() from the old Forge AdvancedModel for OBJ that adapt into MWF.
	 */
    protected List<Pair<NodeModel, MutableBoolean>> singleNodeVisibleToggles;

    /**
     * Render config of the EnhancedModel
     */
    public EnhancedRenderConfig config;

    /**
     * Render config of the EnhancedModel
     */
    public BaseType baseType;

    public EnhancedModel(EnhancedRenderConfig config, BaseType baseType){
        this.config = config;
        this.baseType = baseType;
        if(!isInit){
            MCglTF.getInstance().addGltfModelReceiver(this);
            isInit = true;
        }
    }

    @Override
    @SideOnly(value = Side.CLIENT)
    @Optional.Method(modid="mcgltf")
    public ResourceLocation getModelLocation() {
        return new ResourceLocation(ModularWarfare.MOD_ID, "gltf/" + baseType.getAssetDir() + "/" + this.config.modelFileName);
    }

    @Override
    @SideOnly(value = Side.CLIENT)
    @Optional.Method(modid="mcgltf")
	public boolean isReceiveSharedModel(GltfModel gltfModel, List<Runnable> gltfRenderDatas) {
    	this.gltfModel = gltfModel;
    	IRenderedGltfModelMWF renderedModel;
    	
    	switch(MCglTF.getInstance().getRenderedModelGLProfile()) {
    	case GL43:
    		renderedModel = new RenderedGltfModelMWF(gltfRenderDatas, gltfModel);
    		break;
    	case GL40:
    		renderedModel = new RenderedGltfModelGL40MWF(gltfRenderDatas, gltfModel);
    		break;
    	case GL33:
    		renderedModel = new RenderedGltfModelGL33MWF(gltfRenderDatas, gltfModel);
    		break;
    	case GL30:
    		renderedModel = new RenderedGltfModelGL30MWF(gltfRenderDatas, gltfModel);
    		break;
    	case GL20:
    		renderedModel = new RenderedGltfModelGL20MWF(gltfRenderDatas, gltfModel);
    		break;
		default:
			ContextCapabilities contextCapabilities = GLContext.getCapabilities();
			if(contextCapabilities.OpenGL43) renderedModel = new RenderedGltfModelMWF(gltfRenderDatas, gltfModel);
			else if(contextCapabilities.OpenGL40) renderedModel = new RenderedGltfModelGL40MWF(gltfRenderDatas, gltfModel);
			else if(contextCapabilities.OpenGL33) renderedModel = new RenderedGltfModelGL33MWF(gltfRenderDatas, gltfModel);
			else renderedModel = new RenderedGltfModelGL20MWF(gltfRenderDatas, gltfModel);
    	}
    	
    	renderedScene = renderedModel.getRenderedGltfScenes().get(0);
        
        singleNodeVisibleToggles = renderedModel.getSingleNodeVisibleToggles();
        
        List<AnimationModel> animationModels = gltfModel.getAnimationModels();
        animations = new ArrayList<Pair<String, List<InterpolatedChannel>>>(animationModels.size());
		for(AnimationModel animationModel : animationModels) {
			animations.add(Pair.of(animationModel.getName(), GltfAnimationCreator.createGltfAnimation(animationModel)));
		}
		
		return false;
	}

    public NodeModel getPart(String part){
        for(NodeModel node : gltfModel.getNodeModels()){
            if(node.getName().equalsIgnoreCase(part)) {
                return node;
            }
        }
        return null;
    }

    public void translatef(float x, float y, float z){
        float[] t = getPart("root").getTranslation();
        t[0] = t[0] + x;
        t[1] = t[1] + y;
        t[2] = t[2] + z;
        getPart("root").setTranslation(new float[] {t[0], t[1], t[2]});
    }

    public void rotatef(float x, float y, float z){
        Quaternion quat_actual = new Quaternion(getPart("root").getRotation()[0], getPart("root").getRotation()[1], getPart("root").getRotation()[2], getPart("root").getRotation()[3]);

        float[] rot = MathUtils.getQuat(x, y, z);
        Quaternion quat_rot = new Quaternion(rot[0], rot[1], rot[2], rot[3]);

        Quaternion rot_final = new Quaternion();
        Quaternion.mul(quat_actual, quat_rot, rot_final);

        getPart("root").setRotation(new float[]{rot_final.x, rot_final.y, rot_final.z, rot_final.w});
    }
    
    public void renderAll(){
    	GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        
        RenderedGltfModelMWF.CURRENT_BASE_TYPE = baseType;
        
        if(MCglTF.getInstance().isShaderModActive()) {
			renderedScene.renderForShaderMod();
		}
		else {
			renderedScene.renderForVanilla();
		}
        
        RenderedGltfModelMWF.CURRENT_BASE_TYPE = null;
        
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    
    public void renderPart(String... parts){
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        
        for(Pair<NodeModel, MutableBoolean> nodeVisibleToggle : singleNodeVisibleToggles) {
        	contain: {
        		for(String part : parts) {
        			if(nodeVisibleToggle.getLeft().getName().equalsIgnoreCase(part)) {
            			break contain;
            		}
                }
        		nodeVisibleToggle.getRight().setValue(false);
        	}
    	}
        
        RenderedGltfModelMWF.CURRENT_BASE_TYPE = baseType;
        
        if(MCglTF.getInstance().isShaderModActive()) {
			renderedScene.renderForShaderMod();
		}
		else {
			renderedScene.renderForVanilla();
		}
        
        RenderedGltfModelMWF.CURRENT_BASE_TYPE = null;
        
        for(Pair<NodeModel, MutableBoolean> nodeVisibleToggle : singleNodeVisibleToggles) {
        	nodeVisibleToggle.getRight().setValue(true);
        }
        
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    
    public void renderPartExcept(HashSet<String> except){
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        
        for(Pair<NodeModel, MutableBoolean> nodeVisibleToggle : singleNodeVisibleToggles) {
        	for(String part : except) {
        		if(nodeVisibleToggle.getLeft().getName().equalsIgnoreCase(part)) {
        			nodeVisibleToggle.getRight().setValue(false);
        			break;
        		}
            }
    	}
        
        RenderedGltfModelMWF.CURRENT_BASE_TYPE = baseType;
        
        if(MCglTF.getInstance().isShaderModActive()) {
			renderedScene.renderForShaderMod();
		}
		else {
			renderedScene.renderForVanilla();
		}
        
        RenderedGltfModelMWF.CURRENT_BASE_TYPE = null;
        
        for(Pair<NodeModel, MutableBoolean> nodeVisibleToggle : singleNodeVisibleToggles) {
        	nodeVisibleToggle.getRight().setValue(true);
        }
        
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    
    public void renderPartExcept(String...strings ){
        HashSet<String> nameSet=new HashSet<String>();
        for(String str:strings) {
            nameSet.add(str);
        }
        renderPartExcept(nameSet);
    }

    @Override
    public void renderPart(String part, float scale) {
        renderPart(part);
    }


	public void applyGlobalTransformToOther(String part, Runnable otherModelCommand) {
		for(NodeModel nodeModel : gltfModel.getNodeModels()) {
			if(nodeModel.getName().equalsIgnoreCase(part)) {
				GL11.glPushMatrix();
				BUF_FLOAT_16.clear();
				float[] transform = new float[16];
				nodeModel.computeGlobalTransform(transform);
				BUF_FLOAT_16.put(transform);
				BUF_FLOAT_16.rewind();
				GL11.glMultMatrix(BUF_FLOAT_16);
				otherModelCommand.run();
				GL11.glPopMatrix();
				return;
			}
		}
	}

	public void applyGlobalInverseTransformToOther(String part, Runnable otherModelCommand) {
		for(NodeModel nodeModel : gltfModel.getNodeModels()) {
			if(nodeModel.getName().equalsIgnoreCase(part)) {
				GL11.glPushMatrix();
				BUF_FLOAT_16.clear();
				float[] transform = new float[16];
				nodeModel.computeGlobalTransform(transform);
				de.javagl.jgltf.model.MathUtils.invert4x4(transform, transform);
				BUF_FLOAT_16.put(transform);
				BUF_FLOAT_16.rewind();
				GL11.glMultMatrix(BUF_FLOAT_16);
				otherModelCommand.run();
				GL11.glPopMatrix();
				return;
			}
		}
	}
    
    public void updateAnimation(float time) {
    	for(Pair<String, List<InterpolatedChannel>> animationWithName : animations) {
    		animationWithName.getRight().parallelStream().forEach((channel) -> {
				float[] keys = channel.getKeys();
				channel.update(time % keys[keys.length - 1]);
			});
		}
    }
    
    //TODO: Replace above method with this in the future.
    public void updateAnimation(float time, String animationName) {
    	for(Pair<String, List<InterpolatedChannel>> animationWithName : animations) {
    		if(animationWithName.getLeft().equals(animationName)) {
    			animationWithName.getRight().parallelStream().forEach((channel) -> {
    				float[] keys = channel.getKeys();
    				channel.update(time % keys[keys.length - 1]);
    			});
    		}
		}
    }
}
