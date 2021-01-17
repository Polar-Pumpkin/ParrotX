package org.serverct.parrot.parrotx.utils;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionUtil {

    private static final Map<UUID, Location> pos1Map = new HashMap<>();
    private static final Map<UUID, Location> pos2Map = new HashMap<>();

    public static void setPos1(final UUID id, final Location location) {
        pos1Map.put(id, location);
    }

    public static Location getPos1(final UUID uuid) {
        return pos1Map.get(uuid);
    }

    public static boolean hasPos1(final UUID uuid) {
        return pos1Map.containsKey(uuid);
    }

    public static void setPos2(final UUID id, final Location location) {
        pos2Map.put(id, location);
    }

    public static Location getPos2(final UUID uuid) {
        return pos2Map.get(uuid);
    }

    public static boolean hasPos2(final UUID uuid) {
        return pos2Map.containsKey(uuid);
    }

}
