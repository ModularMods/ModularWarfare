package com.modularwarfare.common.armor;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.MWArmorModel;
import com.modularwarfare.api.MWArmorType;
import com.modularwarfare.client.fpp.basic.configs.ArmorRenderConfig;
import com.modularwarfare.client.model.ModelCustomArmor;
import com.modularwarfare.common.type.BaseType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;

public class ArmorType extends BaseType {
    public Integer durability;
    public double defense;

    @Deprecated
    public boolean simpleArmor = false;

    public HashMap<MWArmorType, ArmorInfo> armorTypes;
    
    public transient ArmorRenderConfig renderConfig;

    public ArmorType() {
        this.armorTypes = new HashMap<MWArmorType, ArmorInfo>();
    }

    public void initializeArmor(final String slot) {
        for (final MWArmorType armorType : this.armorTypes.keySet()) {
            if (armorType.name().toLowerCase().equalsIgnoreCase(slot)) {
                this.armorTypes.get(armorType).internalName = this.internalName + ((this.armorTypes.size() > 1) ? ("_" + slot) : "");
            }
            if (this.armorTypes.get(armorType).armorModels != null) {
                for (MWArmorModel model : MWArmorModel.values()) {
                    if (this.armorTypes.get(armorType).armorModels.contains(model)) {
                        this.armorTypes.get(armorType).showArmorModels.put(model, true);
                    } else {
                        this.armorTypes.get(armorType).showArmorModels.put(model, false);
                    }
                }
            }
        }
    }

    @Override
    public void loadExtraValues() {
        if (this.maxStackSize == null) {
            this.maxStackSize = 1;
        }
        this.loadBaseValues();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void reloadModel() {
        renderConfig=ModularWarfare.getRenderConfig(this, ArmorRenderConfig.class);
        if(!simpleArmor)
            this.bipedModel = new ModelCustomArmor(renderConfig, this);
    }

    @Override
    public String getAssetDir() {
        return "armor";
    }

    public static class ArmorInfo {
        public String displayName;
        public ArrayList<MWArmorModel> armorModels;

        public transient HashMap<MWArmorModel, Boolean> showArmorModels = new HashMap<>();
        public transient String internalName;
    }
}
