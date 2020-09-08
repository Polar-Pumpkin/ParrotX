package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.inventory.BaseInventory;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;

@SuppressWarnings("unused")
public @Data
class InventoryTemplate<T> implements InventoryElement {

    private final InventoryElement base;
    private final List<T> contents;
    private final TempleApplier<ItemStack, T> applyTemple;
    private final Map<Integer, Map<Integer, T>> contentMap = new HashMap<>();

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

    public T getContent(final int page, final int slot) {
        return this.contentMap.getOrDefault(page, new HashMap<>()).get(slot);
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
        final I18n lang = inv.getPlugin().getLang();
        lang.log.debug("预加载 InventoryTemplate: " + getBase().getName());
        lang.log.debug("数据集: " + contents);

        this.contentMap.clear();

        final Iterator<Integer> slotIterator = getBase().getPositions().iterator();
        Map<Integer, T> contents = null;
        int page = 1;

        for (T content : this.contents) {
            if (Objects.isNull(contents)) {
                contents = new HashMap<>();
            }
            if (!slotIterator.hasNext()) {
                this.contentMap.put(page, contents);
                contents = new HashMap<>();
                page++;
            }
            contents.put(slotIterator.next(), content);
        }
        contentMap.put(page, contents);
        lang.log.debug("分页数据集: " + contentMap);

        inv.setPage(getBase().getName(), 1);
        lang.log.debug("BaseInventory 页码数据: " + inv.getPageMap());

        return getBase();
    }

    @Override
    public ItemStack parseItem(BaseInventory<?> inv, int slot) {
        return apply(this.contentMap.getOrDefault(inv.getPage(getBase().getName()), new HashMap<>()).get(slot));
    }

    @Override
    public void click(final BaseInventory<?> holder, final InventoryClickEvent event) {
        if (Objects.isNull(getContent(holder.getPage(getBase().getName()), event.getSlot()))) {
            event.setCancelled(true);
            return;
        }
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
