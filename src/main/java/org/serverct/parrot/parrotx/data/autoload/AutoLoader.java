package org.serverct.parrot.parrotx.data.autoload;

import com.google.common.collect.Multimap;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.Field;
import java.util.*;

@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AutoLoader {

    protected final Map<String, AutoLoadGroup> groupMap = new HashMap<>();
    protected final PPlugin plugin;
    protected final I18n lang;
    private ConfigurationSection defFrom;
    private Object defTo;

    public AutoLoader(final PPlugin plugin) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
    }

    public static void load(
            final PPlugin plugin,
            final String object,
            final String extraPath,
            final ConfigurationSection dataSource,
            final Object to,
            final Map<String, AutoLoadItem> itemMap,
            final Multimap<Class<? extends ConfigurationSerializable>, String> serializableMap
    ) {
        if (Objects.isNull(dataSource) || Objects.isNull(to)) {
            return;
        }

        final I18n lang = plugin.getLang();
        final Class<?> clazz = to.getClass();

        for (Map.Entry<String, AutoLoadItem> entry : itemMap.entrySet()) {
            final String fieldName = entry.getKey();
            final AutoLoadItem item = entry.getValue();

            final String path = (Optional.ofNullable(extraPath).orElse("").length() > 0 ? extraPath + "." : "") + item.getPath();
            if (!dataSource.contains(path, true) || Objects.isNull(dataSource.get(path))) {
                lang.log.error(I18n.LOAD, object, "目标路径未找到: " + path);
                continue;
            }

            final ConfigurationSection section = dataSource.getConfigurationSection(path);
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                switch (item.getType()) {
                    case INT:
                        field.setInt(to, dataSource.getInt(path));
                        break;
                    case STRING:
                        field.set(to, dataSource.getString(path));
                        break;
                    case BOOLEAN:
                        field.setBoolean(to, dataSource.getBoolean(path));
                        break;
                    case LONG:
                        field.setLong(to, dataSource.getLong(path));
                        break;
                    case DOUBLE:
                        field.setDouble(to, dataSource.getDouble(path));
                        break;
                    case LIST:
                        field.set(to, dataSource.getList(path));
                        break;
                    case MAP_LIST:
                        field.set(to, dataSource.getMapList(path));
                        break;
                    case STRING_LIST:
                        field.set(to, dataSource.getStringList(path));
                        break;
                    case STRING_MAP:
                        final Map<String, String> stringMap = new HashMap<>();
                        if (Objects.nonNull(section)) {
                            section.getKeys(false).forEach(key -> stringMap.put(key, section.getString(key)));
                        }
                        field.set(to, stringMap);
                        break;
                    case INT_MAP:
                        final Map<String, Integer> intMap = new HashMap<>();
                        if (Objects.nonNull(section)) {
                            section.getKeys(false).forEach(key -> intMap.put(key, section.getInt(key)));
                        }
                        field.set(to, intMap);
                        break;
                    case SOUND:
                        final String soundName = dataSource.getString(path);
                        final Sound sound = EnumUtil.valueOf(Sound.class, Objects.isNull(soundName) ? "" : soundName.toUpperCase());
                        if (Objects.isNull(sound)) {
                            lang.log.error(I18n.LOAD, object, "未找到目标音效枚举: " + soundName + "(" + path + ")");
                        }
                        field.set(to, sound);
                        break;
                    case ITEMSTACK:
                        ItemStack itemstack = dataSource.getItemStack(path);
                        if (Objects.isNull(itemstack) && Objects.nonNull(section)) {
                            itemstack = ItemUtil.build(plugin, section);
                        }
                        field.set(to, itemstack);
                        break;
                    case COLOR:
                        field.set(to, dataSource.getColor(path));
                        break;
                    case LOCATION:
                        field.set(to, dataSource.getLocation(path));
                        break;
                    case SERIALIZABLE:
                        final Class<? extends ConfigurationSerializable> serializable = getSerializable(path, serializableMap);
                        if (Objects.isNull(serializable)) {
                            lang.log.error(I18n.LOAD, object, "尝试读取未注册的可序列化对象: " + path);
                            break;
                        }
                        field.set(to, dataSource.getSerializable(path, serializable));
                        break;
                    case UNKNOWN:
                    default:
                        field.set(to, dataSource.get(path));
                        break;
                }

            } catch (NoSuchFieldException e) {
                lang.log.error(I18n.LOAD, object, "目标 Field 未找到: " + fieldName);
            } catch (Throwable e) {
                lang.log.error(I18n.LOAD, object, e, null);
            }
        }
    }

    public static void save(final PPlugin plugin, final String object, final ConfigurationSection from, final Object to, final Map<String, AutoLoadItem> itemMap) {
        if (Objects.isNull(from) || Objects.isNull(to)) {
            return;
        }

        final I18n lang = plugin.getLang();
        final Class<?> clazz = to.getClass();

        itemMap.forEach(
                (fieldName, item) -> {
                    try {
                        Field field = clazz.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        switch (item.getType()) {
                            case INT_MAP:
                            case STRING_MAP:
                                final Map<?, ?> map = (Map<?, ?>) field.get(to);
                                map.forEach((key, value) -> from.set((String) key, value));
                                break;
                            default:
                                from.set(item.getPath(), field.get(to));
                                break;
                        }
                    } catch (NoSuchFieldException e) {
                        lang.log.error(I18n.LOAD, object, "目标 Field 未找到: " + item.getField());
                    } catch (Throwable e) {
                        lang.log.error(I18n.LOAD, object, e, null);
                    }
                }
        );
    }

    private static Class<? extends ConfigurationSerializable> getSerializable(final String path, final Multimap<Class<? extends ConfigurationSerializable>, String> map) {
        if (!map.containsValue(path)) {
            return null;
        }
        for (Map.Entry<Class<? extends ConfigurationSerializable>, Collection<String>> entry : map.asMap().entrySet()) {
            final Collection<String> paths = entry.getValue();
            if (paths.contains(path)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void defaultTo(final Object to) {
        this.defTo = to;
        if (this.groupMap.containsKey("default")) {
            getGroup("default").setTo(this.defTo);
        }
    }

    public void defaultFrom(final ConfigurationSection from) {
        this.defFrom = from;
        if (this.groupMap.containsKey("default")) {
            getGroup("default").setFrom(this.defFrom);
        }
    }

    protected void autoLoad() {
        importGroups(defTo);
        this.groupMap.values().forEach(group -> {
            importItems(group.getTo());
            group.load(plugin);
        });
    }

    protected void autoSave() {
        this.groupMap.values().forEach(group -> group.save(plugin));
    }

    protected void importItems(final Object to) {
        final Class<?> clazz = to.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            final Load annotation = field.getAnnotation(Load.class);
            if (Objects.isNull(annotation)) {
                continue;
            }

            final String typeName = field.getType().getSimpleName();
            final AutoLoadItem.DataType type = EnumUtil.valueOf(AutoLoadItem.DataType.class, typeName.toUpperCase());
            if (Objects.isNull(type)) {
                lang.log.error(I18n.LOAD, "自动加载数据组", "通过注解自动导入时遇到不支持的数据类型: " + typeName);
                continue;
            }

            lang.log.action(I18n.CREATE, "新自动加载项目: {0}({1}) -> {2}(组: {3})", field.getName(), type, annotation.path(), annotation.group());
            add(annotation.group(), annotation.path(), type, field.getName());
        }
    }

    protected void importGroups(final Object to) {
        final Class<?> clazz = to.getClass();
        final Groups annotation = clazz.getAnnotation(Groups.class);
        if (Objects.isNull(annotation)) {
            return;
        }

        for (Group group : annotation.value()) {
            group(group.name(), group.path(), defFrom, defTo);
        }
    }

    protected AutoLoadGroup group(final String name, final String path, final ConfigurationSection from, final Object to) {
        final AutoLoadGroup group = AutoLoadGroup.builder()
                .name(name)
                .path(path)
                .from(from)
                .to(to)
                .build();
        lang.log.action(I18n.CREATE, "新自动加载数据组: {0}(路径: {1}, 数据源: {2}, {3})", name, path, from.getName(), to.getClass().getSimpleName() + ".class");
        this.groupMap.put(name, group);
        return group;
    }

    protected AutoLoadGroup getGroup(final String group) {
        if (this.groupMap.containsKey(group)) {
            return this.groupMap.get(group);
        } else {
            return group(group, "", defFrom, defTo);
        }
    }

    protected void add(final String path, final AutoLoadItem.DataType type, final String field) {
        add("default", path, type, field);
    }

    protected void add(final String group, final String path, final AutoLoadItem.DataType type, final String field) {
        getGroup(group).add(
                AutoLoadItem.builder()
                        .path(path)
                        .type(type)
                        .field(field)
                        .build()
        );
    }

    protected void addAll(final AutoLoadItem... items) {
        this.addAll("default", items);
    }

    protected void addAll(final String group, final AutoLoadItem... items) {
        getGroup(group).add(items);
    }

    protected void addAll(final Collection<? extends AutoLoadItem> items) {
        this.addAll("default", items);
    }

    protected void addAll(final String group, final Collection<? extends AutoLoadItem> items) {
        getGroup(group).addAll(items);
    }

    protected void registerSerializable(final Class<? extends ConfigurationSerializable> clazz, final String path) {
        this.registerSerializable("default", clazz, path);
    }

    protected void registerSerializable(final String group, final Class<? extends ConfigurationSerializable> clazz, final String path) {
        getGroup(group).registerSerializable(clazz, path);
    }
}
