package com.modularwarfare.client.hud;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.input.KeyType;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.network.PacketGunAddAttachment;
import com.modularwarfare.common.network.PacketGunUnloadAttachment;
import com.modularwarfare.utility.RenderHelperMW;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class AttachmentUI {

    public int selectedAttachTypeIndex;
    public int selectedAttachIndex;

    public int sizeAttachTypeIndex;
    public int sizeAttachAttachIndex;

    public AttachmentPresetEnum selectedAttachEnum;

    public AttachmentUI() {
        this.selectedAttachTypeIndex = 0;
        this.selectedAttachIndex = 0;

        this.sizeAttachTypeIndex = 0;
        this.sizeAttachAttachIndex = 0;
    }


    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        switch (event.phase) {
            case START:
                if (mc.player != null) {
                    if (mc.player.world != null) {
                        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                            if (ClientRenderHooks.getAnimMachine(mc.player).attachmentMode) {
                                ItemStack gunStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                                ItemGun gun = (ItemGun) gunStack.getItem();

                                if (gun.type.modelSkins != null && gun.type.acceptedAttachments != null) {
                                    if (!gun.type.acceptedAttachments.isEmpty() || gun.type.modelSkins.length > 1) {
                                        List<AttachmentPresetEnum> keys = new ArrayList<>(gun.type.acceptedAttachments.keySet());
                                        if (gun.type.modelSkins.length > 1) {
                                            keys.add(AttachmentPresetEnum.Skin);
                                        }
                                        if ((selectedAttachTypeIndex < keys.size() && selectedAttachTypeIndex >= 0)) {
                                            selectedAttachEnum = keys.get(selectedAttachTypeIndex);
                                            List<Integer> slotsAttachments = checkAttach(mc.player, gun.type, selectedAttachEnum);

                                            sizeAttachTypeIndex = keys.size();
                                            sizeAttachAttachIndex = slotsAttachments.size();

                                            if (selectedAttachIndex < slotsAttachments.size()) {
                                                if (selectedAttachIndex != 0) {
                                                    if (GunType.getAttachment(gunStack, selectedAttachEnum) != mc.player.inventory.getStackInSlot(slotsAttachments.get(selectedAttachIndex))) {
                                                        ModularWarfare.NETWORK.sendToServer(new PacketGunAddAttachment(slotsAttachments.get(selectedAttachIndex)));
                                                        selectedAttachIndex = 0;
                                                    }
                                                }
                                            } else {
                                                selectedAttachIndex = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            Minecraft mc = Minecraft.getMinecraft();
            if (!event.isCancelable()) {
                int width = event.getResolution().getScaledWidth();
                int height = event.getResolution().getScaledHeight();

                if (mc.player.world != null) {
                    if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                        if (ClientRenderHooks.getAnimMachine(mc.player).attachmentMode) {

                            RenderHelperMW.renderCenteredText(TextFormatting.YELLOW + "[Attachment mode]", width / 2, height - 32, 0xFFFFFFFF);
                            if (selectedAttachEnum != null) {
                                ItemStack gunStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                                ItemGun gun = (ItemGun) gunStack.getItem();

                                if (gun.type.modelSkins != null && gun.type.acceptedAttachments != null) {
                                    if (!gun.type.acceptedAttachments.isEmpty() || gun.type.modelSkins.length > 1) {
                                        List<AttachmentPresetEnum> keys = new ArrayList<>(gun.type.acceptedAttachments.keySet());
                                        if (gun.type.modelSkins.length > 1) {
                                            keys.add(AttachmentPresetEnum.Skin);
                                        }

                                        GlStateManager.pushMatrix();
                                        GlStateManager.translate(0, -18, 0);

                                        RenderHelperMW.renderCenteredText(firstArrowType(selectedAttachTypeIndex) + " " + selectedAttachEnum + " " + secondArrowType(selectedAttachTypeIndex, keys.size()), width / 2 - 50, height - 40, 0xFFFFFFFF);

                                        RenderHelperMW.renderCenteredText("Change", width / 2 + 10, height - 40, 0xFFFFFFFF);
                                        RenderHelperMW.renderCenteredText("Unattach", width / 2 + 60, height - 40, 0xFFFFFFFF);


                                        GL11.glPushMatrix();
                                        GL11.glTranslated(width / 2 + 10, height - 42, 0.0D);
                                        GL11.glRotatef(180, 0, 0, 1);
                                        RenderHelperMW.renderCenteredText(firstArrowAttach(selectedAttachIndex, sizeAttachAttachIndex) + "[V]", 0, 0, 0xFFFFFFFF);
                                        GL11.glPopMatrix();

                                        TextFormatting color = TextFormatting.GRAY;
                                        if (GunType.getAttachment(gunStack, selectedAttachEnum) != null) {
                                            color = TextFormatting.GREEN;
                                        }
                                        RenderHelperMW.renderCenteredText(color + "[V]", width / 2 + 60, height - 30, 0xFFFFFFFF);

                                        GlStateManager.popMatrix();
                                    } else {
                                        this.resetAttachmentMode();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public List<Integer> checkAttach(EntityPlayer player, GunType gunType, AttachmentPresetEnum attachmentEnum) {
        List<Integer> attachments = new ArrayList<>();
        attachments.add(-1);
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack itemStack = player.inventory.getStackInSlot(i);
            if (attachmentEnum != AttachmentPresetEnum.Skin) {
                if (itemStack != null && itemStack.getItem() instanceof ItemAttachment) {
                    ItemAttachment itemAttachment = (ItemAttachment) itemStack.getItem();
                    AttachmentType attachType = itemAttachment.type;
                    if (attachType.attachmentType == attachmentEnum) {
                        if (gunType.acceptedAttachments.get(attachType.attachmentType) != null && gunType.acceptedAttachments.get(attachType.attachmentType).size() >= 1) {
                            if (gunType.acceptedAttachments.get(attachType.attachmentType).contains(attachType.internalName)) {
                                attachments.add(i);
                            }
                        }
                    }
                }
            } else {
                if (itemStack != null && itemStack.getItem() instanceof ItemSpray) {
                    ItemSpray itemSpray = (ItemSpray) itemStack.getItem();
                    SprayType attachType = itemSpray.type;
                    for (int j = 0; j < gunType.modelSkins.length; j++) {
                        if (gunType.modelSkins[j].internalName.equalsIgnoreCase(attachType.skinName)) {
                            attachments.add(i);
                        }
                    }
                }
            }
        }
        return attachments;
    }

    public void processKeyInput(KeyType type) {
        switch (type) {
            case Left:
                if ((selectedAttachTypeIndex - 1) >= 0) {
                    selectedAttachTypeIndex--;
                }
                break;
            case Right:
                if ((selectedAttachTypeIndex + 1) < sizeAttachTypeIndex) {
                    selectedAttachTypeIndex++;
                }
                break;
            case Down:
                if ((selectedAttachIndex - 1) >= 0) {
                    selectedAttachIndex--;
                }
                if (selectedAttachIndex == 0) {
                    if (selectedAttachEnum != null) {
                        ModularWarfare.NETWORK.sendToServer(new PacketGunUnloadAttachment(selectedAttachEnum.getName(), false));
                    }
                }
                break;
            case Up:
                if ((selectedAttachIndex + 1) < sizeAttachAttachIndex) {
                    selectedAttachIndex++;
                }
                break;
        }
    }


    public String firstArrowType(int selectedAttachTypeIndex) {
        if (selectedAttachTypeIndex > 0) {
            return TextFormatting.GREEN + "[<]" + TextFormatting.RESET;
        }
        return TextFormatting.GRAY + "[<]" + TextFormatting.RESET;
    }

    public String secondArrowType(int selectedAttachTypeIndex, int size) {
        if (selectedAttachTypeIndex == size - 1) {
            return TextFormatting.GRAY + "[>]" + TextFormatting.RESET;
        }
        return TextFormatting.GREEN + "[>]" + TextFormatting.RESET;
    }

    public String firstArrowAttach(int selectedAttachIndex, int size) {
        if (selectedAttachIndex == size - 1) {
            return TextFormatting.GRAY + "";
        }
        return TextFormatting.GREEN + "";
    }

    public String secondArrowAttach(int selectedAttachIndex, int size) {
        if (selectedAttachIndex > 0 && size <= 1) {
            return TextFormatting.GRAY + "";
        }
        return TextFormatting.GREEN + "";
    }

    public void resetAttachmentMode() {
        this.selectedAttachTypeIndex = 0;
        this.selectedAttachIndex = 0;

        this.sizeAttachTypeIndex = 0;
        this.sizeAttachAttachIndex = 0;
    }

}
