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
    default T get(final String id) {
        return get(buildId(id));
    }

    @Nullable
    T get(final PID id);

    boolean has(final PID id);

    void put(final T data);

    void reload(final PID id);

    void delete(final PID id);

    void save(final PID id);

    void reloadAll();

    void deleteAll();

    void saveAll();

    PID buildId(final String id);

    String objectName(final PID id);
}
