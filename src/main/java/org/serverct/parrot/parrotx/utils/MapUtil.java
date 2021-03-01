package org.serverct.parrot.parrotx.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

    @Contract("null -> null")
    @Nullable
    public static <K, V> Map.Entry<K, V> random(@Nullable final Map<K, V> map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return null;
        }
        return ListUtil.random(new ArrayList<>(map.entrySet()));
    }

    @NotNull
    public static <K, V> Map<K, V> sort(@Nullable final Map<K, V> map,
                                        @Nullable final Comparator<Map.Entry<K, V>> comparator,
                                        final boolean reverse) {
        final Map<K, V> result = new LinkedHashMap<>();
        if (Objects.isNull(map)) {
            return result;
        }
        if (Objects.isNull(comparator)) {
            return map;
        }

        final List<Map.Entry<K, V>> snapshot = new ArrayList<>(map.entrySet());
        snapshot.sort(comparator);
        if (reverse) {
            Collections.reverse(snapshot);
        }

        snapshot.forEach(entry -> mergeEntry(result, entry));
        return result;
    }

    @NotNull
    public static <K2, K1, V> Map<K2, V> transformKey(@NotNull final Map<K1, V> map,
                                                      @NotNull final Function<K1, K2> constructor) {
        final Map<K2, V> result = new HashMap<>();
        map.forEach((key, value) -> result.put(constructor.apply(key), value));
        return result;
    }

    @NotNull
    public static <V2, K, V1> Map<K, V2> transformValue(@NotNull final Map<K, V1> map,
                                                        @NotNull final Function<V1, V2> constructor) {
        final Map<K, V2> result = new HashMap<>();
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
