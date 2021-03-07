package org.serverct.parrot.parrotx.data.autoload;

import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;

public interface AutoRegister {

    @NotNull
    Class<?> getType();

    boolean shouldRegister(final Class<?> clazz);

    void register(final PPlugin plugin, final Class<?> clazz, final Object instance);

    int getPriority();

}
