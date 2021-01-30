package org.serverct.parrot.parrotx.data.autoload;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.autoload.loader.ListLoader;
import org.serverct.parrot.parrotx.data.autoload.loader.MapLoader;
import org.serverct.parrot.parrotx.data.autoload.loader.SerializableLoader;
import org.serverct.parrot.parrotx.data.autoload.loader.SimpleLoader;
import org.serverct.parrot.parrotx.data.autoload.register.CommandHandlerRegister;
import org.serverct.parrot.parrotx.data.autoload.register.ConfigurationRegister;
import org.serverct.parrot.parrotx.data.autoload.register.DataSetRegister;
import org.serverct.parrot.parrotx.data.autoload.register.ListenerRegister;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class Autoloader {

    private static final Map<Class<?>, AutoloadSetting> MODEL_MAP = new HashMap<>();
    private static final Map<Class<?>, DataLoader<?>> LOADER_MAP = new HashMap<>();
    private static final Map<Class<?>, AutoRegister> REGISTER_MAP = new HashMap<>();

    static {
        registerLoader(
                new SimpleLoader<>(int.class, ConfigurationSection::getInt),
                new SimpleLoader<>(Integer.class, ConfigurationSection::getInt),
                new SimpleLoader<>(long.class, ConfigurationSection::getLong),
                new SimpleLoader<>(Long.class, ConfigurationSection::getLong),
                new SimpleLoader<>(double.class, ConfigurationSection::getDouble),
                new SimpleLoader<>(Double.class, ConfigurationSection::getDouble),
                new SimpleLoader<>(boolean.class, ConfigurationSection::getBoolean),
                new SimpleLoader<>(Boolean.class, ConfigurationSection::getBoolean),
                new SimpleLoader<>(String.class, ConfigurationSection::getString),
                new SimpleLoader<>(ItemStack.class, ConfigurationSection::getItemStack),
                new SimpleLoader<>(Vector.class, ConfigurationSection::getVector),
                new ListLoader(),
                new MapLoader(),
                new SerializableLoader()
        );

        try {
            ConfigurationSection.class.getMethod("getLocation", String.class);
            new SimpleLoader<>(Location.class, ConfigurationSection::getLocation);
        } catch (NoSuchMethodException ignored) {
        }

        registerRegister(
                new CommandHandlerRegister(),
                new ConfigurationRegister(),
                new DataSetRegister(),
                new ListenerRegister()
        );
    }

    public static void registerLoader(DataLoader<?>... loaders) {
        if (Objects.isNull(loaders) || loaders.length <= 0) {
            return;
        }
        for (DataLoader<?> loader : loaders) {
            LOADER_MAP.put(loader.getType(), loader);
        }
    }

    public static void registerRegister(AutoRegister... registers) {
        if (Objects.isNull(registers) || registers.length <= 0) {
            return;
        }
        for (AutoRegister register : registers) {
            REGISTER_MAP.put(register.getType(), register);
        }
    }

    public static Collection<Map.Entry<Class<?>, AutoRegister>> getRegisterEntries() {
        return REGISTER_MAP.entrySet();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> DataLoader<T> getLoader(@NotNull final Class<T> clazz) {
        return (DataLoader<T>) LOADER_MAP.get(clazz);
    }

    public static void execute(@NotNull PPlugin plugin, @NotNull final ConfigurationSection from,
                               @NotNull final Object to, final boolean load) {
        final I18n lang = plugin.getLang();
        final String prefix = load ? I18n.AUTOLOAD : I18n.AUTOSAVE;
        final Class<?> clazz = to.getClass();
        final String className = clazz.getSimpleName() + ".class";
        AutoloadSetting setting = MODEL_MAP.get(clazz);
        if (Objects.isNull(setting)) {
            setting = new AutoloadSetting(plugin, clazz);
            MODEL_MAP.put(clazz, setting);
        }

        try {
            for (Map.Entry<String, Collection<AutoloadItem>> entry :
                    setting.getItems().asMap().entrySet()) {
                final String group = entry.getKey();
                final Collection<AutoloadItem> items = entry.getValue();

                for (AutoloadItem item : items) {
                    try {
                        final StringBuilder pathBuilder = new StringBuilder();
                        final String extraPath = setting.getExtraPath(group);
                        pathBuilder.append(StringUtils.isEmpty(extraPath) ? "" : extraPath + ".");
                        pathBuilder.append(item.getPath());
                        final String path = pathBuilder.toString();

                        DataLoader<?> loader = getLoader(item.getType());
                        if (Objects.isNull(loader)) {
                            if (!ConfigurationSerializable.class.isAssignableFrom(item.getType())) {
                                lang.log.error(prefix, className,
                                        "未注册该数据类型的加载器: " + item.getType().getSimpleName() + ".class");
                                continue;
                            }
                            loader = getLoader(ConfigurationSerializable.class);
                            if (Objects.isNull(loader)) {
                                continue;
                            }
                        }

                        final Field field = clazz.getDeclaredField(item.getField());
                        field.setAccessible(true);

                        if (load) {
                            field.set(to, loader.load(path, from, item.getClassChain()));
                        } else {
                            loader.save(path, from, field.get(to), item.getClassChain());
                        }
                    } catch (NoSuchFieldException e) {
                        lang.log.error(prefix, className, "目标字段未找到: " + item.getField());
                    } catch (IllegalAccessException e) {
                        lang.log.error(prefix, className, "赋值时遇到异常(非法访问)");
                    }
                }
            }
        } catch (Exception e) {
            lang.log.error(prefix, className, e, plugin.getPackageName());
        }
    }

    public static void log(@NotNull final String message, final Object... args) {
        Bukkit.getConsoleSender().sendMessage(I18n.color("&bParrotX &aAutoloader 2 &f>> &r" + MessageFormat.format(message, args)));
    }

}
