package org.serverct.parrot.parrotx.command;

import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor {

    protected PPlugin plugin;
    @Getter
    protected Map<String, PCommand> commands = new HashMap<>();
    public String mainCmd;

    public CommandHandler(@NonNull PPlugin plugin, String mainCmd) {
        this.plugin = plugin;
        this.mainCmd = mainCmd;
    }

    protected void registerSubCommand(String cmd, PCommand executor) {
        if (!commands.containsKey(cmd)) {
            commands.put(cmd, executor);
        } else {
            plugin.lang.logError(I18n.REGISTER, "子命令", "重复子命令注册: " + cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
            return true;
        } else {
            if (!commands.containsKey(args[0])) {
                sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.WARN, "未知命令, 请检查您的命令拼写是否正确."));
                plugin.lang.logError(I18n.EXECUTE, "子命令/" + args[0], sender.getName() + " 尝试执行未注册子命令");
                return true;
            }
            PCommand pCommand = commands.get(args[0]);
            boolean hasPerm = (pCommand.getPermission() == null || pCommand.getPermission().equals("")) || sender.hasPermission(pCommand.getPermission());
            if (hasPerm) return pCommand.execute(plugin, sender, args);
            else {
                String msg = plugin.lang.build(plugin.localeKey, I18n.Type.WARN, "您没有权限这么做.");
                if (sender instanceof Player) {
                    TextComponent text = JsonChatUtil.getFromLegacy(msg);
                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&7所需权限 ▶ &c" + pCommand.getPermission()))));
                    ((Player) sender).spigot().sendMessage(text);
                } else sender.sendMessage(msg);
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String[] subCommands = commands.keySet().toArray(new String[0]);
        if (args.length == 0) {
            return new ArrayList<>(Arrays.asList(subCommands));
        } else {
            if (args.length == 1) {
                if (commands.containsKey(args[0]))
                    return Arrays.asList(commands.get(args[0]).getParams(args.length - 1));
                else return query(subCommands, args[0]);
            } else {
                if (commands.containsKey(args[0]))
                    return query(commands.get(args[0]).getParams(args.length - 1), args[args.length - 1]);
                else return new ArrayList<>();
            }
        }
    }

    private List<String> query(String[] params, String input) {
        return Arrays.stream(params).filter(s -> s.startsWith(input)).collect(Collectors.toList());
    }

    public List<String> formatHelp() {
        List<String> result = new ArrayList<>();
        PluginDescriptionFile description = plugin.getDescription();

        result.add(I18n.color("&9&l" + plugin.getName() + " &7" + description.getVersion()));
        StringBuilder author = new StringBuilder();
        List<String> authors = description.getAuthors();
        for (int index = 0; index < authors.size(); index++) {
            author.append(I18n.color("&c" + authors.get(index)));
            if (index != authors.size() - 1) author.append(I18n.color("&7, "));
            else author.append(ChatColor.GRAY);
        }
        if (!authors.isEmpty()) result.add(I18n.color("&7作者: " + author.toString()));
        result.add("");

        commands.forEach((cmd, pCmd) -> result.add(I18n.color("&d/" + mainCmd + " " + cmd + " &9- &7&o" + pCmd.getDescription())));
        if (commands.containsKey("help")) result.add("&6▶ &7使用 &d/" + mainCmd + " help &7指令查看更多信息.");
        return result;
    }
}
