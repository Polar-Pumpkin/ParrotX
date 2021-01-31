package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class InventoryUtil {

    @NotNull
    public static Map<Integer, ItemStack> inventoryFilter(@NotNull Inventory inv,
                                                          @NotNull Predicate<ItemStack> filter) {
        final Map<Integer, ItemStack> result = new HashMap<>();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            final ItemStack item = inv.getItem(slot);
            if (filter.test(item)) {
                result.put(slot, item);
            }
        }
        return result;
    }

    public static void apply(Inventory inv, Map<Integer, ItemStack> items) {
        items.forEach((slot, item) -> {
            if (slot < inv.getSize()) {
                inv.setItem(slot, item);
            }
        });
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

}
