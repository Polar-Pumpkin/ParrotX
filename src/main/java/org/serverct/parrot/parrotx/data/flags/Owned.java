package org.serverct.parrot.parrotx.data.flags;

import org.bukkit.Bukkit;

import java.util.UUID;

public interface Owned {
    UUID getOwner();

    void setOwner(UUID uuid);

    default boolean isOwner(UUID uuid) {
        return getOwner().equals(uuid);
    }

    @SuppressWarnings("deprecation")
    default boolean isOwner(String name) {
        return getOwner().equals(Bukkit.getOfflinePlayer(name).getUniqueId());
    }

    default String getOwnerName() {
        return Bukkit.getOfflinePlayer(getOwner()).getName();
    }
}
