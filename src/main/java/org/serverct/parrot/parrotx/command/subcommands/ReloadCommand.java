package org.serverct.parrot.parrotx.command.subcommands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.BaseCommand;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;

public class ReloadCommand extends BaseCommand {

    private final Runnable customRun;

    public ReloadCommand(@NotNull PPlugin plugin, @Nullable String perm) {
        super(plugin, "reload", 0);
        perm(perm);
        describe("重载插件配置文件");
        this.customRun = null;
    }

    public ReloadCommand(@NotNull PPlugin plugin, @Nullable String perm, @Nullable final Runnable customRun) {
        super(plugin, "reload", 0);
        perm(perm);
        describe("重载插件配置文件");
        this.customRun = customRun;
    }

    @Override
    protected void call(String[] args) {
        try {
            plugin.preDisable();
            plugin.init();

            if (Objects.nonNull(this.customRun)) {
                this.customRun.run();
            }

            sender.sendMessage(plugin.getLang().data.info("重载配置文件成功."));
        } catch (Throwable e) {
            sender.sendMessage(plugin.getLang().data.info("重载配置文件失败, 请查看控制台中的错误信息."));
            lang.log.error(I18n.RELOAD, "配置文件", e, plugin.getPackageName());
        }
    }
}
