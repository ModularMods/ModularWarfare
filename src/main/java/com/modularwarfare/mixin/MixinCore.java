package com.modularwarfare.mixin;

import com.modularwarfare.ModularWarfare;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("modularwarfare")
public class MixinCore implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        ModMixinPatchLoader.load();
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins." + ModularWarfare.MOD_ID + ".json");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
