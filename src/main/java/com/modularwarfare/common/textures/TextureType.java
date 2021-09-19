package com.modularwarfare.common.textures;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class TextureType extends BaseType {

    public TextureEnumType textureType = TextureEnumType.Flash;
    public String[] imageLocation;

    public transient List<ResourceLocation> resourceLocations = new ArrayList<>();

    @Override
    public void loadExtraValues() {
        if(imageLocation != null && imageLocation.length > 0){
            for(int i = 0; i < imageLocation.length; i++){
                resourceLocations.add(new ResourceLocation(ModularWarfare.MOD_ID, "textures/"+textureType.typeName+"/"+imageLocation[i]));
            }
        } else {
            initDefaultTextures(textureType);
        }
    }


    public void initDefaultTextures(TextureEnumType type){
        this.textureType = type;
        switch (type){
            case Flash:
                for(int i = 0; i < 5; i++){
                    resourceLocations.add(new ResourceLocation(ModularWarfare.MOD_ID, "textures/default/flash/mw.flash"+(i+1)+".png"));
                }
                break;
            case Overlay:
                resourceLocations.add(new ResourceLocation(ModularWarfare.MOD_ID, "textures/default/overlay/mw.scope4x.png"));
                break;
            default:
                break;
        }

    }

    @Override
    public void reloadModel() {
    }

    @Override
    public String getAssetDir() {
        return "textures";
    }

}
