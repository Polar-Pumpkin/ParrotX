package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.flags.Timestamp;
import org.serverct.parrot.parrotx.flags.Uniqued;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Goal implements Timestamp, Uniqued {

    private PPlugin plugin;
    private PID id;
    private long startTime;
    @Getter
    private Map<Type, Integer> digitalRemain = new HashMap<>();
    @Getter
    private Map<Material, Integer> itemRemain = new HashMap<>();

    public Goal(PID id, Map<Type, Integer> digital, Map<Material, Integer> item) {
        this.id = id;
        this.plugin = id.getPlugin();
        this.digitalRemain = digital;
        this.itemRemain = item;
        this.startTime = System.currentTimeMillis();
    }

    /*
     * Goal:
     *   StartTime: 1582454325042
     *   Remain:
     *     Digital:
     *       Money: 111
     *       Experience: 999
     *     Items:
     *       WOOL: 999
     *       WOOD: 666
     */
    public Goal(PID id, @NonNull ConfigurationSection section) {
        this.id = id;
        this.plugin = id.getPlugin();
        this.startTime = section.getLong("StartTime");

        try {
            ConfigurationSection remain = section.getConfigurationSection("Remain");
            if (remain != null) {
                ConfigurationSection digital = remain.getConfigurationSection("Digital");
                if (digital != null) {
                    for (String type : digital.getKeys(false)) {
                        digitalRemain.put(Type.valueOf(type.toUpperCase()), digital.getInt(type));
                    }
                }
                ConfigurationSection item = remain.getConfigurationSection("Item");
                if (item != null) {
                    for (String material : item.getKeys(false)) {
                        itemRemain.put(Material.valueOf(material.toUpperCase()), item.getInt(material));
                    }
                }
            }
        } catch (Throwable e) {
            plugin.lang.logError(I18n.LOAD, "目标(" + id.getKey() + ")", e, null);
        }
    }

    public void save(@NonNull ConfigurationSection section) {
        section.set("StartTime", startTime);
        ConfigurationSection remain = section.createSection("Remain");
        ConfigurationSection digital = remain.createSection("Digital");
        for (Type type : digitalRemain.keySet()) {
            digital.set(type.toString(), digitalRemain.get(type));
        }
        ConfigurationSection item = remain.createSection("Item");
        for (Material material : itemRemain.keySet()) {
            item.set(material.toString(), itemRemain.get(material));
        }
    }

    public int contribute(Type type, int amount) {
        if (type == Type.ITEM) {
            plugin.lang.logError(I18n.LOAD, "目标(" + id.getKey() + ")", "尝试数字化提交物品.");
            return 0;
        }
        int result = digitalRemain.get(type) - amount;
        if (result <= 0) {
            digitalRemain.put(type, 0);
            return -result;
        }
        digitalRemain.put(type, result);
        return 0;
    }

    public Map<Material, Integer> contribute(Inventory inventory) {
        Map<Material, Integer> result = new HashMap<>();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                Material material = item.getType();
                if (itemRemain.containsKey(material)) {
                    inventory.removeItem(item);
                    int resultAmount = itemRemain.get(material) - item.getAmount();
                    if (resultAmount < 0) {
                        item.setAmount(-resultAmount);
                        inventory.addItem(item);
                        result.put(material, result.getOrDefault(material, 0) + itemRemain.get(material));
                        itemRemain.put(material, 0);
                    }
                    if (resultAmount >= 0) {
                        result.put(material, result.getOrDefault(material, 0) + item.getAmount());
                        itemRemain.put(material, resultAmount);
                    }
                }
            }
        }
        return result;
    }

    public List<String> list(String prefix) {
        List<String> result = new ArrayList<>();
        this.digitalRemain.forEach(
                (type, value) -> {
                    if (value == 0) {
                        result.add(prefix + "&f[&a&l✔&f] &a&m" + type.getName() + " ▶ " + value);
                    } else {
                        result.add(prefix + "&f[  &f] &7" + type.getName() + " ▶ &c" + value);
                    }
                }
        );
        this.itemRemain.forEach(
                (material, value) -> {
                    ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
                    if (meta != null) {
                        String name = ItemStackUtil.getName(plugin, material);
                        if (value == 0) {
                            result.add(prefix + "&f[&a&l✔&f] &a&m" + name + " ▶ " + value);
                        } else {
                            result.add(prefix + "&f[  &f] &7" + name + " ▶ &c" + value);
                        }
                    }
                }
        );
        result.replaceAll(s -> I18n.color(s));
        return result;
    }

    public boolean isFinish() {
        for (int digital : digitalRemain.values()) {
            if (digital > 0) {
                return false;
            }
        }
        for (int item : itemRemain.values()) {
            if (item > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public long getTimestamp() {
        return startTime;
    }

    @Override
    public void setTime(long time) {
        this.startTime = time;
    }

    @Override
    public PID getID() {
        return id;
    }

    @Override
    public void setID(@NonNull PID pid) {
        this.id = pid;
    }

    public enum Type {
        ITEM("物品"),
        MONEY("金钱"),
        EXPERIENCE("经验等级"),
        POINT("点数");

        private String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
