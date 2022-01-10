package com.modularwarfare.common.commands.kits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.capability.extraslots.CapabilityExtra;
import com.modularwarfare.common.capability.extraslots.IExtraItemHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.io.*;

public class CommandKit extends CommandBase {

    public Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public File KIT_FILE = new File(ModularWarfare.MOD_DIR, "kits.json");

    public int getRequiredPermissionLevel() {
        return 2;
    }

    public String getName() {
        return "mw-kit";
    }

    public String getUsage(ICommandSender sender) {
        return "/mw-kit <save/delete/give> <name> [player]";
    }


    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!KIT_FILE.exists()) {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(KIT_FILE), "UTF-8")) {
                gson.toJson(new Kits(), writer);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("give") && args.length >= 2) {
                String name = args[1];
                EntityPlayerMP player = null;
                if(args.length == 2 && sender instanceof EntityPlayerMP){
                    player = (EntityPlayerMP) sender;
                } else {
                    player = getPlayer(server, sender, args[2]);
                }
                if (sender != null) {
                    try {
                        JsonReader jsonReader = new JsonReader(new FileReader(KIT_FILE));
                        Kits kits = gson.fromJson(jsonReader, Kits.class);

                        Kits.Kit kit = null;
                        for (int i = 0; i < kits.kits.size(); i++) {
                            if (kits.kits.get(i).name.equalsIgnoreCase(name)) {
                                kit = kits.kits.remove(i);
                            }
                        }

                        if (kit != null) {
                            if(player != null) {
                                if (kit.force) {
                                    player.inventory.readFromNBT(JsonToNBT.getTagFromJson(kit.data).getTagList("items", 10));

                                    IExtraItemHandler extra = player.getCapability(CapabilityExtra.CAPABILITY, null);
                                    extra.setStackInSlot(0, new ItemStack(JsonToNBT.getTagFromJson(kit.backpack)));
                                    extra.setStackInSlot(1, new ItemStack(JsonToNBT.getTagFromJson(kit.vest)));
                                } else {
                                    NBTTagList tagList = JsonToNBT.getTagFromJson(kit.data).getTagList("items", 10);
                                    for (int i = 0; i < tagList.tagCount(); ++i) {
                                        NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
                                        int j = nbttagcompound.getByte("Slot") & 255;
                                        ItemStack itemstack = new ItemStack(nbttagcompound);

                                        if (!itemstack.isEmpty()) {
                                            if (j >= 0 && j < player.inventory.mainInventory.size()) {
                                                player.inventory.mainInventory.add(j, itemstack);
                                            } else if (j >= 100 && j < player.inventory.armorInventory.size() + 100) {
                                                player.inventory.armorInventory.add(j, itemstack);
                                            } else if (j >= 150 && j < player.inventory.offHandInventory.size() + 150) {
                                                player.inventory.offHandInventory.add(j, itemstack);
                                            }
                                        }
                                    }
                                }
                                sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " " +TextFormatting.YELLOW + args[2] + TextFormatting.GRAY + " has received the kit "+ TextFormatting.YELLOW + name + TextFormatting.GRAY+"."));
                            } else {
                                sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " " + TextFormatting.YELLOW + args[2] + TextFormatting.GRAY + " is not online."));
                            }
                        } else {
                            sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " The kit " + TextFormatting.YELLOW + name + TextFormatting.GRAY + " doest not exist."));
                        }
                    } catch (FileNotFoundException | NBTException e) {
                        e.printStackTrace();
                    }

                } else {
                    sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " " + TextFormatting.YELLOW + args[2] + TextFormatting.GRAY + " is not connected."));
                }
            } else if (args[0].equalsIgnoreCase("save") && args.length == 2) {
                if (sender instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) sender;

                    NBTTagCompound compound = new NBTTagCompound();
                    NBTTagList kitData = player.inventory.writeToNBT(new NBTTagList());
                    compound.setTag("items", kitData);

                    try {
                        JsonReader jsonReader = new JsonReader(new FileReader(KIT_FILE));
                        Kits kits = gson.fromJson(jsonReader, Kits.class);

                        Kits.Kit kit = new Kits.Kit();
                        kit.name = args[1];
                        kit.data = compound.toString();

                        IExtraItemHandler extra = player.getCapability(CapabilityExtra.CAPABILITY, null);
                        /**
                         * Backpack saving
                         */
                        if(extra.getStackInSlot(0) != null){
                            kit.backpack = extra.getStackInSlot(0).serializeNBT().toString();
                        }

                        /**
                         * Vest saving
                         */
                        if(extra.getStackInSlot(1) != null){
                            kit.vest = extra.getStackInSlot(1).serializeNBT().toString();
                        }

                        boolean found = false;
                        for (int i = 0; i < kits.kits.size(); i++) {
                            if (kits.kits.get(i).name.equalsIgnoreCase(kit.name)) {
                                kits.kits.set(i, kit);
                                found = true;
                            }
                        }
                        if(!found) kits.kits.add(kit);

                        try (Writer writer = new OutputStreamWriter(new FileOutputStream(KIT_FILE), "UTF-8")) {
                            gson.toJson(kits, writer);
                            sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " The kit " + TextFormatting.YELLOW + args[1] + TextFormatting.GRAY + " has been saved."));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            } else if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
                try {
                    JsonReader jsonReader = new JsonReader(new FileReader(KIT_FILE));
                    Kits kits = gson.fromJson(jsonReader, Kits.class);

                    boolean found = false;
                    for (int i = 0; i < kits.kits.size(); i++) {
                        if (kits.kits.get(i).name.equalsIgnoreCase(args[1])) {
                            kits.kits.remove(i);
                            found = true;
                        }
                    }

                    if(found) {
                        try (Writer writer = new OutputStreamWriter(new FileOutputStream(KIT_FILE), "UTF-8")) {
                            gson.toJson(kits, writer);
                            sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " The kit " + TextFormatting.YELLOW + args[1] + TextFormatting.GRAY + " has been deleted."));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " The kit " + TextFormatting.YELLOW + args[1] + TextFormatting.GRAY + " doesn't exist."));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " /mw-kit <save/delete/give> <name> [player]"));
            }
        } else {
            sender.sendMessage(new TextComponentString(ModularWarfare.MOD_PREFIX + " /mw-kit <save/delete/give> <name> [player]"));
        }
    }
}
