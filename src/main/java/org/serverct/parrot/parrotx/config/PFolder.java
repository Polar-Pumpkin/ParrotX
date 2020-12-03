package org.serverct.parrot.parrotx.config;

import lombok.NonNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PData;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.utils.BasicUtil;

import java.io.File;

public abstract class PFolder<T extends PData> extends PDataSet<T> {

    protected String dataKey;

    public PFolder(@NonNull PPlugin plugin, String folderName, String typeName, String dataKey) {
        super(plugin, new File(plugin.getDataFolder(), folderName), typeName);
        this.dataKey = dataKey;
    }

    @Override
    public void init() {
        if (!file.exists()) {
            if (file.mkdirs()) {
                saveDefault();
                lang.log.warn("未找到 &c" + name() + "&7, 已重新生成.");
            } else {
                lang.log.error("尝试生成 &c" + name() + " &7失败.");
            }
        }
        load();
    }

    @Override
    public void saveDefault() {
    }

    @Override
    public void load() {
        File[] files = BasicUtil.getYamls(file);
        if (files == null || files.length == 0) {
            saveDefault();
            files = BasicUtil.getYamls(file);
        }
        if (files != null && files.length != 0) {
            for (File file : files) {
                load(file);
            }
            lang.log.info("共加载 &c" + name() + " &7中的 &c" + dataMap.size() + " &7个数据文件.");
        } else {
            lang.log.warn("&c" + name() + " &7中没有数据可供加载.");
        }
    }

    public PID buildId(String id) {
        return new PID(plugin, dataKey.toLowerCase(), id);
    }
}
