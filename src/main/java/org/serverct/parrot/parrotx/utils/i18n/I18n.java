package org.serverct.parrot.parrotx.utils.i18n;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.BasicUtil;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author EntityParrot_
 * @author Mical
 * @version v3.2
 * <p>
 * 自己写的一个多语言管理工具。
 */

@SuppressWarnings("unused")
public class I18n {

    public static final String TOOL_VERSION = "v3.2";
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
    public static final String CREATE = "创建";
    public static final String GENERATE = "生成";
    public static final String UPLOAD = "上传";
    public static final String REFRESH = "刷新";
    public static final String CLEAR = "清空";
    protected static final String Tool_Prefix = "&7[&b&lEP's &aLocale Tool&7] ";
    protected static final String Tool_INFO = "&a&l> &r";
    protected static final String Tool_WARN = "&e&l> &r";
    protected static final String Tool_ERROR = "&c&l> &r";
    protected static final String Tool_DEBUG = "&d&l> &r";
    public final PLogger log;
    public final PLocaleManager data;
    public final PMessenger sender;
    @Getter
    private final PPlugin plugin;
    private final File dataFolder;
    @Getter
    private final Map<String, FileConfiguration> locales = new HashMap<>();
    @Getter
    @Setter
    private String defaultLocaleKey;

    /**
     * @param plugin           JavaPlugin 对象，一般为插件的主类，将用于语言工具获取插件 jar 包内的默认语言文件。
     * @param defaultLocaleKey 默认语言，需要插件 jar 包内的 Locales 文件夹中拥有以该默认语言命名的语言文件。
     */
    public I18n(PPlugin plugin, String defaultLocaleKey) {
        this.plugin = plugin;
        this.defaultLocaleKey = defaultLocaleKey;
        this.dataFolder = new File(plugin.getDataFolder(), "Locales");
        this.log = new PLogger(this);
        this.data = new PLocaleManager(this);
        this.sender = new PMessenger(this);
        init();
    }

    public static String getToolVersion() {
        return Tool_Prefix + TOOL_VERSION;
    }

    /**
     * 快速向玩家发送信息。
     *
     * @param user    目标玩家。
     * @param message 消息内容
     */
    public static void send(Player user, String message, Object... args) {
        if (user != null) {
            user.sendMessage(MessageFormat.format(color(message), args));
        }
    }

    /**
     * 异步向玩家发送信息。
     *
     * @param user    目标玩家。
     * @param message 消息内容
     */
    public static void sendAsync(@NonNull PPlugin plugin, Player user, String message, Object... args) {
        if (user != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    send(user, message, args);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    /**
     * 快速给文本上色。
     * 因为 ChatColor.translateAlternateColorCodes() 方法名字太长了。
     *
     * @param text 文本。
     * @return 上色后的文本。
     * @see ChatColor
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
        if (Objects.isNull(plugin.getResource("Locales"))) {
            this.log.log("该插件无语言配置文件.", Type.WARN, true);
            return;
        }
        if (!dataFolder.exists()) {
            if (dataFolder.mkdirs()) {
                final String path = "Locales/" + defaultLocaleKey + ".yml";
                if (Objects.nonNull(plugin.getResource(path))) {
                    plugin.saveResource(path, false);
                }
                this.log.log("未找到语言文件夹, 已自动生成.", Type.WARN, true);
            } else {
                this.log.log("尝试生成语言文件夹失败.", Type.ERROR, true);
            }
        }
        File[] files = BasicUtil.getYamls(dataFolder);
        if (files == null || files.length == 0) {
            saveDefault();
            files = BasicUtil.getYamls(dataFolder);
        }
        if (files != null && files.length > 0) {
            boolean check = false;
            for (File file : files) {
                if (BasicUtil.getNoExFileName(file.getName()).equals(defaultLocaleKey)) {
                    check = true;
                }
            }
            if (!check) {
                saveDefault();
            }
        }
        load();
    }

    /**
     * 生成默认语言文件。
     */
    public void saveDefault() {
        plugin.saveResource("Locales/" + defaultLocaleKey + ".yml", false);
        this.log.log("未找到默认语言文件, 已自动生成.", Type.WARN, true);
    }

    /**
     * 加载语言数据，可以通过调用此方法来重载语言文件(但是不会检测/释放默认语言文件)。
     */
    public void load() {
        this.log.log("版本: &c" + TOOL_VERSION, Type.INFO, true);
        File[] localeDataFiles = BasicUtil.getYamls(dataFolder);
        if (localeDataFiles != null && localeDataFiles.length > 0) {
            for (File dataFile : localeDataFiles) {
                locales.put(BasicUtil.getNoExFileName(dataFile.getName()), YamlConfiguration.loadConfiguration(dataFile));
            }
        }
        this.log.log("语言数据加载成功, 共加载了 &c" + locales.size() + " &7个语言文件.", Type.INFO, true);
    }

    /**
     * 判断是否加载了指定的语言文件。
     *
     * @param key 目标语言名。
     * @return 是否加载了该语言。
     */
    public boolean hasLocale(String key) {
        return key != null && locales.containsKey(key);
    }

    /**
     * 获取指定的语言数据。
     *
     * @param key 目标语言名，若需要获取默认语言数据可以填写 null。
     * @return 目标语言数据的 FileConfiguration 对象。
     */
    public FileConfiguration getLocale(String key) {
        return key == null ? locales.get(defaultLocaleKey) : locales.get(key);
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
        FileConfiguration data = getLocale(hasLocale(key) ? key : null);
        List<String> help = data.getStringList("Plugin.Help");
        help.replaceAll(I18n::color);
        help.replaceAll(s -> s.replace("%version%", plugin.getDescription().getVersion()));
        return help;
    }

    public enum Type {
        INFO, WARN, ERROR, DEBUG
    }
}
