package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public @Data
@Builder
class InventorySwitch implements InventoryElement {
    private final BaseElement base;
    private final ItemStack activeItem;
    private final Predicate<Player> condition;
    private final Consumer<Boolean> onSwitch;
    private boolean active;

    public boolean condition(final Player user) {
        return Objects.isNull(condition) || condition.test(user);
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
}
