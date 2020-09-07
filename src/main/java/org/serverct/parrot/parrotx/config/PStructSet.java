package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.PStruct;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.*;

@SuppressWarnings("AccessStaticViaInstance")
public abstract class PStructSet<T extends PStruct> extends PConfig {

    @Getter
    protected Map<PID, T> dataMap = new HashMap<>();
    @Getter
    protected String root;

    public PStructSet(@NonNull PPlugin plugin, String fileName, String typeName, String root) {
        super(plugin, fileName, typeName);
        this.root = root;
    }

    @Override
    public void load(@NonNull File file) {
        super.load(file);

        if (!config.isConfigurationSection(root)) {
            return;
        }
        final ConfigurationSection root = config.getConfigurationSection(this.root);
        if (Objects.isNull(root)) {
            return;
        }
        for (String key : root.getKeys(false)) {
            if (!config.isConfigurationSection(key)) {
                continue;
            }
            final ConfigurationSection section = root.getConfigurationSection(key);
            if (Objects.isNull(section)) {
                continue;
            }
            put(load(section));
        }

        if (this.dataMap.isEmpty()) {
            plugin.lang.log.warn("&c" + getTypename() + " &7中没有数据可供加载.");
        } else {
            plugin.lang.log.info("共加载 &c" + getTypename() + " &7中的 &c" + dataMap.size() + " &7个数据.");
        }
    }

    public abstract T load(final ConfigurationSection section);

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

    public void put(final T data) {
        this.dataMap.put(data.getID(), data);
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
        plugin.lang.log.action(I18n.RELOAD, getTypename());
        init();
    }

    public void saveAll() {
        try {
            this.dataMap.values().forEach(PStruct::save);
            config.save(file);
        } catch (Throwable e) {
            plugin.lang.log.error(I18n.SAVE, getTypename(), e, null);
        }
    }

    public void deleteAll() {
        this.dataMap.values().forEach(struct -> config.set(root + struct.getID().getId(), null));
        this.dataMap.clear();
    }

    public void reload(String id) {
        String object = getTypename() + "(" + id + ")";
        PStruct data = get(id);
        if (Objects.nonNull(data)) {
            plugin.lang.log.action(I18n.RELOAD, object);
            data.reload();
        } else {
            plugin.lang.log.error(I18n.RELOAD, object, "目标数据未找到");
        }
    }

    public void delete(String id) {
        String object = getTypename() + "(" + id + ")";
        PStruct data = get(id);
        if (Objects.nonNull(data)) {
            dataMap.remove(data.getID());
            data.delete();
        } else {
            plugin.lang.log.error(I18n.RELOAD, object, "目标数据未找到");
        }
    }

    public void save(String id) {
        String object = getTypename() + "(" + id + ")";
        PStruct data = get(id);
        if (Objects.nonNull(data)) {
            data.save();
        } else {
            plugin.lang.log.error(I18n.RELOAD, object, "目标数据未找到");
        }
    }
}
