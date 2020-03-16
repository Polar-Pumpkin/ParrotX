package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
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

import java.util.ArrayList;
import java.util.List;

public class BasicUtil {

    public static int[] bubbleSort(int[] a) {
        int temp;
        int size = a.length;
        for (int i = 1; i < size; i++) {
            for (int j = 0; j < size - i; j++) {
                if (a[j] < a[j + 1]) {
                    temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
        return a;
    }

    public static List<Long> bubbleSort(List<Long> list) {
        List<Long> copy = new ArrayList<>(list);
        long temp;
        int size = copy.size();
        for (int time = 1; time < size; time++) {
            for (int index = 0; index < size - time; index++) {
                if (copy.get(index) < copy.get(index + 1)) {
                    temp = copy.get(index);
                    copy.set(index, copy.get(index + 1));
                    copy.set(index + 1, temp);
                }
            }
        }
        return copy;
    }

    public static long[] bubbleSort(long[] a) {
        long temp;
        int size = a.length;
        for (int i = 1; i < size; i++) {
            for (int j = 0; j < size - i; j++) {
                if (a[j] < a[j + 1]) {
                    temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
        return a;
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
        String result = "&c" + location.getBlockX() + "&7, &c" + location.getBlockY() + "&7, &c" + location.getBlockZ();
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static double calculateExpression(@NonNull PPlugin plugin, String expression, int xValue) {
        try {
            Scope scope = Scope.create();
            Variable x = scope.getVariable("x");
            Expression expr = Parser.parse(expression, scope);
            x.setValue(xValue);
            double result = expr.evaluate();
            plugin.lang.logAction(I18n.CALCULATE, "数学表达式(" + expression + ", x = " + xValue + ", 值 = " + result + ")");
            return result;
        } catch (Throwable e) {
            plugin.lang.logError(I18n.CALCULATE, "数学表达式(" + expression + ", x = " + xValue + ")", e, null);
        }
        return 0;
    }

    public static String getRomanNumerals(int number) {
        String numberStr = String.valueOf(number);
        char[] chars = numberStr.toCharArray();
        if (number <= 10) {
            return getRomanBelowTen(number);
        } else if (number < 100) {
            StringBuilder result = new StringBuilder();
            int digits1 = Integer.parseInt(String.valueOf(chars[1]));
            int digits2 = Integer.parseInt(String.valueOf(chars[0]));
            if (digits2 >= 5) {
                digits2 -= 5;
                result.append("L");
            }
            for (int i = 0; i < digits2; i++) {
                result.append(getRomanBelowTen(10));
            }
            return result.append(getRomanBelowTen(digits1)).toString();
        }
        return String.valueOf(number);
    }

    private static String getRomanBelowTen(int number) {
        if (number > 10) {
            return String.valueOf(number);
        }
        switch (number) {
            case 0:
                return "0";
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
            case 6:
                return "VI";
            case 7:
                return "VII";
            case 8:
                return "VIII";
            case 9:
                return "IX";
            case 10:
                return "X";
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
                user.sendMessage(I18n.color(msg));
            }
        }.runTaskLater(plugin, 1);
    }

}
