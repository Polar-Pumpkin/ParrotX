package org.serverct.parrot.parrotx.data.autoload;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DataLoader<T> {

    @NotNull
    Class<T> getType();

    @Nullable
    T load(@NotNull final String path, @NotNull final ConfigurationSection section,
           @NotNull final List<Class<?>> paramTypes);

    void save(@NotNull final String path, @NotNull final ConfigurationSection to,
              final Object value, @NotNull final List<Class<?>> paramTypes);

}
