package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.serverct.parrot.parrotx.ParrotX;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;

import java.util.Objects;
import java.util.function.Consumer;

@Data
@Builder
public class InventoryButton implements InventoryElement {
    private final BaseElement base;
    private final Consumer<InventoryClickEvent> onClick;

    @Override
    public void click(final PInventory<?> holder, final InventoryClickEvent event) {
        ParrotX.debug("InventoryButton {0} 被点击了.", base.getName());
        event.setCancelled(true);
        if (Objects.isNull(onClick)) {
            ParrotX.debug("点击处理函数为 null.");
            return;
        }
        onClick.accept(event);
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
