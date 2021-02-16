package org.serverct.parrot.parrotx.utils;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class InventoryUtil {

    @NotNull
    public static Map<Integer, ItemStack> inventoryFilter(@NotNull final Inventory inv,
                                                          @NotNull final Predicate<ItemStack> filter) {
        final Map<Integer, ItemStack> result = new HashMap<>();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            final ItemStack item = inv.getItem(slot);
            if (filter.test(item)) {
                result.put(slot, item);
            }
        }
        return result;
    }

    public static void apply(@Nullable final Inventory inv,
                             @Nullable final Map<Integer, ItemStack> items) {
        if (Objects.isNull(inv) || Objects.isNull(items)) {
            return;
        }
        for (final Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            final int slot = entry.getKey();
            final ItemStack item = entry.getValue();
            if (slot < inv.getSize()) {
                inv.setItem(slot, item);
            }
        }
    }

    public static int firstMatch(@Nullable final Inventory inv,
                                 @Nullable final ItemStack item) {
        if (Objects.isNull(inv) || ItemUtil.invalid(item)) {
            return -1;
        }
        for (int index = 0; index < inv.getSize(); index++) {
            final ItemStack inSlot = inv.getItem(index);
            if (ItemUtil.invalid(inSlot) || !item.isSimilar(inSlot)) {
                continue;
            }
            final Material material = item.getType();
            if (inSlot.getAmount() >= material.getMaxStackSize()) {
                continue;
            }
            return index;
        }
        return -1;
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
