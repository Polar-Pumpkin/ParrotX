package org.serverct.parrot.parrotx.utils.i18n;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.serverct.parrot.parrotx.PPlugin;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

public class PLogger {

    private final I18n lang;
    private final PPlugin plugin;

    public PLogger(final I18n i18n) {
        this.lang = i18n;
        this.plugin = lang.getPlugin();
    }

    /**
     * 向控制台发送日志信息，不会带有格式化前缀。
     *
     * @param message 消息内容。
     */
    public void log(String message) {
        if (Objects.isNull(message)) {
            return;
        }
        Bukkit.getConsoleSender().sendMessage(I18n.color(message));
    }

    public void log(List<String> messages) {
        messages.forEach(this::log);
    }

    /**
     * 向控制台发送 Debug 类型的信息，区别在于 Debug 类型的消息拥有特别的前缀。
     *
     * @param message 消息内容。
     * @param viaTool 是否通过该语言工具来发送信息。
     */
    public void debug(String message, boolean viaTool) {
        String result = null;
        if (viaTool) {
            result = I18n.Tool_Prefix + I18n.Tool_DEBUG + message;
        } else {
            if (plugin.getConfig().getBoolean("Debug", true)) {
                result = lang.data.build(I18n.Type.DEBUG, message);
            }
        }
        log(result);
    }

    public void debug(final String message, final Object... args) {
        debug(MessageFormat.format(message, args), false);
    }

    /**
     * 向控制台发送带有格式化前缀的日志信息。
     *
     * @param message 消息内容。
     * @param type    消息类型。
     * @param viaTool 是否通过该语言工具来发送信息。
     */
    public void log(String message, I18n.Type type, boolean viaTool) {
        if (type == I18n.Type.DEBUG) {
            debug(message, viaTool);
            return;
        }

        String level;
        switch (type) {
            case INFO:
            default:
                level = I18n.Tool_INFO;
                break;
            case WARN:
                level = I18n.Tool_WARN;
                break;
            case ERROR:
                level = I18n.Tool_ERROR;
                break;
        }
        log(viaTool ? I18n.Tool_Prefix + level + message : lang.data.build(type, message));
    }

    public void log(List<String> messages, I18n.Type type, boolean viaTool) {
        messages.forEach(s -> log(s, type, viaTool));
    }

    public void info(final String message, final Object... args) {
        log(MessageFormat.format(message, args), I18n.Type.INFO, false);
    }

    public void info(final List<String> message) {
        log(message, I18n.Type.INFO, false);
    }

    public void warn(final String message, final Object... args) {
        log(MessageFormat.format(message, args), I18n.Type.WARN, false);
    }

    public void warn(final List<String> message) {
        log(message, I18n.Type.WARN, false);
    }

    public void error(final String message, final Object... args) {
        log(MessageFormat.format(message, args), I18n.Type.ERROR, false);
    }

    public void error(final List<String> message) {
        log(message, I18n.Type.ERROR, false);
    }

    /**
     * 快速向控制台发送带有格式化前缀的操作日志信息。
     *
     * @param action 动作名称。
     * @param object 操作对象。
     */
    public void action(String action, String object, Object... args) {
        log(MessageFormat.format("&7尝试{0} &c{1}&7.", action, MessageFormat.format(object, args)), I18n.Type.DEBUG,
                false);
    }

    /**
     * 快速向控制台输出错误的堆栈跟踪。
     *
     * @param exception     Throwable 类型的异常。
     * @param packageFilter 包名关键词过滤，不需要可以填写 null。
     */
    public void printStackTrace(Throwable exception, String packageFilter) {
        String msg = exception.getLocalizedMessage();
        log("&7===================================&c&l printStackTrace &7===================================");
        log("&7Exception Type ▶");
        log(ChatColor.RED + exception.getClass().getName());
        log(ChatColor.RED + ((msg == null || msg.length() == 0) ? "&7No description." : msg));
        // org.serverct.parrot.plugin.Plugin
        String lastPackage = "";
        for (StackTraceElement elem : exception.getStackTrace()) {
            String key = elem.getClassName();

            boolean pass = true;
            if (packageFilter != null) {
                pass = key.contains(packageFilter);
            }

            final String[] nameSet = key.split("[.]");
            final String className = nameSet[nameSet.length - 1];
            final String[] packageSet = new String[nameSet.length - 2];
            System.arraycopy(nameSet, 0, packageSet, 0, nameSet.length - 2);

            StringBuilder packageName = new StringBuilder();
            int counter = 0;
            for (String nameElem : packageSet) {
                packageName.append(nameElem);
                if (counter < packageSet.length - 1) {
                    packageName.append(".");
                }
                counter++;
            }

            if (pass) {
                if (!packageName.toString().equals(lastPackage)) {
                    lastPackage = packageName.toString();
                    log("");
                    log("&7Package &c" + packageName + " &7▶");
                }
                log("  &7▶ at Class &c" + className + "&7, Method &c" + elem.getMethodName() + "&7. (&c" + elem.getFileName() + "&7, Line &c" + elem.getLineNumber() + "&7)");
            }
        }
        log("&7===================================&c&l printStackTrace &7===================================");
    }

    /**
     * 快速向控制台发送带有格式化前缀的错误日志信息。
     *
     * @param action    动作名称。
     * @param object    操作对象。
     * @param exception 错误内容。
     * @param args      消息变量。
     */
    public void error(String action, String object, String exception, Object... args) {
        log(lang.data.error("&7{0} &c{1} &7时遇到错误(&c{2}&7).", action, object, I18n.format(exception, args)));
    }

    /**
     * 快速向控制台发送带有格式化前缀的错误日志信息。
     * 该方法常用于 try...catch 语句块中，至少我是这么用的。awa
     *
     * @param action        动作名称。
     * @param object        操作对象。
     * @param e             Throwable 类型的异常。
     * @param packageFilter 包名关键词过滤，不需要可以填写 null。
     */
    public void error(String action, String object, Throwable e, String packageFilter) {
        error(action, object, e.toString());
        printStackTrace(e, packageFilter);
    }
}
