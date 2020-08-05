package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.PPlugin;
import parsii.eval.Expression;
import parsii.eval.Parser;
import parsii.eval.Scope;
import parsii.eval.Variable;

public class BasicUtil {

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
        String result = "&c" + location.getBlockX() + "&7, &c" + location.getBlockY() + "&7, &c" + location.getBlockZ() + "&7";
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static double calculateExpression(@NonNull PPlugin plugin, String expression, int xValue) {
        try {
            Scope scope = Scope.create();
            Variable x = scope.getVariable("x");
            Expression expr = Parser.parse(expression, scope);
            x.setValue(xValue);
            double result = expr.evaluate();
            plugin.lang.logAction(I18n.CALCULATE, "数学表达式(" + expression + ", x=" + xValue + ", 值=" + result + ")");
            return result;
        } catch (Throwable e) {
            plugin.lang.logError(I18n.CALCULATE, "数学表达式(" + expression + ", x=" + xValue + ")", e, null);
        }
        return 0;
    }

    public static String arabic2Roman(int number) {
        String rNumber = "";
        int[] aArray = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] rArray = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X",
                "IX", "V", "IV", "I"};
        if (number < 1 || number > 3999) {
            rNumber = "-1";
        } else {
            for (int i = 0; i < aArray.length; i++) {
                while (number >= aArray[i]) {
                    rNumber += rArray[i];
                    number -= aArray[i];
                }
            }
        }
        return rNumber;
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

    public static void send(@NonNull PPlugin plugin, @NonNull Player user, String msg) {
        new BukkitRunnable() {
            @Override
            public void run() {
                I18n.send(user, msg);
            }
        }.runTaskLater(plugin, 1);
    }

    public static void broadcast(String msg) {
        Bukkit.getOnlinePlayers().forEach(user -> I18n.send(user, msg));
    }

    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Bukkit.getOnlinePlayers().forEach(user -> user.sendTitle(title, subtitle, fadeIn * 20, stay * 20, fadeOut * 20));
    }

}
