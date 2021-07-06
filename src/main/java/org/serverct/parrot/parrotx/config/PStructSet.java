package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.PStruct;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class PStructSet<E extends PStruct> extends PDataSet<E> {

    protected final String rootName;
    private final String filename;
    @Getter
    protected FileConfiguration config;
    protected ConfigurationSection root;

    public PStructSet(@NonNull PPlugin plugin, String filename, String typename, String root) {
        super(plugin, new File(plugin.getDataFolder(), filename.endsWith(".yml") ? filename : filename + ".yml"),
                typename);
        this.filename = filename;
        this.rootName = root;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public static <E extends PStruct> void loadTo(@NotNull final ConfigurationSection from,
                                                  @NotNull final Map<String, E> to,
                                                  @NotNull final BiFunction<String, ConfigurationSection, E> loader) {

        loadTo(from, to, loader, null);
    }

    public static <E extends PStruct> void loadTo(@NotNull final ConfigurationSection from,
                                                  @NotNull final Map<String, E> to,
                                                  @NotNull final BiFunction<String, ConfigurationSection, E> loader,
                                                  @Nullable final Consumer<String> onInvalid) {
        for (final String key : from.getKeys(false)) {
            final ConfigurationSection section = from.getConfigurationSection(key);
            if (Objects.isNull(section)) {
                BasicUtil.canDo(onInvalid, consumer -> consumer.accept(key));
                continue;
            }

            final E value = loader.apply(key, section);
            if (Objects.isNull(value)) {
                return;
            }
            to.put(key, value);
        }
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
        clearCache();

        config = YamlConfiguration.loadConfiguration(file);
        root = config.getConfigurationSection(this.rootName);
        if (Objects.isNull(root)) {
            root = config.createSection(this.rootName);
        }
        if (isLazyLoad()) {
            lang.log.info("已为 &c{0} &r启用懒加载.", name());
            return;
        }

        root.getKeys(false).stream()
                .map(this::buildId)
                .forEach(this::load);

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

    @Nullable
    public abstract E loadFromDataSection(@NotNull final ConfigurationSection section);

    @Nullable
    @Override
    public E load(@NotNull PID id) {
        final String key = id.getId();
        final ConfigurationSection section = root.getConfigurationSection(key);
        if (Objects.isNull(section)) {
            lang.log.error(I18n.LOAD, name(), "存在非数据节: " + key);
            return null;
        }

        final E value = loadFromDataSection(section);
        if (Objects.isNull(value)) {
            lang.log.error(I18n.LOAD, name(), "加载数据失败: " + key);
            return null;
        }
        put(value);
        return value;
    }

    @Override
    public void delete(@NotNull PID id) {
        super.delete(id);
        this.root.set(id.getId(), null);
    }

    @NotNull
    public PID buildId(@NotNull final String id) {
        return new PID(plugin, rootName.toLowerCase(), id);
    }

    @NotNull
    public PID buildId(@NotNull final ConfigurationSection section) {
        return buildId(section.getName());
    }
}
