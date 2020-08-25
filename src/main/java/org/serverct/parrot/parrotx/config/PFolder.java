package org.serverct.parrot.parrotx.config;

import lombok.NonNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.io.IOException;

public class PFolder extends PDataFolder {

    protected String id;
    protected String dataKey;

    public PFolder(@NonNull PPlugin plugin, String folderName, String typeName, String dataKey) {
        super(plugin, new File(plugin.getDataFolder(), folderName));
        this.plugin = plugin;
        this.id = typeName;
        this.dataKey = dataKey;
    }

    @Override
    public String getTypeName() {
        return id + "/" + getFileName();
    }

    @Override
    public void init() {
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                saveDefault();
                plugin.lang.log("未找到 &c" + getTypeName() + "&7, 已重新生成.", I18n.Type.WARN, false);
            } else {
                plugin.lang.log("尝试生成 &c" + getTypeName() + " &7失败.", I18n.Type.ERROR, false);
            }
        }
        File[] files = folder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        if (files == null || files.length == 0) {
            saveDefault();
            files = folder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        }
        if (files != null && files.length != 0) {
            for (File file : files) {
                load(file);
            }
            plugin.lang.log("共加载 &c" + getTypeName() + " &7中的 &c" + dataMap.size() + " &7个数据文件.", I18n.Type.INFO, false);
        } else {
            plugin.lang.log("&c" + getTypeName() + " &7中没有数据可供加载.", I18n.Type.WARN, false);
        }
    }

    @Override
    public void saveDefault() {
        if (!folder.mkdirs()) {
            plugin.lang.logError(I18n.GENERATE, getTypeName(), "自动生成失败");
        }
    }

    @Override
    public void load(File file) {
    }

    @Override
    public void reloadAll() {

    }

    public PID buildId(String id) {
        return new PID(plugin, dataKey, id);
    }
}
