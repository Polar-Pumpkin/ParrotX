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
import org.serverct.parrot.parrotx.utils.LocaleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Goal implements Timestamp, Uniqued {

    private PPlugin plugin;
    private PID id;
    private long startTime;
    @Getter
    private Map<Type, Integer> digitalGoal = new HashMap<>();
    @Getter
    private Map<Material, Integer> itemGoal = new HashMap<>();
    @Getter
    private Map<Type, Integer> digitalRemain = new HashMap<>();
    @Getter
    private Map<Material, Integer> itemRemain = new HashMap<>();

    public Goal(PID id, Map<Type, Integer> digital, Map<Material, Integer> item) {
        this.id = id;
        this.plugin = id.getPlugin();
        this.digitalGoal = digital;
        this.digitalRemain = digital;
        this.itemGoal = item;
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
            ConfigurationSection goal = section.getConfigurationSection("All");
            if (goal == null) {
                plugin.lang.logError(LocaleUtil.LOAD, "目标(" + id.getKey() + ")", "All 数据节为 null.");
                return;
            }
            ConfigurationSection digitalAll = goal.getConfigurationSection("Digital");
            if (digitalAll != null) {
                for (String type : digitalAll.getKeys(false)) {
                    digitalRemain.put(Type.valueOf(type.toUpperCase()), digitalAll.getInt(type));
                }
            }
            ConfigurationSection itemAll = goal.getConfigurationSection("Items");
            if (itemAll != null) {
                for (String material : itemAll.getKeys(false)) {
                    itemRemain.put(Material.valueOf(material.toUpperCase()), itemAll.getInt(material));
                }
            }

            ConfigurationSection remain = section.getConfigurationSection("Remain");
            if (remain != null) {
                ConfigurationSection digital = remain.getConfigurationSection("Digital");
                if (digital != null) {
                    for (String type : digital.getKeys(false)) {
                        digitalRemain.put(Type.valueOf(type.toUpperCase()), digital.getInt(type));
                    }
                }
                ConfigurationSection item = remain.getConfigurationSection("Items");
                if (item != null) {
                    for (String material : item.getKeys(false)) {
                        itemRemain.put(Material.valueOf(material.toUpperCase()), item.getInt(material));
                    }
                }
            }
        } catch (Throwable e) {
            plugin.lang.logError(LocaleUtil.LOAD, "目标(" + id.getKey() + ")", e.toString());
        }
    }

    public void save(@NonNull ConfigurationSection section) {
        section.set("StartTime", startTime);
        ConfigurationSection all = section.createSection("All");
        ConfigurationSection digitalAll = all.createSection("Digital");
        for (Type type : digitalGoal.keySet()) {
            digitalAll.set(type.toString(), digitalGoal.get(type));
        }
        ConfigurationSection itemAll = all.createSection("Items");
        for (Material material : itemGoal.keySet()) {
            itemAll.set(material.toString(), itemGoal.get(material));
        }

        ConfigurationSection remain = section.createSection("Remain");
        ConfigurationSection digital = remain.createSection("Digital");
        for (Type type : digitalRemain.keySet()) {
            digital.set(type.toString(), digitalRemain.get(type));
        }
        ConfigurationSection item = remain.createSection("Items");
        for (Material material : itemRemain.keySet()) {
            item.set(material.toString(), itemRemain.get(material));
        }
    }

    public int contribute(Type type, int amount) {
        if (type == Type.ITEM) {
            plugin.lang.logError(LocaleUtil.LOAD, "目标(" + id.getKey() + ")", "尝试数字化提交物品.");
            return 0;
        }
        int result = digitalRemain.get(type) - amount;
        if (result <= 0) {
            digitalRemain.remove(type);
            return result * -1;
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
                    result.put(material, item.getAmount());
                    if (resultAmount < 0) {
                        item.setAmount(resultAmount * -1);
                        inventory.addItem(item);
                    }
                    if (resultAmount <= 0) {
                        itemRemain.remove(material);
                    }
                    if (resultAmount > 0) {
                        itemRemain.put(material, resultAmount);
                    }
                }
            }
        }
        return result;
    }

    public List<String> list(String prefix) {
        List<String> result = new ArrayList<>();
        this.digitalGoal.forEach(
                (type, value) -> {
                    int remain = digitalRemain.getOrDefault(type, 0);
                    if (remain == 0) {
                        result.add(prefix + "&f[&a&l✔&f] &a&m" + type.getName() + " ▶ " + value);
                    } else {
                        result.add(prefix + "&f[  &f] &7" + type.getName() + " ▶ &c" + value + " &7(已提交 &c" + remain + "&7)");
                    }
                }
        );
        this.itemGoal.forEach(
                (material, value) -> {
                    int remain = itemRemain.getOrDefault(material, 0);
                    ItemMeta meta = Bukkit.getItemFactory().getItemMeta(material);
                    if (meta != null) {
                        String name = meta.hasLocalizedName() ? meta.getLocalizedName() : material.toString();
                        if (remain == 0) {
                            result.add(prefix + "&f[&a&l✔&f] &a&m" + name + " ▶ " + value);
                        } else {
                            result.add(prefix + "&f[  &f] &7" + name + " ▶ &c" + value + " &7(已提交 &c" + remain + "&7)");
                        }
                    }
                }
        );
        result.replaceAll(s -> plugin.lang.color(s));
        return result;
    }

    public boolean isFinish() {
        return digitalRemain.isEmpty() && itemRemain.isEmpty();
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
