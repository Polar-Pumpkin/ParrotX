package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PDataFolder;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;

public class PFolder implements PDataFolder {

    protected PPlugin plugin;
    @Getter
    protected File folder;
    @Getter
    protected FileConfiguration config;
    private String id;
    private String name;

    public PFolder(@NonNull PPlugin plugin, String folderName, String typeName) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder() + File.separator + folderName);
        this.id = folderName;
        this.name = typeName;
    }

    @Override
    public String getTypeName() {
        return name + "/" + id;
    }

    @Override
    public String getFolderName() {
        return id;
    }

    @Override
    public void releaseDefaultData() {
    }

    @Override
    public void init() {
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                releaseDefaultData();
                plugin.lang.log("未找到 &c" + getTypeName() + "&7, 已重新生成.", I18n.Type.WARN, false);
            } else {
                plugin.lang.log("尝试生成 &c" + getTypeName() + " &7失败.", I18n.Type.ERROR, false);
            }
        }
        File[] files = folder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        if (files == null || files.length == 0) {
            releaseDefaultData();
            files = folder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        }
        if (files != null && files.length != 0) {
            for (File file : files) {
                load(file);
            }
            plugin.lang.log("共加载 &c" + getTypeName() + " &7中的 &c" + files.length + " &7个数据文件.", I18n.Type.INFO, false);
        } else {
            plugin.lang.log("&c" + getTypeName() + " &7中没有数据可供加载.", I18n.Type.WARN, false);
        }
    }

    @Override
    public void load(File file) {
    }

    @Override
    public void reloadAll() {
        plugin.lang.logAction(I18n.RELOAD, getTypeName());
        init();
    }

    @Override
    public void saveAll() {
        plugin.lang.logAction(I18n.SAVE, getTypeName());
    }

    @Override
    public void reload(String id) {
        load(new File(folder.getAbsolutePath(), id + ".yml"));
    }

    @Override
    public void delete(String id) {
    }

    @Override
    public void save(@NonNull String id) {

    }
}
