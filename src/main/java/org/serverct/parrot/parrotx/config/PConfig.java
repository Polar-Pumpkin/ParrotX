package org.serverct.parrot.parrotx.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("AccessStaticViaInstance")
public class PConfig implements PConfiguration {

    protected final Map<String, ConfigItem> itemMap = new HashMap<>();
    private final String id;
    private final String name;
    private final Map<String, Class<? extends ConfigurationSerializable>> serializableMap = new HashMap<>();
    protected PPlugin plugin;
    @Getter
    protected File file;
    @Getter
    protected FileConfiguration config;
    private Class<?> instance = null;

    public PConfig(@NonNull PPlugin plugin, String fileName, String typeName) {
        this.plugin = plugin;
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
            if (file.exists()) plugin.lang.log.warn("未找到 &c" + getTypename() + "&7, 已自动生成.");
            else plugin.lang.log.error("无法自动生成 &c" + getTypename() + "&7.");
        }
        config = YamlConfiguration.loadConfiguration(file);
        plugin.lang.log.info("已加载 &c" + getTypename() + "&7.");

        try {
            load(file);
        } catch (Throwable e) {
            plugin.lang.log.error(I18n.LOAD, getTypename(), e, null);
        }
    }

    @Override
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void load(@NonNull File file) {
        if (Objects.isNull(instance)) {
            return;
        }

//        System.out.println("Class: " + instance.getName());
//        for (Field field : instance.getDeclaredFields()) {
//            System.out.println(" > Field " + field.getName() + " - Static: " + Modifier.isStatic(field.getModifiers()));
//        }

        this.itemMap.forEach(
                (fieldName, item) -> {
                    final String path = item.getPath();
                    final ConfigurationSection section = config.getConfigurationSection(path);
                    try {
                        Field field = instance.getDeclaredField(item.getField());
                        field.setAccessible(true);
                        switch (item.getType()) {
                            case INT:
                                field.setInt(instance, config.getInt(path));
                                break;
                            case STRING:
                                field.set(instance, config.getString(path));
                                break;
                            case BOOLEAN:
                                field.setBoolean(instance, config.getBoolean(path));
                                break;
                            case LONG:
                                field.setLong(instance, config.getLong(path));
                                break;
                            case DOUBLE:
                                field.setDouble(instance, config.getDouble(path));
                                break;
                            case LIST:
                                field.set(instance, config.getList(path));
                                break;
                            case MAP_LIST:
                                field.set(instance, config.getMapList(path));
                                break;
                            case STRING_MAP:
                                final Map<String, String> stringMap = new HashMap<>();
                                if (Objects.nonNull(section)) {
                                    section.getKeys(false).forEach(key -> stringMap.put(key, section.getString(key)));
                                }
                                field.set(instance, stringMap);
                                break;
                            case INT_MAP:
                                final Map<String, Integer> intMap = new HashMap<>();
                                if (Objects.nonNull(section)) {
                                    section.getKeys(false).forEach(key -> intMap.put(key, section.getInt(key)));
                                }
                                field.set(instance, intMap);
                                break;
                            case SOUND:
                                final String soundName = config.getString(path);
                                final Sound sound = EnumUtil.valueOf(Sound.class, Objects.isNull(soundName) ? "" : soundName.toUpperCase());
                                if (Objects.isNull(sound)) {
                                    plugin.lang.log.error(I18n.LOAD, getTypename(), "未找到目标音效枚举: " + soundName + "(" + path + ")");
                                }
                                field.set(instance, sound);
                                break;
                            case ITEM_STACK:
                                field.set(instance, config.getItemStack(path));
                                break;
                            case COLOR:
                                field.set(instance, config.getColor(path));
                                break;
                            case LOCATION:
                                field.set(instance, config.getLocation(path));
                                break;
                            case SERIALIZABLE:
                                if (!this.serializableMap.containsKey(path)) {
                                    plugin.lang.log.error(I18n.LOAD, getTypename(), "尝试读取未注册的可序列化对象: " + path);
                                    break;
                                }
                                field.set(instance, config.getSerializable(path, this.serializableMap.get(path)));
                                break;
                            case UNKNOWN:
                            default:
                                field.set(instance, config.get(path));
                                break;
                        }

                    } catch (NoSuchFieldException e) {
                        plugin.lang.log.error(I18n.LOAD, getTypename(), "目标 Field 未找到: " + item.getField());
                    } catch (Throwable e) {
                        plugin.lang.log.error(I18n.LOAD, getTypename(), e, null);
                    }
                }
        );
    }

    @Override
    public void reload() {
        plugin.lang.log.action(I18n.RELOAD, getTypename());
        init();
    }

    @Override
    public void save() {
        if (Objects.isNull(instance)) {
            return;
        }
        try {
            this.itemMap.forEach(
                    (fieldName, item) -> {
                        try {
                            Field field = instance.getDeclaredField(item.getField());
                            field.setAccessible(true);
                            switch (item.getType()) {
                                case INT_MAP:
                                case STRING_MAP:
                                    final Map<?, ?> map = (Map<?, ?>) field.get(instance);
                                    map.forEach((key, value) -> config.set((String) key, value));
                                    break;
                                default:
                                    config.set(item.getPath(), field.get(instance));
                                    break;
                            }
                        } catch (NoSuchFieldException e) {
                            plugin.lang.log.error(I18n.LOAD, getTypename(), "目标 Field 未找到: " + item.getField());
                        } catch (Throwable e) {
                            plugin.lang.log.error(I18n.LOAD, getTypename(), e, null);
                        }
                    }
            );

            config.save(file);
        } catch (IOException e) {
            plugin.lang.log.error(I18n.SAVE, getTypename(), e, null);
        }
    }

    @Override
    public void delete() {
        if (file.delete()) {
            plugin.lang.log.action(I18n.DELETE, getTypename());
        } else {
            plugin.lang.log.error(I18n.DELETE, getTypename(), "无法删除该文件");
        }
    }

    @Override
    public void saveDefault() {
        if (Objects.nonNull(plugin.getResource(file.getName()))) plugin.saveResource(file.getName(), false);
        else {
            try {
                if (!file.createNewFile()) {
                    plugin.lang.log.error(I18n.GENERATE, getTypename(), "自动生成失败");
                }
            } catch (IOException e) {
                plugin.lang.log.error(I18n.GENERATE, getTypename(), e, null);
            }
        }
    }

    protected void addItem(String path, ItemType type, String field) {
        this.itemMap.put(field, new ConfigItem(path, type, field));
    }

    protected void removeItem(String field) {
        this.itemMap.remove(field);
    }

    protected void loadTo(final Class<?> clazz) {
        this.instance = clazz;
    }

    protected void registerSerializable(final String path, final Class<? extends ConfigurationSerializable> clazz) {
        this.serializableMap.put(path, clazz);
    }

    public enum ItemType {
        STRING("字符串"),
        INT("整数"),
        DOUBLE("小数(Double)"),
        LONG("长整数"),
        BOOLEAN("布尔值"),
        LIST("列表"),
        MAP_LIST("Map 列表"),
        STRING_MAP("哈希表(String)"),
        INT_MAP("哈希表(Int)"),
        SOUND("音效(Sound)枚举"),
        ITEM_STACK("物品堆(ItemStack)"),
        LOCATION("坐标"),
        COLOR("颜色"),
        SERIALIZABLE("可序列化对象"),
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
