package org.serverct.parrot.parrotx.data.inventory.element;

import lombok.Builder;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.inventory.InventoryElement;
import org.serverct.parrot.parrotx.data.inventory.PInventory;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Data
@Builder
public class BaseElement implements InventoryElement {

    private final int priority;
    private final String name;
    private final Supplier<ItemStack> item;
    private final String xPos;
    private final String yPos;
    private final List<Integer> specPos;
    private final Predicate<Player> condition;

    public static BaseElement of(final PPlugin plugin, @Nullable final ConfigurationSection section,
                                 final int priority, final Predicate<Player> condition) {
        if (BasicUtil.isNull(plugin, section, I18n.LOAD, "某 Gui 元素", "传入 Section 对象为 null")) {
            return null;
        }
        final String name = section.getName();
        final Supplier<ItemStack> item = () -> ItemUtil.build(section);

        final ConfigurationSection posSection = section.getConfigurationSection("Position");
        if (BasicUtil.isNull(plugin, posSection, I18n.LOAD, "Gui 元素 " + name, "未找到 Position 数据节")) {
            return null;
        }

        final String x = posSection.getString("X", "-1");
        final String y = posSection.getString("Y", "-1");
        if ("-1".equals(x) || "-1".equals(y)) {
            plugin.getLang().log.error("Gui 元素 {0} 的 Position 数据无效.", name);
            return null;
        }

        return BaseElement.builder()
                .priority(priority)
                .name(name)
                .item(item)
                .xPos(x)
                .yPos(y)
                .condition(condition)
                .build();
    }

    public boolean condition(final Player user) {
        return Objects.isNull(condition) || condition.test(user);
    }

    @Override
    public @NotNull BaseElement getBase() {
        return this;
    }

    @Override
    public boolean isClickable() {
        return false;
    }

    @Override
    public List<Integer> getPositions() {
        if (Objects.isNull(xPos) || Objects.isNull(yPos)) {
            if (Objects.isNull(this.specPos)) {
                return new ArrayList<>();
            }
            return this.specPos;
        }
        return Position.get(xPos, yPos);
    }

    @Override
    public ItemStack parseItem(PInventory<?> inv, int slot) {
        return this.item.get();
    }

    @Override
    public BaseElement preload(PInventory<?> inv) {
        return this;
    }

    @Override
    public void click(final PInventory<?> holder, final InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
