package org.serverct.parrot.parrotx.data.autoload;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
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
    private final Map<String, String> groups = new HashMap<>();
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
                newGroup(annotation.name(), annotation.value());
            } else {
                for (PAutoloadGroup group : multiAnnotation.value()) {
                    newGroup(group.name(), group.value());
                }
            }
            lang.log.debug("&f从 &c{0}.class &f中自动导入了 &c{1} &f个自动加载项目组.", model.getSimpleName(), this.groups.size());

            for (Field field : model.getDeclaredFields()) {
                final PAutoload annotation = field.getAnnotation(PAutoload.class);

                if (Objects.isNull(annotation)) {
                    continue;
                }

                final List<Class<?>> paramTypes = new ArrayList<>();

                final Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    final ParameterizedType paramType = (ParameterizedType) type;
                    for (Type argument : paramType.getActualTypeArguments()) {
                        paramTypes.add(Class.forName(argument.getTypeName()));
                    }
                }

                if (ConfigurationSerializable.class.isAssignableFrom(field.getType())) {
                    paramTypes.add(field.getType());
                }

                newItem(AutoloadItem.builder()
                        .group(annotation.group())
                        .path(annotation.value().replace("{FIELD}", field.getName()))
                        .field(field.getName())
                        .type(field.getType())
                        .paramTypes(paramTypes)
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

    protected void newGroup(@NotNull final String name, @NotNull final String extraPath) {
        this.groups.put(name, extraPath);
    }

    protected void newItem(final AutoloadItem... items) {
        if (Objects.isNull(items) || items.length <= 0) {
            return;
        }
        for (AutoloadItem item : items) {
            this.items.put(item.getGroup(), item);
        }
    }

    protected String getExtraPath(@NotNull final String group) {
        return this.groups.get(group);
    }

    protected void print() {
        if (this.groups.isEmpty() && this.items.isEmpty()) {
            return;
        }
        final List<String> info = new ArrayList<>();

        info.add("&f自动加载器 &d" + this.model.getSimpleName() + " &f的自动加载项目");
        this.items.asMap().forEach((group, items) -> {
            final String extraPath = this.groups.get(group);
            final String header = MessageFormat.format("&9- &f组 &c{0}{1}",
                    group,
                    Objects.isNull(extraPath) || extraPath.length() == 0 ? "" : "&f, 额外路径: &e" + extraPath
            );
            info.add(header);

            items.forEach(item -> {
                final StringBuilder paramType = new StringBuilder("[");
                Iterator<Class<?>> iterator = item.getParamTypes().iterator();
                while (iterator.hasNext()) {
                    final Class<?> clazz = iterator.next();
                    paramType.append(clazz.getSimpleName()).append(".class");
                    if (iterator.hasNext()) {
                        paramType.append(", ");
                    }
                }
                paramType.append("]");

                final String entry = MessageFormat.format("&7|  &f- 字段 &c{0} &f(&9{1}&f) &a<- &e{2}",
                        item.getField(),
                        item.getType().getSimpleName() + ".class - " + paramType.toString(),
                        item.getPath()
                );
                info.add(entry);
            });
            info.add("&7|");
        });

        info.forEach(lang.log::debug);
    }
}
