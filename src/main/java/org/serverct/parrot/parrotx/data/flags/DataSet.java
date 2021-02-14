package org.serverct.parrot.parrotx.data.flags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.PID;

import java.util.Collection;
import java.util.Set;

public interface DataSet<T> {

    @NotNull
    Collection<T> getAll();

    @NotNull
    Set<PID> getIds();

    @Nullable
    default T get(@NotNull final String id) {
        return get(buildId(id));
    }

    @Nullable
    T get(@NotNull final PID id);

    default boolean has(@NotNull final String id) {
        return has(buildId(id));
    }

    boolean has(@NotNull final PID id);

    void put(@Nullable final T data);

    void reload(@NotNull final PID id);

    void delete(@NotNull final PID id);

    void save(@NotNull final PID id);

    void reloadAll();

    void deleteAll();

    void saveAll();

    @NotNull
    PID buildId(@NotNull final String id);

    @NotNull
    String objectName(@NotNull final PID id);
}
