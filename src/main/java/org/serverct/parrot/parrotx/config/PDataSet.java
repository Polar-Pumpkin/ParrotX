package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.UniqueData;
import org.serverct.parrot.parrotx.data.flags.DataSet;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.*;

@SuppressWarnings({"unused"})
public abstract class PDataSet<T extends UniqueData> implements PConfiguration, FileSaved, DataSet<T> {

    @Getter
    protected final Map<PID, T> dataMap = new HashMap<>();
    protected final PPlugin plugin;
    protected final I18n lang;
    private final String name;
    private boolean readonly = false;
    protected File file;

    public PDataSet(PPlugin plugin, File file, String name) {
        this.plugin = plugin;
        this.file = file;
        this.lang = this.plugin.getLang();
        this.name = name;

        plugin.registerConfiguration(this);
    }

    @Override
    public boolean isReadOnly() {
        return this.readonly;
    }

    public void readOnly(final boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public String name() {
        return this.name + "/" + getFilename();
    }

    @Override
    public String getFilename() {
        return this.file.getName();
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
    public void reload() {
        save();
        reloadAll();
        lang.log.action(I18n.RELOAD, name());
    }

    @Override
    public void save() {
        saveAll();
        lang.log.action(I18n.SAVE, name());
    }

    @Override
    public void delete() {
        deleteAll();
        if (this.file.delete()) {
            lang.log.action(I18n.DELETE, name());
        } else {
            lang.log.error(I18n.DELETE, name(), "删除文件(夹)失败");
        }
    }

    @Override
    public void put(T data) {
        this.dataMap.put(data.getID(), data);
    }

    @Override
    public T get(PID id) {
        return this.dataMap.get(id);
    }

    @Override
    public boolean has(PID id) {
        return this.dataMap.containsKey(id);
    }

    @Override
    public @NotNull Collection<T> getAll() {
        return this.dataMap.values();
    }

    @Override
    public @NotNull Set<PID> getIds() {
        return this.dataMap.keySet();
    }

    @Override
    public void reload(PID id) {
        final UniqueData data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.RELOAD, objectName(id));
            data.reload();
        } else {
            plugin.getLang().log.error(I18n.RELOAD, objectName(id), "目标数据不存在");
        }
    }

    @Override
    public void delete(PID id) {
        UniqueData data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.DELETE, objectName(id));
            dataMap.remove(data.getID());
            data.delete();
        } else {
            plugin.getLang().log.error(I18n.DELETE, objectName(id), "目标数据不存在");
        }
    }

    @Override
    public void save(PID id) {
        UniqueData data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.SAVE, objectName(id));
            data.save();
        } else {
            plugin.getLang().log.error(I18n.SAVE, objectName(id), "目标数据不存在");
        }
    }

    @Override
    public void reloadAll() {
        init();
    }

    @Override
    public void saveAll() {
        this.dataMap.values().forEach(UniqueData::save);
    }

    @Override
    public void deleteAll() {
        this.dataMap.values().forEach(UniqueData::delete);
        this.dataMap.clear();
        lang.log.action(I18n.CLEAR, name());
    }

    @Override
    public String objectName(PID id) {
        return name() + "/" + id;
    }
}
