package com.modularwarfare.loader.api.model;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ConcurrentModificationException;
import java.util.List;

public abstract class AbstractObjModel {
    /**
     * Returns all staticModel parts. Use this if you want for example to pull out some important parts.
     */
    public abstract List<ObjModelRenderer> getParts();


    @SideOnly(Side.CLIENT)
    public abstract ObjModelRenderer getPart(String name);

    /**
     * Renders all staticModel parts with given {@code armScale}.
     *
     * @param scale scaleFactor, that determines your staticModel size.
     */
    @SideOnly(Side.CLIENT)
    public abstract void renderAll(float scale);

    /**
     * Renders only staticModel parts, whose names are included in {@code groupNames}.
     * Renders these parts with given {@code armScale}.
     *
     * @param scale      scaleFactor, that determines your staticModel size.
     * @param groupNames the names of parts to be rendered.
     */
    @SideOnly(Side.CLIENT)
    public abstract void renderOnly(float scale, String... groupNames);

    /**
     * Renders only staticModel parts that equals {@code partsIn}.
     * Renders these parts with given {@code armScale}.
     *
     * @param scale   scaleFactor, that determines your staticModel size.
     * @param partsIn the parts to be rendered.
     */
    @SideOnly(Side.CLIENT)
    public abstract void renderOnly(float scale, ObjModelRenderer... partsIn);

    /**
     * Renders staticModel part with given {@code armScale}.
     *
     * @param scale    scaleFactor, that determines your staticModel size.
     * @param partName the name of part to be rendered.
     */
    @SideOnly(Side.CLIENT)
    public abstract void renderPart(float scale, String partName);

    /**
     * Renders staticModel part with given {@code armScale}.
     *
     * @param scale  scaleFactor, that determines your staticModel size.
     * @param partIn the part to be rendered.
     */
    @SideOnly(Side.CLIENT)
    public abstract void renderPart(float scale, ObjModelRenderer partIn);

    /**
     * Renders all parts with given {@code armScale} except given.
     * If excluded part has children, they will be counted as excluded (but it won't work if you hadn't cleared duplications through {@link #clearDuplications()}).
     *
     * @param scale           scaleFactor, that determines your staticModel size.
     * @param excludedPartsIn the parts that won't be rendered.
     */
    @SideOnly(Side.CLIENT)
    public abstract void renderAllExcept(float scale, ObjModelRenderer... excludedPartsIn);

    /**
     * Removes all generated duplications, which will appear if you add children to other {@link ObjModelRenderer}s.
     * You may separate staticModel parts and add children during for example constructing staticModel.
     * Example can be seen here: {@link example.ModelPhoenix}
     * <p>
     * If you forget to clear duplications, error messages will be printed to console every render frame.
     * <p>
     * MUST be called AFTER adding children to other {@link ObjModelRenderer}s.
     * MUST NOT be called while passing {@link #getParts()}, because it will throw {@link ConcurrentModificationException};
     */
    public abstract void clearDuplications() throws ConcurrentModificationException;

    /**
     * Returns true, if staticModel has duplications, that must be cleared by calling {@link #clearDuplications()} method.
     */
    public abstract boolean hasDuplications();

    protected abstract void addDuplication(ObjModelRenderer renderer);
}