package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public @Data
@Builder
class InventoryPlaceholder implements InventoryElement {
    private final BaseElement base;
    private final Predicate<ItemStack> validate;
    private final Consumer<InventoryClickEvent> onPlace;
    @Getter
    private final Map<Integer, ItemStack> placedMap = new HashMap<>();

    public static InventoryPlaceholder get(final PInventory<?> inv, final String name) {
        return (InventoryPlaceholder) inv.getElement(name);
    }

    public void place(final InventoryClickEvent event) {
        if (Objects.isNull(onPlace)) {
            return;
        }
        onPlace.accept(event);
    }

    public boolean validate(final ItemStack item) {
        return Objects.isNull(validate) || validate.test(item);
    }

    public ItemStack getPlaced(final int slot) {
        return this.placedMap.get(slot);
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
                    if (!validate(cursorItem)) {
                        event.setCancelled(true);
                        return;
                    }

                    this.placedMap.put(event.getSlot(), cursorItem);
                    place(event);
                    break;
                }
                break;
            case SWAP_WITH_CURSOR:
                if (!validate(cursorItem) || Objects.isNull(cursorItem)) {
                    event.setCancelled(true);
                    return;
                }

                this.placedMap.put(event.getSlot(), cursorItem.clone());
                place(event);

                if (Objects.nonNull(slotItem) && slotItem.isSimilar(base.getItem().get())) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            event.getView().setCursor(null);
                        }
                    }.runTaskLater(holder.getPlugin(), 1L);
                }
                break;
            case PICKUP_ALL:
                if (Objects.isNull(slotItem) || slotItem.isSimilar(base.getItem().get())) {
                    event.setCancelled(true);
                    break;
                }
                this.placedMap.remove(event.getSlot());
                break;
            default:
                event.setCancelled(true);
                break;
        }

        lang.log.debug("事件处理结果: {0}", event.isCancelled());
        holder.refresh(event.getInventory());
    }
}
