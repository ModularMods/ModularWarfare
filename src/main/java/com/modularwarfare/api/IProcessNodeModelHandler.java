package com.modularwarfare.api;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.gen.Accessor;

import com.timlee9024.mcgltf.IMaterialHandler;

import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.NodeModel;

public interface IProcessNodeModelHandler {
    public void processSingleNodeModel(NodeModel nodeModel, List<Runnable> renderCommands,
            List<Runnable> skinningCommands, List<Runnable> transformCommands, List<Runnable> transformInverseCommands);

    public Map<NodeModel, float[]> getNodeGlobalTransformLookup();

    public Map<MaterialModel, IMaterialHandler> getMaterialModelToMaterialHandler();

    public float[] getGlobalTransform(NodeModel nodeModel);
}
