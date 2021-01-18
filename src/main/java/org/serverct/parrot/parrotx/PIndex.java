package org.serverct.parrot.parrotx;

import org.bukkit.event.Listener;
import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.config.PDataSet;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.UniqueData;
import org.serverct.parrot.parrotx.data.autoload.Autoload;
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
        // TODO ParrotXAPI 也要 clear;
    }

    protected void init() {
        for (Class<?> clazz : classes) {
            try {
                final Autoload annotation = clazz.getAnnotation(Autoload.class);
                if (Objects.isNull(annotation)) {
                    continue;
                }
                final Object instance = clazz.getConstructor().newInstance();

                if (PConfiguration.class.isAssignableFrom(clazz)) {
                    registerConfiguration((PConfiguration) instance);
                }

                if (Listener.class.isAssignableFrom(clazz)) {
                    plugin.registerListener((Listener) instance);
                }

                if (CommandHandler.class.isAssignableFrom(clazz)) {
                    plugin.registerCommand((CommandHandler) instance);
                }

                if (PDataSet.class.isAssignableFrom(clazz)) {
                    final Type type = clazz.getGenericSuperclass();
                    if (type instanceof ParameterizedType) {
                        final ParameterizedType pType = (ParameterizedType) type;
                        final Type[] types = pType.getActualTypeArguments();
                        if (types.length > 0) {
                            Class<?> dataClass = Class.forName(types[0].getTypeName());
                            if (UniqueData.class.isAssignableFrom(dataClass)) {
                                this.dataManagers.put(
                                        (Class<? extends UniqueData>) dataClass,
                                        (Class<? extends PDataSet<?>>) clazz
                                );
                                ParrotXAPI.registerDataClass(
                                        (Class<? extends UniqueData>) dataClass,
                                        this.plugin.getClass()
                                );
                            }
                        }
                    }
                }
            } catch (NoSuchMethodException exception) {
                lang.log.error(I18n.REGISTER, clazz.getName(), exception, plugin.getClass().getPackage().getName());
            } catch (Exception exception) {
                lang.log.debug("自动加载({0})遇到错误: {1}", clazz.getName(), exception.getMessage());
            }
        }
    }

    protected void registerConfiguration(final PConfiguration configuration) {
        this.configs.put(configuration.getClass(), configuration);
        ParrotXAPI.registerConfigClass(configuration.getClass(), this.plugin.getClass());
    }

    protected <T extends PConfiguration> T getConfigurationInstance(final Class<T> clazz) {
        return clazz.cast(this.configs.get(clazz));
    }

    protected <T extends UniqueData> Class<? extends PDataSet<?>> getDataHandler(final Class<T> clazz) {
        return this.dataManagers.get(clazz);
    }
}
