package org.serverct.parrot.parrotx.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.serverct.parrot.parrotx.PPlugin;

public class ConfigUtil {

    public static Object getOrDefault(PPlugin plugin, FileConfiguration data, String path, Object defaultValue) {
        try {
            return data.get(path);
        } catch (Throwable e) {
            plugin.getLang().log(
                    "尝试获取值时遇到错误(&c" + e.toString() + "&7), 配置文件: &c" + data + " &7, 路径: &c" + path + " &7, 默认值: &c" + defaultValue + "&7.",
                    LocaleUtil.Type.WARN,
                    false);
            return defaultValue;
        }
    }

    public static Object getOrDefault(PPlugin plugin, ConfigurationSection section, String path, Object defaultValue) {
        try {
            return section.get(path);
        } catch (Throwable e) {
            plugin.getLang().log(
                    "尝试获取值时遇到错误(&c" + e.toString() + "&7), 配置文件: &c" + section + " &7, 路径: &c" + path + " &7, 默认值: &c" + defaultValue + "&7.",
                    LocaleUtil.Type.WARN,
                    false);
            return defaultValue;
        }
    }

    public static double getOrDefault(PPlugin plugin, ConfigurationSection section, String path, double defaultValue) {
        try {
            return section.getDouble(path);
        } catch (Throwable e) {
            plugin.getLang().log(
                    "尝试获取值时遇到错误(&c" + e.toString() + "&7), 配置文件: &c" + section + " &7, 路径: &c" + path + " &7, 默认值: &c" + defaultValue + "&7.",
                    LocaleUtil.Type.WARN,
                    false);
            return defaultValue;
        }
    }

}
