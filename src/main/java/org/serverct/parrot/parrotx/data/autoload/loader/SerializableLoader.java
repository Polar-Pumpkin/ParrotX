package org.serverct.parrot.parrotx.data.autoload.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.autoload.DataLoader;

import java.util.List;

@SuppressWarnings("unchecked")
public class SerializableLoader implements DataLoader<ConfigurationSerializable> {
    @Override
    public @NotNull Class<ConfigurationSerializable> getType() {
        return ConfigurationSerializable.class;
    }

    @Nullable
    @Override
    public ConfigurationSerializable load(@NotNull String path, @NotNull ConfigurationSection section,
                                          @NotNull List<Class<?>> classChain) {
        if (classChain.isEmpty()) {
            Autoloader.log("加载 Serializable 数据时未提供泛型类型, 路径: {0}, 数据节: {1}", path, section.getName());
            return null;
        }
        final Class<?> type = classChain.get(0);
        if (!ConfigurationSerializable.class.isAssignableFrom(type)) {
            Autoloader.log("加载 Serializable 数据时需求类型未实现序列化接口: {0}", classChain.get(0));
            return null;
        }
        final Class<? extends ConfigurationSerializable> serializableClass = (Class<?
                extends ConfigurationSerializable>) type;
        return section.getSerializable(path, serializableClass);
    }

    @Override
    public void save(@NotNull String path, @NotNull ConfigurationSection to,
                     Object value, @NotNull List<Class<?>> classChain) {
        to.set(path, value);
    }
}
