package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.AutoLoader;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class PStruct extends AutoLoader implements UniqueData {

    protected final PID id;
    protected final ConfigurationSection section;
    @Getter
    protected final Map<String, Object> dataMap = new HashMap<>();

    public PStruct(PID id, ConfigurationSection section) {
        super(id.getPlugin());
        this.id = id;
        this.section = section;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getAs(final String path, final Class<?> clazz) {
        final Object object = this.dataMap.get(path);
        if (Objects.isNull(object)) {
            return null;
        }
        return (T) clazz.cast(object);
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
