package org.serverct.parrot.parrotx.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

public interface PCommand extends CommandExecutor {
    String getPermission();

    default String getDescription() {
        return "没有介绍";
    }

    default String[] getHelp() {
        return new String[0];
    }

    default String[] getParams(int arg) {
        return new String[0];
    }

    boolean execute(CommandSender sender, String[] args);

    default String optionalParam(String param) {
        return I18n.color("&7[&c" + param + "&7] ");
    }

    default String requiredParam(String param) {
        return I18n.color("&7<&c" + param + "&7> ");
    }

    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return execute(sender, args);
    }
}
