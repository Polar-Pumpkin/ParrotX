package org.serverct.parrot.parrotx;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.I18n;

public class PPlugin extends JavaPlugin {

    @Getter
    private static PPlugin instance;
    public I18n lang;
    public String localeKey;
    public PConfig pConfig;
    @Getter
    private CommandHandler cmdHandler;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        init();

        registerListener();
    }

    public void init() {
        lang = new I18n(this, "Chinese");

        preload();

        localeKey = pConfig.getConfig().getString("Language");

        load();
    }

    protected void preload() {
    }

    protected void load() {
    }

    protected void registerListener() {
    }

    protected void registerCommand(@NonNull CommandHandler handler) {
        PluginCommand command = Bukkit.getPluginCommand(handler.mainCmd);
        if (command != null) {
            this.cmdHandler = handler;
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            lang.logError(I18n.REGISTER, "命令", "无法获取插件主命令.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getScheduler().cancelTasks(this);
    }
}
