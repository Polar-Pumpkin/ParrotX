package org.serverct.parrot.parrotx.config;

import lombok.NonNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.PData;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.*;

@SuppressWarnings({"unused"})
public abstract class PDataFolder<T extends PData> implements PConfiguration, FileSaved {

    protected final Map<PID, T> dataMap = new HashMap<>();
    protected PPlugin plugin;
    protected File folder;

    public PDataFolder(PPlugin plugin, File folder) {
        this.plugin = plugin;
        this.folder = folder;
    }

    @Override
    public String getFilename() {
        return this.folder.getName();
    }

    @Override
    public File getFile() {
        return this.folder;
    }

    @Override
    public void setFile(@NonNull File file) {
        this.folder = file;
    }

    @Override
    public void reload() {
        reloadAll();
    }

    @Override
    public void save() {
        saveAll();
    }

    @Override
    public void delete() {
        deleteAll();
    }

    public void put(T data) {
        this.dataMap.put(data.getID(), data);
    }

    public abstract T loadData(File file);

    @Override
    public void load(@NonNull File file) {
        put(loadData(file));
    }

    public List<String> listId() {
        List<String> ids = new ArrayList<>();
        dataMap.keySet().forEach(pid -> ids.add(pid.getId()));
        return ids;
    }

    public T get(String id) {
        for (T data : this.dataMap.values()) if (data.check(id)) return data;
        return null;
    }

    public void reloadAll() {
        plugin.getLang().log.action(I18n.RELOAD, getTypename());
        init();
    }

    public void saveAll() {
        this.dataMap.values().forEach(PData::save);
    }

    public void deleteAll() {
        this.dataMap.values().forEach(PData::delete);
        this.dataMap.clear();
    }

    public void reload(String id) {
        String object = getTypename() + "(" + id + ")";
        PData data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.RELOAD, object);
            data.reload();
        } else {
            plugin.getLang().log.error(I18n.RELOAD, object, "目标数据未找到");
        }
    }

    public void delete(String id) {
        String object = getTypename() + "(" + id + ")";
        PData data = get(id);
        if (Objects.nonNull(data)) {
            dataMap.remove(data.getID());
            data.delete();
        } else {
            plugin.getLang().log.error(I18n.RELOAD, object, "目标数据未找到");
        }
    }

    public void save(String id) {
        String object = getTypename() + "(" + id + ")";
        PData data = get(id);
        if (Objects.nonNull(data)) {
            data.save();
        } else {
            plugin.getLang().log.error(I18n.RELOAD, object, "目标数据未找到");
        }
    }
}
