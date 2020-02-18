package org.serverct.parrot.parrotx.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemStackUtil {

    public static ItemStack build(ConfigurationSection section) {
        if (section != null) {
            try {
                ItemStack result = new ItemStack(Material.valueOf(Objects.requireNonNull(section.getString("Material")).toUpperCase()));
                ItemMeta meta = result.getItemMeta();

                if (meta == null) {
                    meta = Bukkit.getItemFactory().getItemMeta(result.getType());
                }

                if(meta == null) {
                    return result;
                }

                String display = section.getString("Display");
                if (display != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', display));
                }

                List<String> lore = section.getStringList("Lore");
                if (!lore.isEmpty()) {
                    List<String> resultLore = new ArrayList<>();
                    for (String str : lore) {
                        resultLore.add(ChatColor.translateAlternateColorCodes('&', str));
                    }
                    meta.setLore(resultLore);
                }

                if (section.isConfigurationSection("Enchants")) {
                    ConfigurationSection enchant = section.getConfigurationSection("Enchants");
                    if (enchant != null) {
                        for (String name : enchant.getKeys(false)) {
                            meta.addEnchant(
                                    Objects.requireNonNull(Enchantment.getByName(name.toUpperCase())),
                                    enchant.getInt(name),
                                    true
                            );
                        }
                    }
                }

                List<String> itemFlag = section.getStringList("ItemFlags");
                if (!itemFlag.isEmpty()) {
                    for (String flagName : itemFlag) {
                        meta.addItemFlags(ItemFlag.valueOf(flagName.toUpperCase()));
                    }
                }

                result.setItemMeta(meta);
                return result;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return new ItemStack(Material.AIR);
    }
}
