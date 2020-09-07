package org.serverct.parrot.parrotx.data.flags;

import lombok.NonNull;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.PID;

public interface Unique extends Keyed {
    PID getID();

    void setID(@NonNull PID pid);

    default boolean check(String id) {
        return getID().getId().equals(id);
    }

    @Override
    default @NotNull NamespacedKey getKey() {
        return getID().key();
    }
}
