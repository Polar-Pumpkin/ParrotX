package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;

import java.util.Objects;
import java.util.function.Predicate;

public @Data
@Builder
class InventoryCondition implements InventoryElement {

    private final InventoryElement baseElement;
    private final InventoryElement passElement;
    private final Predicate<Player> condition;
    private Player user;

    public InventoryElement getElement() {
        if (Objects.isNull(condition)) {
            return baseElement;
        }
        return (condition.test(user) ? passElement : baseElement);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return getElement().getBase();
    }

    @Override
    public boolean isClickable() {
        return getElement().isClickable();
    }

    @Override
    public BaseElement preload(PInventory<?> inv) {
        this.user = inv.getBase().getViewer();
        return getBase();
    }

    @Override
    public void click(final PInventory<?> holder, final InventoryClickEvent event) {
        getElement().click(holder, event);
    }
}
