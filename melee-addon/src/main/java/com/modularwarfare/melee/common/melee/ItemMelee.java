package com.modularwarfare.melee.common.melee;

import com.modularwarfare.common.guns.BulletProperty;
import com.modularwarfare.common.guns.BulletType;
import com.modularwarfare.common.guns.PotionEntry;
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

public class ItemMelee extends BaseItem {

    public static final Function<MeleeType, ItemMelee> factory = type -> {
        return new ItemMelee((type));
    };

    public MeleeType type;

    public ItemMelee(MeleeType type) {
        super(type);
        this.type = type;
        this.render3d = true;
    }

    @Override
    public void setType(BaseType type) {
        this.type = (MeleeType) type;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

        tooltip.add(generateLoreListEntry("Damage", String.valueOf(type.damage)));
    }
}
