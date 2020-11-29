package org.serverct.parrot.parrotx.data.inventory;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.data.inventory.element.BaseElement;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.function.Predicate;

public class FileDefinedInventory extends BaseInventory implements FileSaved {

    protected File file;
    protected FileConfiguration settings;
    protected ConfigurationSection items;

    public FileDefinedInventory(PPlugin plugin, Player user, File file) {
        super(plugin, user, null, 6);
        setFile(file);
    }

    @Override
    public Inventory construct(final InventoryHolder executor) {
        load(file);
        return super.construct(executor);
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void load(@NonNull File file) {
        this.settings = YamlConfiguration.loadConfiguration(this.file);
        this.items = this.settings.getConfigurationSection("Items");
        final ConfigurationSection settingSection = this.settings.getConfigurationSection("Settings");
        if (BasicUtil.isNull(plugin, settingSection, I18n.BUILD, name(), "Gui 设置配置节为 null")) {
            this.title = "未初始化 Gui - " + file.getName();
            this.row = 6;
        } else {
            this.title = settingSection.getString("Title", this.file.getName());
            this.row = settingSection.getInt("Row", 6);
        }
    }

    @Override
    public String getFilename() {
        return BasicUtil.getNoExFileName(file.getName());
    }

    @Override
    public String name() {
        return "Gui/" + getFilename();
    }

    public BaseElement get(final String key, final int priority, final Predicate<Player> condition) {
        return BaseElement.of(plugin, items.getConfigurationSection(key), priority, condition);
    }
}
