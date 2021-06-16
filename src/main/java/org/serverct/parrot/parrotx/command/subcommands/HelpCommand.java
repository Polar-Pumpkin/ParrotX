package org.serverct.parrot.parrotx.command.subcommands;

import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.BaseCommand;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Map;

public class HelpCommand extends BaseCommand {

    public HelpCommand(PPlugin plugin) {
        super(plugin, "help", 0);
        describe("查看插件或指定子指令的帮助信息");
        addParam(CommandParam.builder()
                .name("子命令")
                .description("插件的其他子命令")
                .optional(true)
                .position(0)
                .suggest(() -> this.handler.getCommands().keySet().toArray(new String[0]))
                .build());
    }

    @Override
    protected void call(String[] args) {
        CommandHandler handler = this.handler;
        Map<String, PCommand> subCommands = handler.getCommands();

        if (args.length <= 0) {
            // plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
            handler.formatHelp(sender).forEach(sender::sendMessage);
        } else {
            if (subCommands.containsKey(args[0]))
                for (String help : subCommands.get(args[0]).getHelp()) sender.sendMessage(I18n.color(help));
            else
                sender.sendMessage(plugin.getLang().data.warn("未知子命令, 输入 &d/" + this.handler.mainCmd + " help " +
                        "&7获取插件帮助."));
        }
    }
}
