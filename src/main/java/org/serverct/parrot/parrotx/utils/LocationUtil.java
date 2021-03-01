package org.serverct.parrot.parrotx.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class LocationUtil {

    public static Optional<World> checkWorld(@Nullable final Location loc1, @Nullable final Location loc2) {
        if (Objects.isNull(loc1) || Objects.isNull(loc2)) {
            return Optional.empty();
        }

        final World world1 = loc1.getWorld();
        final World world2 = loc2.getWorld();
        if (Objects.isNull(world1) || Objects.isNull(world2)) {
            return Optional.empty();
        }

        if (world1.equals(world2)) {
            return Optional.of(world1);
        }
        return Optional.empty();
    }

    public static double distance(@Nullable final Location loc1, @Nullable final Location loc2) {
        if (Objects.isNull(loc1) || Objects.isNull(loc2)) {
            return -1.0D;
        }

        final Optional<World> world = checkWorld(loc1, loc2);
        if (!world.isPresent()) {
            return Double.MAX_VALUE;
        }

        return loc1.distance(loc2);
    }

    public static void saveLocation(ConfigurationSection section, Location loc) {
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

    public static Location getLocation(ConfigurationSection section) {
        String worldName;
        double x;
        double y;
        double z;

        worldName = section.getString("World", "world");
        World world = Bukkit.getWorld(Objects.requireNonNull(worldName));
        x = section.getDouble("X");
        y = section.getDouble("Y");
        z = section.getDouble("Z");

        return new Location(world, x, y, z);
    }

}
