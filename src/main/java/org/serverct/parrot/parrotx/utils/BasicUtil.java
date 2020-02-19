package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.PPlugin;

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
        for(int time = 1; time < size; time++) {
            for(int index = 0; index < size - time; index++) {
                if(copy.get(index) < copy.get(index + 1)) {
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

    public static void openInventory(PPlugin plugin, Player user, Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                user.openInventory(inventory);
            }
        }.runTask(plugin);
    }

    public static void closeInventory(PPlugin plugin, Player user) {
        new BukkitRunnable() {
            @Override
            public void run() {
                user.closeInventory();
            }
        }.runTask(plugin);
    }

}
