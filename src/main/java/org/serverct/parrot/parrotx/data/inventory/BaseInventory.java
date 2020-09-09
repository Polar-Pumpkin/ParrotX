package org.serverct.parrot.parrotx.data.inventory;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.element.BaseElement;
import org.serverct.parrot.parrotx.data.inventory.element.InventoryTemplate;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.*;

public abstract class BaseInventory<T> implements InventoryExecutor {

    @Getter
    protected final PPlugin plugin;
    @Getter
    protected final T data;
    @Getter
    protected final Player viewer;
    protected final File file;
    protected final FileConfiguration settings;
    protected final String title;
    protected final int row;
    @Getter
    protected final Map<String, Integer> pageMap = new HashMap<>();
    @Getter
    private final Map<String, InventoryElement> elementMap = new HashMap<>();
    @Getter
    private final Map<Integer, String> slotMap = new HashMap<>();
    protected Inventory inventory;
    @Getter
    @Setter
    private int refreshInterval = -1;
    @Getter
    @Setter
    private BukkitTask refreshTask;

    public BaseInventory(PPlugin plugin, T data, Player user, File file) {
        this.plugin = plugin;
        this.data = data;
        this.viewer = user;
        this.file = file;
        this.settings = YamlConfiguration.loadConfiguration(this.file);

        final ConfigurationSection settingSection = this.settings.getConfigurationSection("Settings");
        if (BasicUtil.isNull(plugin, settingSection, I18n.BUILD, getTypename(), "Gui 设置配置节为 null")) {
            this.title = "未初始化 Gui - " + file.getName();
            this.row = 6;
        } else {
            this.title = settingSection.getString("Title", this.file.getName());
            this.row = settingSection.getInt("Row", 6);
        }
    }

    public BaseInventory(PPlugin plugin, T data, Player user, String title, int row) {
        this.plugin = plugin;
        this.data = data;
        this.viewer = user;
        this.file = null;
        this.settings = null;
        this.title = title;
        this.row = row;
    }

    public String getTypename() {
        return "Gui/" + (Objects.isNull(this.file) ? this.title : this.file.getName());
    }

    @Override
    public Inventory construct() {
        final Inventory result = Bukkit.createInventory(this, this.row * 9, I18n.color(this.title));

        final List<InventoryElement> elements = new ArrayList<>(this.elementMap.values());
        elements.sort(Comparator.comparingInt(InventoryElement::getPriority));

        for (InventoryElement element : elements) {
            final BaseElement base = element.preload(this);

            if (!base.condition(viewer)) {
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
        if (this.refreshInterval > 0) {
            plugin.getLang().log.debug(getTypename() + " 刷新间隔大于 0: " + this.refreshInterval);
            this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> refresh(event.getInventory()), 1L, refreshInterval * 20L);
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        if (Objects.nonNull(this.refreshTask) && !this.refreshTask.isCancelled()) {
            plugin.getLang().log.debug(getTypename() + " 取消自动刷新任务");
            this.refreshTask.cancel();
        }
    }

    @Override
    public void execute(InventoryClickEvent event) {
        if (!check(event)) {
            return;
        }

        InventoryElement element = getElement(event.getSlot());
        if (Objects.isNull(element) || !element.isClickable()) {
            event.setCancelled(true);
            return;
        }

        element.click(this, event);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        if (Objects.isNull(this.inventory)) {
            this.inventory = construct();
        }
        return this.inventory;
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

    public int getPage(final String name) {
        return this.pageMap.getOrDefault(name, 0);
    }

    public int getMaxPage(final String name) {
        final InventoryElement element = getElement(name);
        if (element instanceof InventoryTemplate<?>) {
            return ((InventoryTemplate<?>) element).getContentMap().size();
        }
        return 0;
    }

    public void setPage(final String name, final int page) {
        this.pageMap.put(name, Math.min(getMaxPage(name), Math.max(page, 1)));
    }

    public int nextPage(final String name, final boolean cycle) {
        final int page = getPage(name) + 1;
        if (page > getMaxPage(name)) {
            return cycle ? 1 : -1;
        }
        return page;
    }

    public int previousPage(final String name, final boolean cycle) {
        final int page = getPage(name) - 1;
        if (page == 0) {
            return cycle ? getMaxPage(name) : -1;
        }
        return page;
    }
}
