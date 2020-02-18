package org.serverct.parrot.parrotx.hooks;

import lombok.Getter;
import lombok.NonNull;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

public class VaultUtil {

    private PPlugin plugin;
    @Getter
    private boolean hooks;
    private Economy economy;

    public VaultUtil(@NonNull PPlugin plugin, boolean hooks) {
        this.plugin = plugin;
        this.hooks = hooks;
        loadVault();
    }

    public void loadVault() {
        if (hooks) {
            if (!setupEconomy()) {
                hooks = false;
                plugin.getLang().log("未找到 &cVault&7, 扣费功能将被禁用.", LocaleUtil.Type.WARN, false);
            } else {
                hooks = true;
                plugin.getLang().log("已连接 &cVault&7.", LocaleUtil.Type.INFO, false);
            }
        } else {
            plugin.getLang().log("未启用与 &cVault &7的链接.", LocaleUtil.Type.INFO, false);
        }
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            economy = rsp.getProvider();
            return economy != null;
        }
        return false;
    }

    public double getBalances(Player player) {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null) {
            if (!vault.isEnabled()) {
                return 0.0D;
            }
            return this.economy.getBalance(player);
        }
        return 0;
    }

    public boolean give(Player player, double amount) {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null) {
            if (vault.isEnabled()) {
                EconomyResponse localEconomyResponse = this.economy.depositPlayer(player, amount);
                return localEconomyResponse.transactionSuccess();
            }
        }
        return false;
    }

    public boolean take(Player player, double amount) {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null) {
            if (vault.isEnabled()) {
                EconomyResponse localEconomyResponse = this.economy.withdrawPlayer(player, amount);
                return localEconomyResponse.transactionSuccess();
            }
        }
        return false;
    }

}
