package org.serverct.parrot.parrotx.data.autoload;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Builder;
import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.serverct.parrot.parrotx.PPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class AutoLoadGroup {

    private final Multimap<Class<? extends ConfigurationSerializable>, String> serializableMap = HashMultimap.create();
    private final Map<String, AutoLoadItem> itemMap = new HashMap<>();
    private final String name;
    private Class<?> to;
    private ConfigurationSection from;

    public void load(final AutoLoadItem... items) {
        Arrays.stream(items).forEach(item -> this.itemMap.put(item.getField(), item));
    }

    public void loadAll(final Collection<? extends AutoLoadItem> items) {
        items.forEach(this::load);
    }

    public void registerSerializable(final Class<? extends ConfigurationSerializable> clazz, final String path) {
        this.serializableMap.put(clazz, path);
        ConfigurationSerialization.registerClass(clazz);
    }

    public void load(final PPlugin plugin) {
        AutoLoader.autoLoad(plugin, "自动加载数据组/" + name, from, to, itemMap, serializableMap);
    }

    public void save(final PPlugin plugin) {
        AutoLoader.autoSave(plugin, "自动加载数据组/" + name, from, to, itemMap);
    }
}
