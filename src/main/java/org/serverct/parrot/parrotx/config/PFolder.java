package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.PDataFolder;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PFolder implements PDataFolder {

    protected PPlugin plugin;
    @Getter
    protected File folder;
    @Getter
    protected FileConfiguration config;
    private String id;
    private String name;

    private Map<String, PConfiguration> dataMap = new HashMap<>();

    public PFolder(@NonNull PPlugin plugin, String folderName, String typeName) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder() + File.separator + folderName);
        this.id = folderName;
        this.name = typeName;
    }

    @Override
    public String getTypeName() {
        return name;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public Map<String, PConfiguration> getData() {
        return dataMap;
    }

    @Override
    public void init() {
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                plugin.getLang().log("未找到" + name + "数据文件夹, 已重新生成.", LocaleUtil.Type.WARN, false);
            } else {
                plugin.getLang().log("尝试生成" + name + "数据文件夹失败.", LocaleUtil.Type.ERROR, false);
            }
        } else {
            File[] files = folder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
            if (files != null && files.length != 0) {
                String typeFile = "未知类型数据文件";
                for (File file : files) {
                    load(file);
                    typeFile = dataMap.get(BasicUtil.getNoExFileName(file.getName())).getTypeName();
                }
                plugin.getLang().log("共加载 &c" + dataMap.size() + " &7个" + typeFile + ".", LocaleUtil.Type.INFO, false);
            } else {
                plugin.getLang().log(name + "中没有数据可供加载.", LocaleUtil.Type.WARN, false);
            }
        }
    }

    @Override
    public void load(File file) {
    }

    @Override
    public void reloadAll() {
        plugin.getLang().log("尝试重载" + name + "数据文件夹.", LocaleUtil.Type.INFO, false);
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
