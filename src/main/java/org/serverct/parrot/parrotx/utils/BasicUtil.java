package org.serverct.parrot.parrotx.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.ParrotX;
import org.serverct.parrot.parrotx.utils.i18n.I18n;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unused"})
public class BasicUtil {

    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.0%");

    public static double roundToDouble(final double number) {
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }

    public static int roundToInt(final double number) {
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.HALF_DOWN).intValue();
    }

    public static long roundToLong(final double number) {
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.HALF_DOWN).longValueExact();
    }

    public static String roundToPercent(final double number) {
        final String prefix = number <= 0.2D ? "&c" : number < 0.8D ? "&e" : number < 1.0D ? "&a" : "&6";
        return I18n.color(prefix + PERCENT_FORMAT.format(number));
    }

    public static void potion(Player target, PotionEffectType type, int level, int duration) {
        target.addPotionEffect(new PotionEffect(type, duration * 20, level));
    }

    public static double calculate(String expression, int xValue) {
        final Map<String, Double> variable = new HashMap<>();
        variable.put("x", (double) xValue);
        return calculate(expression, variable);
    }

    public static double calculate(final String expression, final Map<String, Double> variable) {
        try {
            final Scope scope = new Scope();
            variable.forEach((symbol, value) -> {
                final Variable var = scope.getVariable(symbol);
                var.setValue(value);
            });
            final Expression expr = Parser.parse(expression, scope);
            return expr.evaluate();
        } catch (Throwable e) {
            ParrotX.log("计算计算数学表达式({0}, 变量: {1}) 时遇到错误: {2}.", expression, variable, e.getMessage());
        }
        return 0;
    }

    public static String arabic2Roman(int number) {
        StringBuilder rNumber = new StringBuilder();
        int[] aArray = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] rArray = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X",
                "IX", "V", "IV", "I"};
        if (number < 1 || number > 3999) {
            rNumber = new StringBuilder("-1");
        } else {
            for (int i = 0; i < aArray.length; i++) {
                while (number >= aArray[i]) {
                    rNumber.append(rArray[i]);
                    number -= aArray[i];
                }
            }
        }
        return rNumber.toString();
    }

    public static int roman2Arabic(String m) {
        int[] graph = new int[400];
        graph['I'] = 1;
        graph['V'] = 5;
        graph['X'] = 10;
        graph['L'] = 50;
        graph['C'] = 100;
        graph['D'] = 500;
        graph['M'] = 1000;
        char[] num = m.toCharArray();
        int sum = graph[num[0]];
        for (int i = 0; i < num.length - 1; i++) {
            if (graph[num[i]] >= graph[num[i + 1]]) {
                sum += graph[num[i + 1]];
            } else {
                sum = sum + graph[num[i + 1]] - 2 * graph[num[i]];
            }
        }
        return sum;
    }

    public static String getSimpleNumber(int number) {
        StringBuilder result = new StringBuilder();
        for (char character : String.valueOf(number).toCharArray())
            result.append(getSimpleNumberBelowTen(Integer.parseInt(String.valueOf(character))));
        return result.toString();
    }

    private static String getSimpleNumberBelowTen(int number) {
        if (number > 10) return String.valueOf(number);
        switch (number) {
            case 0:
                return "零";
            case 1:
                return "一";
            case 2:
                return "二";
            case 3:
                return "三";
            case 4:
                return "四";
            case 5:
                return "五";
            case 6:
                return "六";
            case 7:
                return "七";
            case 8:
                return "八";
            case 9:
                return "九";
            case 10:
                return "十";
            default:
                return String.valueOf(number);
        }
    }

    @Contract("_, null, _, _, _ -> true; _, !null, _, _, _ -> false")
    public static boolean isNull(@NotNull final PPlugin plugin,
                                 @Nullable final Object object,
                                 @NotNull final String action,
                                 @NotNull final String name,
                                 @NotNull final String message) {
        if (Objects.isNull(object)) {
            plugin.getLang().log.error(action, name, message);
            return true;
        }
        return false;
    }

    @Contract("null, _ -> true; !null, _ -> false")
    public static boolean isNull(@Nullable final Object object,
                                 @Nullable final Runnable ifNull) {
        if (Objects.isNull(object)) {
            BasicUtil.canDo(ifNull, Runnable::run);
            return true;
        }
        return false;
    }

    public static boolean multiNull(@Nullable final Object... args) {
        for (Object object : args) {
            if (Objects.isNull(object)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static <T> T orElse(@Nullable final T value, @NotNull final T other) {
        return thisOrElse(value, other);
    }

    @NotNull
    public static <T> T thisOrElse(@Nullable final T value, @NotNull final T other) {
        return Optional.ofNullable(value).orElse(other);
    }

    @Nullable
    public static <T, R> R canReturn(@Nullable final T object,
                                     @NotNull final Function<T, R> callback) {
        if (Objects.isNull(object)) {
            return null;
        }
        return callback.apply(object);
    }

    public static <T> void canDo(@Nullable final T object,
                                 @Nullable final Consumer<T> callback) {
        if (Objects.isNull(object)) {
            return;
        }
        if (Objects.isNull(callback)) {
            return;
        }
        callback.accept(object);
    }

    @NotNull
    public static Optional<String> getUsername(@Nullable final UUID uuid) {
        if (Objects.isNull(uuid)) {
            return Optional.empty();
        }
        return Optional.ofNullable(Bukkit.getOfflinePlayer(uuid).getName());
    }

    @NotNull
    public static String getUsername(@Nullable final UUID uuid, @NotNull final String def) {
        return getUsername(uuid).orElse(def);
    }
}
