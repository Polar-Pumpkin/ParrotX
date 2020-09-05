package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.data.inventory.BaseInventory;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public @Data
class InventoryPlaceholder implements InventoryElement {
    private final BaseElement base;
    private final Predicate<ItemStack> validate;
    private final Consumer<InventoryClickEvent> onPlace;
    private final Map<Integer, ItemStack> placedMap = new HashMap<>();

    @Builder
    public InventoryPlaceholder(BaseElement base, Predicate<ItemStack> validate, Consumer<InventoryClickEvent> onPlace) {
        this.base = base;
        this.validate = validate;
        this.onPlace = onPlace;
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
    public ItemStack parseItem(BaseInventory<?> inv, int slot) {
        return this.placedMap.getOrDefault(slot, this.base.getItem());
    }

    @Override
    public void click(final BaseInventory<?> holder, final InventoryClickEvent event) {
        final ItemStack slotItem = event.getCurrentItem();
        final ItemStack cursorItem = event.getCursor();

        @SuppressWarnings("AccessStaticViaInstance") final I18n lang = holder.getPlugin().lang;
        lang.log.debug("(ParrotX) InventoryPlaceholder " + getBase().getName() + " 已被点击:");
        lang.log.debug("(ParrotX) 槽位内物品: " + slotItem);
        lang.log.debug("(ParrotX) 指针上物品: " + cursorItem);
        lang.log.debug("(ParrotX) 点击操作: " + event.getAction());

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
                event.setCancelled(true);
                break;
            case SWAP_WITH_CURSOR:
                if (!validate(cursorItem)) {
                    event.setCancelled(true);
                    return;
                }

                if (Objects.nonNull(slotItem) && slotItem.isSimilar(base.getItem())) {
                    Bukkit.getScheduler().runTaskLater(holder.getPlugin(), () -> event.getView().setCursor(new ItemStack(Material.AIR)), 1L);
                }
                this.placedMap.put(event.getSlot(), cursorItem);
                place(event);
                break;
            case PICKUP_ALL:
                if (Objects.nonNull(slotItem) && !slotItem.isSimilar(base.getItem())) {
                    this.placedMap.remove(event.getSlot());
                    break;
                }
                event.setCancelled(true);
                break;
            default:
                event.setCancelled(true);
        }

        holder.refresh(event.getInventory());
    }
}
