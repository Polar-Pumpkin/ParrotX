package org.serverct.parrot.parrotx.data.autoload;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoload;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoloadGroup;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoloadGroups;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

@Data
public class AutoloadSetting {

    private final PPlugin plugin;
    private final I18n lang;
    private final Class<?> model;
    private final Map<String, PAutoloadGroup> groups = new HashMap<>();
    private final Multimap<String, AutoloadItem> items = HashMultimap.create();

    public AutoloadSetting(PPlugin plugin, Class<?> model) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.model = model;
        buildIndex();
    }

    protected void buildIndex() {
        try {
            int counter = 0;

            final PAutoloadGroups multiAnnotation = model.getAnnotation(PAutoloadGroups.class);
            if (Objects.isNull(multiAnnotation)) {
                final PAutoloadGroup annotation = model.getAnnotation(PAutoloadGroup.class);
                if (Objects.isNull(annotation)) {
                    lang.log.debug("该类对象无需自动导入.");
                    return;
                }
                newGroup(annotation);
            } else {
                for (PAutoloadGroup group : multiAnnotation.value()) {
                    newGroup(group);
                }
            }
            lang.log.debug("&f从 &c{0}.class &f中自动导入了 &c{1} &f个自动加载项目组.", model.getSimpleName(), this.groups.size());

            for (Field field : model.getDeclaredFields()) {
                final PAutoload annotation = field.getAnnotation(PAutoload.class);

                if (Objects.isNull(annotation)) {
                    continue;
                }

                final List<Class<?>> classChain = chain(field.getGenericType(), new ArrayList<>());

                if (ConfigurationSerializable.class.isAssignableFrom(field.getType())) {
                    classChain.add(field.getType());
                }

                newItem(AutoloadItem.builder()
                        .group(annotation.group())
                        .path(annotation.value().replace("{FIELD}", field.getName()))
                        .field(field.getName())
                        .type(field.getType())
                        .classChain(classChain)
                        .build());
                counter++;
            }

            if (counter > 0) {
                lang.log.debug("&f从 &c{0}.class &f中自动导入了 &c{1} &f个自动加载项目.", model.getSimpleName(), counter);
            }

            print();
        } catch (Exception e) {
            lang.log.error(
                    I18n.INDEX,
                    MessageFormat.format("自动加载设置({0})", model.getSimpleName() + ".class"),
                    e,
                    plugin.getPackageName()
            );
        }
    }

    protected void newGroup(@NotNull final PAutoloadGroup group) {
        this.groups.put(group.name(), group);
    }

    protected void newItem(@Nullable final AutoloadItem... items) {
        if (Objects.isNull(items) || items.length <= 0) {
            return;
        }
        for (AutoloadItem item : items) {
            if (Objects.isNull(item)) {
                continue;
            }
            this.items.put(item.getGroup(), item);
        }
    }

    @Nullable
    protected PAutoloadGroup getGroup(@NotNull final String groupName) {
        return this.groups.get(groupName);
    }

    @NotNull
    protected String getExtraPath(@NotNull final String groupName) {
        final PAutoloadGroup group = getGroup(groupName);
        return Objects.isNull(group) ? "" : group.value();
    }

    protected void print() {
        if (this.groups.isEmpty() && this.items.isEmpty()) {
            return;
        }
        final List<String> info = new ArrayList<>();

        info.add("&f自动加载器 &d" + this.model.getSimpleName() + " &f的自动加载项目");
        for (Map.Entry<String, Collection<AutoloadItem>> entry : this.items.asMap().entrySet()) {
            final String groupName = entry.getKey();
            final Collection<AutoloadItem> value = entry.getValue();

            final PAutoloadGroup group = this.groups.get(groupName);
            if (Objects.isNull(group)) {
                continue;
            }
            final String extraPath = "default".equalsIgnoreCase(groupName) ? "" : group.value().replace("{GROUP}",
                    groupName);
            final String header = MessageFormat.format("&9- &f组 &c{0}{1}&f, 忽略默认组路径: {2}",
                    groupName,
                    StringUtils.isEmpty(extraPath) ? "" : "&f, 额外路径: &e" + extraPath,
                    group.ignoreDefaultPath() ? "&a是" : "&c否"
            );
            info.add(header);

            value.forEach(item -> {
                final StringBuilder paramType = new StringBuilder("[");
                Iterator<Class<?>> iterator = item.getClassChain().iterator();
                while (iterator.hasNext()) {
                    final Class<?> clazz = iterator.next();
                    paramType.append(clazz.getSimpleName()).append(".class");
                    if (iterator.hasNext()) {
                        paramType.append(", ");
                    }
                }
                paramType.append("]");

                final String field = MessageFormat.format("&7|  &f- 字段 &c{0} &f(&9{1}&f) &a<- &e{2}",
                        item.getField(),
                        item.getType().getSimpleName() + ".class - " + paramType,
                        item.getPath()
                );
                info.add(field);
            });
        }

        info.forEach(lang.log::debug);
    }

    @NotNull
    private List<Class<?>> chain(@NotNull final Type type, @NotNull final List<Class<?>> classes) {
        try {
            if (type instanceof ParameterizedType) {
                final ParameterizedType paramType = (ParameterizedType) type;
                classes.add(Class.forName(paramType.getRawType().getTypeName()));
                for (Type argument : paramType.getActualTypeArguments()) {
                    if (argument instanceof ParameterizedType) {
                        return chain(argument, classes);
                    }
                    final String name = argument.getTypeName();
                    if (name.equals("?")) {
                        continue;
                    }
                    classes.add(Class.forName(name));
                }
            } else {
                final String classpath = type.getTypeName();
                final String[] args = classpath.split("[.]");
                if (args.length > 1) {
                    classes.add(Class.forName(classpath));
                }
            }
        } catch (ClassNotFoundException error) {
            lang.log.error(I18n.AUTOLOAD, "探索类型链", error, plugin.getPackageName());
        }
        return classes;
    }
}
