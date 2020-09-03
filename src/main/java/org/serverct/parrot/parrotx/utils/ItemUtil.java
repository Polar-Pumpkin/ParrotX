package org.serverct.parrot.parrotx.utils;

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
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("AccessStaticViaInstance")
public class ItemUtil {

    public static ItemStack build(final @NonNull PPlugin plugin, final @NonNull ConfigurationSection section) {
        ConfigurationSection itemSection = section.getConfigurationSection("ItemStack");
        if (itemSection != null) {
            try {
                ItemStack result = new ItemStack(EnumUtil.getMaterial(itemSection.getString("Material", "AIR").toUpperCase()));
                ItemMeta meta = result.getItemMeta();

                if (meta == null) meta = Bukkit.getItemFactory().getItemMeta(result.getType());
                if (meta == null) return result;

                String display = itemSection.getString("Display");
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
                                plugin.lang.log.error(I18n.BUILD, "ItemStack", "目标附魔不存在: " + name);
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
                            plugin.lang.log.error(I18n.BUILD, "ItemStack", "目标 ItemFlag 不存在: " + flagName);
                            continue;
                        }
                        meta.addItemFlags(flag);
                    }
                }

                result.setItemMeta(meta);
                return result;
            } catch (Throwable e) {
                plugin.lang.log.error(I18n.BUILD, "ItemStack/" + section.getName(), e, null);
            }
        } else plugin.lang.log.error(I18n.BUILD, "ItemStack/" + section.getName(), "未找到 ItemStack 数据节");
        return new ItemStack(Material.AIR);
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
        if (meta.hasLocalizedName()) {
            itemSection.set("Lore", meta.getLore());
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
        if (plugin.lang.hasLocale("Material")) {
            String result = plugin.lang.data.get("Material", "Material", name);
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
