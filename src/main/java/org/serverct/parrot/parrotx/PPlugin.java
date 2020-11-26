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
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.hooks.BaseExpansion;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class PPlugin extends JavaPlugin {

    private final List<PConfiguration> configs = new ArrayList<>();
    private final List<BaseExpansion> expansions = new ArrayList<>();
    public String localeKey = "Chinese";
    protected PConfig pConfig;
    @Getter
    protected I18n lang;
    private Consumer<PluginManager> listenerRegister = null;
    private String timeLog = null;
    @Getter
    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        final long timestamp = System.currentTimeMillis();

        try {
            beforeInit();

            init();

            afterInit();

            if (Objects.nonNull(listenerRegister)) {
                listenerRegister.accept(Bukkit.getPluginManager());
            }

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                this.expansions.forEach(PlaceholderExpansion::register);
            }

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

        this.configs.forEach(PConfiguration::init);

        load();
    }

    protected void beforeInit() {
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

    protected <T extends BaseExpansion> void registerExpansion(final T expansions) {
        this.expansions.add(expansions);
    }

    protected void registerCommand(@NonNull CommandHandler handler) {
        PluginCommand command = Bukkit.getPluginCommand(handler.mainCmd);
        if (command != null) {
            this.commandHandler = handler;
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            lang.log.error(I18n.REGISTER, "命令", "无法获取插件主命令.");
        }
    }

    public void registerConfiguration(final PConfiguration configuration) {
        this.configs.add(configuration);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getScheduler().cancelTasks(this);
        this.configs.forEach(config -> {
            if (!config.isReadOnly()) {
                config.save();
            }
        });
    }

    public File getFile(final String path) {
        return new File(getDataFolder(), path);
    }
}
