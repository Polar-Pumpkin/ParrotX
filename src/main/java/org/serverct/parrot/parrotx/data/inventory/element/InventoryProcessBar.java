package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;

public @Data
@Builder class InventoryProcessBar implements InventoryElement{
    private final BaseElement base;
    private final ItemStack processItem;
    private final int current;
    private final int total;

    public double getRate() {
        return BigDecimal.valueOf(Math.min(1.0D, current / ((double) total))).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
    }

    @Override
    public boolean isClickable() {
        return false;
    }
}
