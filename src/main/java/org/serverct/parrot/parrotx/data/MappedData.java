package org.serverct.parrot.parrotx.data;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.utils.MapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 洋洋
 * 来自洋洋的 HunterBountyCore 的 Param,
 * 但是我不喜欢那个名字, 所以改了一下.
 * <p>
 * 虽然我当初表现出很讨厌 Param,
 * 但是不得不承认真的挺好用, 这个设计,
 * 真香!
 * @since 1.4.5-alpha
 */
@SuppressWarnings("unchecked")
public class MappedData extends HashMap<String, Object> implements ConfigurationSerializable {

    public static Map<String, Object> filter(Map<?, ?> map) {
        return MapUtil.filter(map, String.class);
    }

    public static MappedData of(Map<String, Object> map) {
        MappedData mappedData = MappedData.newInstance();
        for (Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof MemorySection) {
                mappedData.add(entry.getKey(), MappedData.of(((MemorySection) entry.getValue()).getValues(false)));
            } else {
                mappedData.add(entry.getKey(), entry.getValue());
            }

        }
        return mappedData;
    }

    public static MappedData newInstance() {
        return new MappedData();
    }

    public MappedData add(String key, Object value) {
        put(key, value);
        return this;
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String def) {
        return containsKey(key) ? String.valueOf(get(key)) : def;
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public int getInt(String key, int def) {
        return Integer.parseInt(getString(key, String.valueOf(def)));
    }

    public double getDouble(String key) {
        return Double.parseDouble(getString(key));
    }

    public double getDouble(String key, double def) {
        return Double.parseDouble(getString(key, String.valueOf(def)));
    }

    public long getLong(String key) {
        return getLong(key, 0);
    }

    public long getLong(String key, long def) {
        return Long.parseLong(getString(key, String.valueOf(def)));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(getString(key, String.valueOf(def)));
    }

    public MappedData getMappedData(String key) {
        return containsKey(key) ? MappedData.of((Map<String, Object>) get(key)) : MappedData.newInstance();
    }

    public MappedData push(MappedData mappedData) {
        for (Entry<String, Object> entry : mappedData.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public <E> List<E> getList(String key, Class<?> c) {
        List<?> list = (List<?>) get(key);
        List<E> eList = new ArrayList<E>();
        for (Object o : list) {
            eList.add((E) c.cast(o));
        }
        return eList;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return this;
    }
}
