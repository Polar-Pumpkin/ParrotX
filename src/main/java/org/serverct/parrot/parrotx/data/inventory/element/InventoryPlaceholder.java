package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public @Data
@Builder
class InventoryPlaceholder implements InventoryElement {
    private final BaseElement base;
    private final Predicate<ItemStack> validate;
    private final Consumer<InventoryClickEvent> onPlace;

    public void place(final InventoryClickEvent event) {
        if (Objects.isNull(onPlace)) {
            return;
        }
        onPlace.accept(event);
    }

    public boolean validate(final ItemStack item) {
        return Objects.isNull(validate) || validate.test(item);
    }

    @Override
    public boolean isClickable() {
        return true;
    }
}
