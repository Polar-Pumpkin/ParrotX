package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.autoload.AutoLoader;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.utils.BasicUtil;
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
    private boolean readonly = false;

    public PConfig(@NonNull PPlugin plugin, String filename, String typename) {
        super(plugin);
        this.file = new File(plugin.getDataFolder(), filename.endsWith(".yml") ? filename : filename + ".yml");
        this.filename = BasicUtil.getNoExFileName(this.file.getName());
        this.typeName = typename;
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    public void readOnly(final boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public String name() {
        return this.typeName + "/" + this.filename;
    }

    @Override
    public String getFilename() {
        return this.filename;
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
        this.config = YamlConfiguration.loadConfiguration(file);

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
        config = YamlConfiguration.loadConfiguration(file);
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

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void saveDefault() {
        if (Objects.nonNull(plugin.getResource(filename.endsWith(".yml") ? filename : filename + ".yml"))) {
            plugin.saveResource(filename + ".yml", false);
        } else {
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
