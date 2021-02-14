package org.serverct.parrot.parrotx.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapUtil {

    @NotNull
    public static <T> Map<T, Object> filter(@Nullable final Map<?, ?> map, @NotNull final Class<T> clazz) {
        final Map<T, Object> result = new HashMap<>();
        if (Objects.isNull(map)) {
            return result;
        }
        map.forEach((key, value) -> {
            if (clazz.isInstance(key)) {
                result.put(clazz.cast(key), value);
            }
        });
        return result;
    }

    @NotNull
    public static <K, V> Map<K, V> filter(@Nullable final Map<K, V> map, @Nullable Predicate<Map.Entry<K, V>> checker) {
        final Map<K, V> result = new HashMap<>();
        if (Objects.isNull(map)) {
            return result;
        }
        if (Objects.isNull(checker)) {
            return map;
        }
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            if (checker.test(entry)) {
                mergeEntry(result, entry);
            }
        }
        return result;
    }

    public static <K, V> void mergeEntry(@Nullable final Map<K, V> map, @Nullable final Map.Entry<K, V> entry) {
        if (Objects.isNull(map) || Objects.isNull(entry)) {
            return;
        }
        map.put(entry.getKey(), entry.getValue());
    }

    @NotNull
    public static <T, K, V> Map<T, V> transformKey(@NotNull final Map<K, V> map,
                                                   @NotNull final Function<K, T> constructor) {
        final Map<T, V> result = new HashMap<>();
        map.forEach((key, value) -> result.put(constructor.apply(key), value));
        return result;
    }

    @NotNull
    public static <T, K, V> Map<K, T> transformValue(@NotNull final Map<K, V> map,
                                                     @NotNull final Function<V, T> constructor) {
        final Map<K, T> result = new HashMap<>();
        map.forEach((key, value) -> result.put(key, constructor.apply(value)));
        return result;
    }

    @NotNull
    public static <K1, K2, V1, V2> Map<K2, V2> transform(@NotNull final Map<K1, V1> map,
                                                         @NotNull final Function<K1, K2> keyConverter,
                                                         @NotNull final Function<V1, V2> valueConverter) {
        final Map<K2, V2> result = new HashMap<>();
        map.forEach((key, value) -> result.put(keyConverter.apply(key), valueConverter.apply(value)));
        return result;
    }

}
