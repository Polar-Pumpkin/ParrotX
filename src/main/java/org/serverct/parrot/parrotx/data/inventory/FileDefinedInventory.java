package org.serverct.parrot.parrotx.data.inventory;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.data.inventory.element.BaseElement;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.FileUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FileDefinedInventory extends BaseInventory implements FileSaved {

    protected File file;
    @Getter
    private final Map<String, ConfigurationSection> itemMap = new HashMap<>();
    @Getter
    protected FileConfiguration data;
    @Getter
    protected ConfigurationSection items;

    public FileDefinedInventory(PPlugin plugin, Player user, File file) {
        super(plugin, user, null, 6);
        setFile(file);
        load(file);
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
        this.data = YamlConfiguration.loadConfiguration(this.file);
        this.items = this.data.getConfigurationSection("Items");

        final ConfigurationSection settings = this.data.getConfigurationSection("Settings");
        if (BasicUtil.isNull(plugin, settings, I18n.BUILD, name(), "Gui 设置配置节为 null")) {
            addSetting("title", "未初始化 Gui - " + file.getName());
            addSetting("row", 6);
        } else for (final String key : settings.getKeys(true)) {
            final Object value = settings.get(key);
            lang.log.debug("加载 Gui {0} 设置: {1} -> {2}", name(), key, value);
            addSetting(key.toLowerCase(), value);
        }

        this.items.getKeys(false).forEach(key -> this.itemMap.put(key, items.getConfigurationSection(key)));
    }

    @Override
    public String getFilename() {
        return FileUtil.getNoExFilename(file);
    }

    @Override
    public String name() {
        return "Gui/" + getFilename();
    }

    public BaseElement get(final String key, final int priority, final Predicate<Player> condition) {
        return BaseElement.of(plugin, items.getConfigurationSection(key), priority, condition);
    }

    public BaseElement get(final String key, final int priority) {
        return get(key, priority, user -> true);
    }
}
