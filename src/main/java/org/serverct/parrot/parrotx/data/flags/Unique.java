package org.serverct.parrot.parrotx.data.flags;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.PID;

public interface Unique {
    PID getID();

    default boolean check(String id) {
        return getID().getId().equals(id);
    }

    default @NotNull NamespacedKey getKey() {
        return getID().key();
    }
}
