package org.serverct.parrot.parrotx.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@SuppressWarnings({"unused"})
public class ConfigUtil {

    public static void saveLocation(Location loc, ConfigurationSection section) {
        if (loc.getWorld() == null) {
            return;
        }
        String worldName = loc.getWorld().getName();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        section.set("World", worldName);
        section.set("X", x);
        section.set("Y", y);
        section.set("Z", z);
    }

    public static Location getLocation(PPlugin plugin, ConfigurationSection section) {
        String worldName;
        double x;
        double y;
        double z;

        worldName = section.getString("World", "world");
        World world = plugin.getServer().getWorld(Objects.requireNonNull(worldName));
        x = section.getDouble("X");
        y = section.getDouble("Y");
        z = section.getDouble("Z");

        try {
            return new Location(world, x, y, z);
        } catch (Throwable e) {
            plugin.getLang().log.error(I18n.SAVE, "Location", e, plugin.getPackageName());
            return null;
        }
    }

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
