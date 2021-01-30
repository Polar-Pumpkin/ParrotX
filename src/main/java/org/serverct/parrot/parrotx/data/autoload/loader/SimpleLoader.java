package org.serverct.parrot.parrotx.data.autoload.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.DataLoader;

import java.util.List;
import java.util.function.BiFunction;

public class SimpleLoader<T> implements DataLoader<T> {

    private final Class<T> type;
    private final BiFunction<ConfigurationSection, String, T> getter;

    public SimpleLoader(@NotNull Class<T> type, @NotNull BiFunction<ConfigurationSection, String, T> getter) {
        this.type = type;
        this.getter = getter;
    }

    @Override
    public @NotNull Class<T> getType() {
        return this.type;
    }

    @Override
    public @Nullable T load(@NotNull String path, @NotNull ConfigurationSection section,
                            @NotNull List<Class<?>> classChain) {
        return getter.apply(section, path);
    }

    @Override
    public void save(@NotNull String path, @NotNull ConfigurationSection to, Object value,
                     @NotNull List<Class<?>> classChain) {
        to.set(path, value);
    }
}
