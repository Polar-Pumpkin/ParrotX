package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.PData;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

@SuppressWarnings({"unused"})
public abstract class PDataFolder<T extends PData> implements PConfiguration, FileSaved {

    @Getter
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
    public void load(@NonNull File file) {
        put(loadData(file));
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

    public abstract T loadData(File file);

    public void put(T data) {
        this.dataMap.put(data.getID(), data);
    }

    public T get(String id) {
        for (T data : this.dataMap.values()) if (data.check(id)) return data;
        return null;
    }

    public List<T> getAll() {
        return new ArrayList<>(this.dataMap.values());
    }

    public List<String> listId() {
        List<String> ids = new ArrayList<>();
        dataMap.keySet().forEach(pid -> ids.add(pid.getId()));
        return ids;
    }

    public void reload(String id) {
        PData data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.RELOAD, MessageFormat.format(getTypename() + "({0})", id));
            data.reload();
        } else {
            plugin.getLang().log.error(I18n.RELOAD, getTypename(), "目标数据未找到: " + id);
        }
    }

    public void delete(String id) {
        PData data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.DELETE, MessageFormat.format(getTypename() + "({0})", id));
            dataMap.remove(data.getID());
            data.delete();
        } else {
            plugin.getLang().log.error(I18n.DELETE, getTypename(), "目标数据未找到: " + id);
        }
    }

    public void save(String id) {
        PData data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.SAVE, MessageFormat.format(getTypename() + "({0})", id));
            data.save();
        } else {
            plugin.getLang().log.error(I18n.SAVE, getTypename(), "目标数据未找到: " + id);
        }
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
}
