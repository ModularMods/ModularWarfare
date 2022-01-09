package com.modularwarfare.client.handler;

import com.modularwarfare.ModConfig;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.client.ClientProxy;
import com.modularwarfare.client.ClientRenderHooks;
import com.modularwarfare.client.anim.AnimStateMachine;
import com.modularwarfare.client.input.KeyEntry;
import com.modularwarfare.client.input.KeyType;
import com.modularwarfare.client.model.renders.RenderGunStatic;
import com.modularwarfare.common.guns.*;
import com.modularwarfare.common.network.PacketGunReload;
import com.modularwarfare.common.network.PacketGunSwitchMode;
import com.modularwarfare.common.network.PacketGunUnloadAttachment;
import com.modularwarfare.common.network.PacketOpenGui;
import com.modularwarfare.utility.MWSound;
import com.modularwarfare.utility.event.ForgeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.ArrayList;

public class KeyInputHandler extends ForgeEvent {

    private ArrayList<KeyEntry> keyBinds;

    public KeyInputHandler() {
        keyBinds = new ArrayList<KeyEntry>();
        keyBinds.add(new KeyEntry(KeyType.GunReload));
        keyBinds.add(new KeyEntry(KeyType.ClientReload));
        keyBinds.add(new KeyEntry(KeyType.FireMode));
        keyBinds.add(new KeyEntry(KeyType.GunUnload));
        keyBinds.add(new KeyEntry(KeyType.AddAttachment));
        keyBinds.add(new KeyEntry(KeyType.Flashlight));

        if (!ModConfig.INSTANCE.general.customInventory) {
            keyBinds.add(new KeyEntry(KeyType.Backpack));
        }

        keyBinds.add(new KeyEntry(KeyType.Left));
        keyBinds.add(new KeyEntry(KeyType.Right));
        keyBinds.add(new KeyEntry(KeyType.Up));
        keyBinds.add(new KeyEntry(KeyType.Down));

        if (ModularWarfare.DEV_ENV) {
            keyBinds.add(new KeyEntry(KeyType.DebugMode));
        }

        for (KeyEntry keyEntry : keyBinds) {
            ClientRegistry.registerKeyBinding(keyEntry.keyBinding);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        for (KeyEntry keyEntry : keyBinds) {
            if (keyEntry.keyBinding.isPressed()) {
                handleKeyInput(keyEntry.keyType);
                break;
            }
        }
    }

    public void handleKeyInput(KeyType keyType) {
        if (Minecraft.getMinecraft().player != null) {
            EntityPlayerSP entityPlayer = Minecraft.getMinecraft().player;

            switch (keyType) {
                // F9 Reloads Models /// SHIFT + F9 Reloads Textures & Icons
                case ClientReload:

                    if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                        final ItemStack gunStack = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                        final GunType gunType = ((ItemGun)gunStack.getItem()).type;
                        for (AttachmentEnum attachment : AttachmentEnum.values()) {
                            ItemStack itemStack = GunType.getAttachment(gunStack, attachment);
                            if (itemStack != null && itemStack.getItem() != Items.AIR) {
                                AttachmentType attachmentType = ((ItemAttachment) itemStack.getItem()).type;
                                if (attachmentType.hasModel()) {
                                    attachmentType.reloadModel();
                                }
                            }
                        }
                        if (gunType.hasModel()) {
                            gunType.reloadModel();
                        }
                    }

                    if (entityPlayer.isSneaking()) {
                        ModularWarfare.PROXY.reloadModels(true);
                    }
                    break;
                case FireMode:
                    if (entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun) {
                        ModularWarfare.NETWORK.sendToServer(new PacketGunSwitchMode());
                    }
                    break;

                case GunReload:
                    ItemStack reloadStack = entityPlayer.getHeldItemMainhand();
                    if (reloadStack != null && (reloadStack.getItem() instanceof ItemGun || reloadStack.getItem() instanceof ItemAmmo)) {
                        ModularWarfare.NETWORK.sendToServer(new PacketGunReload());
                    }
                    break;

                case GunUnload:
                    ItemStack unloadStack = entityPlayer.getHeldItemMainhand();
                    if (ClientRenderHooks.getAnimMachine(entityPlayer).attachmentMode) {
                        ModularWarfare.NETWORK.sendToServer(new PacketGunUnloadAttachment(ClientProxy.attachmentUI.selectedAttachEnum.getName(), false));
                    } else {
                        if (unloadStack != null && (unloadStack.getItem() instanceof ItemGun || unloadStack.getItem() instanceof ItemAmmo)) {
                            ModularWarfare.NETWORK.sendToServer(new PacketGunReload(true));
                        }
                    }
                    break;

                case DebugMode:
                    if (entityPlayer.isSneaking()) {
                        ModularWarfare.loadContentPacks(true);
                        //ModularWarfare.PROXY.reloadModels(true);
                    }
                    break;

                case AddAttachment:

                    if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                        if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                            AnimStateMachine stateMachine = ClientRenderHooks.getAnimMachine(entityPlayer);
                            stateMachine.attachmentMode = !stateMachine.attachmentMode;
                            ModularWarfare.PROXY.playSound(new MWSound(entityPlayer.getPosition(), "attachment.open", 1f, 1f));
                        }
                    }


                    if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                        if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                            //Minecraft.getMinecraft().displayGuiScreen(new GuiMainScreen(entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND),(ItemGun) entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem()));
                        }
                    }

                    break;
                case Flashlight:
                    if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) != null && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
                        if (entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND).getItem() instanceof ItemGun) {
                            final ItemStack gunStack = entityPlayer.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND);
                            if (GunType.getAttachment(gunStack, AttachmentEnum.Flashlight) != null) {
                                final ItemAttachment itemAttachment = (ItemAttachment) GunType.getAttachment(gunStack, AttachmentEnum.Flashlight).getItem();
                                if (itemAttachment != null) {
                                    RenderGunStatic.isLightOn = !RenderGunStatic.isLightOn;
                                }
                                ModularWarfare.PROXY.playSound(new MWSound(entityPlayer.getPosition(), "attachment.apply", 1f, 1f));
                            }
                        }
                    }
                    break;
                case Backpack:
                    if (!ModConfig.INSTANCE.general.customInventory) {
                        if (!entityPlayer.isCreative()) {
                            ModularWarfare.NETWORK.sendToServer(new PacketOpenGui(0));
                        }
                    }
                    break;
                case Left:
                    ClientProxy.attachmentUI.processKeyInput(KeyType.Left);
                    break;
                case Right:
                    ClientProxy.attachmentUI.processKeyInput(KeyType.Right);
                    break;
                case Up:
                    ClientProxy.attachmentUI.processKeyInput(KeyType.Up);
                    break;
                case Down:
                    ClientProxy.attachmentUI.processKeyInput(KeyType.Down);
                    break;

                default:
                    ModularWarfare.LOGGER.warn("Default case called on handleKeyInput for " + keyType.toString());
                    break;
            }
        }
    }
}