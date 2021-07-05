package org.serverct.parrot.parrotx.utils;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.ParrotX;
import org.serverct.parrot.parrotx.data.MappedData;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.Constructor;
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

            if (data.containsKey("NBT")) {
                final String nbtString = data.getString("NBT");
                if (StringUtils.isNotEmpty(nbtString)) {
                    final NBTItem nbt = new NBTItem(result);
                    final NBTContainer tag = new NBTContainer(nbtString);
                    nbt.mergeCompound(tag);
                }
            }

            final int amount = data.getInt("Amount", 1);
            result.setAmount(amount);

            ItemMeta meta = result.getItemMeta();
            if (Objects.isNull(meta)) {
                meta = Bukkit.getItemFactory().getItemMeta(result.getType());
            }
            if (Objects.isNull(meta)) {
                return result;
            }

            final int damage = data.getInt("Durability", -1);
            if (damage != -1) {
                if (XMaterial.isNewVersion()) {
                    if (meta instanceof Damageable) {
                        final Damageable damageable = (Damageable) meta;
                        damageable.setDamage(damage);
                    }
                } else {
                    //noinspection deprecation
                    result.setDurability((short) damage);
                }
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

            if (XMaterial.supports(8) && data.containsKey("ItemFlags")) {
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
        return build(itemSection.getValues(false), ItemUtil::compatibleGet);
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

    @SuppressWarnings("JavaReflectionMemberAccess")
    @NotNull
    public static ItemStack compatibleGet(final String material) {
        if (StringUtils.isNumeric(material)) {
            try {
                final int id = Integer.parseInt(material);
                final Constructor<ItemStack> constructor = ItemStack.class.getConstructor(int.class);
                return constructor.newInstance(id);
            } catch (NoSuchMethodException exception) {
                ParrotX.log("尝试构建数字 Material 的物品, 但是获取数字 ID 构造器失败: &c{0}&r.", material);
            } catch (Throwable error) {
                ParrotX.log("尝试兼容性获取物品失败: &c{0}&r.", material);
                error.printStackTrace();
            }
        }

        final Material vanilla = EnumUtil.getMaterial(material.toUpperCase());
        if (Objects.nonNull(vanilla)) {
            return new ItemStack(vanilla);
        }

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

    public static void save(@Nullable final ConfigurationSection rootSection, @Nullable final ItemStack item) {
        if (Objects.isNull(rootSection)) {
            return;
        }
        if (Objects.isNull(item)) {
            rootSection.set("ItemStack", null);
            return;
        }
        final ConfigurationSection section = rootSection.createSection("ItemStack");

        final Material material = item.getType();
        final String materialName = material.name();
        if (Objects.isNull(EnumUtil.valueOf(Material.class, materialName))) {
            //noinspection deprecation
            section.set("Material", material.getId());
        } else {
            section.set("Material", item.getType().name());
        }

        final int amount = item.getAmount();
        if (amount > 1) {
            section.set("Amount", amount);
        }

        final ItemMeta meta = item.getItemMeta();
        if (Objects.isNull(meta)) {
            return;
        }

        if (XMaterial.isNewVersion()) {
            if (meta instanceof Damageable) {
                final Damageable damage = (Damageable) meta;
                section.set("Durability", damage.getDamage());
            }
        } else {
            //noinspection deprecation
            section.set("Durability", item.getDurability());
        }

        if (meta.hasDisplayName()) {
            section.set("Display", meta.getDisplayName());
        }

        if (meta.hasLore()) {
            final List<String> lore = new ArrayList<>(Optional.ofNullable(meta.getLore()).orElse(new ArrayList<>()));
            lore.replaceAll(content -> I18n.deColor(content, '&'));
            section.set("Lore", lore);
        }

        if (meta.hasEnchants()) {
            final ConfigurationSection enchantSection = section.createSection("Enchants");
            meta.getEnchants().forEach((enchant, lvl) -> enchantSection.set(enchant.getKey().getKey(), lvl));
        }

        if (XMaterial.supports(8)) {
            Set<ItemFlag> flags = meta.getItemFlags();
            if (!flags.isEmpty()) {
                final List<String> flagList = new ArrayList<>();
                flags.forEach(flag -> flagList.add(flag.name()));
                section.set("ItemFlags", flagList);
            }
        }

        final NBTItem nbt = new NBTItem(item);
        section.set("NBT", nbt.toString());
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

    @NotNull
    public static String getName(@NotNull final PPlugin plugin, @Nullable final ItemStack item) {
        if (Objects.isNull(item)) {
            return "无效物品";
        }

        final ItemMeta meta = item.getItemMeta();
        final String display = BasicUtil.canReturn(meta, ItemMeta::getDisplayName);
        if (StringUtils.isEmpty(display)) {
            return getName(plugin, item.getType());
        }
        return display;
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

    @NotNull
    public static List<String> getLore(@Nullable final ItemStack item) {
        final List<String> result = new ArrayList<>();
        if (Objects.isNull(item)) {
            return result;
        }

        final ItemMeta meta = item.getItemMeta();
        if (Objects.isNull(meta)) {
            return result;
        }

        final List<String> lore = meta.getLore();
        if (Objects.isNull(lore)) {
            return result;
        }

        result.addAll(lore);
        return result;
    }

    @NotNull
    public static List<ItemStack> copy(@NotNull final ItemStack item, int amount) {
        final List<ItemStack> result = new ArrayList<>();

        final int maxStackSize = item.getMaxStackSize();
        if (amount <= maxStackSize) {
            final ItemStack copy = item.clone();
            copy.setAmount(amount);
            result.add(copy);
            return result;
        }

        while (amount > 0) {
            final ItemStack copy = item.clone();
            copy.setAmount(Math.min(amount, maxStackSize));
            result.add(copy);
            amount -= maxStackSize;
        }
        return result;
    }
}
