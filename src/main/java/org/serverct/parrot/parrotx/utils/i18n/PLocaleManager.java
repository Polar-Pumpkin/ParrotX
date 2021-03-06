package org.serverct.parrot.parrotx.utils.i18n;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;

public class PLocaleManager {

    private final I18n lang;
    private final PPlugin plugin;

    public PLocaleManager(final I18n i18n) {
        this.lang = i18n;
        this.plugin = lang.getPlugin();
    }

    /**
     * 尝试获取指定语言的指定语言信息，不会带有格式化前缀。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param section 目标节，若没有分节可以填写 null。
     * @param path    目标路径。
     * @return 不带格式化前缀的语言信息。
     */
    public String get(final String key, final String section, final String path, final Object... args) {
        FileConfiguration data = lang.getLocale(lang.hasLocale(key) ? key : null);
        String message = "&c&l错误&7(获取语言数据时遇到错误, 请联系管理员解决该问题)";
        String testGet = null;

        if (Objects.isNull(section) || "".equalsIgnoreCase(section) || section.length() == 0) {
            testGet = data.getString(path);
        } else {
            if (data.isConfigurationSection(section)) {
                testGet = data.getString(section + "." + path);
            }
        }

        if (Objects.nonNull(testGet)) {
            message = testGet;
        } else {
            String[] getError = {
                    "尝试获取原始语言数据时遇到错误 ▶",
                    "  插件: &c" + plugin.getName(),
                    "  语言: &c" + key,
                    "  节点: &c" + (section == null ? "未指定" : (StringUtils.isEmpty(section) ? "根节点" : section)),
                    "  路径: &c" + path,
                    "------------------------------"
            };
            lang.log.log(Arrays.asList(getError), I18n.Type.ERROR, true);
        }
        if (args.length > 0) {
            message = MessageFormat.format(message, args);
        }
        return I18n.color(message);
    }

    public String get(final String key, final String path, final Object... args) {
        return get(key, "", path, args);
    }

    /**
     * 尝试获取指定语言并带有格式化前缀的指定文本并快速替换变量。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param type    消息类型。
     * @param section 目标节，若没有分节可以填写 null。
     * @param path    目标路径。
     * @param args    文本中需要替换的变量。
     * @return 带有格式化前缀的语言信息。
     */
    public String get(final String key, final I18n.Type type, final String section, final String path, final Object... args) {
        return build(key, type, get(key, section, path, args));
    }

    public String get(final String key, final I18n.Type type, final String path, final Object... args) {
        return build(key, type, get(key, path, args));
    }

    public String get(final I18n.Type type, final String section, final String path, final Object... args) {
        return get(plugin.localeKey, type, section, path, args);
    }

    public String get(final I18n.Type type, final String path, final Object... args) {
        return get(plugin.localeKey, type, path, args);
    }

    public String getInfo(final String section, final String path, final Object... args) {
        return get(plugin.localeKey, I18n.Type.INFO, section, path, args);
    }

    public String getInfo(final String path, final Object... args) {
        return get(plugin.localeKey, I18n.Type.INFO, path, args);
    }

    public String getWarn(final String section, final String path, final Object... args) {
        return get(plugin.localeKey, I18n.Type.WARN, section, path, args);
    }

    public String getWarn(final String path, final Object... args) {
        return get(plugin.localeKey, I18n.Type.WARN, path, args);
    }

    public String getError(final String section, final String path, final Object... args) {
        return get(plugin.localeKey, I18n.Type.ERROR, section, path, args);
    }

    public String getError(final String path, final Object... args) {
        return get(plugin.localeKey, I18n.Type.ERROR, path, args);
    }

    /**
     * 构建带有格式化前缀的文本。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param type    消息类型。
     * @param message 消息内容。
     * @return 带有格式化前缀的文本信息。
     */
    @NotNull
    public String build(final String key, final I18n.Type type, final String message, final Object... args) {
        if (StringUtils.isEmpty(message)) {
            return "";
        }
        String pluginPrefix = I18n.format("{0} ", plugin.getName());
        String typePrefix = "&3▶&r ";
        if (type == I18n.Type.DEBUG) {
            pluginPrefix = I18n.format("&f[&d{0}&f]&7(&d&lDEBUG&7)&r ", plugin.getName());
            typePrefix = "&d&l>>&r ";
        } else {
            FileConfiguration data = lang.getLocale(lang.hasLocale(key) ? key : null);
            if (Objects.nonNull(data)) {
                pluginPrefix = data.getString("Plugin.Prefix", I18n.format("&f[&9&l{0}&f]&r ", plugin.getName()));
                typePrefix = data.getString("Plugin." + type.name(), "&3▶&r ");
            } else {
                lang.log.log("试图读取未加载的语言: &c" + key, I18n.Type.ERROR, true);
            }
        }

        String result = pluginPrefix + typePrefix + ChatColor.RESET + message;
        if (args.length > 0) {
            result = MessageFormat.format(result, args);
        }
        return I18n.color(result);
    }

    public String build(final I18n.Type type, final String message, final Object... args) {
        return build(plugin.localeKey, type, message, args);
    }

    public String info(final String message, final Object... args) {
        return build(I18n.Type.INFO, message, args);
    }

    public String warn(final String message, final Object... args) {
        return build(I18n.Type.WARN, message, args);
    }

    public String error(final String message, final Object... args) {
        return build(I18n.Type.ERROR, message, args);
    }
}
