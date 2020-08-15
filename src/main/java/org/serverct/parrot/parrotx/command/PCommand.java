package org.serverct.parrot.parrotx.command;

import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.utils.I18n;

public interface PCommand {
    String getPermission();

    default String getDescription() {
        return "没有介绍";
    }

    default String[] getHelp() {
        return new String[0];
    }

    default String[] getParams(int arg, String[] args) {
        return new String[0];
    }

    boolean execute(CommandSender sender, String[] args);

    default String optionalParam(String param) {
        return I18n.color("&7[&c" + param + "&7] ");
    }

    default String requiredParam(String param) {
        return I18n.color("&7<&c" + param + "&7> ");
    }
}
