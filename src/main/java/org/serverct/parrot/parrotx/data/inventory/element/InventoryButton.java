package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Objects;
import java.util.function.Consumer;

public @Data
@Builder
class InventoryButton implements InventoryElement {
    private final BaseElement base;
    private final Consumer<InventoryClickEvent> onClick;

    public void click(final InventoryClickEvent event) {
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
