package org.serverct.parrot.parrotx.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@SuppressWarnings({"unused"})
public class ConfigUtil {

    public static Map<String, String> getStringMap(ConfigurationSection section) {
        return getCustomMap(section, ConfigurationSection::getString);
    }

    public static <V> Map<String, V> getCustomMap(ConfigurationSection section,
                                                  BiFunction<ConfigurationSection, String, V> getter) {
        final Map<String, V> result = new HashMap<>();
        if (Objects.isNull(section)) {
            return result;
        }
        section.getKeys(false).forEach(key -> {
            final V value = getter.apply(section, key);
            if (Objects.nonNull(value)) {
                result.put(key, value);
            }
        });
        return result;
    }
}
