package com.modularwarfare.common.commands;

import com.modularwarfare.ModConfig;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandDebug extends CommandBase {
    public int getRequiredPermissionLevel() {
        return 2;
    }

    public String getName() {
        return "mw-debug";
    }

    public String getUsage(ICommandSender sender) {
        return "/mwdebug";
    }

    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            ModConfig.INSTANCE.debug_hits_message = !ModConfig.INSTANCE.debug_hits_message;
            sender.sendMessage(new TextComponentString("[ModularWarfare] Debugs hits set to :"+ModConfig.INSTANCE.debug_hits_message));
        }
    }

}
