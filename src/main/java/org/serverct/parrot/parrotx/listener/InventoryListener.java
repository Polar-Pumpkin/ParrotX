package org.serverct.parrot.parrotx.listener;

import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.InventoryExecutor;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

public class InventoryListener implements Listener {

    private final PPlugin plugin;
    private final I18n lang;

    public InventoryListener(final @NonNull PPlugin plugin) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                executor.execute(event);
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                executor.open(event);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                executor.close(event);
            }
        }
    }
}
