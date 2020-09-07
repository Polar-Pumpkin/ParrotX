package org.serverct.parrot.parrotx.config;

import lombok.NonNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PData;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;

@SuppressWarnings("AccessStaticViaInstance")
public abstract class PFolder<T extends PData> extends PDataFolder<T> {

    protected String id;
    protected String dataKey;

    public PFolder(@NonNull PPlugin plugin, String folderName, String typeName, String dataKey) {
        super(plugin, new File(plugin.getDataFolder(), folderName));
        this.plugin = plugin;
        this.id = typeName;
        this.dataKey = dataKey;
    }

    @Override
    public String getTypename() {
        return id + "/" + getFilename();
    }

    @Override
    public void init() {
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                saveDefault();
                plugin.lang.log.warn("未找到 &c" + getTypename() + "&7, 已重新生成.");
            } else {
                plugin.lang.log.error("尝试生成 &c" + getTypename() + " &7失败.");
            }
        }
        load();
    }

    @Override
    public void saveDefault() {
    }

    @Override
    public void load() {
        File[] files = BasicUtil.getYamls(folder);
        if (files == null || files.length == 0) {
            saveDefault();
            files = BasicUtil.getYamls(folder);
        }
        if (files != null && files.length != 0) {
            for (File file : files) {
                load(file);
            }
            plugin.lang.log.info("共加载 &c" + getTypename() + " &7中的 &c" + dataMap.size() + " &7个数据文件.");
        } else {
            plugin.lang.log.warn("&c" + getTypename() + " &7中没有数据可供加载.");
        }
    }

    public PID buildId(String id) {
        return new PID(plugin, dataKey, id);
    }
}
