package org.serverct.parrot.parrotx.command.subcommands;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.BaseCommand;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.Arrays;

public class VersionCommand extends BaseCommand {
    public VersionCommand(@NotNull PPlugin plugin) {
        super(plugin, "version", 0);
    }

    @Override
    protected void call(String[] args) {
        Arrays.asList(
                "&9&l" + plugin.getName() + " &7版本信息 ᚏᚎᚍᚔᚓᚒᚑᚐ",
                "  &7当前服务端版本: &c" + Bukkit.getVersion(),
                "  &7当前插件版本: &c" + plugin.getDescription().getVersion(),
                "",
                "  &7ParrotX 版本: &c1.2.1-Alpha",
                "  &7I18n 信息:",
                "  &9&l-> &7" + I18n.getToolVersion(),
                "  &9&l-> &7绑定插件: &c" + plugin.lang.getPlugin().getName(),
                "  &9&l-> &7语言: &c" + plugin.localeKey
        ).forEach(msg -> sender.sendMessage(I18n.color(msg)));
    }
}
