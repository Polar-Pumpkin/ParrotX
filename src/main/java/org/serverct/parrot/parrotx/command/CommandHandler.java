package org.serverct.parrot.parrotx.command;

import com.cryptomorin.xseries.XMaterial;
import lombok.Getter;
import lombok.NonNull;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor {

    public final String mainCmd;
    protected final PPlugin plugin;
    protected final I18n lang;
    @Getter
    protected final Map<String, PCommand> commands = new HashMap<>();
    protected String defaultCmd = null;
    private boolean authorized = true;

    public CommandHandler(@NonNull PPlugin plugin, String mainCmd) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.mainCmd = mainCmd;
    }

    protected void authorize(boolean authorize) {
        this.authorized = authorize;
    }

    protected void defaultCommand(String cmd) {
        this.defaultCmd = cmd;
    }

    protected void addCommand(String cmd, PCommand executor) {
        if (!commands.containsKey(cmd)) {
            commands.put(cmd, executor);
        } else {
            lang.log.error(I18n.REGISTER, "子命令", "重复子命令注册: " + cmd);
        }
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command,
                             @NotNull final String label, @Nullable final String[] args) {
        if (args.length == 0) {
            PCommand defCommand = commands.get((Objects.isNull(defaultCmd) ? "help" : defaultCmd));
            if (defCommand == null) {
                // plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
                formatHelp(sender).forEach(sender::sendMessage);
            } else {
                boolean hasPerm =
                        (defCommand.getPermission() == null || defCommand.getPermission().equals("")) || sender.hasPermission(defCommand.getPermission());
                if (hasPerm) {
                    return defCommand.execute(sender, args);
                }

                String msg = plugin.getLang().data.warn("您没有权限这么做.");
                if (sender instanceof Player) {
                    TextComponent text = JsonChatUtil.getFromLegacy(msg);
                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            TextComponent.fromLegacyText(I18n.color("&7所需权限 ▶ &c" + defCommand.getPermission()))));
                    ((Player) sender).spigot().sendMessage(text);
                } else sender.sendMessage(msg);
            }
            return true;
        }

        PCommand pCommand = commands.get(args[0].toLowerCase());
        if (pCommand == null) {
            final String similar = getMostSimilarCommand(args[0].toLowerCase());
            if (StringUtils.isEmpty(similar)) {
                lang.sender.warnMessage(sender, "未知命令, 请检查您的命令拼写是否正确.");
            } else {
                lang.sender.warnMessage(sender, "未知命令, 您想执行的命令是不是: &d{0}&r.", similar);
            }
            lang.log.error(I18n.EXECUTE, "子命令/" + args[0], sender.getName() + " 尝试执行未注册子命令");
            return true;
        }

        boolean hasPerm = (pCommand.getPermission() == null || pCommand.getPermission().equals(""))
                || sender.hasPermission(pCommand.getPermission());
        if (hasPerm) {
            String[] newArg = new String[args.length - 1];
            if (args.length >= 2) {
                System.arraycopy(args, 1, newArg, 0, args.length - 1);
            }
            return pCommand.execute(sender, newArg);
        }

        String msg = plugin.getLang().data.warn("您没有权限这么做.");
        if (sender instanceof Player && XMaterial.supports(8)) {
            TextComponent text = JsonChatUtil.getFromLegacy(msg);
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(
                    "&7所需权限 ▶ &c" + pCommand.getPermission()))));
            ((Player) sender).spigot().sendMessage(text);
        } else sender.sendMessage(msg);

        return true;
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull final CommandSender sender, @NotNull final Command cmd,
                                      @NotNull final String label, @Nullable final String[] args) {
        String[] subCommands = commands.keySet().toArray(new String[0]);
        if (args.length == 0) {
            return new ArrayList<>(Arrays.asList(subCommands));
        } else {
            PCommand command = commands.get(args[0]);
            if (args.length == 1) {
                return new ArrayList<>();
            } else {
                if (Objects.nonNull(command))
                    return query(command.getParams(args.length - 2), args[args.length - 1]);
                else return new ArrayList<>();
            }
        }
    }

    @NotNull
    private List<String> query(String[] params, String input) {
        return Arrays.stream(params).filter(s -> s.startsWith(input)).collect(Collectors.toList());
    }

    @NotNull
    public List<String> formatHelp() {
        return formatHelp(null);
    }

    @NotNull
    public List<String> formatHelp(@Nullable final Permissible sender) {
        final List<String> result = new ArrayList<>();
        final PluginDescriptionFile description = plugin.getDescription();

        result.add(I18n.color("&9&l{0} &fv{1}", description.getName(), description.getVersion()));

        if (authorized) {
            final String authorList = description.getAuthors().toString();
            if (authorList.length() > 2) {
                final String authors = authorList.substring(1, authorList.length() - 1);
                result.add(I18n.color("&7作者: &f{0}", authors));
            }

        }
        result.add("");

        final Command command = Bukkit.getPluginCommand(this.mainCmd);
        final List<String> aliases = new ArrayList<>();
        if (Objects.nonNull(command)) {
            aliases.addAll(command.getAliases());
        }

        String mainCommand = this.mainCmd;
        for (final String alias : aliases) {
            if (alias.length() < mainCommand.length()) {
                mainCommand = alias;
            }
        }

        final String prefix = "/" + mainCommand;
        boolean first = true;

        final List<Map.Entry<String, PCommand>> commands = new ArrayList<>();

        if (Objects.nonNull(sender)) {
            final List<Map.Entry<String, PCommand>> hasPerm = this.commands.entrySet().stream()
                    .filter(entry -> {
                        final String perm = entry.getValue().getPermission();
                        if (StringUtils.isEmpty(perm)) {
                            return true;
                        }
                        return sender.hasPermission(perm);
                    })
                    .collect(Collectors.toList());
            commands.addAll(hasPerm);
        } else {
            commands.addAll(this.commands.entrySet());
        }

        for (Map.Entry<String, PCommand> entry : commands) {
            final String subcommand = entry.getKey();
            final PCommand executor = entry.getValue();

            if (first) {
                result.add(I18n.color("&f{0} {1}", prefix, subcommand));
            } else {
                result.add(I18n.color("{0}&7- &f{1}", I18n.blank(prefix.length() - 1), subcommand));
            }
            result.add(I18n.color("{0} &7{1}", I18n.blank(prefix.length()), executor.getDescription()));
            first = false;
        }

        if (this.commands.containsKey("help")) {
            result.add("");
            result.add(I18n.color("&6▶ &7使用 &f/{0} help &7指令查看更多信息.", mainCmd));
        }

        if (!aliases.isEmpty()) {
            result.add(I18n.color("&a▶ &7可用主命令缩写: &f" + aliases));
        }
        return result;
    }

    public void register(final BaseCommand command) {
        addCommand(command.getName().toLowerCase(), command);
    }

    private int compare(@NotNull final String str, @NotNull final String target) {
        int[][] d;
        int n = str.length();
        int m = target.length();
        int i;
        int j;
        char ch1;
        char ch2;
        int temp;

        if (n == 0) {
            return m;
        }

        if (m == 0) {
            return n;
        }

        d = new int[n + 1][m + 1];

        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }

        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }

        for (i = 1; i <= n; i++) {
            ch1 = str.charAt(i - 1);
            for (j = 1; j <= m; j++) {
                ch2 = target.charAt(j - 1);
                if (ch1 == ch2 || ch1 == ch2 + 32 || ch1 + 32 == ch2) {
                    temp = 0;
                } else {
                    temp = 1;
                }
                d[i][j] = Math.min(
                        Math.min(d[i - 1][j] + 1,
                                d[i][j - 1] + 1),
                        d[i - 1][j - 1] + temp);
            }
        }

        return d[n][m];
    }

    /**
     * 获取两个字符串的相似度.
     *
     * @param str    第一个字符串.
     * @param target 第二个字符串.
     * @return 相似度.
     * @link https://www.cnblogs.com/yangyang2018/p/10496744.html
     */
    private float getSimilarityRatio(@NotNull final String str, @NotNull final String target) {
        int max = Math.max(str.length(), target.length());
        return 1 - (float) compare(str, target) / max;
    }

    @Nullable
    private String getMostSimilarCommand(@NotNull final String command) {
        String result = null;
        float similarity = 0f;
        for (final String subCommand : this.commands.keySet()) {
            float t = getSimilarityRatio(command, subCommand);
            if (t > similarity) {
                similarity = t;
                result = subCommand;
            }
        }
        return result;
    }
}
