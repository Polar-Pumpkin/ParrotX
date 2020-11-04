package org.serverct.parrot.parrotx.data.inventory;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.element.BaseElement;
import org.serverct.parrot.parrotx.data.inventory.element.InventoryTemplate;
import org.serverct.parrot.parrotx.data.inventory.flags.Pageable;

import java.io.File;
import java.util.*;

public abstract class PInventory<T> extends AutoRefreshInventory implements Pageable {

    @Getter
    protected final T data;
    @Getter
    protected final Map<String, Integer> pageMap = new HashMap<>();
    @Getter
    private final Map<String, InventoryElement> elementMap = new HashMap<>();
    @Getter
    private final Map<Integer, String> slotMap = new HashMap<>();

    public PInventory(PPlugin plugin, T data, Player user, File file) {
        super(new FileDefinedInventory(plugin, user, file));
        this.data = data;
    }

    public PInventory(PPlugin plugin, T data, Player user, String title, int row) {
        super(new BaseInventory(plugin, user, title, row));
        this.data = data;
    }

    @Override
    public Inventory construct() {
        final Inventory result = base.construct();

        final List<InventoryElement> elements = new ArrayList<>(this.elementMap.values());
        elements.sort(Comparator.comparingInt(InventoryElement::getPriority));

        for (InventoryElement element : elements) {
            final BaseElement base = element.preload(this);

            if (!base.condition(super.base.viewer)) {
                continue;
            }

            for (int slot : element.getPositions()) {
                this.slotMap.put(slot, base.getName());
                result.setItem(slot, element.parseItem(this, slot));
            }
        }

        return result;
    }

    @Override
    public void open(InventoryOpenEvent event) {
        startRefresh(event.getInventory());
    }

    @Override
    public void close(InventoryCloseEvent event) {
        endRefresh();
    }

    @Override
    public void execute(InventoryClickEvent event) {
        if (!check(event)) {
            return;
        }

        final InventoryElement element = getElement(event.getSlot());
        if (Objects.isNull(element) || !element.isClickable()) {
            event.setCancelled(true);
            return;
        }

        element.click(this, event);
    }

    protected void addElement(InventoryElement element) {
        this.elementMap.put(element.getBase().getName(), element);
    }

    protected InventoryElement getElement(int slot) {
        return this.elementMap.get(this.slotMap.get(slot));
    }

    protected InventoryElement getElement(String name) {
        return this.elementMap.get(name);
    }

    public InventoryTemplate<?> getTemplate(final String name) {
        return (InventoryTemplate<?>) getElement(name);
    }

    @Override
    public int getMaxPage(final String element) {
        final InventoryElement inventoryElement = getElement(element);
        if (inventoryElement instanceof InventoryTemplate<?>) {
            return ((InventoryTemplate<?>) inventoryElement).getContentMap().size();
        }
        return 0;
    }
}
