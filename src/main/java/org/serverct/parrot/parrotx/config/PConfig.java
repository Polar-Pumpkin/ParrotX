package org.serverct.parrot.parrotx.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PConfig implements PConfiguration {

    protected final Map<String, ConfigItem> itemMap = new HashMap<>();
    protected PPlugin plugin;
    @Getter
    protected File file;
    @Getter
    protected FileConfiguration config;
    private String id;
    private String name;

    public PConfig(@NonNull PPlugin plugin, String fileName, String typeName) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), fileName + ".yml");
        this.id = fileName;
        this.name = typeName;
    }

    @Override
    public String getTypeName() {
        return name + "/" + id;
    }

    @Override
    public String getFileName() {
        return id;
    }

    @Override
    public void init() {
        if (!file.exists()) {
            saveDefault();
            if (file.exists()) plugin.lang.log("未找到 &c" + getTypeName() + "&7, 已自动生成.", I18n.Type.WARN, false);
            else plugin.lang.log("无法自动生成 &c" + getTypeName() + "&7.", I18n.Type.ERROR, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        plugin.lang.log("已加载 &c" + getTypeName() + "&7.", I18n.Type.INFO, false);

        try {
            load(file);
        } catch (Throwable e) {
            plugin.lang.logError(I18n.LOAD, getTypeName(), e, null);
        }
    }

    @Override
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void load(@NonNull File file) {
        Class<? extends PConfig> configClass = this.getClass();
        this.itemMap.forEach(
                (fieldName, item) -> {
                    try {
                        Field field = configClass.getField(item.getField());
                        field.setAccessible(true);
                        switch (item.getType()) {
                            case INT:
                                field.setInt(this, config.getInt(item.getPath()));
                                break;
                            case STRING:
                                field.set(this, config.getString(item.getPath()));
                                break;
                            case BOOLEAN:
                                field.setBoolean(this, config.getBoolean(item.getPath()));
                                break;
                            case LIST:
                                field.set(this, config.getStringList(item.getPath()));
                                break;
                            default:
                                field.set(this, config.get(item.getPath()));
                                break;
                        }

                    } catch (NoSuchFieldException e) {
                        plugin.lang.logError(I18n.LOAD, getTypeName(), "目标配置项未找到(" + item.toString() + ")");
                    } catch (Throwable e) {
                        plugin.lang.logError(I18n.LOAD, getTypeName(), e, null);
                    }
                }
        );
    }

    @Override
    public void reload() {
        plugin.lang.logAction(I18n.RELOAD, getTypeName());
        init();
    }

    @Override
    public void save() {
        try {
            Class<? extends PConfig> configClass = this.getClass();
            this.itemMap.forEach(
                    (fieldName, item) -> {
                        try {
                            Field field = configClass.getField(item.getField());
                            field.setAccessible(true);
                            config.set(item.getPath(), field.get(this));
                        } catch (NoSuchFieldException e) {
                            plugin.lang.logError(I18n.LOAD, getTypeName(), "目标配置项未找到(" + item.toString() + ")");
                        } catch (Throwable e) {
                            plugin.lang.logError(I18n.LOAD, getTypeName(), e, null);
                        }
                    }
            );

            config.save(file);
        } catch (IOException e) {
            plugin.lang.logError(I18n.SAVE, getTypeName(), e, null);
        }
    }

    @Override
    public void delete() {
        if (file.delete()) {
            plugin.lang.logAction(I18n.DELETE, getTypeName());
        } else {
            plugin.lang.logError(I18n.DELETE, getTypeName(), "无法删除该文件");
        }
    }

    @Override
    public void saveDefault() {
        if (Objects.nonNull(plugin.getResource(file.getName()))) plugin.saveResource(file.getName(), false);
    }

    protected void addItem(String path, ItemType type, String field) {
        this.itemMap.put(field, new ConfigItem(path, type, field));
    }

    protected void removeItem(String field) {
        this.itemMap.remove(field);
    }

    public enum ItemType {
        STRING("字符串"),
        INT("整数"),
        BOOLEAN("布尔值"),
        LIST("列表"),
        SOUND("SpigotAPI 声音(Sound)枚举"),
        UNKNOWN("未知类型");

        public final String name;

        ItemType(String name) {
            this.name = name;
        }
    }

    protected @Data
    @AllArgsConstructor
    static class ConfigItem {
        private String path;
        private ItemType type;
        private String field;
    }
}
