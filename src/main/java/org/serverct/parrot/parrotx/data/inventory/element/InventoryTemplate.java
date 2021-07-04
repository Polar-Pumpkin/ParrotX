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
import java.util.function.Supplier;

@SuppressWarnings({"unused", "unchecked"})
@Data
public class InventoryTemplate<T> implements InventoryElement {

    private final InventoryElement base;
    private final Collection<T> contents;
    private final Supplier<Collection<T>> advancedContents;
    private final BiFunction<ItemStack, T, ItemStack> applyTemple;
    private final Map<Integer, Map<Integer, T>> contentMap = new HashMap<>();
    private final boolean permitNullContent;
    private int currentPage;

    public InventoryTemplate(InventoryElement base, Collection<T> contents,
                             boolean permitNullContent,
                             BiFunction<ItemStack, T, ItemStack> applyTemple) {
        this.base = base;
        this.contents = contents;
        this.advancedContents = null;
        this.applyTemple = applyTemple;
        this.permitNullContent = permitNullContent;
    }

    public InventoryTemplate(InventoryElement base, Supplier<Collection<T>> advancedContents,
                             boolean permitNullContent,
                             BiFunction<ItemStack, T, ItemStack> applyTemple) {
        this.base = base;
        this.contents = null;
        this.advancedContents = advancedContents;
        this.applyTemple = applyTemple;
        this.permitNullContent = permitNullContent;
    }

    public static <T> InventoryTemplate<T> get(final PInventory<?> inv, final String name) {
        return (InventoryTemplate<T>) inv.getElement(name);
    }

    public InventoryElement getElement() {
        return base;
    }

    public ItemStack apply(final T data) {
        if (!permitNullContent && Objects.isNull(data)) {
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
        final Collection<T> actualContents = Objects.nonNull(this.advancedContents) ? this.advancedContents.get() :
                (Objects.nonNull(this.contents) ? this.contents : new ArrayList<>());
        lang.log.debug("预加载 InventoryTemplate: {0}", getBase().getName());
        lang.log.debug("数据集: {0}", actualContents);

        this.contentMap.clear();

        Iterator<Integer> slotIterator = getBase().getPositions().iterator();
        Map<Integer, T> contents = new HashMap<>();
        int page = 1;

        for (T content : actualContents) {
            if (!slotIterator.hasNext()) {
                this.contentMap.put(page, contents);
                contents = new HashMap<>();
                slotIterator = getBase().getPositions().iterator();
                page++;
            }
            // FIXME 在 BaseElement 忘记写 x/yPos 的时候这里可能会报错.
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
        final T content = getContent(this.currentPage, event.getSlot());
        if (!permitNullContent && Objects.isNull(content)) {
            event.setCancelled(true);
            return;
        }
        this.base.click(holder, event);
    }

    public @NoArgsConstructor
    static class InventoryTemplateBuilder<T> {
        private InventoryElement base;
        private Collection<T> contents;
        private Supplier<Collection<T>> advancedContents;
        private BiFunction<ItemStack, T, ItemStack> applyTemple;
        private boolean permitNullContent;

        public InventoryTemplateBuilder<T> base(final InventoryElement base) {
            this.base = base;
            return this;
        }

        public InventoryTemplateBuilder<T> contents(final List<T> contents) {
            this.contents = contents;
            return this;
        }

        public InventoryTemplateBuilder<T> contents(final Supplier<Collection<T>> contents) {
            this.advancedContents = contents;
            return this;
        }

        public InventoryTemplateBuilder<T> applyTemple(final BiFunction<ItemStack, T, ItemStack> applyTemple) {
            this.applyTemple = applyTemple;
            return this;
        }

        public InventoryTemplateBuilder<T> permitNullContent(final boolean permitNullContent) {
            this.permitNullContent = permitNullContent;
            return this;
        }

        public InventoryTemplate<T> build() {
            if (Objects.nonNull(this.advancedContents)) {
                return new InventoryTemplate<>(base, advancedContents, permitNullContent, applyTemple);
            }
            return new InventoryTemplate<>(base, contents, permitNullContent, applyTemple);
        }
    }
}
