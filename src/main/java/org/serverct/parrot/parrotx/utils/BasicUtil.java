package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings({"unused"})
public class BasicUtil {

    public static double roundToDouble(final double number) {
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }

    public static int roundToInt(final double number) {
        return BigDecimal.valueOf(number).setScale(2, RoundingMode.HALF_DOWN).intValue();
    }

    public static void potion(Player target, PotionEffectType type, int level, int duration) {
        target.addPotionEffect(new PotionEffect(type, duration * 20, level), true);
    }

    public static File[] listFiles(final File folder, final String suffix) {
        if (Objects.isNull(folder)) {
            return new File[0];
        }
        return folder.listFiles(file -> file.getName().endsWith(suffix));
    }

    public static File[] getYamls(final File folder) {
        return listFiles(folder, ".yml");
    }

    public static String getNoExFileName(String fileName) {
        if ((fileName != null) && (fileName.length() > 0)) {
            int dot = fileName.lastIndexOf('.');
            if ((dot > -1) && (dot < (fileName.length()))) {
                return fileName.substring(0, dot);
            }
        }
        return fileName;
    }

    public static String formatLocation(@NonNull Location location) {
        final World world = location.getWorld();
        String result = "&c" + location.getBlockX() + "&7, &c" + location.getBlockY() + "&7, &c" + location.getBlockZ() + "&7";
        if (Objects.nonNull(world)) {
            result = result + "(&c" + world.getName() + "&7)&r";
        }
        return I18n.color(result);
    }

    public static double calculate(@NonNull PPlugin plugin, String expression, int xValue) {
        final I18n lang = plugin.getLang();
        try {
            final Scope scope = Scope.create();
            final Variable x = scope.getVariable("x");
            final Expression expr = Parser.parse(expression, scope);
            x.setValue(xValue);
            double result = expr.evaluate();

            lang.log.action(I18n.CALCULATE, "数学表达式({0}, x={1}, 值={2})", expression, xValue, result);
            return result;
        } catch (Throwable e) {
            lang.log.error(I18n.CALCULATE, "数学表达式(" + expression + ", x=" + xValue + ")", e, null);
        }
        return 0;
    }

    public static double calculate(@NonNull PPlugin plugin, final String expression, final Map<String, Double> variable) {
        final I18n lang = plugin.getLang();
        try {
            final Scope scope = Scope.create();
            variable.forEach((symbol, value) -> {
                final Variable var = scope.getVariable(symbol);
                var.setValue(value);
            });
            final Expression expr = Parser.parse(expression, scope);
            final double result = expr.evaluate();

            lang.log.action(I18n.CALCULATE, "数学表达式({0} = {1}): {2}", expression, result, variable.toString());
            return result;
        } catch (Throwable e) {
            lang.log.error(I18n.CALCULATE, "数学表达式(" + expression + "): " + variable.toString(), e, null);
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

    public static void openInventory(@NonNull PPlugin plugin, @NonNull Player user, @NonNull Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                user.closeInventory();
                user.openInventory(inventory);
            }
        }.runTask(plugin);
    }

    public static void closeInventory(@NonNull PPlugin plugin, @NonNull Player user) {
        new BukkitRunnable() {
            @Override
            public void run() {
                user.closeInventory();
            }
        }.runTask(plugin);
    }

    public static void broadcast(String msg) {
        Bukkit.getOnlinePlayers().forEach(user -> I18n.send(user, msg));
    }

    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Bukkit.getOnlinePlayers().forEach(user -> user.sendTitle(title, subtitle, fadeIn * 20, stay * 20,
                fadeOut * 20));
    }

    public static <T> Map<T, Object> filter(final Map<?, ?> map, final Class<T> clazz) {
        final Map<T, Object> result = new HashMap<>();
        map.forEach((key, value) -> {
            if (clazz.isInstance(key)) {
                result.put(clazz.cast(key), value);
            }
        });
        return result;
    }

    public static <T, K, V> Map<T, V> transformKey(final Map<K, V> map, final Function<K, T> constructor) {
        final Map<T, V> result = new HashMap<>();
        map.forEach((key, value) -> result.put(constructor.apply(key), value));
        return result;
    }

    public static <T, K, V> Map<K, T> transformValue(final Map<K, V> map, final Function<V, T> constructor) {
        final Map<K, T> result = new HashMap<>();
        map.forEach((key, value) -> result.put(key, constructor.apply(value)));
        return result;
    }

    @Contract("_, null, _, _, _ -> true; _, !null, _, _, _ -> false")
    public static boolean isNull(PPlugin plugin, Object object, String action, String name, String message) {
        if (Objects.isNull(object)) {
            plugin.getLang().log.error(action, name, message);
            return true;
        }
        return false;
    }

    public static boolean multiNull(Object... args) {
        for (Object object : args) {
            if (Objects.isNull(object)) {
                return true;
            }
        }
        return false;
    }

    public static int indexOf(final List<String> list, final String target) {
        for (String content : list) {
            if (content.contains(target)) {
                return list.indexOf(content);
            }
        }
        return -1;
    }

    public static boolean contains(final List<String> list, final String target) {
        return indexOf(list, target) != -1;
    }
}
