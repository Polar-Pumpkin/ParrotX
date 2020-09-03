package org.serverct.parrot.parrotx.config;

import lombok.NonNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.PData;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.*;

@SuppressWarnings({"unused", "AccessStaticViaInstance"})
public abstract class PDataFolder implements PConfiguration {

    protected PPlugin plugin;
    protected final Map<PID, PData> dataMap = new HashMap<>();
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

    public void put(PData data) {
        this.dataMap.put(data.getID(), data);
    }

    public List<String> listId() {
        return new ArrayList<String>() {{
            dataMap.keySet().forEach(pid -> add(pid.getId()));
        }};
    }

    public PData get(String id) {
        for (PData data : this.dataMap.values()) if (data.check(id)) return data;
        return null;
    }

    public void reloadAll() {
        plugin.lang.log.action(I18n.RELOAD, getTypename());
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
            plugin.lang.log.action(I18n.RELOAD, object);
            data.reload();
        } else {
            plugin.lang.log.error(I18n.RELOAD, object, "目标数据未找到。");
        }
    }

    public void delete(String id) {
        String object = getTypename() + "(" + id + ")";
        PData data = get(id);
        if (Objects.nonNull(data)) {
            dataMap.remove(data.getID());
            data.delete();
        } else {
            plugin.lang.log.error(I18n.RELOAD, object, "目标数据未找到。");
        }
    }

    public void save(String id) {
        String object = getTypename() + "(" + id + ")";
        PData data = get(id);
        if (Objects.nonNull(data)) {
            data.save();
        } else {
            plugin.lang.log.error(I18n.RELOAD, object, "目标数据未找到。");
        }
    }
}
