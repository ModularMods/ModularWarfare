package com.modularwarfare.common.handler.data;

import net.minecraft.item.ItemStack;

public class DataGunReloadEnhancedTask {
    public int gunSlot;
    public ItemStack reloadGun;
    public ItemStack prognosisAmmo;
    public int reloadCount;
    public boolean currentAmmo;
    public boolean multi;
    public Integer multiMagToLoad;
    public boolean isUnload;

    public DataGunReloadEnhancedTask() {
        // TODO Auto-generated constructor stub
    }

    public DataGunReloadEnhancedTask(int gunSlot, ItemStack reloadGun, boolean isUnload) {
        this.gunSlot=gunSlot;
        this.reloadGun=reloadGun;
        this.isUnload=isUnload;
    }
    
    public DataGunReloadEnhancedTask(int gunSlot, ItemStack reloadGun, boolean isUnload,int reloadCount) {
        this.gunSlot=gunSlot;
        this.reloadGun=reloadGun;
        this.isUnload=isUnload;
        this.reloadCount=reloadCount;
    }

    public DataGunReloadEnhancedTask(int gunSlot, ItemStack reloadGun, ItemStack prognosisAmmo, int reloadCount,
            boolean currentAmmo, boolean multi, Integer multiMagToLoad) {
        this.gunSlot=gunSlot;
        this.reloadGun = reloadGun;
        this.prognosisAmmo = prognosisAmmo;
        this.reloadCount = reloadCount;
        this.currentAmmo = currentAmmo;
        this.multi = multi;
        this.multiMagToLoad = multiMagToLoad;
    }

    public DataGunReloadEnhancedTask(int gunSlot, ItemStack reloadGun, ItemStack prognosisAmmo, int reloadCount) {
        this(gunSlot, reloadGun, prognosisAmmo, reloadCount, false, false, null);
    }

}
