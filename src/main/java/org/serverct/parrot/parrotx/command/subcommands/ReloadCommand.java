package org.serverct.parrot.parrotx.command.subcommands;

import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.I18n;

public class ReloadCommand implements PCommand {

    private String permission;

    public ReloadCommand(String perm) {
        this.permission = perm;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        try {
            plugin.init();
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.INFO, "重载配置文件成功."));
        } catch (Throwable e) {
            plugin.lang.logError(I18n.RELOAD, "配置文件", e.toString());
        }
        return true;
    }
}
