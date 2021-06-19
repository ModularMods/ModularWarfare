package com.modularwarfare.utility;

import com.modularwarfare.ModConfig;
import com.modularwarfare.common.guns.ItemGun;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nullable;

public class ModularDamageSource extends EntityDamageSource {

    public ItemGun gun;
    public boolean isHeadshot;

    public ModularDamageSource(String damageTypeIn, @Nullable Entity damageSourceEntityIn, ItemGun gun, boolean isHeadshot) {
        super(damageTypeIn, damageSourceEntityIn);
        this.gun = gun;
        this.isHeadshot = isHeadshot;
    }

    /**
     * Gets the death message that is displayed when the player dies
     */
    public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
        if (!ModConfig.INSTANCE.killFeed.sendDefaultKillMessage)
            return null;
        ItemStack itemstack = this.damageSourceEntity instanceof EntityLivingBase ? ((EntityLivingBase) this.damageSourceEntity).getHeldItemMainhand() : ItemStack.EMPTY;
        String s = "death.attack." + this.damageType;
        String s1 = s + ".item";
        return !itemstack.isEmpty() && itemstack.hasDisplayName() && I18n.canTranslate(s1) ? new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName(), this.damageSourceEntity.getDisplayName(), itemstack.getTextComponent()}) : new TextComponentTranslation(s, new Object[]{entityLivingBaseIn.getDisplayName(), this.damageSourceEntity.getDisplayName()});
    }
}
