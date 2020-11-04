package org.serverct.parrot.parrotx.data.inventory;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;

public class BaseInventory implements InventoryExecutor {

    @Getter
    protected final Player viewer;
    protected final PPlugin plugin;
    protected final I18n lang;
    protected String title;
    protected int row;
    protected Inventory inventory;


    public BaseInventory(PPlugin plugin, Player user, String title, int row) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.viewer = user;
        this.title = title;
        this.row = row;
        this.inventory = construct();
    }

    @Override
    public PPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Inventory construct() {
        return Bukkit.createInventory(this, this.row, I18n.color(this.title));
    }

    @Override
    public void execute(InventoryClickEvent event) {
    }

    @Override
    public String name() {
        return "Gui/" + this.title;
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (Objects.isNull(this.inventory)) {
            this.inventory = construct();
        }
        return this.inventory;
    }
}
