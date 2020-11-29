package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;

import java.util.Objects;
import java.util.function.Consumer;

public @Data
@Builder
class InventoryButton implements InventoryElement {
    private final BaseElement base;
    private final Consumer<InventoryClickEvent> onClick;

    @Override
    public void click(final PInventory<?> holder, final InventoryClickEvent event) {
        event.setCancelled(true);
        if (Objects.isNull(onClick)) {
            return;
        }
        onClick.accept(event);
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
