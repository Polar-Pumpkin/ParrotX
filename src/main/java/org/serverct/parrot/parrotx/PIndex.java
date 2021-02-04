package org.serverct.parrot.parrotx;

import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.config.PDataSet;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.UniqueData;
import org.serverct.parrot.parrotx.data.autoload.AutoRegister;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoload;
import org.serverct.parrot.parrotx.utils.ClassUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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
        for (Class<?> clazz : classes) {
            try {
                final PAutoload annotation = clazz.getAnnotation(PAutoload.class);
                if (Objects.isNull(annotation)) {
                    continue;
                }
                final Object instance = clazz.getConstructor().newInstance();

                for (Map.Entry<Class<?>, AutoRegister> entry : Autoloader.getRegisterEntries()) {
                    final AutoRegister register = entry.getValue();
                    if (register.shouldRegister(clazz)) {
                        register.register(plugin, clazz, instance);
                    }
                }
            } catch (NoSuchMethodException exception) {
                lang.log.error(I18n.REGISTER, clazz.getName(), exception, plugin.getPackageName());
            } catch (Exception exception) {
                lang.log.debug("自动加载({0})遇到错误: {1}", clazz.getName(), exception.getMessage());
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
