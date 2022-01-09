package com.modularwarfare.loader;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.loader.api.model.AbstractObjModel;
import com.modularwarfare.loader.api.model.ObjModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ObjModel extends AbstractObjModel {

    public List<ObjModelRenderer> parts;
    public float modelScale = 1.0f;
    private List<ObjModelRenderer> duplications = new ArrayList<>();

    public ObjModel(List<ObjModelRenderer> parts) {
        this.parts = parts;
    }

    public ObjModel() {
    }

    @Override
    public List<ObjModelRenderer> getParts() {
        return parts;
    }

    void setParts(List<ObjModelRenderer> renderers) {
        parts = renderers;
    }

    public ObjModelRenderer getPart(String name) {
        for (ObjModelRenderer part : parts) {
            if (name.contains(part.getName())) {
                return part;
            }
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderAll(float scale) {
        checkForNoDuplications();
        for (ObjModelRenderer part : parts) {
            part.render(scale);
        }
    }

    /**
     * Removes all generated duplications, which will appear if you add children to other {@link ObjModelRenderer}s.
     * You may separate staticModel parts and add children during for example constructing staticModel.
     * <p>
     * If you forget to clear duplications, error messages will be printed to console every render frame.
     * <p>
     * MUST be called AFTER adding children to other {@link ObjModelRenderer}s.
     * MUST NOT be called while passing {@link #getParts()}, because it will throw {@link ConcurrentModificationException};
     */
    @Override
    public void clearDuplications() throws ConcurrentModificationException {
        try {
            for (ObjModelRenderer renderer : duplications) {
                parts.remove(renderer);
            }
        } catch (ConcurrentModificationException e) {
            throw new ConcurrentModificationException("You must clear duplications ONLY AFTER passing ObjModelRaw#parts!!!\n" + e.getMessage());
        }

        duplications.clear();
    }

    @Override
    public boolean hasDuplications() {
        return !duplications.isEmpty();
    }

    private String[] formDuplicationList() {
        String[] list = new String[duplications.size()];
        for (int i = 0; i < duplications.size(); i++) {
            list[i] = duplications.get(i).getName();
        }

        return list;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOnly(float scale, String... groupNames) {
        checkForNoDuplications();
        for (ObjModelRenderer part : parts) {
            for (String groupName : groupNames) {
                if (groupName.contains(part.getName())) {
                    part.render(scale);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderOnly(float scale, ObjModelRenderer... partsIn) {
        checkForNoDuplications();
        for (ObjModelRenderer part : parts) {
            for (ObjModelRenderer partIn : partsIn) {
                if (part.equals(partIn)) {
                    part.render(scale);
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderPart(float scale, String partName) {
        checkForNoDuplications();
        for (ObjModelRenderer part : parts) {
            if (partName.contains(part.getName())) {
                part.render(scale);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderPart(float scale, ObjModelRenderer partIn) {
        checkForNoDuplications();
        for (ObjModelRenderer part : parts) {
            if (part.equals(partIn)) {
                part.render(scale);
            }
        }
    }

    /**
     * Renders all parts except given. If excluded part has children, they will be counted as excluded (but it won't work if you hadn't cleared duplications through {@link #clearDuplications()}).
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderAllExcept(float scale, ObjModelRenderer... excludedPartsIn) {
        checkForNoDuplications();
        for (ObjModelRenderer part : parts) {
            boolean skipPart = isExcepted(part, excludedPartsIn);

            if (!skipPart) {
                part.render(scale);
            }
        }
    }

    private boolean isExcepted(ObjModelRenderer part, ObjModelRenderer[] excludedList) {
        for (ObjModelRenderer excludedPart : excludedList) {
            if (part.equals(excludedPart)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addDuplication(ObjModelRenderer renderer) {
        duplications.add(renderer);
    }

    private void checkForNoDuplications() {
        if (hasDuplications()) {
            ModularWarfare.LOGGER.error("=============================================================");
            ModularWarfare.LOGGER.error("Duplications were found! You must call method ObjModelRaw#clearDuplications() after adding children to renders.");
            ModularWarfare.LOGGER.error("Duplications:");

            for (String str : formDuplicationList()) {
                ModularWarfare.LOGGER.error(str);
            }

            ModularWarfare.LOGGER.error("=============================================================");
        }
    }
}
