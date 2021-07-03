package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class PStruct implements UniqueData {

    protected final PPlugin plugin;
    protected final I18n lang;
    protected final PID id;
    protected final ConfigurationSection section;
    @Getter
    protected final Map<String, Object> dataMap = new HashMap<>();
    private final String typeName;
    @Getter
    @Setter
    protected boolean readonly = false;

    public PStruct(PID id, ConfigurationSection section, String typeName) {
        this.id = id;
        this.plugin = id.getPlugin();
        this.lang = this.plugin.getLang();
        this.section = section;
        this.typeName = typeName;
    }

    @Override
    public void load() {
        Autoloader.execute(plugin, section, this, true);
    }

    @Override
    public void save() {
        Autoloader.execute(plugin, section, this, false);
    }

    @Override
    public String name() {
        return this.typeName + "/" + this.id.getId();
    }

    @Override
    public void init() {
    }

    @Override
    public void saveDefault() {
    }

    @Nullable
    public <T> T getAs(final String path, final Class<T> clazz) {
        final Object object = this.dataMap.get(path);
        if (Objects.isNull(object)) {
            return null;
        }
        return clazz.cast(object);
    }

    @Override
    public void reload() {
        plugin.getLang().log.action(I18n.RELOAD, name());
        load();
    }

    @Override
    public PID getID() {
        return this.id;
    }

    @Override
    public void delete() {
    }
}
