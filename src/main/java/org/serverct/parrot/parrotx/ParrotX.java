package org.serverct.parrot.parrotx;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotx.listener.InventoryListener;

public final class ParrotX extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
    }
}
