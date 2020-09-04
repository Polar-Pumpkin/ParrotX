package org.serverct.parrot.parrotx.data.inventory;

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
import org.serverct.parrot.parrotx.data.inventory.element.*;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("AccessStaticViaInstance")
public abstract class BaseInventory<T> implements InventoryExecutor {

    protected final PPlugin plugin;
    protected final T data;
    protected final Player viewer;
    protected final File file;
    protected final FileConfiguration settings;
    protected final String title;
    protected final int row;
    @Getter
    private final Map<String, InventoryElement> elementMap = new HashMap<>();
    @Getter
    private final Map<String, ItemStack> placedItemMap = new HashMap<>();
    @Getter
    private final Map<Integer, Object> indexTemplateMap = new HashMap<>();
    @Getter
    private final Map<Integer, String> slotMap = new HashMap<>();
    protected Inventory inventory;
    @Getter
    @Setter
    private int refreshInterval = -1;
    @Getter
    @Setter
    private BukkitTask refreshTask;
    private final I18n lang;

    public BaseInventory(PPlugin plugin, T data, Player user, File file) {
        this.plugin = plugin;
        this.lang = plugin.lang;
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
        this.lang = plugin.lang;
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
        Collections.reverse(elements);

        for (InventoryElement element : elements) {
            if (element instanceof InventoryCondition) {
                final InventoryCondition condition = (InventoryCondition) element;
                if (Objects.isNull(condition.getUser())) {
                    condition.setUser(viewer);
                }
            }
            final BaseElement base = element.getBase();
            if (Objects.isNull(base.getXPos()) || Objects.isNull(base.getYPos())) {
                continue;
            }

            ItemStack item = base.getItem();
            if (BasicUtil.isNull(plugin, item, I18n.BUILD, getTypename(), "Gui 元素 " + base.getName() + " 的显示物品为 null")) {
                item = new ItemStack(Material.AIR);
            }

            if (!base.condition(viewer)) {
                continue;
            }

            ListIterator<?> contentIterator = null;
            if (element instanceof InventorySwitch) {
                final InventorySwitch switchElem = (InventorySwitch) element;
                if (switchElem.isActive()) {
                    item = switchElem.getActiveItem();
                }
            } else if (element instanceof InventoryPlaceholder) {
                final ItemStack placedItem = this.placedItemMap.get(base.getName());
                if (Objects.nonNull(placedItem)) {
                    item = placedItem;
                }
            } else if (element instanceof InventoryTemplate) {
                contentIterator = new ArrayList<>(((InventoryTemplate<?>) element).getContents()).listIterator();
            }

            final List<Integer> slots = Position.get(base.getXPos(), base.getYPos());
            for (int slot : slots) {
                this.slotMap.put(slot, base.getName());

                if (element instanceof InventoryTemplate) {
                    final InventoryTemplate<?> listElem = (InventoryTemplate<?>) element;
                    if (contentIterator.hasNext()) {
                        final Object content = contentIterator.next();
                        item = listElem.apply(content);
                        this.indexTemplateMap.put(slot, content);
                    } else {
                        break;
                    }
                }

                result.setItem(slot, item);

                if (element instanceof InventoryProcessBar) {
                    final InventoryProcessBar barElem = (InventoryProcessBar) element;
                    if (barElem.getTotal() > 0) {
                        final int processAmount = BigDecimal.valueOf(slots.size() * barElem.getRate()).intValue();
                        ItemStack processItem = barElem.getProcessItem();
                        if (BasicUtil.isNull(plugin, processItem, I18n.BUILD, getTypename(), "Gui 元素 " + base.getName() + " 被标记进度条化显示但是进度显示物品为 null")) {
                            processItem = new ItemStack(Material.AIR);
                        }
                        for (int amount = 0; amount < processAmount; amount++) {
                            result.setItem(slot, processItem);
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

        InventoryElement element = getElement(event.getSlot());
        if (Objects.isNull(element) || !element.isClickable()) {
            event.setCancelled(true);
            return;
        }
        if (element instanceof InventoryCondition) {
            element = ((InventoryCondition) element).getElement();
        } else if (element instanceof InventoryTemplate) {
            element = ((InventoryTemplate<?>) element).getElement();
        }

        final BaseElement base = element.getBase();
        final ItemStack slotItem = event.getCurrentItem();
        final ItemStack cursorItem = event.getCursor();

        if (element instanceof InventorySwitch) {
            event.setCancelled(true);
            final InventorySwitch switchElem = (InventorySwitch) element;
            if (!switchElem.condition(viewer)) {
                return;
            }
            switchElem.setActive(!switchElem.isActive());
            switchElem.onSwitch();
            refresh(inv);
        } else if (element instanceof InventoryPlaceholder) {
            final InventoryPlaceholder placeholderElem = (InventoryPlaceholder) element;
            if (!placeholderElem.validate(cursorItem)) {
                event.setCancelled(true);
                return;
            }
            if (Objects.nonNull(slotItem) && Objects.nonNull(base.getItem()) && slotItem.isSimilar(base.getItem())) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> event.getView().setCursor(new ItemStack(Material.AIR)), 1L);
            }
            placedItemMap.put(base.getName(), cursorItem);
            placeholderElem.place(event);
            refresh(inv);
        } else if (element instanceof InventoryButton) {
            event.setCancelled(true);
            ((InventoryButton) element).click(event);
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
        this.elementMap.put(element.getBase().getName(), element);
    }

    protected InventoryElement getElement(int slot) {
        return this.elementMap.get(this.slotMap.get(slot));
    }

    protected InventoryElement getElement(String name) {
        return this.elementMap.get(name);
    }

    protected ItemStack getPlacedItem(final String elementName) {
        return this.placedItemMap.get(elementName);
    }

    protected Object getTemplateContent(final int slot) {
        return this.indexTemplateMap.get(slot);
    }
}
