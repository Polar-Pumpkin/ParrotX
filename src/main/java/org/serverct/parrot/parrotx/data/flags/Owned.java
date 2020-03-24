package org.serverct.parrot.parrotx.data.flags;

import org.bukkit.Bukkit;

import java.util.UUID;

public interface Owned {
    UUID getOwner();

    void setOwner(UUID uuid);

    default boolean isOwner(UUID uuid) {
        return getOwner().equals(uuid);
    }

    default boolean isOwner(String uuid) {
        return getOwner().equals(Bukkit.getOfflinePlayer(uuid).getUniqueId());
    }
}
