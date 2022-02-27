package org.serverct.parrot.parrotx.utils;

import com.cryptomorin.xseries.XItemStack;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.ParrotX;

import java.util.*;
import java.util.function.Predicate;

public class InventoryUtil {

    @NotNull
    public static Map<Integer, ItemStack> filter(@NotNull final Inventory inv,
                                                 @NotNull final Predicate<ItemStack> filter) {
        final Map<Integer, ItemStack> result = new HashMap<>();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            final ItemStack item = inv.getItem(slot);
            if (item != null && filter.test(item)) {
                result.put(slot, item.clone());
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

    public static int count(@NotNull final Inventory inventory, @NotNull final Predicate<ItemStack> checker) {
        int amount = 0;
        final Map<Integer, ItemStack> map = filter(inventory, checker);
        for (final ItemStack value : map.values()) {
            amount += value.getAmount();
        }
        return amount;
    }

    public static void give(@NotNull final Player user, @NotNull final ItemStack item,
                            final int amount) {
        if (ItemUtil.invalid(item)) {
            return;
        }

        final Material material = item.getType();
        final int maxStack = material.getMaxStackSize();

        final List<ItemStack> gives = new ArrayList<>();
        int left = amount;
        while (left > 0) {
            if (left >= maxStack) {
                final ItemStack fullItem = item.clone();
                fullItem.setAmount(maxStack);
                gives.add(fullItem);
                left -= maxStack;
                continue;
            }

            final ItemStack lastItem = item.clone();
            lastItem.setAmount(left);
            gives.add(lastItem);
            break;
        }

        XItemStack.giveOrDrop(user, gives.toArray(new ItemStack[0]));
    }

    public static boolean remove(@NotNull final Inventory inventory, @NotNull final Predicate<ItemStack> checker,
                                 final int amount) {
        if (amount <= 0) {
            return true;
        }

        int exist = 0;
        final Map<Integer, ItemStack> filter = filter(inventory, checker);
        for (final ItemStack value : filter.values()) {
            exist += value.getAmount();
        }

        if (exist < amount) {
            return false;
        }

        int left = amount;
        for (Map.Entry<Integer, ItemStack> entry : filter.entrySet()) {
            final int index = entry.getKey();
            final ItemStack value = entry.getValue();
            final int stack = value.getAmount();

            if (left >= stack) {
                final Map<Integer, ItemStack> map = inventory.removeItem(value);
                if (!map.isEmpty()) {
                    ParrotX.debug("移除物品失败: &c{0}&r.", map);
                    return false;
                }
                left -= stack;
                continue;
            }

            value.setAmount(stack - left);
            inventory.setItem(index, value);
            break;
        }


        final int after = count(inventory, checker);
        final boolean value = exist - after >= amount;
        if (!value) {
            ParrotX.debug("移除物品后检查失败: Exist &e{0}&r, After &e{1}&r, Amount &e{2}&r.",
                    exist, after, amount);
        }
        return value;
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
