package org.serverct.parrot.parrotx.command.subcommands;

import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.I18n;

public class ReloadCommand implements PCommand {

    private String permission;
    private PPlugin plugin;

    public ReloadCommand(PPlugin plugin, String perm) {
        this.plugin = plugin;
        this.permission = perm;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getDescription() {
        return "重载插件配置文件";
    }

    @Override
    public String[] getHelp() {
        return new String[]{
                "&9&l" + plugin.getName() + " &7指令帮助 ᚏᚎᚍᚔᚓᚒᚑᚐ",
                "  &9▶ &d/" + plugin.getCmdHandler().mainCmd + " reload",
                "    &7&o" + getDescription(),
                "    &7所需权限: &c" + (getPermission() == null ? "无" : getPermission())
        };
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        try {
            plugin.init();
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.INFO, "重载配置文件成功."));
        } catch (Throwable e) {
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.WARN, "重载配置文件失败, 请查看控制台中的错误信息."));
            plugin.lang.logError(I18n.RELOAD, "配置文件", e, null);
        }
        return true;
    }
}
