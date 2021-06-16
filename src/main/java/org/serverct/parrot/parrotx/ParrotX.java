package org.serverct.parrot.parrotx;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.text.MessageFormat;

public final class ParrotX extends JavaPlugin {

    public static final int PLUGIN_ID = 9515;

    @Getter
    @Setter
    private static boolean debugMode;

    @Override
    public void onEnable() {
//        final Metrics metrics = new Metrics(this, PLUGIN_ID);
//        metrics.addCustomChart(new SimplePie("integration_method", () -> "Depend"));

        final org.serverct.parrot.parrotx.utils.Metrics cStats = new org.serverct.parrot.parrotx.utils.Metrics(this);
    }

    public static void log(final String message, final Object... args) {
        Bukkit.getConsoleSender().sendMessage(I18n.color("&aParrotX &7>> &r" + MessageFormat.format(message, args)));
    }

    public static void debug(final String message, final Object... args) {
        if (!debugMode) {
            return;
        }
        log(message, args);
    }
}
