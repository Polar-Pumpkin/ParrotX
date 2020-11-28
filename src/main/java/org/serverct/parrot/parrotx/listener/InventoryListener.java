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
import org.serverct.parrot.parrotx.utils.i18n.I18n;

public class InventoryListener implements Listener {

    private final PPlugin plugin;
    private final I18n lang;

    public InventoryListener(final @NonNull PPlugin plugin) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
    }

    @EventHandler
    public void onEvent(InventoryEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof InventoryExecutor) {
            final InventoryExecutor executor = (InventoryExecutor) holder;
            if (plugin.equals(executor.getPlugin())) {
                lang.log.debug("监听到属于插件的 Inventory 事件: {0}", event.toString());

                if (event instanceof InventoryClickEvent) executor.execute((InventoryClickEvent) event);
                else if (event instanceof InventoryOpenEvent) executor.open((InventoryOpenEvent) event);
                else if (event instanceof InventoryCloseEvent) executor.close((InventoryCloseEvent) event);
                else executor.event(event);
            }
        }
    }
}
