package org.serverct.parrot.parrotx.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class SelectionUtil {

    private static final Map<UUID, Location> pos1Map = new HashMap<>();
    private static final Map<UUID, Location> pos2Map = new HashMap<>();

    public static void setPos1(final Player user) {
        final Location loc = user.getLocation();
        pos1Map.put(user.getUniqueId(), loc);
    }

    public static Location getPos1(final Player user) {
        return pos1Map.get(user.getUniqueId());
    }

    public static boolean hasPos1(final Player user) {
        return Objects.nonNull(getPos1(user));
    }

    public static void setPos2(final Player user) {
        final Location loc = user.getLocation();
        pos2Map.put(user.getUniqueId(), loc);
    }

    public static Location getPos2(final Player user) {
        return pos2Map.get(user.getUniqueId());
    }

    public static boolean hasPos2(final Player user) {
        return Objects.nonNull(getPos2(user));
    }

}
