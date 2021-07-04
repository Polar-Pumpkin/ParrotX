package org.serverct.parrot.parrotx.utils;

import com.cryptomorin.xseries.XMaterial;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class ItemBuilder {

    private String display;
    private XMaterial material;
    private int amount;
    private List<String> lore;
    private Map<Enchantment, Integer> enchants;
    private List<ItemFlag> flags;

    @NotNull
    public static ItemBuilder start() {
        return new ItemBuilder();
    }

    @NotNull
    public ItemBuilder display(@Nullable final String display) {
        this.display = display;
        return this;
    }

    @NotNull
    public ItemBuilder material(@NotNull final XMaterial material) {
        this.material = material;
        return this;
    }

    @NotNull
    public ItemBuilder amount(final int amount) {
        this.amount = amount;
        return this;
    }

    @NotNull
    public ItemBuilder lore(@Nullable final List<String> lore) {
        if (Objects.isNull(lore)) {
            return this;
        }
        if (Objects.isNull(this.lore)) {
            this.lore = new ArrayList<>();
        }
        this.lore.addAll(lore);
        return this;
    }

    @NotNull
    public ItemBuilder lore(@NotNull final String... lores) {
        return lore(Arrays.asList(lores));
    }

    @NotNull
    public ItemBuilder elseLore(@NotNull final BooleanSupplier condition,
                                @NotNull final List<String> lores, @NotNull final List<String> elseLores) {
        return elseDo(condition, builder -> builder.lore(lores), builder -> builder.lore(elseLores));
    }

    @NotNull
    public ItemBuilder orLore(@NotNull final BooleanSupplier condition, @NotNull final List<String> lores) {
        return orDo(condition, builder -> builder.lore(lores));
    }

    @NotNull
    public ItemBuilder orLore(@NotNull final BooleanSupplier condition, @NotNull final String... lores) {
        return orDo(condition, builder -> builder.lore(lores));
    }

    @NotNull
    public ItemBuilder enchant(@Nullable final Enchantment enchant, final int level) {
        if (Objects.isNull(enchant)) {
            return this;
        }
        if (Objects.isNull(this.enchants)) {
            this.enchants = new HashMap<>();
        }
        this.enchants.put(enchant, level);
        return this;
    }

    @NotNull
    public ItemBuilder flag(@Nullable final ItemFlag flag) {
        if (Objects.isNull(flag)) {
            return this;
        }
        if (Objects.isNull(this.flags)) {
            this.flags = new ArrayList<>();
        }
        this.flags.add(flag);
        return this;
    }

    @NotNull
    public ItemBuilder orDo(@NotNull final BooleanSupplier condition, @Nullable final Consumer<ItemBuilder> todo) {
        if (condition.getAsBoolean()) {
            BasicUtil.canDo(todo, action -> action.accept(this));
        }
        return this;
    }

    @NotNull
    public ItemBuilder elseDo(@NotNull final BooleanSupplier condition,
                              @Nullable final Consumer<ItemBuilder> todo,
                              @Nullable final Consumer<ItemBuilder> elseDo) {
        if (condition.getAsBoolean()) {
            BasicUtil.canDo(todo, action -> action.accept(this));
        } else {
            BasicUtil.canDo(elseDo, action -> action.accept(this));
        }
        return this;
    }

    @NotNull
    public Supplier<ItemStack> supplier() {
        return this::build;
    }

    @NotNull
    public ItemStack build() {
        if (Objects.isNull(this.material)) {
            return new ItemStack(Material.AIR);
        }

        final ItemStack item = this.material.parseItem();
        if (Objects.isNull(item)) {
            return new ItemStack(Material.AIR);
        }

        final ItemMeta meta = item.getItemMeta();
        if (Objects.isNull(meta)) {
            return item;
        }

        if (!StringUtils.isEmpty(this.display)) {
            meta.setDisplayName(I18n.color(this.display));
        }

        if (Objects.nonNull(this.lore)) {
            final List<String> color = new ArrayList<>(this.lore);
            color.replaceAll(I18n::color);
            meta.setLore(color);
        }

        if (Objects.nonNull(this.enchants)) {
            for (final Map.Entry<Enchantment, Integer> entry : this.enchants.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        if (Objects.nonNull(this.flags)) {
            for (final ItemFlag flag : this.flags) {
                meta.addItemFlags(flag);
            }
        }

        item.setItemMeta(meta);
        item.setAmount(Math.max(this.amount, 1));
        return item;
    }

}
