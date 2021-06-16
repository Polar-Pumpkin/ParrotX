package org.serverct.parrot.parrotx.config;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PData;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.utils.FileUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public abstract class PFolder<E extends PData> extends PDataSet<E> {

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
        load(this.file);
    }

    @Override
    public void load(@NonNull File folder) {
        clearCache();

        File[] files = FileUtil.getYamls(folder);
        if (files == null || files.length == 0) {
            saveDefault();
            files = FileUtil.getYamls(folder);
        }
        if (isLazyLoad()) {
            lang.log.info("已为 &c{0} &r启用懒加载.", name());
            return;
        }

        if (files != null) {
            Arrays.stream(files)
                    .map(this::buildId)
                    .forEach(this::load);
            lang.log.info("共加载 &c" + name() + " &7中的 &c" + dataMap.size() + " &7个数据文件.");
        } else {
            lang.log.warn("&c" + name() + " &7中没有数据可供加载.");
        }
    }

    @Nullable
    public abstract E loadFromDataFile(final File dataFile);

    @Nullable
    @Override
    public E load(@NotNull PID id) {
        final String key = id.getId();
        final File file = new File(this.file, key + ".yml");
        if (!file.exists()) {
            lang.log.error(I18n.LOAD, name(), "数据文件不存在: " + key);
            return null;
        }

        final E value = loadFromDataFile(file);
        if (Objects.isNull(value)) {
            lang.log.error(I18n.LOAD, name(), "加载数据失败: " + key);
            return null;
        }
        put(value);
        return value;
    }

    @NotNull
    public PID buildId(@NotNull final String id) {
        return new PID(plugin, dataKey.toLowerCase(), id);
    }

    @NotNull
    public PID buildId(@NotNull final File file) {
        return buildId(FileUtil.getNoExFilename(file));
    }
}
