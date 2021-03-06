package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings({"unused", "unchecked"})
@Data
public class InventoryTemplate<T> implements InventoryElement {

    private final InventoryElement base;
    private final List<T> contents;
    private final BiFunction<ItemStack, T, ItemStack> applyTemple;
    private final Map<Integer, Map<Integer, T>> contentMap = new HashMap<>();
    private int currentPage;

    public static <T> InventoryTemplate<T> get(final PInventory<?> inv, final String name) {
        return (InventoryTemplate<T>) inv.getElement(name);
    }

    public InventoryElement getElement() {
        return base;
    }

    public ItemStack apply(final T data) {
        if (Objects.isNull(data)) {
            return new ItemStack(Material.AIR);
        }
        final ItemStack item = getBase().getItem().get().clone();
        if (Objects.isNull(applyTemple)) {
            return item;
        }
        return applyTemple.apply(item, data);
    }

    public T getContent(final int page, final int slot) {
        return this.contentMap.getOrDefault(page, new HashMap<>()).get(slot);
    }

    public T getContent(final int slot) {
        return getContent(this.currentPage, slot);
    }

    public int getMaxPage() {
        return this.contentMap.size();
    }

    public void setCurrentPage(final int page) {
        this.currentPage = Math.min(getMaxPage(), Math.max(page, 1));
    }

    public boolean hasNextPage() {
        return this.currentPage + 1 <= getMaxPage();
    }

    public boolean hasPreviousPage() {
        return this.currentPage - 1 >= 1;
    }

    public int nextPage(final PInventory<?> holder) {
        int page = this.currentPage + 1;
        if (page > getMaxPage()) {
            page = 1;
        }
        this.currentPage = page;
        holder.refresh(holder.getInventory());
        return page;
    }

    public int previousPage(final PInventory<?> holder) {
        int page = this.currentPage - 1;
        if (page < 1) {
            page = getMaxPage();
        }
        this.currentPage = page;
        holder.refresh(holder.getInventory());
        return page;
    }

    @Override
    public @NotNull BaseElement getBase() {
        return this.base.getBase();
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public BaseElement preload(PInventory<?> inv) {
        final I18n lang = inv.getPlugin().getLang();
        lang.log.debug("预加载 InventoryTemplate: {0}", getBase().getName());
        lang.log.debug("数据集: {0}", contents);

        this.contentMap.clear();

        Iterator<Integer> slotIterator = getBase().getPositions().iterator();
        Map<Integer, T> contents = new HashMap<>();
        int page = 1;

        for (T content : this.contents) {
            if (!slotIterator.hasNext()) {
                this.contentMap.put(page, contents);
                contents = new HashMap<>();
                slotIterator = getBase().getPositions().iterator();
                page++;
            }
            contents.put(slotIterator.next(), content);
        }
        contentMap.put(page, contents);
        lang.log.debug("分页数据集: {0}", contentMap);

        if (this.currentPage < 1 || this.currentPage > getMaxPage()) {
            this.currentPage = 1;
        }
        return getBase();
    }

    @Override
    public ItemStack parseItem(PInventory<?> inv, int slot) {
        return apply(this.contentMap.getOrDefault(currentPage, new HashMap<>()).get(slot));
    }

    @Override
    public void click(final PInventory<?> holder, final InventoryClickEvent event) {
        final I18n lang = holder.getPlugin().getLang();
        if (Objects.isNull(getContent(this.currentPage, event.getSlot()))) {
            event.setCancelled(true);
            return;
        }
        this.base.click(holder, event);
    }

    public @NoArgsConstructor
    static class InventoryTemplateBuilder<T> {
        private InventoryElement base;
        private List<T> contents;
        private BiFunction<ItemStack, T, ItemStack> applyTemple;

        public InventoryTemplateBuilder<T> base(final InventoryElement base) {
            this.base = base;
            return this;
        }

        public InventoryTemplateBuilder<T> contents(final List<T> contents) {
            this.contents = contents;
            return this;
        }

        public InventoryTemplateBuilder<T> applyTemple(final BiFunction<ItemStack, T, ItemStack> applyTemple) {
            this.applyTemple = applyTemple;
            return this;
        }

        public InventoryTemplate<T> build() {
            return new InventoryTemplate<>(base, contents, applyTemple);
        }
    }
}
