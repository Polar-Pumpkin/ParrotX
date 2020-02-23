package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

import java.io.File;
import java.io.IOException;

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
        return name + "(" + id + ")";
    }

    @Override
    public String getFileName() {
        return id;
    }

    @Override
    public void init() {
        if (!file.exists()) {
            saveDefault();
            plugin.lang.log("未找到 &c" + getTypeName() + "&7, 已自动生成.", LocaleUtil.Type.WARN, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        plugin.lang.log("已加载 &c" + getTypeName() + "&7.", LocaleUtil.Type.INFO, false);

        try {
            load(file);
        } catch (Throwable e) {
            plugin.lang.logError(LocaleUtil.LOAD, getTypeName(), e.toString());
        }
    }


    @Override
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void load(@NonNull File file) {

    }

    @Override
    public void reload() {
        plugin.lang.logAction(LocaleUtil.RELOAD, getTypeName());
        init();
    }

    @Override
    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.lang.logError(LocaleUtil.SAVE, getTypeName(), e.toString());
        }
    }

    @Override
    public void delete() {
        if (file.delete()) {
            plugin.lang.logAction(LocaleUtil.DELETE, getTypeName());
        } else {
            plugin.lang.logError(LocaleUtil.DELETE, getTypeName(), "无法删除该文件");
        }
    }

    @Override
    public void saveDefault() {

    }

}
