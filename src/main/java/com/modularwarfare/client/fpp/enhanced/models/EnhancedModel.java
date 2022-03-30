package com.modularwarfare.client.fpp.enhanced.models;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.fpp.enhanced.transforms.DefaultTransform;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.common.vector.Vector3f;
import com.modularwarfare.utility.maths.MathUtils;
import com.timlee9024.mcgltf.IGltfModelReceiver;
import com.timlee9024.mcgltf.MCglTF;
import com.timlee9024.mcgltf.RenderedGltfModel;
import de.javagl.jgltf.model.GltfAnimations;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.MeshModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.animation.Animation;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnhancedModel implements IGltfModelReceiver {

    /**
     * Animation Runnable
     */
    protected List<Runnable> commands;

    /**
     * List of all .gltf animations loaded
     */
    protected List<Animation> animations;

    private boolean isInit = false;

    /**
     * .gltf Model
     */
    protected GltfModel gltfModel;

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
        commands = renderedModel.sceneCommands.get(0);
        animations = GltfAnimations.createModelAnimations(renderedModel.gltfModel.getAnimationModels());
        gltfModel = renderedModel.gltfModel;
        for(NodeModel node : gltfModel.getNodeModels()){
            this.defaultTransforms.put(node, new DefaultTransform(node.getTranslation(), node.getRotation(), node.getScale(), node.getWeights()));
        }
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

    public void render(float time){
        GL11.glPushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        for(NodeModel node : gltfModel.getNodeModels()){
            DefaultTransform defaultTransform = defaultTransforms.get(node);
            node.setTranslation(defaultTransform.translation);
            node.setRotation(defaultTransform.rotation);
            node.setScale(defaultTransform.scale);
            node.setWeights(defaultTransform.weight);
        }

        for(Animation animation : animations) {
            animation.update(time);
        }

        commands.forEach(
                (command) -> {
                    command.run();
                }
        );

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}
