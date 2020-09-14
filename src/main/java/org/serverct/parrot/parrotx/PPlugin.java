package org.serverct.parrot.parrotx;

import lombok.Getter;
import lombok.NonNull;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class PPlugin extends JavaPlugin {

    private final List<PlaceholderExpansion> expansions = new ArrayList<>();
    public String localeKey = "Chinese";
    protected PConfig pConfig;
    @Getter
    protected I18n lang;
    private Consumer<PluginManager> listenerRegister = null;
    private String timeLog = null;
    @Getter
    private CommandHandler cmdHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        final long timestamp = System.currentTimeMillis();

        try {
            init();

            afterInit();

            if (Objects.nonNull(listenerRegister)) {
                listenerRegister.accept(Bukkit.getPluginManager());
            }

            this.expansions.forEach(PlaceholderExpansion::register);

            if (Objects.nonNull(timeLog)) {
                final long time = System.currentTimeMillis() - timestamp;
                lang.log.info(MessageFormat.format(timeLog, time));
            }
        } catch (Throwable e) {
            lang.log.error(I18n.INIT, "插件", e, null);
            this.setEnabled(false);
        }
    }

    public void init() {
        lang = new I18n(this, localeKey);

        preload();

        if (Objects.nonNull(pConfig)) {
            pConfig.init();
            localeKey = pConfig.getConfig().getString("Language");
        }

        load();
    }

    protected void preload() {
    }

    protected void load() {
    }

    protected void afterInit() {
    }

    protected void listen(Consumer<PluginManager> register) {
        this.listenerRegister = register;
    }

    protected void setTimeLog(final String format) {
        this.timeLog = format;
    }

    protected void registerExpansion(final PlaceholderExpansion... expansions) {
        this.expansions.addAll(Arrays.asList(expansions));
    }

    protected void registerCommand(@NonNull CommandHandler handler) {
        PluginCommand command = Bukkit.getPluginCommand(handler.mainCmd);
        if (command != null) {
            this.cmdHandler = handler;
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            lang.log.error(I18n.REGISTER, "命令", "无法获取插件主命令.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.expansions.forEach(PlaceholderExpansion::unregister);
        getServer().getScheduler().cancelTasks(this);
    }
}
