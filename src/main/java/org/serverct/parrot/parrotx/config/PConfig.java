package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

import java.io.File;

public class PConfig implements PConfiguration {

    protected PPlugin plugin;
    @Getter
    protected File file;
    @Getter
    protected FileConfiguration config;
    private String id;
    private String name;

    public PConfig(@NonNull PPlugin plugin, String fileName, String typeName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder() + File.separator + fileName + ".yml");
        this.id = fileName;
        this.name = typeName;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public void init() {
        if (!file.exists()) {
            saveDefault();
            plugin.getLang().log("未找到配置文件(&c" + file.getName() + "&7), 已自动生成.", LocaleUtil.Type.WARN, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        plugin.getLang().log("已加载配置文件(&c" + file.getName() + "&7).", LocaleUtil.Type.INFO, false);

        try {
            load();
        } catch (Throwable e) {
            plugin.getLang().logError(LocaleUtil.LOAD, "配置文件(" + file.getName() + ")", e.toString());
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void reload() {
        init();
    }

    @Override
    public void delete() {
    }

    @Override
    public void saveDefault() {

    }

}
