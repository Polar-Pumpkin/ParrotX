package org.serverct.parrot.parrotx.command.subcommands;

import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.BaseCommand;
import org.serverct.parrot.parrotx.utils.I18n;

public class ReloadCommand extends BaseCommand {

    public ReloadCommand(PPlugin plugin, String perm) {
        super(plugin, "reload", 0);
        perm(perm);
        describe("重载插件配置文件");
    }

    @Override
    protected void call(String[] args) {
        try {
            plugin.init();
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.INFO, "重载配置文件成功."));
        } catch (Throwable e) {
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.WARN, "重载配置文件失败, 请查看控制台中的错误信息."));
            plugin.lang.logError(I18n.RELOAD, "配置文件", e, null);
        }
    }
}
