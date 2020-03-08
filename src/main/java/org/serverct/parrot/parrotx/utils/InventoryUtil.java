package org.serverct.parrot.parrotx.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class InventoryUtil {

    public static Map<Integer, ItemStack> inventoryFilter(Inventory inv, Predicate<ItemStack> filter) {
        Map<Integer, ItemStack> result = new HashMap<>();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            ItemStack item = inv.getItem(slot);
            if (filter.test(item)) {
                result.put(slot, item);
            }
        }
        return result;
    }

    public static void apply(Inventory inv, Map<Integer, ItemStack> items) {
        items.forEach(
                (slot, item) -> {
                    if (slot < inv.getSize()) {
                        inv.setItem(slot, item);
                    }
                }
        );
    }

}
