package com.modularwarfare.client.fpp.basic.renderers;

import com.modularwarfare.client.model.ModelAttachment;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderType;
import com.modularwarfare.client.fpp.basic.models.objects.CustomItemRenderer;
import com.modularwarfare.common.guns.AttachmentType;
import com.modularwarfare.common.guns.ItemAttachment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class RenderAttachment extends CustomItemRenderer {

    public static float smoothing;
    public static float randomOffset;
    public static float randomRotateOffset;
    public static float adsSwitch = 0f;
    private static TextureManager renderEngine;
    private int direction = 0;

    @Override
    public void renderItem(CustomItemRenderType type, EnumHand hand, ItemStack item, Object... data) {
        if (!(item.getItem() instanceof ItemAttachment))
            return;

        AttachmentType AttachmentType = ((ItemAttachment) item.getItem()).type;
        if (AttachmentType == null)
            return;

        ModelAttachment model = (ModelAttachment) AttachmentType.model;
        if (model == null)
            return;
        {
            renderAttachment(type, item, AttachmentType, data);
        }
    }

    private void renderAttachment(CustomItemRenderType renderType, ItemStack item, AttachmentType attachmentType, Object... data) {

        ModelAttachment model = (ModelAttachment) attachmentType.model;
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        if (renderEngine == null)
            renderEngine = Minecraft.getMinecraft().renderEngine;

        if (model == null)
            return;

        GL11.glPushMatrix();
        {
            switch (renderType) {

                case ENTITY: {
                    GL11.glTranslatef(-0.45F, -0.05F, 0.0F);
                    break;
                }

                case EQUIPPED: {
                    float crouchOffset = player.isSneaking() ? 0.2f : 0.0f;
                    GL11.glRotatef(-10F, 1F, 0F, 0F);
                    GL11.glRotatef(-90F, 0F, 1F, 0F);
                    GL11.glRotatef(90F, 0F, 0F, 1F);
                    GL11.glTranslatef(-0.15F, 0.15F, -0.025F);
                    GL11.glScalef(1F, 1F, 1F);
                    //GL11.glTranslatef(staticModel.thirdPersonOffset.x + crouchOffset, staticModel.thirdPersonOffset.y, staticModel.thirdPersonOffset.z);
                    break;
                }

                case EQUIPPED_FIRST_PERSON: {
                    float modelScale = model.config.extra.modelScale;
                    //System.out.println(staticModel.modelScale);
                    float rotateX = 0; //ROLL LEFT-RIGHT (0 Total ADS Default)
                    float rotateY = 46F - 1F * adsSwitch; //ANGLE LEFT-RIGHT (45 Total ADS Default)
                    float rotateZ = 1 + (-1.0F * adsSwitch); //ANGLE UP-DOWN (0 Total ADS Default)
                    Vector3f translateXYZ = new Vector3f(0.05F + -1.35F, 0.834F - -0.064F * adsSwitch, -1.05F - 0.35F * adsSwitch); //(-1.3F, 0.898F, -1.4F Total ADS Defaults)

                    GL11.glRotatef(rotateX, 1F, 0F, 0F); //ROLL LEFT-RIGHT
                    GL11.glRotatef(rotateY, 0F, 1F, 0F); //ANGLE LEFT-RIGHT
                    GL11.glRotatef(rotateZ, 0F, 0F, 1F); //ANGLE UP-DOWN
                    GL11.glTranslatef(translateXYZ.x, translateXYZ.y, translateXYZ.z);
                }

                default:
                    break;

            }

            GL11.glPushMatrix();
            {
                if (item != null && item.getTagCompound() != null) {
                    float f = 1F / 16F;
                    float modelScale = model.config.extra.modelScale;
                    //int skinId = item.getTagCompound().getInteger("skinId");
                    int skinId = 0;
                    String path = skinId > 0 ? "skins/" + attachmentType.modelSkins[skinId].getSkin() : attachmentType.modelSkins[0].getSkin();
                    bindTexture("attachments", path);
                    GL11.glScalef(modelScale, modelScale, modelScale);
                    model.renderAttachment(f);
                }
            }
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

}
