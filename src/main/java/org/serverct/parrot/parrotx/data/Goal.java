package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.flags.Timestamp;
import org.serverct.parrot.parrotx.data.flags.Unique;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.InventoryUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class Goal implements Timestamp, Unique {

    private final PPlugin plugin;
    private final I18n lang;
    private final PID id;
    private long startTime;
    @Getter
    private Map<Type, Integer> digitalRemain = new HashMap<>();
    @Getter
    private Map<Material, Integer> itemRemain = new HashMap<>();

    public Goal(PID id, Map<Type, Integer> digital, Map<Material, Integer> item) {
        this.id = id;
        this.plugin = id.getPlugin();
        this.lang = this.plugin.getLang();
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
        this.lang = this.plugin.getLang();
        this.startTime = section.getLong("StartTime");

        try {
            ConfigurationSection remain = section.getConfigurationSection("Remain");
            if (remain != null) {
                ConfigurationSection digital = remain.getConfigurationSection("Digital");
                if (digital != null) for (String type : digital.getKeys(false))
                    digitalRemain.put(EnumUtil.valueOf(Type.class, type.toUpperCase()), digital.getInt(type));
                ConfigurationSection item = remain.getConfigurationSection("Item");
                if (item != null) for (String material : item.getKeys(false))
                    itemRemain.put(EnumUtil.getMaterial(material.toUpperCase()), item.getInt(material));
            }
        } catch (Throwable e) {
            lang.log.error(I18n.LOAD, "目标/" + id.getKey(), e, plugin.getPackageName());
        }
    }

    public void save(@NonNull ConfigurationSection section) {
        section.set("StartTime", startTime);
        ConfigurationSection remain = section.createSection("Remain");
        ConfigurationSection digital = remain.createSection("Digital");
        digitalRemain.forEach((type, amount) -> digital.set(type.toString(), amount));
        ConfigurationSection item = remain.createSection("Item");
        itemRemain.forEach((material, amount) -> item.set(material.name(), amount));
    }

    public int contribute(Type type, int amount) {
        if (type == Type.ITEM) {
            lang.log.error(I18n.CONTRIBUTE, "目标/" + id.getKey(), "尝试数字化提交物品");
            return 0;
        }
        int result = digitalRemain.get(type) - amount;
        if (result < 0) {
            digitalRemain.put(type, 0);
            return -result;
        }
        digitalRemain.put(type, result);
        return 0;
    }

    public Map<Material, Integer> contribute(Inventory inventory) {
        Map<Material, Integer> result = new HashMap<>();
        InventoryUtil.filter(inventory, item -> item != null && item.getType() != Material.AIR).forEach(
                (slot, item) -> {
                    Material material = item.getType();
                    int contributed = result.getOrDefault(material, 0);
                    if (itemRemain.containsKey(material)) {
                        int remained = itemRemain.get(material) - item.getAmount();
                        if (remained < 0) {
                            item.setAmount(-remained);
                            inventory.setItem(slot, item);
                            contributed += itemRemain.get(material);
                            remained = 0;
                        } else {
                            inventory.removeItem(item);
                            contributed += item.getAmount();
                        }
                        itemRemain.put(material, remained);
                        result.put(material, contributed);
                    }
                }
        );
        return result;
    }

    public List<String> list(String prefix) {
        List<String> result = new ArrayList<>();
        this.digitalRemain.forEach(
                (type, value) -> {
                    if (value == 0) result.add(prefix + "&f[&a&l✔&f] &a&m" + type.getName() + " ▶ " + value);
                    else result.add(prefix + "&f[  &f] &7" + type.getName() + " ▶ &c" + value);
                }
        );
        this.itemRemain.forEach(
                (material, value) -> {
                    String name = ItemUtil.getName(plugin, material);
                    if (value == 0) result.add(prefix + "&f[&a&l✔&f] &a&m" + name + " ▶ " + value);
                    else result.add(prefix + "&f[  &f] &7" + name + " ▶ &c" + value);
                }
        );
        result.replaceAll(I18n::color);
        return result;
    }

    public boolean isFinish() {
        for (int digital : digitalRemain.values()) if (digital > 0) return false;
        for (int item : itemRemain.values()) if (item > 0) return false;
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

    public enum Type {
        ITEM("物品"),
        MONEY("金钱"),
        EXPERIENCE("经验等级"),
        POINT("点数");

        @Getter
        private final String name;

        Type(String name) {
            this.name = name;
        }
    }
}
