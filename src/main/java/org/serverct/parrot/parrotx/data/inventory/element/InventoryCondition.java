package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
        if (Objects.isNull(condition)) {
            return baseElement.getBase();
        }
        return (condition.test(user) ? passElement.getBase() : baseElement.getBase());
    }

    @Override
    public boolean isClickable() {
        if (Objects.isNull(condition)) {
            return baseElement.isClickable();
        }
        return (condition.test(user) ? passElement.isClickable() : baseElement.isClickable());
    }
}
