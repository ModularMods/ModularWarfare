package com.modularwarfare.common.guns;

import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;

public class ItemBullet extends BaseItem {

    public static final Function<BulletType, ItemBullet> factory = type -> {
        return new ItemBullet(type);
    };
    public BulletType type;

    public ItemBullet(BulletType type) {
        super(type);
        this.type = type;
        this.render3d = false;
    }

    @Override
    public void setType(BaseType type) {
        this.type = (BulletType) type;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

        tooltip.add(generateLoreListEntry("Damage", type.bulletDamageFactor + "x"));

        if (type.bulletProperties != null) {
            for (String key : type.bulletProperties.keySet()) {
                tooltip.add(generateLoreHeader("Modifiers"));
                BulletProperty bulletProperty = type.bulletProperties.get(key);

                if (bulletProperty.potionEffects != null) {
                    tooltip.add(generateLoreHeader("Effects"));
                    for (PotionEntry potionEntry : bulletProperty.potionEffects) {
                        if (bulletProperty.potionEffects != null) {
                            tooltip.add(generateLoreListEntry(potionEntry.potionEffect.name(), ""));
                        }
                    }
                }
                break;
            }
        }
    }

}