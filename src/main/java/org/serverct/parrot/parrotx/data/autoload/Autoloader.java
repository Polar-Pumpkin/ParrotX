package org.serverct.parrot.parrotx.data.autoload;

import com.cryptomorin.xseries.XItemStack;
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
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoloadGroup;
import org.serverct.parrot.parrotx.data.autoload.loader.*;
import org.serverct.parrot.parrotx.data.autoload.register.CommandHandlerRegister;
import org.serverct.parrot.parrotx.data.autoload.register.ConfigurationRegister;
import org.serverct.parrot.parrotx.data.autoload.register.DataSetRegister;
import org.serverct.parrot.parrotx.data.autoload.register.ListenerRegister;
import org.serverct.parrot.parrotx.utils.ItemUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

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
                new SimpleLoader<>(ItemStack.class, (section, path) -> {
                    final ItemStack fromSection = section.getItemStack(path);
                    if (Objects.nonNull(fromSection)) {
                        return fromSection;
                    }
                    final ConfigurationSection item = section.getConfigurationSection(path);
                    if (Objects.isNull(item)) {
                        return null;
                    }
                    final ItemStack fromXSeries = XItemStack.deserialize(item);
                    if (Objects.nonNull(fromXSeries)) {
                        return fromXSeries;
                    }
                    return ItemUtil.build(item);
                }).setter((section, path, item) -> {
                    ConfigurationSection target = section.getConfigurationSection(path);
                    if (Objects.isNull(target)) {
                        target = section.createSection(path);
                    }
                    ItemUtil.save(target, item);
                }),
                new SimpleLoader<>(Vector.class, ConfigurationSection::getVector),
                new SimpleLoader<>(ConfigurationSection.class, (section, path) -> {
                    final ConfigurationSection exist = section.getConfigurationSection(path);
                    if (Objects.nonNull(exist)) {
                        return exist;
                    }
                    return section.createSection(path);
                }),
                new SimpleLoader<>(UUID.class, (section, path) -> {
                    final String uuid = section.getString(path);
                    if (Objects.isNull(uuid)) {
                        return null;
                    }
                    return UUID.fromString(uuid);
                }).setter((section, path, uuid) -> section.set(path, Objects.isNull(uuid) ? null : uuid.toString())),
                new ListLoader(),
                new MapLoader(),
                new SerializableLoader(),
                new EnumLoader()
        );

        try {
            ConfigurationSection.class.getMethod("getLocation", String.class);
            registerLoader(new SimpleLoader<>(Location.class, ConfigurationSection::getLocation));
        } catch (NoSuchMethodException exception) {
            registerLoader(new SimpleLoader<>(Location.class, ((section, path) -> section.getSerializable(path,
                    Location.class))));
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

    public static Collection<AutoRegister> getRegisters() {
        return REGISTER_MAP.values();
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

        try {
            for (Class<?> model : chainSuper(plugin, clazz, new ArrayList<>())) {
                final AutoloadSetting setting = getSetting(plugin, model);

                for (Map.Entry<String, Collection<AutoloadItem>> entry :
                        setting.getItems().asMap().entrySet()) {
                    final String groupName = entry.getKey();
                    final Collection<AutoloadItem> items = entry.getValue();

                    for (AutoloadItem item : items) {
                        try {
                            final PAutoloadGroup group = setting.getGroup(groupName);
                            if (Objects.isNull(group)) {
                                lang.log.error(prefix, className, "未声明项目组: " + groupName);
                                continue;
                            }

                            final StringBuilder pathBuilder = new StringBuilder();
                            String extraPath = group.value();
                            if (!"default".equalsIgnoreCase(groupName)) {
                                if (!group.ignoreDefaultPath()) {
                                    final String defaultPath = setting.getExtraPath("default");
                                    pathBuilder.append(StringUtils.isEmpty(defaultPath) ? "" : defaultPath + ".");
                                }
                                extraPath = extraPath.replace("{GROUP}", groupName);
                            } else {
                                extraPath = extraPath.replace("{GROUP}", "");
                            }
                            pathBuilder.append(StringUtils.isEmpty(extraPath) ? "" : extraPath + ".");

                            pathBuilder.append(item.getPath());

                            final String path = pathBuilder.toString();
                            DataLoader<?> loader = getLoader(item.getType());
                            if (Objects.isNull(loader)) {
                                if (ConfigurationSerializable.class.isAssignableFrom(item.getType())) {
                                    loader = getLoader(ConfigurationSerializable.class);
                                }
                                if (Enum.class.isAssignableFrom(item.getType())) {
                                    loader = getLoader(Enum.class);
                                }

                                if (Objects.isNull(loader)) {
                                    lang.log.error(prefix, className,
                                            "未注册该数据类型的加载器: " + item.getType().getSimpleName() + ".class");
                                    continue;
                                }
                            }

                            final Field field = model.getDeclaredField(item.getField());
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
            }
        } catch (Exception e) {
            lang.log.error(prefix, className, e, plugin.getPackageName());
        }
    }

    public static void log(@NotNull final String message, final Object... args) {
        Bukkit.getConsoleSender().sendMessage(I18n.color("&bParrotX &aAutoloader 2 &f>> &r" + MessageFormat.format(message, args)));
    }

    @NotNull
    public static AutoloadSetting getSetting(@NotNull final PPlugin plugin, @NotNull final Class<?> clazz) {
        AutoloadSetting setting = MODEL_MAP.get(clazz);
        if (Objects.isNull(setting)) {
            setting = new AutoloadSetting(plugin, clazz);
            MODEL_MAP.put(clazz, setting);
        }
        return setting;
    }

    @NotNull
    private static List<Class<?>> chainSuper(@NotNull final PPlugin plugin,
                                             @NotNull final Class<?> start, @NotNull final List<Class<?>> classes) {
        final String packageName = plugin.getPackageName();

        try {
            classes.add(start);

            final Type type = start.getGenericSuperclass();
            final String classpath;
            if (type instanceof ParameterizedType) {
                classpath = ((ParameterizedType) type).getRawType().getTypeName();
            } else {
                classpath = type.getTypeName();
            }

            if (!classpath.contains(packageName)) {
                return classes;
            }

            final String[] args = classpath.split("[.]");
            if (args.length > 1) {
                final Class<?> clazz = Class.forName(classpath);
                return chainSuper(plugin, clazz, classes);
            }
        } catch (ClassNotFoundException error) {
            plugin.getLang().log.error(I18n.AUTOLOAD, "探索继承链", error, packageName);
        }
        return classes;
    }

}
