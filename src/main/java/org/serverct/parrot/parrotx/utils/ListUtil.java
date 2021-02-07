package org.serverct.parrot.parrotx.utils;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        final String prefix = template.substring(0, template.indexOf(keyword.replace("[", "\\[").replace("(", "\\(")));

        if (Objects.isNull(contents) || contents.isEmpty()) {
            result.set(index, prefix + BasicUtil.thisOrElse(nonContent, "无"));
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

}
