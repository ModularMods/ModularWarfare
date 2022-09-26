package com.modularwarfare.common.guns;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.fpp.basic.configs.AmmoRenderConfig;
import com.modularwarfare.client.model.ModelAmmo;
import com.modularwarfare.common.type.BaseType;

public class AmmoType extends BaseType {

    /**
     * Ammo Capacity Amount
     */
    public int ammoCapacity = 30;
    /**
     * Magazine Count
     */
    public int magazineCount = 1;

    /**
     * Override ammo deletion, to allow for enabling or disabling of returned empty mags
     */
    public boolean allowEmptyMagazines = true;

    /**
     * If the ammo model need to be rendered on guns
     */
    public boolean isDynamicAmmo = false;

    /**
     * The reload time factor of the ammo (example Fast Mags)
     */
    public float reloadTimeFactor = 1f;
    
    /**
     * for enhanced ASM
     *  the factor bigger the animation plays fasterly
     */
    public float reloadSpeedFactor = 1f;

    /**
     * If the ammo model should use the gun skin
     **/
    public boolean sameTextureAsGun = true;

    // Sub Ammo
    /**
     * The ammo type(s) that can be loaded into this item
     */
    public String[] subAmmo;

    @Override
    public void loadExtraValues() {
        if (maxStackSize == null)
            maxStackSize = 4;

        loadBaseValues();
    }

    @Override
    public void reloadModel() {
        if (isDynamicAmmo) {
            model = new ModelAmmo(ModularWarfare.getRenderConfig(this, AmmoRenderConfig.class), this);
        }
    }

    @Override
    public String getAssetDir() {
        return "ammo";
    }

}