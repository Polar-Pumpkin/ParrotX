package org.serverct.parrot.parrotx.command.subcommands;

import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;

public class HelpCommand implements PCommand {
    private String permission;

    public HelpCommand(String perm) {
        this.permission = perm;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
        return true;
    }
}
