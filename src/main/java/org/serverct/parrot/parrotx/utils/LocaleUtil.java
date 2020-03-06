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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocaleUtil {

    public static final String TOOL_VERSION = "v1.7.4";
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

    public LocaleUtil(Plugin plugin, String defaultLocaleKey) {
        this.plugin = plugin;
        this.defaultLocaleKey = defaultLocaleKey;
        dataFolder = new File(plugin.getDataFolder() + File.separator + "Locales");
        init();
    }

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

    private void load() {
        log("版本: &c" + TOOL_VERSION, Type.INFO, true);
        File[] localeDataFiles = dataFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        if (localeDataFiles != null && localeDataFiles.length > 0) {
            for (File dataFile : localeDataFiles) {
                locales.put(getNoExFileName(dataFile.getName()), YamlConfiguration.loadConfiguration(dataFile));
            }
        }
        log("语言数据加载成功, 共加载了 &c" + locales.size() + " &7个语言文件.", Type.INFO, true);
    }

    public void debug(String message, boolean viaTool) {
        if (viaTool) {
            Bukkit.getLogger().info(color(Tool_Prefix + Tool_DEBUG + message));
        } else {
            if (plugin.getConfig().getBoolean("Debug")) {
                Bukkit.getLogger().info(build(null, Type.DEBUG, message));
            }
        }
    }

    public void logRaw(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }

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

    public void logAction(String action, String object) {
        String message = "尝试%action% &c%object%&7."
                .replace("%action%", action)
                .replace("%object%", object);
        log(message, Type.INFO, false);
    }

    public void logError(String action, String object, String exception) {
        String message = "%action% &c%object% &7时遇到错误(&c%exception%&7)."
                .replace("%action%", action)
                .replace("%object%", object)
                .replace("%exception%", exception);
        log(message, Type.ERROR, false);
    }

    public void logError(String action, String object, Throwable e) {
        logError(action, object, e.toString());
        logRaw("==================== &c&l以下是堆栈跟踪 &7====================");
        logRaw("&d▶ &7异常类型: &c" + e.toString());
        String lastPackage = null;
        for (StackTraceElement element : e.getStackTrace()) {
            String key = element.getClassName();
            Class<? extends StackTraceElement> targetClass = element.getClass();
            String packageName = targetClass.getPackage().getName();
            String className = targetClass.getSimpleName();
            String methodName = element.getMethodName();
            int lineNumber = element.getLineNumber();
            String fileName = element.getFileName();
            if (key.contains("serverct")) {
                if (!packageName.equals(lastPackage)) {
                    lastPackage = packageName;
                    logRaw("&c" + packageName + " &7包 ▶");
                }
                logRaw("&d▶ &7于类 &c" + className + " &7中 &c" + methodName + " &7方法处. (&c" + fileName + "&7, 第 &c" + lineNumber + " &7行)");
            }
        }
        logRaw("==================== &c&l请反馈给开发者 &7====================");
    }

    public boolean hasKey(String key) {
        return locales.containsKey(key);
    }

    public String getRaw(String key, String section, String path) {
        FileConfiguration data = hasKey(key) ? locales.get(key) : locales.get(defaultLocaleKey);

        if (data.getKeys(false).contains(section)) {
            String message = data.getConfigurationSection(section).getString(path);
            if (message != null && !message.equalsIgnoreCase("")) {
                return color(message);
            }
        }

        log("尝试获取原始语言数据时遇到错误." + "\n"
                + "&7语言: &c" + key + "\n"
                + "&7节: &c" + section + "\n"
                + "&7路径: &c" + path
                + "&7.", Type.ERROR, true);
        return color("&c&l错误&7(获取语言数据时遇到错误, 请联系管理员解决该问题.)");
    }

    public String get(String key, Type type, String section, String path) {
        FileConfiguration data = hasKey(key) ? locales.get(key) : locales.get(defaultLocaleKey);

        if (data.getKeys(false).contains(section)) {
            String message = data.getConfigurationSection(section).getString(path);
            if (message != null && !message.equalsIgnoreCase("")) {
                return build(key, type, message);
            }
        }

        log("尝试获取语言数据时遇到错误." + "\n"
                + "&7语言: &c" + key + "\n"
                + "&7消息类型: &c" + type.toString() + "\n"
                + "&7节: &c" + section + "\n"
                + "&7路径: &c" + path
                + "&7.", Type.ERROR, true);
        return color("&c&l错误&7(获取语言数据时遇到错误, 请联系管理员解决该问题.)");
    }

    public String build(String key, Type type, String message) {
        FileConfiguration data = locales.containsKey(key) ? locales.get(key) : locales.get(defaultLocaleKey);
        String pluginPrefix = type != Type.DEBUG ? data.getString("Plugin.Prefix") : "&9[&d" + plugin.getName() + "&9]&7(&d&lDEBUG&7) ";
        String typePrefix = type != Type.DEBUG ? data.getString("Plugin." + type.toString()) : "&d&l>> ";

        return color(pluginPrefix + typePrefix + ChatColor.GRAY + message);
    }

    public List<String> getHelp(String key) {
        List<String> help;
        if (locales.containsKey(key)) {
            help = locales.get(key).getStringList("Plugin.HelpMessage");
        } else {
            help = locales.get(defaultLocaleKey).getStringList("Plugin.HelpMessage");
        }
        help.replaceAll(this::color);
        return help;
    }

    public void send(@NonNull Player user, String message) {
        user.sendMessage(color(message));
    }

    public String getNoExFileName(String fileName) {
        if ((fileName != null) && (fileName.length() > 0)) {
            int dot = fileName.lastIndexOf('.');
            if ((dot > -1) && (dot < (fileName.length()))) {
                return fileName.substring(0, dot);
            }
        }
        return fileName;
    }

    public String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public enum Type {
        INFO, WARN, ERROR, DEBUG;
    }
}
