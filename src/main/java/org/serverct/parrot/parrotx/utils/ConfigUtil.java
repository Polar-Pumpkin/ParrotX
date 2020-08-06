package org.serverct.parrot.parrotx.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigUtil {

    public static Map<String, Object> getMap(FileConfiguration data, String path) {
        Map<String, Object> result = new HashMap<>();
        ConfigurationSection section = data.getConfigurationSection(path);
        if (section == null) return result;
        for (String key : section.getKeys(false)) result.put(key, section.get(key));
        return result;
    }

    public static Map<String, Object> getMap(ConfigurationSection section, String path) {
        Map<String, Object> result = new HashMap<>();
        ConfigurationSection targetSection = section.getConfigurationSection(path);
        if (targetSection == null) return result;
        for (String key : targetSection.getKeys(false)) result.put(key, targetSection.get(key));
        return result;
    }
}
