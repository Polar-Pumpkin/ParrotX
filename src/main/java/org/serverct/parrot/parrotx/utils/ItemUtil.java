package org.serverct.parrot.parrotx.utils;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.ParrotX;
import org.serverct.parrot.parrotx.data.MappedData;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.Function;

public class ItemUtil {

    @NotNull
    public static ItemStack build(@NotNull final Map<?, ?> map,
                                  @NotNull final Function<String, ItemStack> constructor) {
        final MappedData data = MappedData.of(MappedData.filter(map));
        ItemStack result = new ItemStack(Material.AIR);

        try {
            result = constructor.apply(data.getString("Material"));

            if (Objects.isNull(result)) {
                result = new ItemStack(Material.AIR);
                return result;
            }

            ItemMeta meta = result.getItemMeta();
            if (Objects.isNull(meta)) {
                meta = Bukkit.getItemFactory().getItemMeta(result.getType());
            }
            if (Objects.isNull(meta)) {
                return result;
            }

            final String display = data.getString("Display");
            if (Objects.nonNull(display)) {
                meta.setDisplayName(I18n.color(display));
            }

            final List<String> lore = data.getList("Lore", String.class);
            if (!lore.isEmpty()) {
                lore.replaceAll(I18n::color);
                meta.setLore(lore);
            }

            if (data.containsKey("Enchants")) {
                final MappedData enchants = data.getMappedData("Enchants");
                for (final String name : enchants.keySet()) {
                    final Optional<XEnchantment> xEnchantment = XEnchantment.matchXEnchantment(name.toLowerCase());
                    if (!xEnchantment.isPresent()) {
                        ParrotX.log("构建 ItemStack 时读取到未知附魔: {0}.", name);
                        continue;
                    }

                    final Enchantment enchantment = xEnchantment.get().parseEnchantment();
                    if (Objects.isNull(enchantment)) {
                        ParrotX.log("构建 ItemStack 时读取到未知附魔: {0}.", name);
                        continue;
                    }
                    meta.addEnchant(enchantment, enchants.getInt(name), true);
                }
            }

            if (data.containsKey("ItemFlags")) {
                final List<String> flags = data.getList("ItemFlags", String.class);
                for (final String name : flags) {
                    final ItemFlag flag = EnumUtil.valueOf(ItemFlag.class, name.toUpperCase());
                    if (flag == null) {
                        ParrotX.log("构建 ItemStack 时读取到未知 ItemFlag: {0}.", name);
                        continue;
                    }
                    meta.addItemFlags(flag);
                }
            }

            result.setItemMeta(meta);
        } catch (final Exception exception) {
            ParrotX.log("构建 ItemStack 时遇到错误: {0}.", exception.getMessage());
            exception.printStackTrace();
        }
        return result;
    }

    @NotNull
    public static ItemStack build(@NotNull final ConfigurationSection section) {
        final ConfigurationSection itemSection = section.getConfigurationSection("ItemStack");
        if (itemSection == null) {
            return new ItemStack(Material.AIR);
        }
        return build(itemSection.getValues(false), ItemUtil::getByXMaterial);
    }

    @NotNull
    public static ItemStack build(@NotNull final ConfigurationSection section,
                                  @NotNull final Function<String, ItemStack> constructor) {
        final ConfigurationSection itemSection = section.getConfigurationSection("ItemStack");
        if (itemSection == null) {
            return new ItemStack(Material.AIR);
        }
        return build(itemSection.getValues(false), constructor);
    }

    @NotNull
    public static ItemStack getByXMaterial(final String material) {
        final Optional<XMaterial> xMaterial = XMaterial.matchXMaterial(material);
        if (!xMaterial.isPresent()) {
            return new ItemStack(Material.AIR);
        }

        final ItemStack item = xMaterial.get().parseItem();
        if (Objects.isNull(item)) {
            return new ItemStack(Material.AIR);
        }
        return item;
    }

    public static void save(@Nullable final ConfigurationSection section, @Nullable final ItemStack item) {
        if (Objects.isNull(section)) {
            return;
        }
        if (Objects.isNull(item)) {
            section.set("ItemStack", null);
            return;
        }
        final ConfigurationSection itemSection = section.createSection("ItemStack");

        final Material material = item.getType();
        final String materialName = material.name();
        if (Objects.isNull(EnumUtil.valueOf(Material.class, materialName))) {
            //noinspection deprecation
            itemSection.set("Material", material.getId());
        } else {
            itemSection.set("Material", item.getType().name());
        }

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

    @NotNull
    public static String getName(@NotNull final PPlugin plugin, @Nullable final Material material) {
        if (Objects.isNull(material)) {
            return "未知物品";
        }
        String name = material.name();
        if (plugin.getLang().hasLocale("Material")) {
            String result = plugin.getLang().data.get("Material", "Material", name);
            if (!result.contains("错误")) {
                name = ChatColor.stripColor(result);
            }
        }
        return name;
    }


    @Contract("null, _, _ -> null;!null, _, _ -> !null")
    @Nullable
    public static ItemStack replace(@Nullable final ItemStack item, @Nullable final String placeholder,
                                    @Nullable final String value) {
        if (Objects.isNull(item)) {
            return null;
        }
        if (Objects.isNull(placeholder)) {
            return item;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (meta.hasDisplayName()) {
            meta.setDisplayName(meta.getDisplayName().replace(placeholder, BasicUtil.thisOrElse(value, "")));
        }
        final List<String> lore = meta.getLore();
        if (Objects.nonNull(lore)) {
            lore.replaceAll(content -> content.replace(placeholder, BasicUtil.thisOrElse(value, "")));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    @Contract("null -> true")
    public static boolean invalid(@Nullable final ItemStack item) {
        return Objects.isNull(item) || item.getType() == Material.AIR;
    }
}
