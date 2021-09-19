package com.modularwarfare.common.commands;

import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class CommandClear extends CommandBase {
    public int getRequiredPermissionLevel() {
        return 2;
    }

    public String getName() {
        return "mw-clear";
    }

    public String getUsage(ICommandSender sender) {
        return "/mw-clear <player>";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        {
            if (args.length != 1) {
                if(sender instanceof EntityPlayerMP) {
                    EntityPlayerMP localPlayer = (EntityPlayerMP) sender;
                    for (int i = 0; i < ((IExtraItemHandler) localPlayer.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getSlots(); i++) {
                        final ItemStack extra = ((IExtraItemHandler) localPlayer.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getStackInSlot(i);
                        if (!extra.isEmpty()) {
                            ((IExtraItemHandler) localPlayer.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).setStackInSlot(i, ItemStack.EMPTY);
                            CapabilityExtra.sync(localPlayer, server.getPlayerList().getPlayers());
                        }
                    }
                }
            } else {
                EntityPlayerMP player = getPlayer(server, sender, args[0]);
                if(player != null){
                    for (int i = 0; i < ((IExtraItemHandler) player.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getSlots(); i++) {
                        final ItemStack extra = ((IExtraItemHandler) player.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).getStackInSlot(i);
                        if (!extra.isEmpty()) {
                            ((IExtraItemHandler) player.getCapability((Capability) CapabilityExtra.CAPABILITY, (EnumFacing) null)).setStackInSlot(i, ItemStack.EMPTY);
                            CapabilityExtra.sync(player, server.getPlayerList().getPlayers());
                        }
                    }
                }
            }
        }
    }

}
