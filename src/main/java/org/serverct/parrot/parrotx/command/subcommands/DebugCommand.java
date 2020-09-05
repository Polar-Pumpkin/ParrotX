package org.serverct.parrot.parrotx.command.subcommands;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.BaseCommand;

@SuppressWarnings("AccessStaticViaInstance")
public class DebugCommand extends BaseCommand {
    public DebugCommand(@NotNull PPlugin plugin, String perm) {
        super(plugin, "debug", 0);
        describe("开关插件的 Debug 模式, 将会在插件运行期间在后台输出表示插件工作状态的调试信息");
        perm(perm);
    }

    @Override
    protected void call(String[] args) {
        final FileConfiguration config = plugin.getConfig();
        final boolean debug = !config.getBoolean("Debug", false);
        config.set("Debug", debug);
        if (sender instanceof Player) {
            plugin.lang.sender.infoMessage(user, "Debug 模式已{0}", debug ? "&a&l开启" : "&c&l关闭");
        } else {
            plugin.lang.log.info("Debug 模式已" + (debug ? "&a&l开启" : "&c&l关闭"));
        }
    }
}
