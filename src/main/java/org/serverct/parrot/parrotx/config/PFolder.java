package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PDataFolder;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

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
        return name + "(" + id + ")";
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
                plugin.getLang().log("未找到 &c" + getTypeName() + "&7, 已重新生成.", LocaleUtil.Type.WARN, false);
            } else {
                plugin.getLang().log("尝试生成 &c" + getTypeName() + " &7失败.", LocaleUtil.Type.ERROR, false);
            }
        } else {
            File[] files = folder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
            if (files != null && files.length != 0) {
                releaseDefaultData();
            }
            if (files != null && files.length != 0) {
                for (File file : files) {
                    load(file);
                }
                plugin.getLang().log("共加载 &c" + getTypeName() + " &7中的 &c" + files.length + " &7个数据文件.", LocaleUtil.Type.INFO, false);
            } else {
                plugin.getLang().log("&c" + getTypeName() + " &7中没有数据可供加载.", LocaleUtil.Type.WARN, false);
            }
        }
    }

    @Override
    public void load(File file) {
    }

    @Override
    public void reloadAll() {
        plugin.getLang().logAction(LocaleUtil.RELOAD, getTypeName());
        init();
    }

    @Override
    public void reload(String id) {
        load(new File(folder.getAbsolutePath() + File.separator + id + ".yml"));
    }

    @Override
    public void delete(String id) {
    }
}
