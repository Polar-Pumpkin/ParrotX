package org.serverct.parrot.parrotx.hooks;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class BaseExpansion extends PlaceholderExpansion {

    protected final PPlugin plugin;
    @Getter
    private final Map<String, PlaceholderParam> paramMap = new HashMap<>();
    protected String identifier;
    protected String author;
    protected String version;

    public BaseExpansion(final PPlugin plugin, String identifier, String author, String version) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.author = author;
        this.version = version;
    }

    public BaseExpansion(final PPlugin plugin) {
        this.plugin = plugin;
        final PluginDescriptionFile desc = plugin.getDescription();
        this.identifier = plugin.getName().toLowerCase();
        this.author = Arrays.toString(desc.getAuthors().toArray());
        this.version = desc.getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        final String[] source = params.split("[_]");
        if (!this.paramMap.containsKey(source[0])) {
            return "";
        }

        final PlaceholderParam param = this.paramMap.get(source[0]);
        if (Objects.isNull(param.getParse())) {
            return "";
        }

        final String[] args = new String[source.length - 1];
        if (args.length >= 2) {
            System.arraycopy(args, 1, args, 0, args.length - 1);
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
        Arrays.stream(params).forEach(param -> this.paramMap.put(param.getName(), param));
    }

    @Data
    @Builder
    protected static class PlaceholderParam {
        private final String name;
        private final BiFunction<OfflinePlayer, String[], String> parse;
    }
}
