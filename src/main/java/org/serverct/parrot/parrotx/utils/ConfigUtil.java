package org.serverct.parrot.parrotx.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unused"})
public class ConfigUtil {

    public static Map<String, Object> getMap(ConfigurationSection section, String path) {
        Map<String, Object> result = new HashMap<>();
        ConfigurationSection targetSection = section.getConfigurationSection(path);
        if (targetSection == null) return result;
        for (String key : targetSection.getKeys(false)) result.put(key, targetSection.get(key));
        return result;
    }

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
            plugin.getLang().log.error(I18n.SAVE, "Location", e, null);
            return null;
        }
    }
}
