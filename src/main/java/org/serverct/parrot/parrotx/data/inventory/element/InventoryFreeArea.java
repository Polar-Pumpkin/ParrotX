package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
public class InventoryFreeArea implements InventoryElement {

    private final PPlugin plugin;
    private final I18n lang;
    private final BaseElement base;
    private final Consumer<InventoryClickEvent> onPlace;
    private final PInventory<?> holder;
    private final Map<Integer, ItemStack> placedMap = new HashMap<>();

    @Builder
    public InventoryFreeArea(BaseElement base, Consumer<InventoryClickEvent> onPlace, PInventory<?> holder) {
        this.base = base;
        this.onPlace = onPlace;
        this.holder = holder;

        this.plugin = holder.getPlugin();
        this.lang = this.plugin.getLang();
    }

    @Nullable
    public static InventoryFreeArea get(final PInventory<?> inv, final String name) {
        return (InventoryFreeArea) inv.getElement(name);
    }

    public Map<Integer, ItemStack> getPlacedMap() {
        refresh();
        return placedMap;
    }

    public void place(final InventoryClickEvent event) {
        if (Objects.isNull(onPlace)) {
            return;
        }
        onPlace.accept(event);
    }

    @Nullable
    public ItemStack getPlaced(final int slot) {
        final ItemStack item = this.placedMap.get(slot);
        if (Objects.isNull(item)) {
            return null;
        }
        return item.clone();
    }

    @NotNull
    public List<ItemStack> addPlaced(final ItemStack... items) {
        refresh();
        final List<ItemStack> result = new ArrayList<>();
        if (Objects.isNull(items) || items.length <= 0) {
            return result;
        }

        final Iterator<ItemStack> iterator = Arrays.asList(items).iterator();
        for (int slot : getPositions()) {
            if (!iterator.hasNext()) {
                break;
            }
            if (this.placedMap.containsKey(slot)) {
                continue;
            }
            this.placedMap.put(slot, iterator.next().clone());
        }

        while (iterator.hasNext()) {
            result.add(iterator.next().clone());
        }
        return result;
    }

    public void clear() {
        this.placedMap.clear();
    }

    public void refresh() {
        lang.log.debug("刷新 InventoryFreeArea: {0}", getBase().getName());
        lang.log.debug("初始数据集: {0}", this.placedMap.size());

        this.placedMap.clear();
        final Inventory inv = holder.getInventory();
        for (int slot : base.getPositions()) {
            final ItemStack item = inv.getItem(slot);
            if (Objects.isNull(item) || item.getType() == Material.AIR) {
                continue;
            }
            this.placedMap.put(slot, item);
        }

        final List<Map.Entry<Integer, ItemStack>> filter = this.placedMap.entrySet().stream().filter(entry -> {
            final ItemStack item = entry.getValue();
            return Objects.nonNull(item) && item.getType() != Material.AIR;
        }).collect(Collectors.toList());

        this.placedMap.clear();
        filter.forEach(entry -> this.placedMap.put(entry.getKey(), entry.getValue()));
        lang.log.debug("过滤后数据集: {0}", this.placedMap.size());
    }

    @Override
    public ItemStack parseItem(PInventory<?> inv, int slot) {
        return this.placedMap.getOrDefault(slot, this.base.getItem().get());
    }

    @Override
    public void click(PInventory<?> holder, InventoryClickEvent event) {
        Bukkit.getScheduler().runTask(plugin, this::refresh);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return this.base;
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
