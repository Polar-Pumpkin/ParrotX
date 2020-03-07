package org.serverct.parrot.parrotx.utils;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.PPlugin;

import java.util.*;

public class LocationUtil {

    public static final int RADIUS = 3;
    public static final Vector3D[] VOLUME;
    private static final Set<Material> WATER_TYPES = EnumUtil.getAllMatching(Material.class, "WATER", "FLOWING_WATER");
    private static final Set<Material> HOLLOW_MATERIALS = new HashSet<>();
    private static final Set<Material> TRANSPARENT_MATERIALS = new HashSet<>();

    static {
        for (Material mat : Material.values()) {
            if (mat.isTransparent()) {
                HOLLOW_MATERIALS.add(mat);
            }
        }
        TRANSPARENT_MATERIALS.addAll(HOLLOW_MATERIALS);
        TRANSPARENT_MATERIALS.addAll(WATER_TYPES);
        List<Vector3D> pos = new ArrayList<>();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++)
                    pos.add(new Vector3D(x, y, z));
            }
        }
        pos.sort(Comparator.comparingInt(a -> a.x * a.x + a.y * a.y + a.z * a.z));
        VOLUME = pos.toArray(new Vector3D[0]);
    }

    public static void setIsWaterSafe(boolean isWaterSafe) {
        if (isWaterSafe) {
            HOLLOW_MATERIALS.addAll(WATER_TYPES);
        } else {
            HOLLOW_MATERIALS.removeAll(WATER_TYPES);
        }
    }

    public static ItemStack convertBlockToItem(Block block) {
        return new ItemStack(block.getType(), 1);
    }

    public static Location getTarget(LivingEntity entity) throws Exception {
        Block block = null;
        try {
            block = entity.getTargetBlock(TRANSPARENT_MATERIALS, 300);
        } catch (NoSuchMethodError ignored) {}
        if (block == null) {
            throw new Exception("Not targeting a block");
        }
        return block.getLocation();
    }

    public static boolean isBlockAboveAir(World world, int x, int y, int z) {
        return (y > world.getMaxHeight() || HOLLOW_MATERIALS.contains(world.getBlockAt(x, y - 1, z).getType()));
    }

    public static boolean isBlockUnsafeForUser(Player user, World world, int x, int y, int z) {
        if (user.isOnline() && world.equals(user.getWorld()) && (user.getGameMode() == GameMode.CREATIVE || user.getGameMode() == GameMode.SPECTATOR) && user.getAllowFlight()) {
            return false;
        }
        if (isBlockDamaging(world, x, y, z)) {
            return true;
        }
        return isBlockAboveAir(world, x, y, z);
    }

    public static boolean isBlockUnsafe(World world, int x, int y, int z) {
        return (isBlockDamaging(world, x, y, z) || isBlockAboveAir(world, x, y, z));
    }

    public static boolean isBlockDamaging(World world, int x, int y, int z) {
        Block below = world.getBlockAt(x, y - 1, z);
        switch (below.getType()) {
            case LAVA:
            case FIRE:
                return true;
        }
        if (MaterialUtil.isBed(below.getType())) {
            return true;
        }
        try {
            if (below.getType() == Material.valueOf("FLOWING_LAVA")) {
                return true;
            }
        } catch (Exception ignored) {}
        Material PORTAL = EnumUtil.getMaterial("NETHER_PORTAL", "PORTAL");
        if (world.getBlockAt(x, y, z).getType() == PORTAL) {
            return true;
        }
        return (!HOLLOW_MATERIALS.contains(world.getBlockAt(x, y, z).getType()) || !HOLLOW_MATERIALS.contains(world.getBlockAt(x, y + 1, z).getType()));
    }

    public static Location getRoundedDestination(Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        int z = loc.getBlockZ();
        return new Location(world, x + 0.5D, y, z + 0.5D, loc.getYaw(), loc.getPitch());
    }

    public static Location getSafeDestination(Player user, Location loc) throws Exception {
        if (user.isOnline() && loc.getWorld().equals(user.getWorld()) && (user.getGameMode() == GameMode.CREATIVE) && user.getAllowFlight()) {
            if (shouldFly(loc)) {
                user.setFlying(true);
            }
            return getRoundedDestination(loc);
        }
        return getSafeDestination(loc);
    }

    public static Location getSafeDestination(Location loc) throws Exception {
        if (loc == null || loc.getWorld() == null) {
            throw new Exception("destinationNotSet");
        }
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        int z = loc.getBlockZ();
        int origX = x;
        int origY = y;
        int origZ = z;
        while (isBlockAboveAir(world, x, y, z)) {
            y--;
            if (y < 0) {
                y = origY;
                break;
            }
        }
        if (isBlockUnsafe(world, x, y, z)) {
            x = (Math.round(loc.getX()) == origX) ? (x - 1) : (x + 1);
            z = (Math.round(loc.getZ()) == origZ) ? (z - 1) : (z + 1);
        }
        int i = 0;
        while (isBlockUnsafe(world, x, y, z)) {
            i++;
            if (i >= VOLUME.length) {
                x = origX;
                y = origY + 3;
                z = origZ;
                break;
            }
            x = origX + (VOLUME[i]).x;
            y = origY + (VOLUME[i]).y;
            z = origZ + (VOLUME[i]).z;
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y++;
            if (y >= world.getMaxHeight()) {
                x++;
                break;
            }
        }
        while (isBlockUnsafe(world, x, y, z)) {
            y--;
            if (y <= 1) {
                x++;
                y = world.getHighestBlockYAt(x, z);
                if (x - 48 > loc.getBlockX()) {
                    throw new Exception("holeInFloor");
                }
            }
        }
        return new Location(world, x + 0.5D, y, z + 0.5D, loc.getYaw(), loc.getPitch());
    }

    public static boolean shouldFly(Location loc) {
        World world = loc.getWorld();
        int x = loc.getBlockX();
        int y = (int) Math.round(loc.getY());
        int z = loc.getBlockZ();
        int count = 0;

        while (isBlockUnsafe(world, x, y, z) && y > -1) {
            y--;
            count++;
            if (count > 2) {
                return true;
            }
        }

        return (y < 0);
    }

    public static ConfigurationSection save(Location loc, ConfigurationSection section) {
        String worldName = loc.getWorld().getName();
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        section.set("World", worldName);
        section.set("X", x);
        section.set("Y", y);
        section.set("Z", z);

        return section;
    }

    public static Location get(PPlugin plugin, ConfigurationSection section) {
        String worldName;
        double x;
        double y;
        double z;

        worldName = section.getString("World");
        World world = plugin.getServer().getWorld(worldName);
        x = section.getDouble("X");
        y = section.getDouble("Y");
        z = section.getDouble("Z");

        try {
            return new Location(world, x, y, z);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Vector3D {
        public int x;
        public int y;
        public int z;

        Vector3D(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
