package org.serverct.parrot.parrotx.hooks;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@SuppressWarnings("UnstableApiUsage")
public class BaseExpansion extends PlaceholderExpansion {

    protected final PPlugin plugin;
    protected final I18n lang;
    @Getter
    private final Map<String, PlaceholderParam> paramMap = new HashMap<>();
    protected String identifier;
    protected String author;
    protected String version;
    private Runnable unreg;

    public BaseExpansion(final PPlugin plugin, String identifier, String author, String version) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.identifier = identifier;
        this.author = author;
        this.version = version;

        unreg = () -> {
            try {
                super.unregister();
            } catch (Throwable exception) {
                lang.log.error("注销 PlaceholderAPI 拓展包时遇到错误: {0}.", exception.getMessage());
                PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().unregister(this);
            }
        };
    }

    public BaseExpansion(final PPlugin plugin, String identifier) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.identifier = identifier;

        final PluginDescriptionFile desc = plugin.getDescription();
        this.author = Arrays.toString(desc.getAuthors().toArray());
        this.version = desc.getVersion();

        unreg = () -> {
            try {
                super.unregister();
            } catch (Throwable exception) {
                lang.log.error("注销 PlaceholderAPI 拓展包时遇到错误: {0}.", exception.getMessage());
                PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().unregister(this);
            }
        };
    }

    public BaseExpansion(final PPlugin plugin) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();

        final PluginDescriptionFile desc = plugin.getDescription();
        this.identifier = plugin.getName().toLowerCase();
        this.author = Arrays.toString(desc.getAuthors().toArray());
        this.version = desc.getVersion();

        unreg = () -> {
            try {
                super.unregister();
            } catch (Throwable exception) {
                lang.log.error("注销 PlaceholderAPI 拓展包时遇到错误: {0}.", exception.getMessage());
                PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().unregister(this);
            }
        };
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        final String[] source = params.split("[_]");
        final String key = source[0].toLowerCase();
        if (!this.paramMap.containsKey(key)) {
            return "";
        }

        final PlaceholderParam param = this.paramMap.get(key);
        if (Objects.isNull(param.getParse())) {
            return "";
        }

        final String[] args = new String[source.length - 1];
        if (source.length >= 2) {
            System.arraycopy(source, 1, args, 0, source.length - 1);
        }

        return param.getParse().apply(player, args);
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.identifier;
    }

    @Override
    public @NotNull String getAuthor() {
        return this.author;
    }

    @Override
    public @NotNull String getVersion() {
        return this.version;
    }

    protected void addParam(final PlaceholderParam... params) {
        Arrays.stream(params).forEach(param -> this.paramMap.put(param.getName().toLowerCase(), param));
    }

    public void reg() {
        super.register();
    }

    public void unreg() {
        if (Objects.nonNull(this.unreg)) {
            this.unreg.run();
        }
    }

    public void setUnreg(Runnable unreg) {
        this.unreg = unreg;
    }

    @Data
    @Builder
    protected static class PlaceholderParam {
        private final String name;
        private final BiFunction<OfflinePlayer, String[], String> parse;
    }
}
