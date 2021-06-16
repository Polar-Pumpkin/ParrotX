package org.serverct.parrot.parrotx.config;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.UniqueData;
import org.serverct.parrot.parrotx.data.flags.DataSet;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public abstract class PDataSet<E extends UniqueData> implements PConfiguration, FileSaved, DataSet<E> {

    @Getter
    protected final Map<PID, E> dataMap = new HashMap<>();
    protected final PPlugin plugin;
    protected final I18n lang;
    private final String name;
    @Getter
    @Setter
    protected File file;
    @Getter
    @Setter
    private boolean readonly = false;
    @Getter
    @Setter
    private boolean lazyLoad = true;

    public PDataSet(@NotNull final PPlugin plugin, @NotNull final File file, @NotNull final String name) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.file = file;
        this.name = name;
    }

    @Nullable
    public abstract E load(@NotNull final PID id);

    public void clearCache() {
        this.dataMap.clear();
    }

    @NotNull
    @Override
    public String name() {
        return this.name + "/" + getFilename();
    }

    @NotNull
    @Override
    public String getFilename() {
        return this.file.getName();
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
    public void put(@Nullable final E data) {
        if (Objects.isNull(data)) {
            return;
        }
        lang.log.info("加载数据: &a{0}&r.", data.getID().getId());
        this.dataMap.put(data.getID(), data);
    }

    @Nullable
    @Override
    public E get(@NotNull final PID id) {
        final E value = this.dataMap.get(id);
        if (this.lazyLoad && Objects.isNull(value)) {
            return load(id);
        }
        return value;
    }

    @Override
    public boolean has(@NotNull final PID id) {
        if (this.lazyLoad && !this.dataMap.containsKey(id)) {
            load(id);
        }
        return this.dataMap.containsKey(id);
    }

    @NotNull
    @Override
    public Collection<E> getAll() {
        return this.dataMap.values();
    }

    @NotNull
    @Override
    public Set<PID> getIds() {
        return this.dataMap.keySet();
    }

    @NotNull
    @Override
    public Set<String> getStringIds() {
        return getIds().stream().map(PID::getId).collect(Collectors.toSet());
    }

    @Override
    public void reload(@NotNull final PID id) {
        final UniqueData data = get(id);
        if (!Objects.nonNull(data)) {
            plugin.getLang().log.error(I18n.RELOAD, objectName(id), "目标数据不存在");
            return;
        }
        plugin.getLang().log.action(I18n.RELOAD, objectName(id));
        data.reload();
    }

    @Override
    public void delete(@NotNull final PID id) {
        UniqueData data = get(id);
        if (!Objects.nonNull(data)) {
            plugin.getLang().log.error(I18n.DELETE, objectName(id), "目标数据不存在");
            return;
        }
        plugin.getLang().log.action(I18n.DELETE, objectName(id));
        dataMap.remove(data.getID());
        data.delete();
    }

    @Override
    public void save(@NotNull final PID id) {
        UniqueData data = get(id);
        if (!Objects.nonNull(data)) {
            plugin.getLang().log.error(I18n.SAVE, objectName(id), "目标数据不存在");
            return;
        }
        plugin.getLang().log.action(I18n.SAVE, objectName(id));
        data.save();
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
    @NotNull
    public String objectName(@NotNull final PID id) {
        return name() + "/" + id;
    }
}
