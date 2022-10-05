package com.modularwarfare.melee.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.api.GenerateJsonModelsEvent;
import com.modularwarfare.api.HandleKeyEvent;
import com.modularwarfare.api.OnTickRenderEvent;
import com.modularwarfare.api.RenderHeldItemLayerEvent;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.fpp.basic.renderers.RenderParameters;
import com.modularwarfare.client.fpp.enhanced.models.EnhancedModel;
import com.modularwarfare.client.input.KeyType;
import com.modularwarfare.common.type.BaseItem;
import com.modularwarfare.common.type.BaseType;
import com.modularwarfare.melee.ModularWarfareMelee;
import com.modularwarfare.melee.client.configs.AnimationMeleeType;
import com.modularwarfare.melee.client.configs.MeleeRenderConfig;
import com.modularwarfare.melee.common.melee.ItemMelee;
import com.modularwarfare.melee.common.melee.MeleeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;

public class ClientEvents {

    @SubscribeEvent
    public void onHandleKey(HandleKeyEvent event) {
        EntityPlayerSP entityPlayer = Minecraft.getMinecraft().player;
        if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemMelee) {
            final ItemStack itemStack = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
            final MeleeType meleeType = ((ItemMelee) itemStack.getItem()).type;
            if(event.keyType == KeyType.ClientReload) {
                meleeType.enhancedModel.config = ModularWarfare.getRenderConfig(meleeType, MeleeRenderConfig.class);
            } else if (event.keyType == KeyType.Inspect) {
                RenderMelee.controller.INSPECT = 0;
            }
        }
    }

    /**
     * Generate the .render.json file of the melee weapons
     */
    @SubscribeEvent
    public void onGenerateJsonModels(GenerateJsonModelsEvent event) {
        System.out.println("Generate JSON Melee");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        for (ItemMelee itemMelee : ModularWarfareMelee.meleeTypes.values()) {
            MeleeType type = itemMelee.type;
            if (type.contentPack == null)
                continue;

            File contentPackDir = new File(ModularWarfare.MOD_DIR, type.contentPack);
            if (contentPackDir.exists() && contentPackDir.isDirectory()) {
                if (ModularWarfare.DEV_ENV) {
                    final File dir = new File(contentPackDir, "/" + type.getAssetDir() + "/render");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    final File renderFile = new File(dir, type.internalName + ".render.json");
                    if (!renderFile.exists()) {
                        try {
                            FileWriter fileWriter = new FileWriter(renderFile, false);

                            MeleeRenderConfig renderConfig = new MeleeRenderConfig();
                            renderConfig.modelFileName = type.internalName.replaceAll(type.contentPack + ".", "");
                            renderConfig.modelFileName = renderConfig.modelFileName + ".glb";
                            gson.toJson(renderConfig, fileWriter);

                            fileWriter.flush();
                            fileWriter.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderTickEvent(OnTickRenderEvent event) {
        if (RenderMelee.controller != null) {
            RenderMelee.controller.updateCurrentItem();
            RenderMelee.controller.onTickRender(event.smooth);
        }
    }

    @SubscribeEvent
    public void onAttack(PlayerInteractEvent event){
        if((event instanceof PlayerInteractEvent.LeftClickEmpty || event instanceof PlayerInteractEvent.LeftClickBlock)) {
            if (event.getItemStack().getItem() instanceof ItemMelee) {
                RenderMelee.controller.applyAnim(AnimationMeleeType.ATTACK);
            }
        }
    }

    @SubscribeEvent
    public void onRenderHeldLayer(RenderHeldItemLayerEvent event){
        if (!(event.stack.getItem() instanceof ItemMelee)) {
            return;
        }
        BaseType type = ((BaseItem) event.stack.getItem()).baseType;
        if (!type.hasModel()) {
            return;
        }

        EnhancedModel model = type.enhancedModel;
        MeleeRenderConfig config = (MeleeRenderConfig) type.enhancedModel.config;

        if(config.extra.thirdPersonRender3D) {
            RenderPlayer renderplayer = (RenderPlayer) Minecraft.getMinecraft().getRenderManager().<AbstractClientPlayer>getEntityRenderObject(event.entitylivingbaseIn);
            renderplayer.getMainModel().postRenderArm(0.0625F, EnumHandSide.RIGHT);

            GlStateManager.translate(-0.06, 0.38, -0.02);

            GL11.glRotatef(-90F, 0F, 1F, 0F);
            GL11.glRotatef(90F, 0F, 0F, 1F);
            GL11.glTranslatef(0.25F, 0.2F, -0.05F);
            GL11.glScalef(1 / 16F, 1 / 16F, 1 / 16F);

            GL11.glRotatef(config.extra.thirdPersonRotation.x, 1F, 0F,0F);
            GL11.glRotatef(config.extra.thirdPersonRotation.y, 0F, 1F,0F);
            GL11.glRotatef(config.extra.thirdPersonRotation.z, 0F, 0F,1F);

            GL11.glTranslatef(config.extra.thirdPersonOffset.x, config.extra.thirdPersonOffset.y, config.extra.thirdPersonOffset.z);


            model.updateAnimation((float) config.animations.get(AnimationMeleeType.DEFAULT).get(0).getStartTime(config.FPS));


            int skinId = 0;
            if (event.stack.hasTagCompound()) {
                if (event.stack.getTagCompound().hasKey("skinId")) {
                    skinId = event.stack.getTagCompound().getInteger("skinId");
                }
            }
            String path = skinId > 0 ? type.modelSkins[skinId].getSkin() : type.modelSkins[0].getSkin();
            RenderMelee meleeRender = (RenderMelee) ClientRenderHooks.customRenderers[ModularWarfareMelee.meleeEntryId];
            meleeRender.bindTexture("melee", path);

            model.renderPartExcept(RenderParameters.partsWithAmmo);
        }
    }
}
