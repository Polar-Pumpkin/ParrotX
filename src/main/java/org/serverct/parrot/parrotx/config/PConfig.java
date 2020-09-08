package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.autoload.AutoLoader;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public abstract class PConfig extends AutoLoader implements PConfiguration, FileSaved {

    private final String id;
    private final String name;

    @Getter
    protected File file;
    @Getter
    protected FileConfiguration config;

    public PConfig(@NonNull PPlugin plugin, String fileName, String typeName) {
        super(plugin);
        this.file = new File(plugin.getDataFolder(), fileName + ".yml");
        this.id = fileName;
        this.name = typeName;
    }

    @Override
    public String getTypename() {
        return name + "/" + id;
    }

    @Override
    public String getFilename() {
        return id;
    }

    @Override
    public void init() {
        if (!file.exists()) {
            saveDefault();
            if (file.exists()) plugin.getLang().log.warn("未找到 &c" + getTypename() + "&7, 已自动生成.");
            else plugin.getLang().log.error("无法自动生成 &c" + getTypename() + "&7.");
        }
        config = YamlConfiguration.loadConfiguration(file);

        try {
            defaultFrom(config);
            defaultTo(this);

            load();
            plugin.getLang().log.info("已加载 &c" + getTypename() + "&7.");
        } catch (Throwable e) {
            plugin.getLang().log.error(I18n.LOAD, getTypename(), e, null);
        }
    }

    @Override
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void load() {
        load(file);
    }

    @Override
    public void load(@NonNull File file) {
        autoLoad();
    }

    @Override
    public void reload() {
        plugin.getLang().log.action(I18n.RELOAD, getTypename());
        init();
    }

    @Override
    public void save() {
        try {
            autoSave();
            config.save(file);
        } catch (IOException e) {
            plugin.getLang().log.error(I18n.SAVE, getTypename(), e, null);
        }
    }

    @Override
    public void delete() {
        if (file.delete()) {
            plugin.getLang().log.action(I18n.DELETE, getTypename());
        } else {
            plugin.getLang().log.error(I18n.DELETE, getTypename(), "无法删除该文件");
        }
    }

    @Override
    public void saveDefault() {
        if (Objects.nonNull(plugin.getResource(id + ".yml"))) plugin.saveResource(id + ".yml", false);
        else {
            try {
                if (!file.createNewFile()) {
                    plugin.getLang().log.error(I18n.GENERATE, getTypename(), "自动生成失败");
                }
            } catch (IOException e) {
                plugin.getLang().log.error(I18n.GENERATE, getTypename(), e, null);
            }
        }
    }
}
