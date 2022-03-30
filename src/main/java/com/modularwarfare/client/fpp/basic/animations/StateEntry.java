package com.modularwarfare.client.fpp.basic.animations;

import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.guns.WeaponSoundType;
import com.modularwarfare.common.guns.WeaponType;
import com.modularwarfare.utility.NumberHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EntityEquipmentSlot;

public class StateEntry {

    public static float smoothing = 1f;
    public float stateTime = 0;
    public float currentValue = 0f;
    public float lastValue = 0f;
    public StateType stateType;
    public float cutOffTime;
    public boolean finished = false;
    private MathType mathType;
    private float minValue;
    private float incrementValue;
    private float startingValue;
    private float operationCount;

    public StateEntry(StateType stateType, float stateTime, float startingValue, MathType mathType) {
        this(stateType, stateTime, startingValue, mathType, 1);
    }

    public StateEntry(StateType stateType, float stateTime, float startingValue, MathType mathType, int operationCount) {
        this.stateTime = stateTime;
        this.startingValue = currentValue = lastValue = startingValue;
        this.mathType = mathType;
        this.stateType = stateType;
        this.minValue = 0;
        this.incrementValue = 1f;
        this.operationCount = operationCount;
    }

    public void onTick(float reloadTime) {
        lastValue = currentValue;
        if (mathType == MathType.Add)
            currentValue += (incrementValue * smoothing / (reloadTime * stateTime)) * operationCount;
        else if (mathType == MathType.Sub)
            currentValue -= (incrementValue * smoothing / (reloadTime * stateTime)) * operationCount;

        currentValue = NumberHelper.clamp(currentValue, minValue, 0.999f);

        if ((currentValue == 1f || currentValue == 0f) && operationCount > 1) {
            currentValue = startingValue;
            if (Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null) {
                if (Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                    ItemGun gun = (ItemGun) Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem();
                    if (gun.type.weaponType == WeaponType.Shotgun || gun.type.weaponType == WeaponType.Revolver) {
                        gun.type.playClientSound(Minecraft.getMinecraft().player, WeaponSoundType.BulletLoad);
                    }
                }
            }
            ClientRenderHooks.getAnimMachine(Minecraft.getMinecraft().player).bulletsToRender++;
            operationCount--;
        }
    }

    public static enum MathType {
        Add,
        Sub;
    }

}
