package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public @Data
@Builder
class BaseElement implements InventoryElement {
    private final int priority;
    private final String name;
    private final ItemStack item;
    private final String xPos;
    private final String yPos;
    private final Predicate<Player> condition;

    public boolean condition(final Player user) {
        return Objects.isNull(condition) || condition.test(user);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return this;
    }

    @Override
    public boolean isClickable() {
        return false;
    }
}
