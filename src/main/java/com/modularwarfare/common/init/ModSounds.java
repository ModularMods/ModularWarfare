package com.modularwarfare.common.init;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = "modularwarfare")
@GameRegistry.ObjectHolder("modularwarfare")
public class ModSounds {

    public static SoundEvent STEP_GRASS_WALK;
    public static SoundEvent STEP_GRASS_SPRINT;

    public static SoundEvent STEP_STONE_WALK;
    public static SoundEvent STEP_STONE_SPRINT;

    public static SoundEvent STEP_GRAVEL_WALK;
    public static SoundEvent STEP_GRAVEL_SPRINT;

    public static SoundEvent STEP_METAL_WALK;
    public static SoundEvent STEP_METAL_SPRINT;

    public static SoundEvent STEP_WOOD_WALK;
    public static SoundEvent STEP_WOOD_SPRINT;

    public static SoundEvent STEP_SAND_WALK;
    public static SoundEvent STEP_SAND_SPRINT;

    public static SoundEvent STEP_SNOW_WALK;
    public static SoundEvent STEP_SNOW_SPRINT;

    public static SoundEvent EQUIP_EXTRA;

    public static SoundEvent GRENADE_THROW;
    public static SoundEvent GRENADE_HIT;
    public static SoundEvent GRENADE_ARM;

    public static SoundEvent GRENADE_SMOKE;
    public static SoundEvent GRENADE_STUN;
    public static SoundEvent FLASHED;

    public static SoundEvent EXPLOSIONS_CLOSE;
    public static SoundEvent EXPLOSIONS_DISTANT;
    public static SoundEvent EXPLOSIONS_FAR;

    public static SoundEvent WHISTLE;

    @SubscribeEvent
    public static void onRegisterSound(RegistryEvent.Register<SoundEvent> event) {

        registerSound(event.getRegistry(), "human.step.grass.walk");
        registerSound(event.getRegistry(), "human.step.grass.sprint");

        registerSound(event.getRegistry(), "human.step.stone.walk");
        registerSound(event.getRegistry(), "human.step.stone.sprint");

        registerSound(event.getRegistry(), "human.step.gravel.walk");
        registerSound(event.getRegistry(), "human.step.gravel.sprint");

        registerSound(event.getRegistry(), "human.step.metal.walk");
        registerSound(event.getRegistry(), "human.step.metal.sprint");

        registerSound(event.getRegistry(), "human.step.wood.walk");
        registerSound(event.getRegistry(), "human.step.wood.sprint");

        registerSound(event.getRegistry(), "human.step.sand.walk");
        registerSound(event.getRegistry(), "human.step.sand.sprint");

        registerSound(event.getRegistry(), "human.step.snow.walk");
        registerSound(event.getRegistry(), "human.step.snow.sprint");

        registerSound(event.getRegistry(), "human.equip.extra");

        registerSound(event.getRegistry(), "grenade_throw");
        registerSound(event.getRegistry(), "grenade_hit");
        registerSound(event.getRegistry(), "grenade_arm");

        registerSound(event.getRegistry(), "explosions.close");
        registerSound(event.getRegistry(), "explosions.distant");
        registerSound(event.getRegistry(), "explosions.far");

        registerSound(event.getRegistry(), "human.other.whistle");


        registerSound(event.getRegistry(), "smoke_grenade");

        registerSound(event.getRegistry(), "stun_grenade");
        registerSound(event.getRegistry(), "flashed");

        STEP_GRASS_WALK = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.grass.walk"));
        STEP_GRASS_SPRINT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.grass.sprint"));

        STEP_STONE_WALK = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.stone.walk"));
        STEP_STONE_SPRINT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.stone.sprint"));

        STEP_GRAVEL_WALK = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.gravel.walk"));
        STEP_GRAVEL_SPRINT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.gravel.sprint"));

        STEP_METAL_WALK = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.metal.walk"));
        STEP_METAL_SPRINT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.metal.sprint"));

        STEP_WOOD_WALK = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.wood.walk"));
        STEP_WOOD_SPRINT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.wood.sprint"));

        STEP_SAND_WALK = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.sand.walk"));
        STEP_SAND_SPRINT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.sand.sprint"));

        STEP_SNOW_WALK = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.snow.walk"));
        STEP_SNOW_SPRINT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.step.snow.sprint"));

        EQUIP_EXTRA = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.equip.extra"));

        GRENADE_THROW = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "grenade_throw"));
        GRENADE_HIT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "grenade_hit"));
        GRENADE_ARM = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "grenade_arm"));
        GRENADE_SMOKE = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "smoke_grenade"));

        GRENADE_STUN = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "stun_grenade"));
        FLASHED = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "flashed"));

        EXPLOSIONS_CLOSE = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "explosions.close"));
        EXPLOSIONS_DISTANT = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "explosions.distant"));
        EXPLOSIONS_FAR = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "explosions.far"));


        WHISTLE = SoundEvent.REGISTRY.getObject(new ResourceLocation("modularwarfare", "human.other.whistle"));
    }

    public static void registerSound(IForgeRegistry<SoundEvent> r, String name) {
        ResourceLocation loc = new ResourceLocation("modularwarfare", name);
        r.register(new SoundEvent(loc).setRegistryName(loc));
    }

}
