package org.serverct.parrot.parrotx.data.inventory;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class BaseInventory implements InventoryExecutor {

    @Getter
    protected final Player viewer;
    protected final PPlugin plugin;
    protected final I18n lang;
    protected final Map<String, Object> settings = new HashMap<>();
    protected Inventory inventory;


    public BaseInventory(PPlugin plugin, Player user, String title, int row) {
        this.plugin = plugin;
        this.lang = plugin.getLang();
        this.viewer = user;
        this.settings.put("title", title);
        this.settings.put("row", row);
    }

    @Override
    public PPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Inventory construct(final InventoryHolder executor) {
        return Bukkit.createInventory(executor, getRow() * 9, I18n.color(getTitle()));
    }

    @Override
    public void execute(InventoryClickEvent event) {
    }

    @Override
    public String name() {
        return "Gui/" + getTitle();
    }

    @Override
    public @NotNull Inventory getInventory() {
        if (Objects.isNull(this.inventory)) {
            this.inventory = construct(this);
        }
        return this.inventory;
    }

    protected void addSetting(final String key, final Object value) {
        this.settings.put(key, value);
    }

    protected <T> T getSetting(final String key, final Class<?> clazz) {
        return this.settings.containsKey(key) ? (T) clazz.cast(this.settings.get(key)) : null;
    }

    protected String getTitle() {
        return (String) this.settings.getOrDefault("title", "ParrotX 未初始化 Gui");
    }

    protected int getRow() {
        return (int) this.settings.getOrDefault("row", 1);
    }
}
