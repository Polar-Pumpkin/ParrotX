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
        ParrotX.debug("获取 InventoryCondition 的具体元素: {0} - {1}.",
                baseElement.getBase().getName(),
                passElement.getBase().getName());
        if (Objects.isNull(condition)) {
            ParrotX.debug("由于条件判断函数为 null, 最终返回基元素.");
            return baseElement;
        }
        final InventoryElement element = (condition.test(user) ? passElement : baseElement);
        ParrotX.debug("具体元素为: {0}.", element.getBase().getName());
        return element;
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
