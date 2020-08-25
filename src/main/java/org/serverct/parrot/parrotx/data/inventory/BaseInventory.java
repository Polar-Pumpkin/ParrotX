package org.serverct.parrot.parrotx.data.inventory;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class BaseInventory<T> implements InventoryExecutor {

    protected final PPlugin plugin;
    protected final T data;
    protected final Player viewer;
    protected final File file;
    protected final FileConfiguration settings;
    @Getter
    private final Map<String, InventoryElement> elementMap = new HashMap<>();
    @Getter
    private final Map<Integer, String> slotMap = new HashMap<>();
    @Getter
    @Setter
    private int refreshInterval = -1;
    @Getter
    @Setter
    private BukkitTask refreshTask;
    protected Inventory inventory;

    public BaseInventory(PPlugin plugin, T data, Player user, File file) {
        this.plugin = plugin;
        this.data = data;
        this.viewer = user;
        this.file = file;
        this.settings = YamlConfiguration.loadConfiguration(this.file);
    }

    @Override
    public Inventory construct() {
        final ConfigurationSection settingSection = this.settings.getConfigurationSection("Settings");
        if (BasicUtil.isNull(plugin, settingSection, I18n.BUILD, "Gui/" + this.file.getName(), "Gui 设置配置节为 null")) {
            return Bukkit.createInventory(this, 6 * 9, this.file.getName() + " 未初始化 Gui");
        }
        final Inventory result = Bukkit.createInventory(this, settingSection.getInt("Raw") * 9, I18n.color(settingSection.getString("Title", this.file.getName())));

        for (InventoryElement element : this.elementMap.values()) {
            if (Objects.isNull(element.xPos) || Objects.isNull(element.yPos)) {
                continue;
            }

            ItemStack item = element.item;
            if (BasicUtil.isNull(plugin, item, I18n.BUILD, "Gui/" + this.file.getName(), "Gui 元素 " + element.name + " 的显示物品为 null")) {
                item = new ItemStack(Material.AIR);
            }

            if (Objects.nonNull(element.condition) && !element.condition.test(viewer)) {
                continue;
            }

            if (element.switchable) {
                if (element.active) {
                    item = element.activeItem;
                }
            }

            final List<Integer> slots = Position.getPositionList(element.xPos, element.yPos);
            for (int slot : slots) {
                this.slotMap.put(slot, element.name);
                result.setItem(slot, item);

                if (element.processable) {
                    if (element.total > 0) {
                        final double rate = BigDecimal.valueOf(Math.min(1.0D, element.current / ((double) element.total))).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
                        final int activeAmount = BigDecimal.valueOf(slots.size() * rate).intValue();
                        ItemStack activeItem = element.processItem;
                        if (BasicUtil.isNull(plugin, activeItem, I18n.BUILD, "Gui/" + this.file.getName(), "Gui 元素 " + element.name + " 被标记进度条化显示但是进度显示物品为 null")) {
                            activeItem = new ItemStack(Material.AIR);
                        }
                        for (int amount = 0; amount < activeAmount; amount++) {
                            result.setItem(slot, activeItem);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void open(InventoryOpenEvent event) {
        if (this.refreshInterval > 0) {
            this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> refresh(event.getInventory()), 1L, refreshInterval * 20L);
        }
    }

    @Override
    public void close(InventoryCloseEvent event) {
        if (Objects.nonNull(this.refreshTask) && !this.refreshTask.isCancelled()) {
            this.refreshTask.cancel();
        }
    }

    @Override
    public void execute(InventoryClickEvent event) {
        final Inventory inv = event.getInventory();
        final Inventory clickedInv = event.getClickedInventory();
        if (Objects.isNull(clickedInv) || Objects.isNull(clickedInv.getHolder()) || !clickedInv.getHolder().equals(this)) {
            return;
        }

        final InventoryElement element = getElement(event.getSlot());
        if (Objects.isNull(element)) {
            event.setCancelled(true);
            return;
        }
        final ItemStack slotItem = event.getCurrentItem();
        final ItemStack cursorItem = event.getCursor();

        if (element.switchable) {
            event.setCancelled(true);
            if (Objects.isNull(element.switchCondition) || !element.switchCondition.test(viewer)) {
                return;
            }
            element.setActive(!element.active);
            if (Objects.nonNull(element.onSwitch)) {
                element.onSwitch.accept(element.active);
            }
            refresh(inv);
        } else if (element.placeable) {
            if (Objects.isNull(element.validate) || !element.validate.test(cursorItem)) {
                event.setCancelled(true);
                return;
            }
            if (Objects.nonNull(slotItem) && Objects.nonNull(element.item) && slotItem.isSimilar(element.item)) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> event.getView().setCursor(new ItemStack(Material.AIR)), 1L);
            }
            if (Objects.nonNull(element.onPlace)) {
                element.onPlace.accept(event);
            }
        } else if (element.clickable) {
            event.setCancelled(true);
            if (Objects.nonNull(element.onClick)) {
                element.onClick.accept(event);
            }
        }
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
        this.elementMap.put(element.getName(), element);
    }

    protected InventoryElement getElement(int slot) {
        return this.elementMap.get(this.slotMap.get(slot));
    }

    protected InventoryElement getElement(String name) {
        return this.elementMap.get(name);
    }

    protected @Data
    @Builder
    static class InventoryElement {
        private final String name;
        private final ItemStack item;
        private final String xPos;
        private final String yPos;
        private final Predicate<Player> condition;

        private final boolean clickable;
        private final Consumer<InventoryClickEvent> onClick;

        private final boolean processable;
        private final ItemStack processItem;
        private final int current;
        private final int total;

        private final boolean switchable;
        private final ItemStack activeItem;
        private final Predicate<Player> switchCondition;
        private final Consumer<Boolean> onSwitch;
        private boolean active;

        private final boolean placeable;
        private final Predicate<ItemStack> validate;
        private final Consumer<InventoryClickEvent> onPlace;
    }
}
