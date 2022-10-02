package com.modularwarfare.common.type;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.GunBobbingEvent;
import com.modularwarfare.api.MWArmorType;
import com.modularwarfare.api.TypeRegisterEvent;
import com.modularwarfare.client.fpp.basic.configs.*;
import com.modularwarfare.client.fpp.enhanced.configs.GunEnhancedRenderConfig;
import com.modularwarfare.client.model.*;
import com.modularwarfare.common.armor.ArmorType;
import com.modularwarfare.common.armor.ItemMWArmor;
import com.modularwarfare.common.armor.ItemSpecialArmor;
import com.modularwarfare.common.backpacks.BackpackType;
import com.modularwarfare.common.backpacks.ItemBackpack;
import com.modularwarfare.common.grenades.GrenadeType;
import com.modularwarfare.common.grenades.ItemGrenade;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.textures.TextureType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ContentTypes {

    public static ArrayList<TypeEntry> values = new ArrayList<TypeEntry>();
    private static int typeId = 0;

    public static void registerTypes() {
        registerType("textures", TextureType.class, (type, reload) -> {
            ModularWarfare.textureTypes.put(type.internalName, (TextureType) type);
        });

        registerType("guns", GunType.class, (type, reload) -> {
            ContentTypes.<GunType, ItemGun>assignType(ModularWarfare.gunTypes, ItemGun.factory, (GunType) type, reload);

            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                if(((GunType)type).animationType.equals(WeaponAnimationType.ENHANCED)){
                    ((type.enhancedModel)).config = ModularWarfare.getRenderConfig(type, GunEnhancedRenderConfig.class);
                } else {
                    ((ModelGun) (type.model)).config = ModularWarfare.getRenderConfig(type, GunRenderConfig.class);
                }
            }
        });

        registerType("ammo", AmmoType.class, (type, reload) -> {
            ContentTypes.<AmmoType, ItemAmmo>assignType(ModularWarfare.ammoTypes, ItemAmmo.factory, (AmmoType) type, reload);

            if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
                if (((AmmoType) type).isDynamicAmmo) {
                    ((ModelAmmo) (type.model)).config = ModularWarfare.getRenderConfig(type, AmmoRenderConfig.class);
                }
        });

        registerType("attachments", AttachmentType.class, (type, reload) -> {
            ContentTypes.<AttachmentType, ItemAttachment>assignType(ModularWarfare.attachmentTypes, ItemAttachment.factory, (AttachmentType) type, reload);

            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                ((ModelAttachment) (type.model)).config = ModularWarfare.getRenderConfig(type, AttachmentRenderConfig.class);
                ((ModelAttachment) (type.model)).config.init();
            }
                
        });

        registerType("armor", ArmorType.class, (type, reload) -> {
            ArmorType armorType = (ArmorType) type;

            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                if (!armorType.simpleArmor) {
                    ((ModelCustomArmor) (type.bipedModel)).config = ModularWarfare.getRenderConfig(type, ArmorRenderConfig.class);
                }
            }
            if (!reload) {
                for (MWArmorType mwArmorType : armorType.armorTypes.keySet()) {
                    if (MWArmorType.isVanilla(mwArmorType)) {
                        ModularWarfare.armorTypes.put(armorType.internalName + "_" + mwArmorType.name().toLowerCase(), new ItemMWArmor(armorType, mwArmorType));
                        if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                            if (!armorType.simpleArmor) {
                                ((ModelCustomArmor) type.bipedModel).config = ModularWarfare.getRenderConfig(type, ArmorRenderConfig.class);
                            }
                        }
                    } else {
                        ModularWarfare.specialArmorTypes.put(armorType.internalName, new ItemSpecialArmor(armorType, mwArmorType));
                        if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
                            ((ModelCustomArmor) ModularWarfare.specialArmorTypes.get(armorType.internalName).type.bipedModel).config = ModularWarfare.getRenderConfig(type, ArmorRenderConfig.class);
                    }
                }
            } else {
                if (ModularWarfare.armorTypes.containsKey(type.internalName)) {
                    ModularWarfare.armorTypes.get(type.internalName).setType(type);
                }
            }
        });

        registerType("bullets", BulletType.class, (type, reload) -> {
            ContentTypes.<BulletType, ItemBullet>assignType(ModularWarfare.bulletTypes, ItemBullet.factory, (BulletType) type, reload);
        });

        registerType("sprays", SprayType.class, (type, reload) -> {
            ContentTypes.<SprayType, ItemSpray>assignType(ModularWarfare.sprayTypes, ItemSpray.factory, (SprayType) type, reload);
        });

        registerType("backpacks", BackpackType.class, (type, reload) -> {
            ContentTypes.<BackpackType, ItemBackpack>assignType(ModularWarfare.backpackTypes, ItemBackpack.factory, (BackpackType) type, reload);

            if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
                ((ModelBackpack) (type.model)).config = ModularWarfare.getRenderConfig(type, BackpackRenderConfig.class);
        });

        registerType("grenades", GrenadeType.class, (type, reload) -> {
            ContentTypes.<GrenadeType, ItemGrenade>assignType(ModularWarfare.grenadeTypes, ItemGrenade.factory, (GrenadeType) type, reload);

            if (FMLCommonHandler.instance().getSide() == Side.CLIENT)
                ((ModelGrenade) (type.model)).config = ModularWarfare.getRenderConfig(type, GrenadeRenderConfig.class);
        });

        /**
         * Send TypeRegisterEvent
         */
        TypeRegisterEvent event = new TypeRegisterEvent();
        MinecraftForge.EVENT_BUS.post(event);
    }


    public static <T extends BaseType, U extends BaseItem> void assignType(HashMap<String, U> map, Function<T, U> factory, T type, Boolean reload) {
        if (reload) {
            map.get(type.internalName).setType(type);
        } else {
            map.put(type.internalName, factory.apply(type));
        }
    }

    public static void registerType(String name, Class<? extends BaseType> typeClass, BiConsumer<BaseType, Boolean> typeAssignFunction) {
        values.add(new TypeEntry(name, typeClass, typeId, typeAssignFunction));
        typeId += 1;
    }

}
