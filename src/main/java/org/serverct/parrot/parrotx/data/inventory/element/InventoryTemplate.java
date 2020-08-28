package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public @Data
class InventoryTemplate<T> implements InventoryElement {

    private final InventoryElement base;
    private final List<T> contents;
    private final TempleApplier<ItemStack, T> applyTemple;

    public InventoryElement getElement() {
        return base;
    }

    public ItemStack apply(final Object data) {
        final ItemStack item = getBase().getItem();
        if (Objects.isNull(applyTemple) || Objects.isNull(data)) {
            return item;
        }
        //noinspection unchecked
        return applyTemple.apply(item, (T) data);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return base.getBase();
    }

    @Override
    public boolean isClickable() {
        return true;
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
