package org.serverct.parrot.parrotx.utils;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.PPlugin;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author EntityParrot_
 * @author Mical
 * @version v2.1
 * <p>
 * 自己写的一个多语言管理工具。
 */

public class I18n {

    public static final String TOOL_VERSION = "v2.1";
    public static final String LOAD = "加载";
    public static final String SAVE = "保存";
    public static final String REGISTER = "注册";
    public static final String RELOAD = "重载";
    public static final String DELETE = "删除";
    public static final String CALCULATE = "计算";
    public static final String PROTECT = "保护";
    public static final String UPGRADE = "升级";
    public static final String PARSE = "赋值变量";
    public static final String GET = "获取值";
    public static final String SET = "设置值";
    public static final String MSG = "发送消息";
    public static final String BUILD = "构建";
    public static final String LOG = "记录";
    public static final String CONTRIBUTE = "贡献";
    public static final String EXECUTE = "执行";
    public static final String INIT = "初始化";
    private String Tool_Prefix = "&7[&b&lEP's &aLocale Tool&7] ";
    private String Tool_INFO = "&a&l> ";
    private String Tool_WARN = "&e&l> ";
    private String Tool_ERROR = "&c&l> ";
    private String Tool_DEBUG = "&d&l> ";
    @Getter
    @Setter
    private String defaultLocaleKey;
    private Plugin plugin;
    private File dataFolder;
    @Getter
    private Map<String, FileConfiguration> locales = new HashMap<>();

    /**
     * @param plugin           JavaPlugin 对象，一般为插件的主类，将用于语言工具获取插件 jar 包内的默认语言文件。
     * @param defaultLocaleKey 默认语言，需要插件 jar 包内的 Locales 文件夹中拥有以该默认语言命名的语言文件。
     */
    public I18n(Plugin plugin, String defaultLocaleKey) {
        this.plugin = plugin;
        this.defaultLocaleKey = defaultLocaleKey;
        this.dataFolder = new File(plugin.getDataFolder(), "Locales");
        init();
    }

    /**
     * 快速向玩家发送信息。
     *
     * @param user    目标玩家。
     * @param message 消息内容
     */
    public static void send(Player user, String message) {
        if (user != null) {
            user.sendMessage(color(message));
        }
    }

