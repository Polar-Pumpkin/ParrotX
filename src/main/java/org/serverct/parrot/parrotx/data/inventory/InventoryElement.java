package org.serverct.parrot.parrotx.data.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.inventory.element.BaseElement;

import java.util.List;

public interface InventoryElement {
    @NotNull BaseElement getBase();

    boolean isClickable();

    default int getPriority() {
        return getBase().getPriority();
    }

    default List<Integer> getPositions() {
        return getBase().getPositions();
    }

    default BaseElement preload(final BaseInventory<?> inv) {
        return getBase().preload(inv);
    }

    default ItemStack parseItem(final BaseInventory<?> inv, final int slot) {
        return getBase().parseItem(inv, slot);
    }

    default void click(final BaseInventory<?> holder, final InventoryClickEvent event) {
        getBase().click(holder, event);
    }
}
