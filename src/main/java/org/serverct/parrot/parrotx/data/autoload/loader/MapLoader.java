package org.serverct.parrot.parrotx.data.autoload.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.autoload.DataLoader;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class MapLoader implements DataLoader<Map> {

    private static final Map<Class<?>, Function<String, ?>> PARSER_MAP = new HashMap<>();

    static {
        registerParser(String.class, string -> string);
        registerParser(Short.class, Short::parseShort);
        registerParser(Integer.class, Integer::parseInt);
        registerParser(Long.class, Long::parseLong);
        registerParser(Float.class, Float::parseFloat);
        registerParser(Double.class, Double::parseDouble);
    }

    public static void registerParser(@NotNull final Class<?> type,
                                      @NotNull final Function<String, ?> parser) {
        PARSER_MAP.put(type, parser);
    }

    @Override
    public @NotNull Class<Map> getType() {
        return Map.class;
    }

    @Override
    public @Nullable Map load(@NotNull String path, @NotNull ConfigurationSection section,
                              @NotNull List<Class<?>> paramTypes) {
        final Map<Object, Object> map = new HashMap<>();
        if (paramTypes.size() < 2) {
            Autoloader.log("加载 Map 数据时未提供泛型类型, 路径: {0}, 数据节: {1}", path, section.getName());
            return map;
        }

        final Function<String, ?> keyParser = PARSER_MAP.get(paramTypes.get(0));
        final DataLoader<?> valueLoader = Autoloader.getLoader(paramTypes.get(1));
        if (Objects.isNull(keyParser)) {
            Autoloader.log("加载 Map 数据时未注册 Key 对应分析函数: {0}", paramTypes.get(0));
            return map;
        }
        if (Objects.isNull(valueLoader)) {
            Autoloader.log("加载 Map 数据时未注册 Value 对应加载器: {0}", paramTypes.get(1));
            return map;
        }

        final ConfigurationSection mapSection = section.getConfigurationSection(path);
        if (Objects.isNull(mapSection)) {
            Autoloader.log("加载 Map 数据时 Path 无效: {0}", path);
            return map;
        }

        final List<Class<?>> cacheTypes = new ArrayList<>();
        for (String key : mapSection.getKeys(false)) {
            if (mapSection.isConfigurationSection(key)) {
                Autoloader.log("加载 Map 数据时遇到数据节: {0}", key);
                continue;
            }

            final Object mapKey = keyParser.apply(key);
            final Object mapValue = valueLoader.load(key, mapSection, cacheTypes);
            if (Objects.isNull(mapKey)) {
                Autoloader.log("加载 Map 数据时 Key 值为 null: {0}", key);
                continue;
            }
            if (Objects.isNull(mapValue)) {
                Autoloader.log("加载 Map 数据时 Value 值为 null: {0}", key);
                continue;
            }

            map.put(mapKey, mapValue);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void save(@NotNull String path, @NotNull ConfigurationSection to, Object value,
                     @NotNull List<Class<?>> paramTypes) {
        if (Objects.isNull(value)) {
            to.set(path, null);
            return;
        }
        final Map<Object, Object> map = (Map<Object, Object>) value;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            to.set(String.valueOf(entry.getKey()), entry.getValue());
        }
    }
}
