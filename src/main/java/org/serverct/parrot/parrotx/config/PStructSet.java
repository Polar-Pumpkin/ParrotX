package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.PStruct;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public abstract class PStructSet<E extends PStruct> extends PDataSet<E> {

    @Getter
    protected FileConfiguration config;
    protected final String rootName;
    private final String filename;
    protected ConfigurationSection root;

    public PStructSet(@NonNull PPlugin plugin, String filename, String typename, String root) {
        super(plugin, new File(plugin.getDataFolder(), filename.endsWith(".yml") ? filename : filename + ".yml"),
                typename);
        this.filename = filename;
        this.rootName = root;
        this.config = YamlConfiguration.loadConfiguration(file);
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
        load();
    }

    @Override
    public void load() {
        load(this.file);
    }

    @Override
    public void load(@NonNull File file) {
        config = YamlConfiguration.loadConfiguration(file);
        root = config.getConfigurationSection(this.rootName);
        if (Objects.isNull(root)) {
            root = config.createSection(this.rootName);
        }
        for (String key : root.getKeys(false)) {
            final ConfigurationSection section = root.getConfigurationSection(key);
            if (Objects.isNull(section)) {
                lang.log.error(I18n.LOAD, name(), "存在非数据节: " + key);
                continue;
            }
            final E value = loadFromDataSection(section);
            if (Objects.isNull(value)) {
                lang.log.error(I18n.LOAD, name(), "加载数据失败: " + key);
                continue;
            }
            put(value);
        }

        if (dataMap.isEmpty()) {
            lang.log.warn("&c" + name() + " &7中没有数据可供加载.");
        } else {
            lang.log.info("共加载 &c" + name() + " &7中的 &c" + dataMap.size() + " &7个数据.");
        }
    }

    @Override
    public void save() {
        super.save();
        try {
            this.config.save(file);
        } catch (IOException e) {
            lang.log.error(I18n.SAVE, name(), e, plugin.getPackageName());
        }
    }

    @Override
    public void deleteAll() {
        this.dataMap.values().forEach(struct -> root.set(struct.getID().getId(), null));
        this.dataMap.clear();
        lang.log.action(I18n.CLEAR, name());
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
                lang.log.error(I18n.GENERATE, name(), e, plugin.getPackageName());
            }
        }
    }

    public abstract E loadFromDataSection(final ConfigurationSection section);

    @NotNull
    public PID buildId(@NotNull final String id) {
        return new PID(plugin, rootName.toLowerCase(), id);
    }

    @NotNull
    public PID buildId(@NotNull final ConfigurationSection section) {
        return buildId(section.getName());
    }
}
