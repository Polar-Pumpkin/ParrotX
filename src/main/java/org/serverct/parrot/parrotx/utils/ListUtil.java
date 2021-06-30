package org.serverct.parrot.parrotx.utils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListUtil {

    @NotNull
    public static <E> List<E> filter(@NotNull final List<E> list, @NotNull final Predicate<E> checker) {
        return list.stream().filter(checker).collect(Collectors.toList());
    }

    public static <E> int indexOf(@NotNull final List<E> list, @NotNull final Predicate<E> checker) {
        for (E content : list) {
            if (checker.test(content)) {
                return list.indexOf(content);
            }
        }
        return -1;
    }

    public static int indexOf(@NotNull final List<String> list, @NotNull final String target) {
        return indexOf(list, string -> string.contains(target));
    }

    public static <E> boolean contains(@NotNull final List<E> list, @NotNull final Predicate<E> checker) {
        return indexOf(list, checker) != -1;
    }

    public static boolean contains(@NotNull final List<String> list, @NotNull final String target) {
        return indexOf(list, target) != -1;
    }

    @NotNull
    public static List<String> insert(@Nullable final List<String> list, @Nullable final List<String> contents,
                                      @Nullable final String keyword, @Nullable final String nonContent) {
        final List<String> result = new ArrayList<>();
        if (Objects.isNull(list) || list.isEmpty()) {
            return result;
        }
        result.addAll(list);

        if (StringUtils.isEmpty(keyword)) {
            return list;
        }

        final int index = indexOf(list, keyword);
        if (index == -1) {
            return list;
        }
        final String template = list.get(index);
        final String prefix = template.substring(0, template.indexOf(keyword));

        if (Objects.isNull(contents) || contents.isEmpty()) {
            if (StringUtils.isEmpty(nonContent)) {
                list.remove(index);
            } else {
                result.set(index, prefix + nonContent);
            }
        } else {
            int current = index;
            for (String content : contents) {
                final String build = prefix + content;
                if (current == index) {
                    result.set(current, build);
                } else {
                    result.add(current, build);
                }
                current++;
            }
        }

        result.replaceAll(I18n::color);
        return result;
    }

    @Contract("null -> null")
    @Nullable
    public static <E> E random(@Nullable final List<E> list) {
        if (Objects.isNull(list) || list.isEmpty()) {
            return null;
        }
        final Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    @NotNull
    public static <E extends Comparable<E>> List<E> sort(@Nullable final Collection<E> collections,
                                                         @Nullable final Comparator<E> comparator,
                                                         final boolean reverse) {
        final List<E> result = new ArrayList<>();
        if (Objects.isNull(collections) || collections.isEmpty()) {
            return result;
        }

        if (Objects.isNull(comparator)) {
            result.addAll(collections);
            return result;
        }

        result.addAll(collections.stream().sorted(comparator).collect(Collectors.toList()));

        if (reverse) {
            Collections.reverse(result);
        }
        return result;
    }

    @NotNull
    public static <E extends Comparable<E>> List<E> sort(@Nullable final Collection<E> collections,
                                                         @Nullable final Comparator<E> comparator) {
        return sort(collections, comparator, false);
    }

}
