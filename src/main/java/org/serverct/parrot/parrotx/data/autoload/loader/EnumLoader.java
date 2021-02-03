package org.serverct.parrot.parrotx.data.autoload.loader;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.autoload.DataLoader;
import org.serverct.parrot.parrotx.utils.EnumUtil;

import java.util.List;
import java.util.Objects;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EnumLoader implements DataLoader<Enum> {
    @Override
    public @NotNull Class<Enum> getType() {
        return Enum.class;
    }

    @Override
    public @Nullable Enum load(@NotNull String path, @NotNull ConfigurationSection section,
                               @NotNull List<Class<?>> classChain) {
        if (classChain.size() < 2) {
            Autoloader.log(
                    "加载枚举数据时未提供泛型类型, 路径: {0}, 数据节: {1}, 类型链: {2}",
                    path, section.getName(), classChain
            );
            return null;
        }
        final Class<?> type = classChain.get(1);
        if (!Enum.class.isAssignableFrom(type)) {
            Autoloader.log("加载枚举数据时需求类型未实现序列化接口: {0}", classChain.get(1));
            return null;
        }
        final String name = section.getString(path);
        if (Objects.isNull(name)) {
            Autoloader.log(
                    "加载枚举数据时枚举名无效, 路径: {0}, 数据节: {1}, 类型链: {2}",
                    path, section.getName(), classChain
            );
            return null;
        }
        return EnumUtil.valueOf((Class<? extends Enum>) type, name.toUpperCase());
    }

    @Override
    public void save(@NotNull String path, @NotNull ConfigurationSection to, Object value,
                     @NotNull List<Class<?>> classChain) {
        to.set(path, Objects.isNull(value) ? null : ((Enum) value).name());
    }
}
