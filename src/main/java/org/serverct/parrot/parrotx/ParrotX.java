package org.serverct.parrot.parrotx;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class ParrotX extends JavaPlugin {

    public static final int PLUGIN_ID = 9515;

    @Override
    public void onEnable() {
        final Metrics metrics = new Metrics(this, PLUGIN_ID);
        metrics.addCustomChart(new Metrics.SimplePie("integration_method", () -> "Depend"));

        final org.serverct.parrot.parrotx.utils.Metrics cStats = new org.serverct.parrot.parrotx.utils.Metrics(this);
    }
}
