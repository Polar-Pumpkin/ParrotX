package org.serverct.parrot.parrotx.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtil {

    @NotNull
    public static <T> List<T> filter(@NotNull final List<T> list, @NotNull final Predicate<T> checker) {
        return list.stream().filter(checker).collect(Collectors.toList());
    }

    public static <T> int indexOf(@NotNull final List<T> list, @NotNull final Predicate<T> checker) {
        for (T content : list) {
            if (checker.test(content)) {
                return list.indexOf(content);
            }
        }
        return -1;
    }

    public static int indexOf(@NotNull final List<String> list, @NotNull final String target) {
        return indexOf(list, string -> string.contains(target));
    }

    public static <T> boolean contains(@NotNull final List<T> list, @NotNull final Predicate<T> checker) {
        return indexOf(list, checker) != -1;
    }

    public static boolean contains(@NotNull final List<String> list, @NotNull final String target) {
        return indexOf(list, target) != -1;
    }

}
