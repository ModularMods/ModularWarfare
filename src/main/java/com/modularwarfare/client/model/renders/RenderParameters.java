package com.modularwarfare.client.model.renders;

import com.modularwarfare.client.ClientRenderHooks;

public class RenderParameters {

    public static float adsSwitch = 0f;
    public static float sprintSwitch = 0f;
    public static float crouchSwitch = 0f;
    public static float reloadSwitch = 1f;
    public static float attachmentSwitch = 0f;

    public static int switchDelay = 20;

    public static float swayVertical = 0f;
    public static float swayHorizontal = 0f;
    public static Float swayVerticalEP;
    public static Float swayHorizontalEP;

    public static float triggerPullSwitch;

    public static String lastModel = "";

    //Default minecraft smoothing tick
    public static float smoothing;

    public static float GUN_ROT_X = 0;
    public static float GUN_ROT_Y = 0;
    public static float GUN_ROT_Z = 0;

    public static float GUN_ROT_X_LAST = 0;
    public static float GUN_ROT_Y_LAST = 0;
    public static float GUN_ROT_Z_LAST = 0;

    // Recoil variables
    /**
     * The recoil applied to the player view by shooting
     */
    public static float playerRecoilPitch;
    public static float playerRecoilYaw;
    public static float prevPitch = 0;

    /**
     * The amount of compensation applied to recoil in order to bring it back to normal
     */
    public static float antiRecoilPitch;
    public static float antiRecoilYaw;

    /**
     * SWAY
     **/
    public static float SMOOTH_SWING;
    public static float VAL;
    public static float VAL2;
    public static float VALROT;
    public static float VALSPRINT;

    // Resets render modifiers
    public static void resetRenderMods() {
        RenderParameters.swayHorizontal = 0f;
        RenderParameters.swayVertical = 0f;
        RenderParameters.swayHorizontalEP = 0f;
        RenderParameters.swayVerticalEP = 0f;
        RenderParameters.reloadSwitch = 0f;
        RenderParameters.sprintSwitch = 0f;
        RenderParameters.adsSwitch = 0f;
        RenderParameters.crouchSwitch = 0f;
        ClientRenderHooks.isAimingScope = false;
        ClientRenderHooks.isAiming = false;
    }

}
