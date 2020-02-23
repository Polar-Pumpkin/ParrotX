package org.serverct.parrot.parrotx;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

public class PPlugin extends JavaPlugin {

    public static PPlugin instance;
    public LocaleUtil lang;
    public String localeKey;
    @Getter
    @Setter
    public PConfig pConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        init();

        registerListener();
    }

    public void init() {
        preload();

        lang = new LocaleUtil(this, "Chinese");
        localeKey = pConfig.getConfig().getString("Language");

        load();
    }

    protected void preload() {
    }

    protected void load() {
    }

    protected void registerListener() {
    }

    protected void registerCommand(String cmd, CommandHandler handler) {
        PluginCommand command = Bukkit.getPluginCommand(cmd);
        if (command != null) {
            command.setExecutor(handler);
        } else {
            lang.logError(LocaleUtil.REGISTER, "命令", "无法获取插件主命令.");
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getScheduler().cancelTasks(this);
    }
}
