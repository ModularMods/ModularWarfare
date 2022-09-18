package com.modularwarfare.client.fpp.basic.configs;

import com.modularwarfare.api.WeaponAnimations;
import com.modularwarfare.client.fpp.basic.models.objects.BreakActionData;
import com.modularwarfare.client.fpp.basic.models.objects.RenderVariables;
import com.modularwarfare.common.guns.AttachmentPresetEnum;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;

public class GunRenderConfig {

    public String modelFileName = "";

    public Arms arms = new Arms();

    public Sprint sprint = new Sprint();

    public ThirdPerson thirdPerson = new ThirdPerson();

    public Aim aim = new Aim();

    public Bolt bolt = new Bolt();

    public Attachments attachments = new Attachments();

    public Maps maps = new Maps();

    public BreakAction breakAction = new BreakAction();

    public HammerAction hammerAction = new HammerAction();

    public RevolverBarrel revolverBarrel = new RevolverBarrel();

    public ItemFrame itemFrame = new ItemFrame();


    public Extra extra = new Extra();

    public static class Arms {

        public boolean leftHandAmmo = true;
        public EnumArm actionArm = EnumArm.Left;

        public EnumAction actionType = EnumAction.Charge;

        public LeftArm leftArm = new LeftArm();
        public RightArm rightArm = new RightArm();

        public enum EnumArm {
            Left, Right;
        }

        public enum EnumAction {
            Bolt, Pump, Charge
        }

        public class LeftArm {
            public Vector3f armScale = new Vector3f(0.8F, 0.8F, 0.8F);

            public Vector3f armPos = new Vector3f(0.25F, -0.59F, 0.06F);
            public Vector3f armRot = new Vector3f(65.0F, 32.0F, -46.0F);

            public Vector3f armReloadPos = new Vector3f(-0.1F, -0.65F, 0.02F);
            public Vector3f armReloadRot = new Vector3f(35.0F, 0.0F, -25.0F);

            public Vector3f armChargePos = new Vector3f(0.0F, 0.0F, 0.0F);
            public Vector3f armChargeRot = new Vector3f(0.0F, 0.0F, 0.0F);
        }

        public class RightArm {
            public Vector3f armScale = new Vector3f(0.8F, 0.8F, 0.8F);

            public Vector3f armPos = new Vector3f(0.26F, -0.65F, 0.0F);
            public Vector3f armRot = new Vector3f(0.0F, 0.0F, -90.0F);

            public Vector3f armReloadPos = new Vector3f(0.27F, -0.65F, 0.04F);
            public Vector3f armReloadRot = new Vector3f(0.0F, 0.0F, -90.0F);

            public Vector3f armChargePos = new Vector3f(0.47F, -0.39F, 0.14F);
            public Vector3f armChargeRot = new Vector3f(0.0F, 0.0F, -90.0F);
        }

    }

    public static class Sprint {
        public Vector3f sprintRotate = new Vector3f(-20.0F, 30.0F, -0.0F);
        public Vector3f sprintTranslate = new Vector3f(0.5F, -0.10F, -0.65F);
    }

    public static class ThirdPerson {

        public Vector3f thirdPersonOffset = new Vector3f(0.0F, -0.1F, 0.0F);
        public Vector3f backPersonOffset = new Vector3f(0.0F, 0.0F, 0.0F);
        public float thirdPersonScale = 0.8F;

    }

    public static class Aim {

        //Advanced configuration - Allows you to change how the gun is held without effecting the sight alignment
        public Vector3f rotateHipPosition = new Vector3f(0F, 0F, 0F);
        //Advanced configuration - Allows you to change how the gun is held without effecting the sight alignment
        public Vector3f translateHipPosition = new Vector3f(0F, 0F, 0F);
        //Advanced configuration - Allows you to change how the gun is held while aiming
        public Vector3f rotateAimPosition = new Vector3f(0F, 0.065F, 0.3F);
        //Advanced configuration - Allows you to change how the gun is held while aiming
        public Vector3f translateAimPosition = new Vector3f(0.14f, 0.01f, 0f);
    }

    public static class Bolt {
        /**
         * For bolt action weapons
         */
        public float boltRotation = 0F;
        /**
         * The rotation point for the bolt twist
         */
        public Vector3f boltRotationPoint = new Vector3f();
        public Vector3f chargeModifier = new Vector3f(0.3F, 0F, 0F);

        public float pumpHandleDistance = 4F / 16F;

    }

    public static class Attachments {
        public HashMap<AttachmentPresetEnum, ArrayList<Vector3f>> attachmentPointMap = new HashMap<AttachmentPresetEnum, ArrayList<Vector3f>>();

        public HashMap<String, ArrayList<Vector3f>> positionPointMap = new HashMap<String, ArrayList<Vector3f>>();
        public HashMap<String, ArrayList<Vector3f>> aimPointMap = new HashMap<String, ArrayList<Vector3f>>();

        public Vector3f attachmentModeRotate = new Vector3f(10.0F, 30.0F, 0.0F);

        public boolean scopeIsOnSlide = false;
    }

    public static class Maps {
        public HashMap<String, RenderVariables> ammoMap = new HashMap<String, RenderVariables>();
        public HashMap<String, RenderVariables> bulletMap = new HashMap<String, RenderVariables>();
    }

    public static class BreakAction {
        public ArrayList<BreakActionData> breakActions = new ArrayList<BreakActionData>();
        /**
         * If true, then the scope attachment will move with the break action. Can be combined with the above
         */
        public boolean scopeIsOnBreakAction = false;
    }

    public static class HammerAction {
        public Vector3f hammerRotationPoint = new Vector3f(0.0F, 0.0F, 0.0F);
    }

    public static class RevolverBarrel {
        public Vector3f cylinderOriginPoint = new Vector3f(0.0F, 0.0F, 0.0F);
        public Vector3f cylinderReloadTranslation = new Vector3f(0.0F, 0.0F, 0.0F);
        public Integer numberBullets;
    }

    public static class ItemFrame {
        public Vector3f translate = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public static class Extra {

        public Vector3f translateAll = new Vector3f(1F, -1.02F, -0.07F);

        public float modelScale = 1f;

        public String reloadAnimation = WeaponAnimations.RIFLE;

        public boolean needExtraChargeModel = false;
        public float chargeHandleDistance = 0F;

        // If true, gun will translate when scoping equipped with a sight attachment
        public float gunOffsetScoping = 0F;
        // Zoom/translate the gun staticModel towards player when crouching
        public float crouchZoom = -0.035f;

        //Allows you to modify the ADS speed per gun, adjust in small increments (+/- 0.01)
        public float adsSpeed = 0.02F;

        //Model based recoil variables
        public float gunSlideDistance = 1F / 4F;

        /**
         * Adds backwards recoil translations to the gun staticModel when firing
         */
        public float modelRecoilBackwards = 0.15F;
        /**
         * Adds upwards/downwards recoil translations to the gun staticModel when firing
         */
        public float modelRecoilUpwards = 1.0F;
        /**
         * Adds a left-right staticModel shaking motion when firing, default 0.5
         */
        public float modelRecoilShake = 0.5F;

    }
}