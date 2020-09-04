package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.inventory.BaseInventory;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;

import java.util.*;

@SuppressWarnings("unused")
public @Data
class InventoryTemplate<T> implements InventoryElement {

    private final InventoryElement base;
    private final List<T> contents;
    private final TempleApplier<ItemStack, T> applyTemple;
    private final Map<Integer, T> contentMap = new HashMap<>();
    private ListIterator<T> iterator;

    public InventoryElement getElement() {
        return base;
    }

    public ItemStack apply(final T data) {
        final ItemStack item = getBase().getItem();
        if (Objects.isNull(applyTemple) || Objects.isNull(data)) {
            return item;
        }
        return applyTemple.apply(item, data);
    }

    public T getContent(final int slot) {
        return this.contentMap.get(slot);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return base.getBase();
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public BaseElement preload(BaseInventory<?> inv) {
        this.contentMap.clear();
        iterator = this.contents.listIterator();
        return getBase();
    }

    @Override
    public ItemStack parseItem(BaseInventory<?> inv, int slot) {
        if (Objects.isNull(iterator)) {
            return null;
        }
        if (iterator.hasNext()) {
            final T data = iterator.next();
            this.contentMap.put(slot, data);
            return apply(data);
        }
        return null;
    }

    @Override
    public void click(final BaseInventory<?> holder, final InventoryClickEvent event) {
        this.base.click(holder, event);
    }

    @FunctionalInterface
    public interface TempleApplier<Temple, Data> {
        Temple apply(Temple temple, Data data);
    }

    public @NoArgsConstructor
    static class InventoryTemplateBuilder<T> {
        private InventoryElement base;
        private List<T> contents;
        private TempleApplier<ItemStack, T> applyTemple;

        public InventoryTemplateBuilder<T> base(final InventoryElement base) {
            this.base = base;
            return this;
        }

        public InventoryTemplateBuilder<T> contents(final List<T> contents) {
            this.contents = contents;
            return this;
        }

        public InventoryTemplateBuilder<T> applyTemple(final TempleApplier<ItemStack, T> applyTemple) {
            this.applyTemple = applyTemple;
            return this;
        }

        public InventoryTemplate<T> build() {
            return new InventoryTemplate<>(base, contents, applyTemple);
        }
    }
}
