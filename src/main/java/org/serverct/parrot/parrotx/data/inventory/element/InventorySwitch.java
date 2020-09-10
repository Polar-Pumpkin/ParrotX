package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.data.inventory.BaseInventory;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public @Data
@Builder
class InventorySwitch implements InventoryElement {
    private final BaseElement base;
    private final Supplier<ItemStack> activeItem;
    private final BooleanSupplier condition;
    private final Consumer<Boolean> onSwitch;
    private boolean active;

    public boolean condition() {
        return Objects.isNull(condition) || condition.getAsBoolean();
    }

    public void onSwitch() {
        if (Objects.isNull(onSwitch)) {
            return;
        }
        onSwitch.accept(active);
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public ItemStack parseItem(BaseInventory<?> inv, int slot) {
        return active ? activeItem.get() : base.getItem().get();
    }

    @Override
    public void click(final BaseInventory<?> holder, final InventoryClickEvent event) {
        event.setCancelled(true);
        if (!condition()) {
            return;
        }
        this.active = !this.active;
        onSwitch();
        holder.refresh(event.getInventory());
    }
}
