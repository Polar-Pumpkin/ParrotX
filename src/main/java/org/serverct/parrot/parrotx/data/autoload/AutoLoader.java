package org.serverct.parrot.parrotx.data.autoload;

import com.google.common.collect.Multimap;
import lombok.Getter;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AutoLoader {

    @Getter
    protected final Map<String, AutoLoadGroup> groupMap = new HashMap<>();
    protected final PPlugin plugin;
    protected final I18n lang;
    private ConfigurationSection defFrom;
    private Object defTo;

    public AutoLoader(final PPlugin plugin) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
    }

    public static void autoLoad(final PPlugin plugin, final String object, final ConfigurationSection from, final Object to, final Map<String, AutoLoadItem> itemMap, final Multimap<Class<? extends ConfigurationSerializable>, String> serializableMap) {
        if (Objects.isNull(from) || Objects.isNull(to)) {
            return;
        }

        final I18n lang = plugin.getLang();
        final Class<?> clazz = to.getClass();

        itemMap.forEach(
                (fieldName, item) -> {
                    final String path = item.getPath();
                    if (!from.contains(path, true) || Objects.isNull(from.get(path))) {
                        lang.log.error(I18n.LOAD, object, "目标路径未找到: " + path);
                    } else {
                        final ConfigurationSection section = from.getConfigurationSection(path);
                        try {
                            Field field = clazz.getDeclaredField(fieldName);
                            field.setAccessible(true);
                            switch (item.getType()) {
                                case INT:
                                    field.setInt(to, from.getInt(path));
                                    break;
                                case STRING:
                                    field.set(to, from.getString(path));
                                    break;
                                case BOOLEAN:
                                    field.setBoolean(to, from.getBoolean(path));
                                    break;
                                case LONG:
                                    field.setLong(to, from.getLong(path));
                                    break;
                                case DOUBLE:
                                    field.setDouble(to, from.getDouble(path));
                                    break;
                                case LIST:
                                    field.set(to, from.getList(path));
                                    break;
                                case MAP_LIST:
                                    field.set(to, from.getMapList(path));
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
                                    final String soundName = from.getString(path);
                                    final Sound sound = EnumUtil.valueOf(Sound.class, Objects.isNull(soundName) ? "" : soundName.toUpperCase());
                                    if (Objects.isNull(sound)) {
                                        lang.log.error(I18n.LOAD, object, "未找到目标音效枚举: " + soundName + "(" + path + ")");
                                    }
                                    field.set(to, sound);
                                    break;
                                case ITEM_STACK:
                                    field.set(to, from.getItemStack(path));
                                    break;
                                case COLOR:
                                    field.set(to, from.getColor(path));
                                    break;
                                case LOCATION:
                                    field.set(to, from.getLocation(path));
                                    break;
                                case SERIALIZABLE:
                                    final Class<? extends ConfigurationSerializable> serializable = getSerializable(path, serializableMap);
                                    if (Objects.isNull(serializable)) {
                                        lang.log.error(I18n.LOAD, object, "尝试读取未注册的可序列化对象: " + path);
                                        break;
                                    }
                                    field.set(to, from.getSerializable(path, serializable));
                                    break;
                                case UNKNOWN:
                                default:
                                    field.set(to, from.get(path));
                                    break;
                            }

                        } catch (NoSuchFieldException e) {
                            lang.log.error(I18n.LOAD, object, "目标 Field 未找到: " + fieldName);
                        } catch (Throwable e) {
                            lang.log.error(I18n.LOAD, object, e, null);
                        }
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

    public static void autoSave(final PPlugin plugin, final String object, final ConfigurationSection from, final Object to, final Map<String, AutoLoadItem> itemMap) {
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
        this.groupMap.values().forEach(group -> group.load(plugin));
    }

    protected void autoSave() {
        this.groupMap.values().forEach(group -> group.save(plugin));
    }

    protected AutoLoadGroup group(final String name, final ConfigurationSection from, final Object to) {
        final AutoLoadGroup group = AutoLoadGroup.builder()
                .name(name)
                .from(from)
                .to(to)
                .build();
        this.groupMap.put(name, group);
        return group;
    }

    protected AutoLoadGroup getGroup(final String group) {
        if (this.groupMap.containsKey(group)) {
            return this.groupMap.get(group);
        } else {
            return group(group, defFrom, defTo);
        }
    }

    protected void autoLoad(final String group, final AutoLoadItem... items) {
        getGroup(group).load(items);
    }

    protected void loadAll(final String group, final Collection<? extends AutoLoadItem> items) {
        getGroup(group).loadAll(items);
    }

    protected void registerSerializable(final String group, final Class<? extends ConfigurationSerializable> clazz, final String path) {
        getGroup(group).registerSerializable(clazz, path);
    }

    protected void autoLoad(final String path, final AutoLoadItem.DataType type, final String field) {
        autoLoad(AutoLoadItem.builder()
                .path(path)
                .type(type)
                .field(field)
                .build());
    }

    protected void autoLoad(final AutoLoadItem... items) {
        this.autoLoad("default", items);
    }

    protected void loadAll(final Collection<? extends AutoLoadItem> items) {
        this.loadAll("default", items);
    }

    protected void registerSerializable(final Class<? extends ConfigurationSerializable> clazz, final String path) {
        this.registerSerializable("default", clazz, path);
    }
}
