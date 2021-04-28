package org.serverct.parrot.parrotx;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.data.PConfiguration;
import org.serverct.parrot.parrotx.data.UniqueData;
import org.serverct.parrot.parrotx.data.flags.DataSet;
import org.serverct.parrot.parrotx.hooks.BaseExpansion;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public abstract class PPlugin extends JavaPlugin {

    public static final int PARROTX_ID = 9515;
    public final String PARROTX_VERSION = "1.4.7-Alpha (Build 21)";

    private final List<Listener> listeners = new ArrayList<>();
    private final List<BaseExpansion> expansions = new ArrayList<>();
    private final Map<Integer, Consumer<Metrics>> pluginIds = new HashMap<>();

    public String localeKey = "Chinese";
    public PIndex index;
    protected PConfig pConfig;
    @Getter
    protected I18n lang;

    @Getter
    private CommandHandler commandHandler;
    @Setter
    private String versionLog = "本插件基于 ParrotX {0}, 感谢使用.";
    @Setter
    private String timeLog = "插件加载完成, 共耗时&a{0}ms&r.";

    @Override
    public void onEnable() {
        // Plugin startup logic
        final long timestamp = System.currentTimeMillis();

        try {
            ParrotXAPI.registerPlugin(this);
            ParrotX.setDebugMode(getConfig().getBoolean("Debug", false));

            beforeInit();

            init();

            afterInit();

            if (!this.listeners.isEmpty()) {
                this.listeners.forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));
                lang.log.info("已注册监听器.");
            }

            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && !this.expansions.isEmpty()) {
                this.expansions.forEach(BaseExpansion::reg);
                lang.log.info("已注册 PlaceholderAPI 拓展包.");
            }

            if (getConfig().getBoolean("bStats", true)) {
                boolean bStats = false;
                for (final Map.Entry<Integer, Consumer<Metrics>> entry : this.pluginIds.entrySet()) {
                    try {
                        final int pluginId = entry.getKey();
                        final Consumer<Metrics> callback = entry.getValue();

                        final Metrics targetMetrics = new Metrics(this, pluginId);
                        bStats = true;
                        if (Objects.nonNull(callback)) {
                            callback.accept(targetMetrics);
                        }
                    } catch (Exception exception) {
                        lang.log.error(I18n.LOAD, "自定义 bStats 数据统计", exception, getPackageName());
                    }
                }

                if (!Bukkit.getPluginManager().isPluginEnabled("ParrotX")) {
                    try {
                        final Metrics metrics = new Metrics(this, PARROTX_ID);
                        metrics.addCustomChart(new SingleLineChart("plugins_using_parrotx", () -> 1));
                        metrics.addCustomChart(new SimplePie("plugin_name", this::getName));
                        metrics.addCustomChart(new SimplePie("parrotx_version", () -> PARROTX_VERSION));
                        metrics.addCustomChart(new SimplePie("integration_method", () -> "Compile"));
                    } catch (Exception exception) {
                        lang.log.error(I18n.LOAD, "ParrotX bStats 数据统计", exception, getPackageName());
                    }
                    bStats = true;
                }

                if (bStats) {
                    lang.log.info("已启用 bStats 数据统计.");
                    if (Objects.nonNull(this.pConfig)) {
                        lang.log.info("若您需要禁用此功能, 一般情况下可于配置文件 {0}.yml 中编辑或新增 \"bStats: false\" 关闭此功能.",
                                pConfig.getFilename());
                    }
                }
            } else {
                lang.log.warn("bStats 数据统计已被禁用.");
            }

            if (Objects.nonNull(timeLog)) {
                final long time = System.currentTimeMillis() - timestamp;
                lang.log.info(timeLog, time);
            }
            if (Objects.nonNull(versionLog)) {
                lang.log.info(versionLog, PARROTX_VERSION);
            }
        } catch (Throwable e) {
            lang.log.error(I18n.INIT, "插件", e, getPackageName());
            this.setEnabled(false);
        }
    }

    public void init() {
        lang = new I18n(this, localeKey);

        index = new PIndex(this);

        preload();

        index.init();

        if (Objects.nonNull(pConfig)) {
            pConfig.init();
            localeKey = pConfig.getConfig().getString("Language");
        }

        index.initConfig();
        index.registerConfiguration(pConfig);

        load();
    }

    protected void beforeInit() {
    }

    protected void preload() {
    }

    protected void load() {
    }

    protected void afterInit() {
    }

    public <T extends BaseExpansion> void registerExpansion(final T expansions) {
        this.expansions.add(expansions);
    }

    public void registerStats(final int pluginId, @Nullable final Consumer<Metrics> callback) {
        this.pluginIds.put(pluginId, callback);
    }

    public void registerCommand(@NonNull CommandHandler handler) {
        PluginCommand command = Bukkit.getPluginCommand(handler.mainCmd);
        if (command != null) {
            this.commandHandler = handler;
            command.setExecutor(handler);
            command.setTabCompleter(handler);
        } else {
            lang.log.error(I18n.REGISTER, "命令", "无法获取插件主命令.");
        }
    }

    public void registerListener(final Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        index.saveConfig();
        preDisable();
    }

    public void preDisable() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.expansions.forEach(BaseExpansion::unreg);
        }
        getServer().getScheduler().cancelTasks(this);
        index.clearConfig();
    }

    public <T extends PConfiguration> T getManager(final Class<T> clazz) {
        return index.getConfigurationInstance(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T extends UniqueData, U extends DataSet<T>> U getDataSet(final Class<T> dataClass) {
        final Class<U> managerClass = (Class<U>) index.getDataHandler(dataClass);
        return managerClass.cast(getManager((Class<? extends PConfiguration>) managerClass));
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        if (Objects.isNull(this.pConfig) || Objects.isNull(this.pConfig.getConfig())) {
            return super.getConfig();
        }
        return this.pConfig.getConfig();
    }

    public File getFile(final String path) {
        return new File(getDataFolder(), path);
    }

    public File getFileWithResource(final String path) {
        final File file = getFile(path);
        if (!file.exists()) {
            saveResource(path, false);
            lang.log.warn("未找到 &c{0}&f, 已自动生成.", path);
            if (!file.exists()) {
                lang.log.warn("未找到内置默认文件: &c{0}&f.", path);
            }
        }
        return file;
    }

    public String getPackageName() {
        return getClass().getPackage().getName();
    }
}
