package org.serverct.parrot.parrotx.command;

import lombok.NonNull;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler implements CommandExecutor {

    protected PPlugin plugin;

    public CommandHandler(@NonNull PPlugin plugin) {
        this.plugin = plugin;
    }

    protected Map<String, PCommand> commands = new HashMap<>();

    protected void registerSubCommand(String cmd, PCommand executor) {
        if (!commands.containsKey(cmd)) {
            commands.put(cmd, executor);
        } else {
            plugin.lang.logError(LocaleUtil.REGISTER, "子命令", "重复子命令注册: " + cmd);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0) {
            plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
        } else {
            if(commands.containsKey(args[0])) {
                return commands.get(args[0]).execute(plugin, sender, args);
            }
        }
        return false;
    }
}
