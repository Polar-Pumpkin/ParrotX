package org.serverct.parrot.parrotx.api;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.UniqueData;

import java.util.HashMap;
import java.util.Map;

public class ParrotXAPI {

    private static final Map<Class<? extends PPlugin>, PPlugin> PLUGIN_MAP = Maps.newConcurrentMap();
    private static final Map<Class<? extends UniqueData>, Class<? extends PPlugin>> DATA_CLASS_MAP =
            new HashMap<>();
    private static final Map<Class<? extends PConfiguration>, Class<? extends PPlugin>> CONFIG_CLASS_MAP =
            new HashMap<>();

    public static <T extends PPlugin> T getPlugin(@NotNull final Class<T> clazz) {
        return clazz.cast(PLUGIN_MAP.get(clazz));
    }

    public static <T extends PPlugin> void registerPlugin(@NotNull final T plugin) {
        final Class<? extends PPlugin> clazz = plugin.getClass();
        PLUGIN_MAP.put(clazz, plugin);
    }

    public static void registerDataClass(@NotNull final Class<? extends UniqueData> clazz,
                                         @NotNull final Class<? extends PPlugin> pluginClass) {
        DATA_CLASS_MAP.put(clazz, pluginClass);
    }

    public static void registerConfigClass(@NotNull final Class<? extends PConfiguration> clazz,
                                           @NotNull final Class<? extends PPlugin> pluginClass) {
        CONFIG_CLASS_MAP.put(clazz, pluginClass);
    }

    public static <T extends UniqueData> T getData(final String id, final Class<T> clazz) {
        return getPlugin(DATA_CLASS_MAP.get(clazz)).getDataSet(clazz).get(id);
    }

    public static <T extends PConfiguration> T getManager(final Class<T> clazz) {
        return getPlugin(CONFIG_CLASS_MAP.get(clazz)).getManager(clazz);
    }


}
