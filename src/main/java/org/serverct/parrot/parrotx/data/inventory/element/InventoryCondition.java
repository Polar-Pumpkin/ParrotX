package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.ParrotX;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;

import java.util.Objects;
import java.util.function.Predicate;

@Data
public class InventoryCondition implements InventoryElement {

    private final InventoryElement baseElement;
    private final InventoryElement passElement;
    private final Predicate<Player> condition;
    private Player user;

    @Builder
    public InventoryCondition(InventoryElement baseElement, InventoryElement passElement, Predicate<Player> condition) {
        this.baseElement = baseElement;
        this.passElement = passElement;
        this.condition = condition;
    }

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
        final InventoryElement element = getElement();
        ParrotX.debug("由 {0}({1}) 元素受理该 InventoryCondition 的点击.",
                element.getBase().getName(), element.getClass().getSimpleName());
        element.click(holder, event);
    }
}
