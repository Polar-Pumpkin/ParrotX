package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;
import org.serverct.parrot.parrotx.utils.ItemUtil;

import java.util.Objects;
import java.util.function.Consumer;

@Data
@Builder
public class InventoryItemButton implements InventoryElement {

    private final BaseElement base;
    private final Consumer<ItemStack> onClick;

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public void click(PInventory<?> holder, InventoryClickEvent event) {
        event.setCancelled(true);

        final InventoryAction action = event.getAction();
        if (!ItemUtil.invalid(getBase().getItem().get())) {
            if (action != InventoryAction.SWAP_WITH_CURSOR) {
                return;
            }
        } else {
            if (action != InventoryAction.PLACE_ALL) {
                return;
            }
        }

        if (Objects.isNull(this.onClick)) {
            return;
        }

        final ItemStack item = event.getCursor();
        this.onClick.accept(item);
    }
}
