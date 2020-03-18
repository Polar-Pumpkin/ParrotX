package org.serverct.parrot.parrotx.command.subcommands;

import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.Map;

public class HelpCommand implements PCommand {
    private String permission;
    private PPlugin plugin;
    private Map<String, PCommand> subCommands;

    public HelpCommand(PPlugin plugin, String perm) {
        this.plugin = plugin;
        this.permission = perm;
        this.subCommands = plugin.getCmdHandler().getCommands();
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String getDescription() {
        return "查看插件或指定子指令的帮助信息";
    }

    @Override
    public String[] getHelp() {
        return new String[]{
                "&9&l" + plugin.getName() + " &7指令帮助 ᚏᚎᚍᚔᚓᚒᚑᚐ",
                "  &9▶ &d/" + plugin.getCmdHandler().mainCmd + " help " + optionalParam("子指令"),
                "    &7&o" + getDescription(),
                "    &7所需权限: &c" + (getPermission() == null ? "无" : getPermission())
        };
    }

    @Override
    public String[] getParams(int arg) {
        if (arg == 0) {
            return subCommands.keySet().toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 0) {
            // plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
            plugin.getCmdHandler().formatHelp().forEach(sender::sendMessage);
        } else {
            if (subCommands.containsKey(args[0]))
                for (String help : subCommands.get(args[0]).getHelp()) sender.sendMessage(I18n.color(help));
            else
                sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.WARN, "未知子命令, 输入 &d/" + plugin.getCmdHandler().mainCmd + " help &7获取插件帮助."));
        }
        return true;
    }
}
