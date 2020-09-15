package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.PStruct;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

public abstract class PStructSet<T extends PStruct> extends PConfig {

    @Getter
    protected Map<PID, T> dataMap = new HashMap<>();
    @Getter
    protected String rootName;
    protected ConfigurationSection root;

    public PStructSet(@NonNull PPlugin plugin, String fileName, String typeName, String rootName) {
        super(plugin, fileName, typeName);
        this.rootName = rootName;
    }

    @Override
    public void load(@NonNull File file) {
        super.load(file);

        root = config.getConfigurationSection(this.rootName);
        if (Objects.isNull(root)) {
            root = config.createSection(this.rootName);
            plugin.getLang().log.error(I18n.LOAD, getTypename(), "根数据节为 null: " + this.rootName);
            return;
        }
        for (String key : root.getKeys(false)) {
            final ConfigurationSection section = root.getConfigurationSection(key);
            if (Objects.isNull(section)) {
                plugin.getLang().log.error(I18n.LOAD, getTypename(), "存在非数据节: " + key);
                continue;
            }
            put(loadData(section));
        }

        if (this.dataMap.isEmpty()) {
            plugin.getLang().log.warn("&c" + getTypename() + " &7中没有数据可供加载.");
        } else {
            plugin.getLang().log.info("共加载 &c" + getTypename() + " &7中的 &c" + dataMap.size() + " &7个数据.");
        }
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

    public abstract T loadData(final ConfigurationSection section);

    public void put(final T data) {
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
        PStruct data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.RELOAD, MessageFormat.format(getTypename() + "({0})", id));
            data.reload();
        } else {
            plugin.getLang().log.error(I18n.RELOAD, getTypename(), "目标数据未找到: " + id);
        }
    }

    public void delete(String id) {
        PStruct data = get(id);
        if (Objects.nonNull(data)) {
            plugin.getLang().log.action(I18n.DELETE, MessageFormat.format(getTypename() + "({0})", id));
            dataMap.remove(data.getID());
            data.delete();
            root.set(id, null);
        } else {
            plugin.getLang().log.error(I18n.DELETE, getTypename(), "目标数据未找到: " + id);
        }
    }

    public void save(String id) {
        PStruct data = get(id);
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
        try {
            this.dataMap.values().forEach(PStruct::save);
            config.save(file);
        } catch (Throwable e) {
            plugin.getLang().log.error(I18n.SAVE, getTypename(), e, null);
        }
    }

    public void deleteAll() {
        this.dataMap.values().forEach(struct -> root.set(struct.getID().getId(), null));
        this.dataMap.clear();
    }

    public PID buildId(String id) {
        return new PID(plugin, rootName.toLowerCase(), id);
    }
}
