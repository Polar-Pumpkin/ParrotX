package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;
import org.serverct.parrot.parrotx.utils.ItemUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Data
@Builder
public class InventoryPlaceholder implements InventoryElement {
    private final BaseElement base;
    private final Predicate<ItemStack> validate;
    private final Consumer<InventoryClickEvent> onPlace;
    @Getter
    private final Map<Integer, ItemStack> placedMap = new HashMap<>();

    @Nullable
    public static InventoryPlaceholder get(final PInventory<?> inv, final String name) {
        return (InventoryPlaceholder) inv.getElement(name);
    }

    public void place(final InventoryClickEvent event) {
        if (Objects.isNull(onPlace)) {
            return;
        }
        onPlace.accept(event);
    }

    public boolean invalid(final ItemStack item) {
        return Objects.nonNull(validate) && !validate.test(item);
    }

    public ItemStack getPlaced(final int slot) {
        return this.placedMap.get(slot);
    }

    @NotNull
    public List<ItemStack> addPlaced(final ItemStack... items) {
        final List<ItemStack> result = new ArrayList<>();
        if (Objects.isNull(items) || items.length <= 0) {
            return result;
        }

        final Iterator<ItemStack> iterator = Arrays.asList(items).iterator();
        for (int slot : getPositions()) {
            if (!iterator.hasNext()) {
                break;
            }
            if (this.placedMap.containsKey(slot)) {
                continue;
            }
            this.placedMap.put(slot, iterator.next().clone());
        }

        while (iterator.hasNext()) {
            result.add(iterator.next().clone());
        }
        return result;
    }

    public boolean addPlaced(final int slot, @Nullable final ItemStack item, final boolean force) {
        if (ItemUtil.invalid(item)) {
            return false;
        }
        if (force) {
            this.placedMap.put(slot, item);
            return true;
        } else {
            return Objects.isNull(this.placedMap.putIfAbsent(slot, item));
        }
    }

    public void clear() {
        this.placedMap.clear();
    }

    @Override
    public boolean isClickable() {
        return true;
    }

    @Override
    public ItemStack parseItem(PInventory<?> inv, int slot) {
        return this.placedMap.getOrDefault(slot, this.base.getItem().get());
    }

    @Override
    public void click(final PInventory<?> holder, final InventoryClickEvent event) {
        final ItemStack slotItem = event.getCurrentItem();
        final ItemStack cursorItem = event.getCursor();

        final I18n lang = holder.getPlugin().getLang();
        lang.log.debug("InventoryPlaceholder {0} 已被点击:", getBase().getName());
        lang.log.debug("槽位内物品: {0}", slotItem);
        lang.log.debug("指针上物品: {0}", cursorItem);
        lang.log.debug("点击操作: {0}", event.getAction());
        lang.log.debug("当前已放置物品集: {0}", this.placedMap);

        switch (event.getAction()) {
            case PLACE_ALL:
                if (Objects.isNull(slotItem) || slotItem.getType() == Material.AIR) {
                    if (invalid(cursorItem)) {
                        event.setCancelled(true);
                        break;
                    }

                    this.placedMap.put(event.getSlot(), cursorItem);
                    place(event);
                    break;
                }
                break;
            case SWAP_WITH_CURSOR:
                if (invalid(cursorItem) || Objects.isNull(cursorItem)) {
                    event.setCancelled(true);
                    break;
                }

                this.placedMap.put(event.getSlot(), cursorItem.clone());
                place(event);

                if (Objects.nonNull(slotItem) && slotItem.isSimilar(base.getItem().get())) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            event.getView().setCursor(null);
                        }
                    }.runTask(holder.getPlugin());
                }
                break;
            case PICKUP_ALL:
                if (Objects.isNull(slotItem) || slotItem.isSimilar(base.getItem().get())) {
                    event.setCancelled(true);
                    break;
                }

                place(event);
                this.placedMap.remove(event.getSlot());
                break;
            default:
                event.setCancelled(true);
                break;
        }

        lang.log.debug("事件处理结果: {0}", (Object) event.isCancelled());
        Bukkit.getScheduler().runTask(holder.getPlugin(), () -> {
            holder.refresh(event.getInventory());
            holder.getBase().getViewer().updateInventory();
        });
    }
}
