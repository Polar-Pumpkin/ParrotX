package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;
import java.util.function.Consumer;

public @Data
@Builder
class InventoryButton implements InventoryElement {
    private final BaseElement base;
    private final Consumer<InventoryClickEvent> onClick;

    @Override
    public void click(final PInventory<?> holder, final InventoryClickEvent event) {
        final I18n lang = holder.getPlugin().getLang();
        lang.log.debug("处理 InventoryButton 点击...");
        event.setCancelled(true);
        if (Objects.isNull(onClick)) {
            lang.log.debug("未指定点击处理函数.");
            return;
        }
        onClick.accept(event);
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
