package org.serverct.parrot.parrotx.data.inventory;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;

public abstract class AutoRefreshInventory implements InventoryExecutor {

    protected final PPlugin plugin;
    protected final I18n lang;
    @Getter
    protected final BaseInventory base;
    @Getter
    @Setter
    private int refreshInterval = -1;
    @Getter
    @Setter
    private BukkitTask refreshTask;

    public AutoRefreshInventory(final BaseInventory base) {
        this.base = base;
        this.plugin = this.base.plugin;
        this.lang = this.base.lang;
    }

    public void startRefresh(final Inventory inv) {
        if (this.refreshInterval > 0) {
            lang.log.debug(name() + " 刷新间隔大于 0: " + this.refreshInterval + "s");
            this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> refresh(inv), 1L, refreshInterval * 20L);
        }
    }

    public void endRefresh() {
        if (Objects.nonNull(this.refreshTask) && !this.refreshTask.isCancelled()) {
            lang.log.debug(name() + " 取消自动刷新任务");
            this.refreshTask.cancel();
        }
    }

    @Override
    public String name() {
        return base.name();
    }

    @Override
    public PPlugin getPlugin() {
        return this.plugin;
    }
}
