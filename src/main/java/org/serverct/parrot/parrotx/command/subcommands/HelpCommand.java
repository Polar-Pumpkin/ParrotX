package org.serverct.parrot.parrotx.command.subcommands;

import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.BaseCommand;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.Collections;

public class HelpCommand extends BaseCommand {

    public HelpCommand(PPlugin plugin) {
        super(plugin, "help", 0);
        describe("查看插件或指定子指令的帮助信息");

        addChain(CommandChain.builder()
                .name("帮助")
                .description("查询插件命令帮助信息")
                .params(Collections.singletonList(CommandParam.builder()
                        .name("子命令")
                        .description("插件的其他子命令")
                        .optional(true)
                        .position(0)
                        .suggest(() -> plugin.getCmdHandler().getCommands().keySet().toArray(new String[0]))
                        .validate(cmd -> plugin.getCmdHandler().getCommands().containsKey(cmd))
                        .validateMessage(warn("未知子命令, 输入 &d/" + plugin.getCmdHandler().mainCmd + " help &7获取插件帮助."))
                        .build()))
                .run((args, chain) -> {
                    CommandHandler handler = plugin.getCmdHandler();

                    if (args.length <= 0) {
                        handler.formatHelp().forEach(sender::sendMessage);
                    } else {
                        for (String help : handler.getCommands().get(args[0]).getHelp())
                            sender.sendMessage(I18n.color(help));
                    }
                })
                .build());
    }

    @Override
    protected void call(String[] args) {
    }
}
