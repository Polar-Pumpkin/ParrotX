package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public @Data
@Builder
class InventoryTemplate<T> implements InventoryElement {

    private final InventoryElement base;
    private final List<T> contents;
    private final TempleApplier<ItemStack, T> applyTemple;

    public InventoryElement getElement() {
        return base;
    }

    public ItemStack apply(final Object data) {
        final ItemStack item = getBase().getItem();
        if (Objects.isNull(applyTemple) || Objects.isNull(data)) {
            return item;
        }
        //noinspection unchecked
        return applyTemple.apply(item, (T) data);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return base.getBase();
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @FunctionalInterface
    protected interface TempleApplier<Temple, Data> {
        Temple apply(Temple temple, Data data);
    }
}
