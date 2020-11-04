package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public @Data
class InventoryProcessBar implements InventoryElement {
    private final BaseElement base;
    private final Supplier<ItemStack> processItem;
    private final int current;
    private final int total;
    private final Map<Integer, ItemStack> barMap = new HashMap<>();

    @Builder
    public InventoryProcessBar(BaseElement base, Supplier<ItemStack> processItem, int current, int total) {
        this.base = base;
        this.processItem = processItem;
        this.current = current;
        this.total = total;
    }

    public double getRate() {
        return BigDecimal.valueOf(Math.min(1.0D, current / ((double) total))).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public BaseElement preload(PInventory<?> inv) {
        if (this.total > 0) {
            this.barMap.clear();
            final List<Integer> slots = getPositions();
            for (int amount = 0; amount < BigDecimal.valueOf(slots.size() * getRate()).intValue(); amount++) {
                this.barMap.put(slots.get(amount), processItem.get());
            }
        }
        return getBase();
    }

    @Override
    public ItemStack parseItem(PInventory<?> inv, int slot) {
        return this.barMap.getOrDefault(slot, getBase().getItem().get());
    }
}
