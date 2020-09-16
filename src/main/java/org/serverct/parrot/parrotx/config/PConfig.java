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

    private final String filename;
    private final String typeName;

    @Getter
    protected File file;
    @Getter
    protected FileConfiguration config;

    public PConfig(@NonNull PPlugin plugin, String filename, String typeName) {
        super(plugin);
        this.file = new File(plugin.getDataFolder(), filename + ".yml");
        this.filename = filename;
        this.typeName = typeName;
    }

    @Override
    public String name() {
        return typeName + "/" + filename;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public void init() {
        if (!file.exists()) {
            saveDefault();
            if (file.exists()) {
                lang.log.warn("未找到 &c" + name() + "&7, 已自动生成.");
            } else {
                lang.log.error("无法自动生成 &c" + name() + "&7.");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);

        try {
            defaultFrom(config);
            defaultTo(this);

            load();
            lang.log.info("已加载 &c" + name() + "&7.");
        } catch (Throwable e) {
            lang.log.error(I18n.LOAD, name(), e, null);
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
        init();
        lang.log.action(I18n.RELOAD, name());
    }

    @Override
    public void save() {
        try {
            autoSave();
            config.save(file);
        } catch (IOException e) {
            lang.log.error(I18n.SAVE, name(), e, null);
        }
    }

    @Override
    public void delete() {
        if (file.delete()) {
            lang.log.action(I18n.DELETE, name());
        } else {
            lang.log.error(I18n.DELETE, name(), "删除文件失败");
        }
    }

    @Override
    public void saveDefault() {
        if (Objects.nonNull(plugin.getResource(filename + ".yml"))) plugin.saveResource(filename + ".yml", false);
        else {
            try {
                if (!file.createNewFile()) {
                    lang.log.error(I18n.GENERATE, name(), "自动生成失败");
                }
            } catch (IOException e) {
                lang.log.error(I18n.GENERATE, name(), e, null);
            }
        }
    }
}
