package org.serverct.parrot.parrotx.config;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.PStruct;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public abstract class PStructSet<T extends PStruct> extends PDataSet<T> {

    protected final FileConfiguration config;
    protected final String rootName;
    protected ConfigurationSection root;

    public PStructSet(@NonNull PPlugin plugin, String fileName, String typeName, String rootName) {
        super(plugin, new File(plugin.getDataFolder(), fileName + ".yml"), typeName);
        this.rootName = rootName;
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public void init() {
        if (!file.exists()) {
            if (file.mkdirs()) {
                saveDefault();
                plugin.getLang().log.warn("未找到 &c" + name() + "&7, 已重新生成.");
            } else {
                plugin.getLang().log.error("尝试生成 &c" + name() + " &7失败.");
            }
        }
        load();
    }

    @Override
    public void load() {
        load(file);
    }


    @Override
    public void load(@NonNull File file) {
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
            put(load(section));
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
            lang.log.error(I18n.SAVE, name(), e, null);
        }
    }

    public abstract T load(final ConfigurationSection section);

    public PID buildId(String id) {
        return new PID(plugin, rootName.toLowerCase(), id);
    }
}
