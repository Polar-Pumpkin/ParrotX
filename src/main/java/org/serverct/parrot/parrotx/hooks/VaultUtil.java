package org.serverct.parrot.parrotx.hooks;

import lombok.Getter;
import lombok.NonNull;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.serverct.parrot.parrotx.PPlugin;

@SuppressWarnings({"unused"})
public class VaultUtil {

    private final PPlugin plugin;
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
                plugin.getLang().log.warn("未找到 &cVault&7, 扣费功能将被禁用.");
            } else {
                hooks = true;
                plugin.getLang().log.info("已连接 &cVault&7.");
            }
        } else {
            plugin.getLang().log.info("未启用与 &cVault &7的链接.");
        }
    }

    private boolean setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            economy = rsp.getProvider();
            return true;
        }
        return false;
    }

    public double getBalances(OfflinePlayer player) {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null) {
            if (!vault.isEnabled()) {
                return 0.0D;
            }
            return this.economy.getBalance(player);
        }
        return 0;
    }

    public boolean give(OfflinePlayer player, double amount) {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null) {
            if (vault.isEnabled()) {
                EconomyResponse localEconomyResponse = this.economy.depositPlayer(player, amount);
                return localEconomyResponse.transactionSuccess();
            }
        }
        return false;
    }

    public boolean take(OfflinePlayer player, double amount) {
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
