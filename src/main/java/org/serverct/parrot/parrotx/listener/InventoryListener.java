package org.serverct.parrot.parrotx.listener;

import lombok.NonNull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.InventoryExecutor;

public class InventoryListener implements Listener {

    private final PPlugin plugin;

    public InventoryListener(final @NonNull PPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                executor.execute(event);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                executor.close(event);
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                executor.open(event);
            }
        }
    }

    @EventHandler
    public void onEvent(InventoryEvent event) {
        if (event instanceof InventoryCloseEvent
                || event instanceof InventoryClickEvent
                || event instanceof InventoryOpenEvent)
            return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                executor.event(event);
            }
        }
    }
}
