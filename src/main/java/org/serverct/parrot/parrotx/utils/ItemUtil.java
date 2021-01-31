package org.serverct.parrot.parrotx.utils;

import com.cryptomorin.xseries.XMaterial;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.ParrotX;
import org.serverct.parrot.parrotx.data.MappedData;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;

public class ItemUtil {

    public static ItemStack build(final PPlugin plugin, final LinkedHashMap<?, ?> map) {
        final MappedData data = MappedData.of(MappedData.filter(map));
        ItemStack result = new ItemStack(Material.AIR);

        try {
            result = XMaterial.valueOf(data.getString("Material")).parseItem();

            if (Objects.isNull(result)) {
                result = new ItemStack(Material.AIR);
                return result;
            }
            ItemMeta meta = result.getItemMeta();
            if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(result.getType());
            if (meta == null) return result;

            final String display = data.getString("Display");
            if (display != null) meta.setDisplayName(I18n.color(display));

            List<String> lore = data.getList("Lore", String.class);
            if (!lore.isEmpty()) {
                lore.replaceAll(I18n::color);
                meta.setLore(lore);
            }

            if (data.containsKey("Enchants")) {
                final MappedData enchant = MappedData.of(MappedData.filter((Map<?, ?>) data.get("Enchants")));
                for (String name : enchant.keySet()) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
                    if (enchantment == null) {
                        plugin.getLang().log.error(I18n.BUILD, "ItemStack", "目标附魔不存在: " + name);
                        continue;
                    }
                    meta.addEnchant(enchantment, enchant.getInt(name), true);
                }
            }

            if (data.containsKey("ItemFlags")) {
                final List<String> itemFlag = data.getList("ItemFlags", String.class);
                for (String flagName : itemFlag) {
                    ItemFlag flag = EnumUtil.valueOf(ItemFlag.class, flagName.toUpperCase());
                    if (flag == null) {
                        plugin.getLang().log.error(I18n.BUILD, "ItemStack", "目标 ItemFlag 不存在: " + flagName);
                        continue;
                    }
                    meta.addItemFlags(flag);
                }
            }

            result.setItemMeta(meta);
        } catch (Throwable e) {
            plugin.getLang().log.error(I18n.BUILD, "ItemStack/" + map, e, plugin.getPackageName());
        }
        return result;
    }

    @NotNull
    public static ItemStack build(final ConfigurationSection section) {
        final ConfigurationSection itemSection = section.getConfigurationSection("ItemStack");
        ItemStack result = new ItemStack(Material.AIR);

        if (itemSection == null) {
            ParrotX.log("未找到数据节: {0}.", section.getName());
            return result;
        }

        try {
            result = XMaterial.valueOf(itemSection.getString("Material")).parseItem();

            if (Objects.isNull(result)) {
                result = new ItemStack(Material.AIR);
                return result;
            }
            ItemMeta meta = result.getItemMeta();
            if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(result.getType());
            if (meta == null) return result;

            final String display = itemSection.getString("Display");
            if (display != null) meta.setDisplayName(I18n.color(display));

            List<String> lore = itemSection.getStringList("Lore");
            if (!lore.isEmpty()) {
                lore.replaceAll(I18n::color);
                meta.setLore(lore);
            }

            if (itemSection.isConfigurationSection("Enchants")) {
                ConfigurationSection enchant = itemSection.getConfigurationSection("Enchants");
                if (enchant != null) {
                    for (String name : enchant.getKeys(false)) {
                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
                        if (enchantment == null) {
                            ParrotX.log("目标附魔不存在: {0}.", name);
                            continue;
                        }
                        meta.addEnchant(enchantment, enchant.getInt(name), true);
                    }
                }
            }

            List<String> itemFlag = itemSection.getStringList("ItemFlags");
            if (!itemFlag.isEmpty()) {
                for (String flagName : itemFlag) {
                    ItemFlag flag = EnumUtil.valueOf(ItemFlag.class, flagName.toUpperCase());
                    if (flag == null) {
                        ParrotX.log("目标 ItemFlag 不存在: {0}.", flagName);
                        continue;
                    }
                    meta.addItemFlags(flag);
                }
            }

            result.setItemMeta(meta);
        } catch (Throwable e) {
            ParrotX.log("构建 ItemStack({0}) 时遇到错误: {1}.", section.getName(), e.getMessage());
        }
        return result;
    }

    public static void save(final @NonNull ItemStack item, final @NonNull ConfigurationSection section) {
        final ConfigurationSection itemSection = section.createSection("ItemStack");
        itemSection.set("Material", item.getType().name());

        final ItemMeta meta = item.getItemMeta();
        if (Objects.isNull(meta)) {
            return;
        }

        if (meta.hasDisplayName()) {
            itemSection.set("Display", meta.getDisplayName());
        }
        if (meta.hasLore()) {
            final List<String> lore = new ArrayList<>(Optional.ofNullable(meta.getLore()).orElse(new ArrayList<>()));
            lore.replaceAll(content -> I18n.deColor(content, '&'));
            itemSection.set("Lore", lore);
        }
        if (meta.hasEnchants()) {
            final ConfigurationSection enchantSection = itemSection.createSection("Enchants");
            meta.getEnchants().forEach((enchant, lvl) -> enchantSection.set(enchant.getKey().getKey(), lvl));
        }
        Set<ItemFlag> flags = meta.getItemFlags();
        if (!flags.isEmpty()) {
            final List<String> flagList = new ArrayList<>();
            flags.forEach(flag -> flagList.add(flag.name()));
            itemSection.set("ItemFlags", flagList);
        }
    }

    public static String getName(final @NonNull PPlugin plugin, final @NonNull Material material) {
        String name = material.name();
        if (plugin.getLang().hasLocale("Material")) {
            String result = plugin.getLang().data.get("Material", "Material", name);
            if (!result.contains("错误")) name = ChatColor.stripColor(result);
        }
        return name;
    }

    public static ItemStack replace(final @NonNull ItemStack item, final String placeholder, final String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (meta.hasDisplayName()) meta.setDisplayName(meta.getDisplayName().replace(placeholder, value));
        List<String> lore = meta.getLore();
        if (lore != null) {
            lore.replaceAll(s -> s.replace(placeholder, value));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
