package com.modularwarfare.common.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class CommandNBT extends CommandBase {
    public int getRequiredPermissionLevel() {
        return 2;
    }

    public String getName() {
        return "mw-nbt";
    }

    public String getUsage(ICommandSender sender) {
        return "/mw-nbt";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        {
            if (args.length != 1) {
                if (sender instanceof EntityPlayerMP) {
                    EntityPlayerMP localPlayer = (EntityPlayerMP) sender;
                    localPlayer.sendMessage(new TextComponentString(TextFormatting.GRAY+"["+TextFormatting.RED+"ModularWarfare"+TextFormatting.GRAY+"] Item NBT copied to clipboard !"));
                    Toolkit.getDefaultToolkit()
                            .getSystemClipboard()
                            .setContents(
                                    new StringSelection(localPlayer.getHeldItemMainhand().serializeNBT().toString()),
                                    null
                            );
                }
            }

        }
    }

}
