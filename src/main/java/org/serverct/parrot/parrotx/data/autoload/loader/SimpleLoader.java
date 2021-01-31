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
    private Setter<ConfigurationSection, String, T> setter = ConfigurationSection::set;

    public SimpleLoader(@NotNull Class<T> type, @NotNull BiFunction<ConfigurationSection, String, T> getter) {
        this.type = type;
        this.getter = getter;
    }

    public SimpleLoader(Class<T> type,
                        BiFunction<ConfigurationSection, String, T> getter,
                        Setter<ConfigurationSection, String, T> setter) {
        this.type = type;
        this.getter = getter;
        this.setter = setter;
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

    @SuppressWarnings("unchecked")
    @Override
    public void save(@NotNull String path, @NotNull ConfigurationSection to, Object value,
                     @NotNull List<Class<?>> classChain) {
        this.setter.accept(to, path, (T) value);
    }

    public SimpleLoader<T> setter(final @NotNull Setter<ConfigurationSection, String, T> setter) {
        this.setter = setter;
        return this;
    }

    @FunctionalInterface
    public interface Setter<T, U, V> {
        void accept(T t, U u, V v);
    }
}
