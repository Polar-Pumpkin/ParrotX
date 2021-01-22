package org.serverct.parrot.parrotx.data.autoload.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.autoload.DataLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@SuppressWarnings("rawtypes")
public class ListLoader implements DataLoader<List> {

    private static final Map<Class<?>, BiFunction<ConfigurationSection, String, List>> GETTER_MAP = new HashMap<>();

    static {
        registerLoader(Byte.class, ConfigurationSection::getByteList);
        registerLoader(Character.class, ConfigurationSection::getCharacterList);
        registerLoader(String.class, ConfigurationSection::getStringList);
        registerLoader(Short.class, ConfigurationSection::getShortList);
        registerLoader(Integer.class, ConfigurationSection::getIntegerList);
        registerLoader(Long.class, ConfigurationSection::getLongList);
        registerLoader(Float.class, ConfigurationSection::getFloatList);
        registerLoader(Double.class, ConfigurationSection::getDoubleList);
        registerLoader(Boolean.class, ConfigurationSection::getBooleanList);
        registerLoader(Map.class, ConfigurationSection::getMapList);
    }

    public static void registerLoader(@NotNull final Class<?> type,
                                      @NotNull final BiFunction<ConfigurationSection, String, List> getter) {
        GETTER_MAP.put(type, getter);
    }

    @Override
    public @NotNull Class<List> getType() {
        return List.class;
    }

    @Override
    public @Nullable List load(@NotNull String path, @NotNull ConfigurationSection section,
                               @NotNull List<Class<?>> paramTypes) {
        if (paramTypes.isEmpty()) {
            Autoloader.log("加载 List 数据时未提供泛型类型, 路径: {0}, 数据节: {1}", path, section.getName());
            return new ArrayList();
        }
        final Class<?> element = paramTypes.get(0);
        if (!GETTER_MAP.containsKey(element)) {
            Autoloader.log("加载 List 数据时未注册对应加载器: {0}", element);
            return section.getList(path, new ArrayList<>());
        }
        return GETTER_MAP.get(element).apply(section, path);
    }

    @Override
    public void save(@NotNull String path, @NotNull ConfigurationSection to, Object value,
                     @NotNull List<Class<?>> paramTypes) {
        to.set(path, value);
    }
}
