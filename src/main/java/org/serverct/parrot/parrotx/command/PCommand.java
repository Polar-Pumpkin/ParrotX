package org.serverct.parrot.parrotx.command;

import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.PPlugin;

public interface PCommand {
    String getPermission();

    boolean execute(PPlugin plugin, CommandSender sender, String[] args);
}
