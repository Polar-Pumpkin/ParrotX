package org.serverct.parrot.parrotx.flags;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;

public interface Located {
    Location getLocation();

    default boolean isLocation(@NonNull Location location) {
        Location core = getLocation();
        if (core != null) {
            World world = core.getWorld();
            World targetWorld = location.getWorld();
            if (world != null && targetWorld != null) {
                if (world.getName().equals(targetWorld.getName())) {
                    boolean x = core.getBlockX() == location.getBlockX();
                    boolean y = core.getBlockY() == location.getBlockY();
                    boolean z = core.getBlockZ() == location.getBlockZ();
                    return x && y && z;
                }
            }
        }
        return false;
    }

    boolean setLocation(@NonNull Location location);
}
