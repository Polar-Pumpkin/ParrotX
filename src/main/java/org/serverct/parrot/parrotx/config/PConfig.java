package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

import java.io.File;

public class PConfig {

    protected PPlugin plugin;
    @Getter
    protected File file;
    @Getter
    protected FileConfiguration config;

    public PConfig(@NonNull PPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder() + File.separator + "config.yml");
    }

    public void init() {
        if (!file.exists()) {
            plugin.saveDefaultConfig();
            plugin.getLang().log("未找到配置文件, 已自动生成.", LocaleUtil.Type.WARN, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        plugin.getLang().log("已加载配置文件.", LocaleUtil.Type.INFO, false);

        try {
            load();
        } catch (Throwable e) {
            plugin.getLang().log("尝试读取配置数据时遇到错误(&c" + e.toString() + "&7).", LocaleUtil.Type.ERROR, false);
        }
    }

    public void load() {
    }

}
