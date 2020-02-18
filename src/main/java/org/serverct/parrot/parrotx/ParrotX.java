package org.serverct.parrot.parrotx;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

public final class ParrotX extends JavaPlugin {

    @Getter private static Plugin instance;
    public static LocaleUtil lang;
    public static String localeKey;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
    }

    public void init() {

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