    /**
     * 异步向玩家发送信息。
     *
     * @param user    目标玩家。
     * @param message 消息内容
     */
    public static void sendAsync(@NonNull PPlugin plugin, Player user, String message) {
        if (user != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    user.sendMessage(color(message));
                }
            }.runTaskLater(plugin, 1);
        }
    }


    /**
     * 获取不带后缀名的文件名。
     *
     * @param fileName 文件全名。
     * @return 不带后缀名的文件名。
     */
    public static String getNoExFileName(String fileName) {
        if ((fileName != null) && (fileName.length() > 0)) {
            int dot = fileName.lastIndexOf('.');
            if ((dot > -1) && (dot < (fileName.length()))) {
                return fileName.substring(0, dot);
            }
        }
        return fileName;
    }

    /**
     * 快速给文本上色。
     * 因为 ChatColor.translateAlternateColorCodes() 方法名字太长了。
     *
     * @param text 文本。
     * @return 上色后的文本。
     */
    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * 快速给文本替换颜色代码字符为替代字符。
     *
     * @param text               文本。
     * @param alternateColorCode 替代颜色字符。
     * @return 使用替代字符表示颜色代码的文本。
     */
    public static String deColor(String text, char alternateColorCode) {
        return text.replace("§", String.valueOf(alternateColorCode));
    }

    /**
     * 初始化语言工具，比如生成默认语言文件啥的。
     */
    public void init() {
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                plugin.saveResource("Locales/" + defaultLocaleKey + ".yml", false);
                log("未找到语言文件夹, 已自动生成.", Type.WARN, true);
            } else {
                log("尝试生成语言文件夹失败.", Type.ERROR, true);
            }
        }
        File[] files = dataFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.saveResource("Locales/" + defaultLocaleKey + ".yml", false);
            log("未找到默认语言文件, 已自动生成.", Type.WARN, true);
            files = dataFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        }
        if (files != null && files.length > 0) {
            boolean check = false;
            for (File file : files) {
                if (getNoExFileName(file.getName()).equals(defaultLocaleKey)) {
                    check = true;
                }
            }
            if (!check) {
                plugin.saveResource("Locales/" + defaultLocaleKey + ".yml", false);
                log("未找到默认语言文件, 已自动生成.", Type.WARN, true);
            }
        }
        load();
    }

    /**
     * 加载语言数据，可以通过调用此方法来重载语言文件(但是不会检测/释放默认语言文件)。
     */
    public void load() {
        log("版本: &c" + TOOL_VERSION, Type.INFO, true);
        File[] localeDataFiles = dataFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        if (localeDataFiles != null && localeDataFiles.length > 0) {
            for (File dataFile : localeDataFiles) {
                locales.put(getNoExFileName(dataFile.getName()), YamlConfiguration.loadConfiguration(dataFile));
            }
        }
        log("语言数据加载成功, 共加载了 &c" + locales.size() + " &7个语言文件.", Type.INFO, true);
    }

    /**
     * 判断是否加载了指定的语言文件。
     *
     * @param key 目标语言名。
     * @return 是否加载了该语言。
     */
    public boolean hasKey(String key) {
        return key != null && locales.containsKey(key);
    }

    /**
     * 获取指定的语言数据。
     *
     * @param key 目标语言名，若需要获取默认语言数据可以填写 null。
     * @return 目标语言数据的 FileConfiguration 对象。
     */
    public FileConfiguration getData(String key) {
        return key == null ? locales.get(defaultLocaleKey) : locales.getOrDefault(key, null);
    }

    /**
     * 向控制台发送日志信息，不会带有格式化前缀。
     *
     * @param message 消息内容。
     */
    public void logRaw(String message) {
        if (message != null) {
            Bukkit.getConsoleSender().sendMessage(color(message));
        }
    }

    public void logRaw(List<String> messages) {
        messages.forEach(this::logRaw);
    }

    public void logRaw(String[] messages) {
        logRaw(Arrays.asList(messages));
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
            result = color(Tool_Prefix + Tool_DEBUG + message);
        } else {
            if (plugin.getConfig().getBoolean("Debug", true)) {
                result = build(defaultLocaleKey, Type.DEBUG, message);
            }
        }
        logRaw(result);
    }

    /**
     * 向控制台发送带有格式化前缀的日志信息。
     *
     * @param message 消息内容。
     * @param type    消息类型。
     * @param viaTool 是否通过该语言工具来发送信息。
     */
    public void log(String message, Type type, boolean viaTool) {
        if (type != Type.DEBUG) {
            String level;
            switch (type) {
                case INFO:
                default:
                    level = Tool_INFO;
                    break;
                case WARN:
                    level = Tool_WARN;
                    break;
                case ERROR:
                    level = Tool_ERROR;
                    break;
            }
            logRaw(viaTool ? color(Tool_Prefix + level + ChatColor.GRAY + message) : build(null, type, message));
        } else {
            debug(message, viaTool);
        }
    }

    public void log(List<String> messages, Type type, boolean viaTool) {
        messages.forEach(s -> log(s, type, viaTool));
    }

    public void log(String[] messages, Type type, boolean viaTool) {
        log(Arrays.asList(messages), type, viaTool);
    }

    /**
     * 快速向控制台发送带有格式化前缀的操作日志信息。
     *
     * @param action 动作名称。
     * @param object 操作对象。
     */
    public void logAction(String action, String object) {
        String message = "尝试%action% &c%object%&7."
                .replace("%action%", action)
                .replace("%object%", object);
        log(message, Type.INFO, false);
    }

    /**
     * 尝试获取指定语言的指定语言信息并快速替换。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param section 目标节，若没有分节可以填写 null。
     * @param path    目标路径。
     * @param args    快速替换的文本信息。
     * @return 带有格式化前缀的语言信息。
     */
    public String format(String key, Type type, String section, String path, Object... args) {
        return MessageFormat.format(get(key, type, section, path), args);
    }

    /**
     * 尝试获取指定语言的指定语言信息并快速替换，不会带有格式化前缀。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param section 目标节，若没有分节可以填写 null。
     * @param path    目标路径。
     * @param args    快速替换的文本信息。
     * @return 不带格式化前缀的语言信息。
     */
    public String formatRaw(String key, String section, String path, Object... args) {
        return MessageFormat.format(getRaw(key, section, path), args);
    }

    /**
     * 快速向控制台发送带有格式化前缀的错误日志信息。
     *
     * @param action    动作名称。
     * @param object    操作对象。
     * @param exception 错误内容。
     */
    public void logError(String action, String object, String exception) {
        String message = "%action% &c%object% &7时遇到错误(&c%exception%&7)."
                .replace("%action%", action)
                .replace("%object%", object)
                .replace("%exception%", exception);
        log(message, Type.ERROR, false);
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
    public void logError(String action, String object, Throwable e, String packageFilter) {
        logError(action, object, e.toString());
        printStackTrace(e, packageFilter);
    }

    /**
     * 快速向控制台输出错误的堆栈跟踪。
     *
     * @param exception     Throwable 类型的异常。
     * @param packageFilter 包名关键词过滤，不需要可以填写 null。
     */
    public void printStackTrace(Throwable exception, String packageFilter) {
        String msg = exception.getLocalizedMessage();
        logRaw("========================= &c&lprintStackTrace &7=========================");
        logRaw("Exception Type ▶");
        logRaw(ChatColor.RED + exception.getClass().getName());
        logRaw(ChatColor.RED + ((msg == null || msg.length() == 0) ? "No description." : msg));
        // org.serverct.parrot.plugin.Plugin
        String lastPackage = "";
        for (StackTraceElement elem : exception.getStackTrace()) {
            String key = elem.getClassName();

            boolean pass = true;
            if (packageFilter != null) {
                pass = key.contains(packageFilter);
            }

            String[] nameSet = key.split("[.]");
            String className = nameSet[nameSet.length - 1];
            String[] packageSet = new String[nameSet.length - 2];
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
                    logRaw("");
                    logRaw("Package &c" + packageName + " &7▶");
                }
                logRaw("  ▶ at Class &c" + className + "&7, Method &c" + elem.getMethodName() + "&7. (&c" + elem.getFileName() + "&7, Line &c" + elem.getLineNumber() + "&7)");
            }
        }
        logRaw("========================= &c&lprintStackTrace &7=========================");
    }

    /**
     * 尝试获取指定语言的指定语言信息，不会带有格式化前缀。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param section 目标节，若没有分节可以填写 null。
     * @param path    目标路径。
     * @return 不带格式化前缀的语言信息。
     */
    public String getRaw(String key, String section, String path) {
        FileConfiguration data = getData(hasKey(key) ? key : null);
        String message = "&c&l错误&7(获取语言数据时遇到错误, 请联系管理员解决该问题)";
        String testGet = null;

        if (section == null) {
            testGet = data.getString(path);
        } else {
            if (data.isConfigurationSection(section)) {
                testGet = data.getString(section + "." + path);
            }
        }

        if (testGet != null && !testGet.equalsIgnoreCase("")) {
            message = testGet;
        } else {
            String[] getError = {
                    "尝试获取原始语言数据时遇到错误 ▶",
                    "插件名 ▶ &c" + plugin.getName(),
                    "目标语言 ▶ &c" + key,
                    "节 ▶ &c" + (section == null ? "无" : section),
                    "路径 ▶ &c" + path,
                    "------------------------------"
            };
            log(getError, Type.ERROR, true);
        }
        return color(ChatColor.GRAY + message);
    }

    /**
     * 尝试获取指定语言并带有格式化前缀的指定语言信息。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param type    消息类型。
     * @param section 目标节，若没有分节可以填写 null。
     * @param path    目标路径。
     * @return 带有格式化前缀的语言信息。
     */
    public String get(String key, Type type, String section, String path) {
        return build(key, type, getRaw(key, section, path));
    }

    /**
     * 尝试获取指定语言的插件帮助信息。
     * 可用变量：
     * %version% -> 插件的版本。
     *
     * @param key 目标语言名，当未加载时使用默认语言名。
     * @return 帮助信息列表。
     */
    public List<String> getHelp(String key) {
        FileConfiguration data = getData(hasKey(key) ? key : null);
        List<String> help = data.getStringList("Plugin.Help");
        help.replaceAll(I18n::color);
        help.replaceAll(s -> s.replace("%version%", plugin.getDescription().getVersion()));
        return help;
    }

    /**
     * 构建带有格式化前缀的文本。
     *
     * @param key     目标语言名，当未加载时使用默认语言名。
     * @param type    消息类型。
     * @param message 消息内容。
     * @return 带有格式化前缀的文本信息。
     */
    public String build(String key, Type type, String message) {
        FileConfiguration data = getData(hasKey(key) ? key : null);
        String pluginPrefix = type != Type.DEBUG ? data.getString("Plugin.Prefix", "&f[&9&l" + plugin.getName() + "&f] ") : "&f[&d" + plugin.getName() + "&f]&7(&d&lDEBUG&7) ";
        String typePrefix = type != Type.DEBUG ? data.getString("Plugin." + type.toString(), "&3▶ ") : "&d&l>> ";
        return color(pluginPrefix + typePrefix + ChatColor.GRAY + message);
    }

    public enum Type {
        INFO, WARN, ERROR, DEBUG
    }
}
