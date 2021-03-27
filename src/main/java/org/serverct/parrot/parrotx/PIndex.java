package org.serverct.parrot.parrotx;

import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.config.PDataSet;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.UniqueData;
import org.serverct.parrot.parrotx.data.autoload.AutoRegister;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoload;
import org.serverct.parrot.parrotx.utils.ClassUtil;
import org.serverct.parrot.parrotx.utils.MapUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class PIndex {

    private final PPlugin plugin;
    private final I18n lang;
    private final List<Class<?>> classes = new ArrayList<>();
    private final Map<Class<? extends UniqueData>, Class<? extends PDataSet<? extends UniqueData>>> dataManagers =
            new HashMap<>();
    private final Map<Class<? extends PConfiguration>, PConfiguration> configs = new HashMap<>();

    public PIndex(PPlugin plugin) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.classes.addAll(ClassUtil.getClasses(plugin));
    }

    protected void initConfig() {
        this.configs.values().forEach(PConfiguration::init);
    }

    protected void saveConfig() {
        this.configs.values().forEach(config -> {
            if (!config.isReadOnly()) {
                config.save();
            }
        });
    }

    protected void clearConfig() {
        this.configs.clear();
    }

    protected void init() {
        Map<Class<?>, Integer> prioritizedClasses = new HashMap<>();

        final List<Class<?>> automated = this.classes.stream()
                .filter(clazz -> Objects.nonNull(clazz.getAnnotation(PAutoload.class)))
                .collect(Collectors.toList());
        for (Class<?> clazz : automated) {
            int priority = 999;
            final List<AutoRegister> shouldRegister = Autoloader.getRegisters().stream()
                    .filter(register -> register.shouldRegister(clazz))
                    .collect(Collectors.toList());
            for (final AutoRegister register : shouldRegister) {
                priority = Math.min(priority, register.getPriority());
            }
            prioritizedClasses.put(clazz, priority);
            lang.log.debug("&r添加自动注册项目: &a{0} &r(&a{1}&r)", clazz.getSimpleName(), priority);
        }

        prioritizedClasses = MapUtil.sortByValue(prioritizedClasses);
        for (final Map.Entry<Class<?>, Integer> entry : prioritizedClasses.entrySet()) {
            final Class<?> clazz = entry.getKey();
            final int priority = entry.getValue();

            final String className = clazz.getSimpleName();
//            lang.log.debug("&9- &r尝试自动注册 &a{0}.class&r, 优先级 &a{1}&r.", className, priority);

            try {
                final Object instance = clazz.getConstructor().newInstance();

                Autoloader.getRegisters().stream()
                        .filter(register -> register.shouldRegister(clazz))
                        .forEach(register -> {
                            register.register(plugin, clazz, instance);
//                            final String registerName = register.getClass().getSimpleName();
//                            lang.log.debug("&7| &r使用注册器 &a{0} &r自动注册 &a{1}.class&r.", registerName, className);
                        });
            } catch (NoSuchMethodException exception) {
                lang.log.error(I18n.AUTOREGISTER, clazz.getName(), "自动注册项目需要一个无参构造器.");
            } catch (Exception exception) {
                lang.log.error(I18n.AUTOREGISTER, clazz.getName(), exception, plugin.getPackageName());
                exception.printStackTrace();
            }
        }
    }

    public void registerConfiguration(final PConfiguration configuration) {
        if (Objects.isNull(configuration)) {
            return;
        }
        this.configs.put(configuration.getClass(), configuration);
        ParrotXAPI.registerConfigClass(configuration.getClass(), this.plugin.getClass());
    }

    public void registerDataSet(final Class<? extends PDataSet<?>> clazz) {
        try {
            final Type type = clazz.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                final ParameterizedType pType = (ParameterizedType) type;
                final Type[] types = pType.getActualTypeArguments();
                if (types.length > 0) {
                    Class<?> dataClass = Class.forName(types[0].getTypeName());

                    if (UniqueData.class.isAssignableFrom(dataClass)) {
                        this.dataManagers.put((Class<? extends UniqueData>) dataClass, clazz);
                        ParrotXAPI.registerDataClass(
                                (Class<? extends UniqueData>) dataClass,
                                this.plugin.getClass()
                        );
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            lang.log.debug("自动加载({0})遇到错误: {1}", clazz.getName(), e.getMessage());
        }
    }

    public <T extends PConfiguration> T getConfigurationInstance(final Class<T> clazz) {
        return clazz.cast(this.configs.get(clazz));
    }

    public <T extends UniqueData> Class<? extends PDataSet<?>> getDataHandler(final Class<T> clazz) {
        return this.dataManagers.get(clazz);
    }
}
