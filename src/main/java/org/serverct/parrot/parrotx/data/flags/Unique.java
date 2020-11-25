package org.serverct.parrot.parrotx.data.flags;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.PID;

public interface Unique extends Keyed {
    PID getID();

    default boolean check(String id) {
        return getID().getId().equals(id);
    }

    @Override
    default @NotNull NamespacedKey getKey() {
        return getID().key();
    }
}
