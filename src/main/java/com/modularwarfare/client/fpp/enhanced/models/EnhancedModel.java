package com.modularwarfare.client.fpp.enhanced.models;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.IMWModel;
import com.modularwarfare.api.IProcessNodeModelHandler;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.fpp.enhanced.transforms.DefaultTransform;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.utility.maths.MathUtils;
import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.IMaterialHandler;
import com.timlee9024.mcgltf.MCglTF;
import com.timlee9024.mcgltf.RenderedGltfModel;
import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.SceneModel;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Quaternion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EnhancedModel implements IGltfModelReceiver,IMWModel{

    /**
     * Animation Runnable
     */
    protected List<Runnable> commands;

    /**
     * List of all .gltf animations loaded
     */
    protected List<Animation> animations=new ArrayList<Animation>();

    private boolean isInit = false;

    /**
     * .gltf Model
     */
    public GltfModel gltfModel;
    public RenderedGltfModel model;
    protected HashMap<String, Runnable> renderStreamMap=new HashMap<String, Runnable>();
    protected HashMap<String, Runnable> skinningStreamMap=new HashMap<String, Runnable>();
    protected HashMap<String, Runnable> transformStreamMap=new HashMap<String, Runnable>();
    protected HashMap<String, Runnable> transformInverseStreamMap=new HashMap<String, Runnable>();

    /**
     * Default transforms of Nodes
     */
    protected Map<NodeModel, DefaultTransform> defaultTransforms = new HashMap<>();

    /**
     * Render config of the EnhancedModel
     */
    public GunEnhancedRenderConfig config;

    /**
     * Render config of the EnhancedModel
     */
    public BaseType baseType;

    public EnhancedModel(GunEnhancedRenderConfig config, BaseType baseType){
        this.config = config;
        this.baseType = baseType;
        if(!isInit){
            MCglTF.getInstance().addGltfModelReceiver(this);
            isInit = true;
        }
    }

    @Override
    public ResourceLocation getModelLocation() {
        return new ResourceLocation(ModularWarfare.MOD_ID, "gltf/" + baseType.getAssetDir() + "/" + this.config.modelFileName);
    }

    @Override
    public void onModelLoaded(RenderedGltfModel renderedModel) {
        model=renderedModel;
        commands = renderedModel.sceneCommands.get(0);
        animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
        gltfModel = renderedModel.gltfModel;
        for(NodeModel node : gltfModel.getNodeModels()){
            this.defaultTransforms.put(node, new DefaultTransform(node.getTranslation(), node.getRotation(), node.getScale(), node.getWeights()));
        }
        ((IProcessNodeModelHandler) model).getMaterialModelToMaterialHandler().keySet().forEach((material) -> {
            ((IProcessNodeModelHandler) model).getMaterialModelToMaterialHandler().put(material,
                    new IMaterialHandler() {
                        public Runnable getPreMeshDrawCommand() {
                            return () -> {
                                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                                //this binding is necessary
                                Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
                                Minecraft.getMinecraft().renderEngine
                                        .bindTexture(ClientProxy.gunEnhancedRenderer.bindingTexture);
                                GL11.glColor4f(ClientProxy.gunEnhancedRenderer.r, ClientProxy.gunEnhancedRenderer.g,
                                        ClientProxy.gunEnhancedRenderer.b, ClientProxy.gunEnhancedRenderer.a);
                            };
                        }
                    });
        });
        SceneModel sceneModel = gltfModel.getSceneModels().get(0);

        Consumer<NodeModel> consumer = new Consumer<NodeModel>() {

            @Override
            public void accept(NodeModel t) {
                List<Runnable> singleRenderCommands = new ArrayList<Runnable>();
                List<Runnable> singleSkinningCommands = new ArrayList<Runnable>();
                List<Runnable> transformCommands = new ArrayList<Runnable>();
                List<Runnable> transformInverseCommands = new ArrayList<Runnable>();
                ((IProcessNodeModelHandler) renderedModel).processSingleNodeModel(t, singleRenderCommands,
                        singleSkinningCommands, transformCommands,transformInverseCommands);
                renderStreamMap.put(t.getName(), () -> {
                    singleRenderCommands.forEach((command) -> command.run());
                    onPostRender(t.getName());
                    GlStateManager.popMatrix();
                });
                skinningStreamMap.put(t.getName(), () -> {
                    singleSkinningCommands.forEach((command) -> command.run());
                    onPostSkinning(t.getName());
                    //GlStateManager.popMatrix();
                });
                transformStreamMap.put(t.getName(), () -> {
                    transformCommands.forEach((command) -> command.run());
                });
                transformInverseStreamMap.put(t.getName(), () -> {
                    transformInverseCommands.forEach((command) -> command.run());
                });
                t.getChildren().forEach(this);
            }
        };
        for (NodeModel nodeModel : sceneModel.getNodeModels()) {
            consumer.accept(nodeModel);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
        GL30.glBindVertexArray(0);
        GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
    }

    public float getDuration(){
        float duration = 0f;
        for(Animation anim : animations){
            if(anim.getEndTimeS() > duration){
                duration = anim.getEndTimeS();
            }
        }
        return duration;
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
    
    public void onPostRender(String name) {
        
    }
    
    public void onPostSkinning(String name) {
        
    }

    public void renderAll(){
        if(model==null) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);


        if(!skinningStreamMap.isEmpty()) {
            int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            GL20.glUseProgram(MCglTF.getInstance().getGlProgramSkinnig());
            skinningStreamMap.values().forEach((command)->command.run());
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
            GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
            GL20.glUseProgram(currentProgram);
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);  
        }
        GL20.glVertexAttrib2f(RenderedGltfModel.mc_midTexCoord, 1.0F, 1.0F);
        renderStreamMap.values().forEach((command)->command.run());
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    
    public void renderPart(String... part){
        if(model==null) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);


        HashSet<String> nameSet=new HashSet<>();
        for(String str:part) {
            nameSet.add(str);
        }
        if(!skinningStreamMap.isEmpty()) {
            int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            GL20.glUseProgram(MCglTF.getInstance().getGlProgramSkinnig());
            nameSet.forEach((name)->{
                if(skinningStreamMap.containsKey(name)) {
                    skinningStreamMap.get(name).run();
                }
            });
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
            GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
            GL20.glUseProgram(currentProgram);
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);  
        }
        GL20.glVertexAttrib2f(RenderedGltfModel.mc_midTexCoord, 1.0F, 1.0F);
        nameSet.forEach((name)->{
            if(renderStreamMap.containsKey(name)) {
               renderStreamMap.get(name).run();
            }
        });
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
    
    public void renderPartExcept(HashSet<String> except){
        if(model==null) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);


        HashSet<String> nameSet=except;
        if(!skinningStreamMap.isEmpty()) {
            int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            GL20.glUseProgram(MCglTF.getInstance().getGlProgramSkinnig());
            skinningStreamMap.keySet().forEach((name)->{
                if(!nameSet.contains(name)) {
                    skinningStreamMap.get(name).run();
                }
            });
            GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
            GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);
            GL20.glUseProgram(currentProgram);
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);  
        }
        GL20.glVertexAttrib2f(RenderedGltfModel.mc_midTexCoord, 1.0F, 1.0F);
        renderStreamMap.keySet().forEach((name)->{
            if(!nameSet.contains(name)) {
                renderStreamMap.get(name).run();
            }
        });
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

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
    
    
    public void applyGlobalTransform(String part, Runnable run) {
        if(transformStreamMap.containsKey(part)) {
            transformStreamMap.get(part).run();
            run.run();
            GlStateManager.popMatrix();
        }
    }
    
    public void applyGlobalInverseTransform(String part, Runnable run) {
        if(transformInverseStreamMap.containsKey(part)) {
            transformInverseStreamMap.get(part).run();
            run.run();
            GlStateManager.popMatrix();
        }
    }
    
    public void clearTransformLookup() {
        ((IProcessNodeModelHandler) model).getNodeGlobalTransformLookup().clear();
    }
    
    public void updateAnimation(float time) {
        try {
            this.clearTransformLookup();
            for(Animation animation : animations) {
                animation.update(time);
            }  
        }catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
