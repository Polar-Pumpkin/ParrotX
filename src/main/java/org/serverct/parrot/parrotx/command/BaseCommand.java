package org.serverct.parrot.parrotx.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.config.PDataSet;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({"unused"})
public abstract class BaseCommand implements PCommand {

    protected final PPlugin plugin;
    protected final I18n lang;
    @Getter
    private final String name;
    private final int leastArgLength;
    @Getter
    private final Map<Integer, CommandParam> paramMap = new HashMap<>();
    protected Player user;
    protected CommandSender sender;
    private String desc = "没有介绍";
    private String perm = null;
    private boolean mustPlayer = false;
    private Predicate<String[]> customValidate = null;

    public BaseCommand(@NotNull final PPlugin plugin, final String name, final int length) {
        this.plugin = plugin;
        this.lang = this.plugin.getLang();
        this.name = name;
        this.leastArgLength = length;
        // plugin.getCmdHandler().register(this);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.sender = sender;
        this.user = sender instanceof Player ? (Player) sender : null;

        if (mustPlayer) {
            if (Objects.isNull(user)) {
                sender.sendMessage(warn("&7该命令仅玩家可执行."));
                return true;
            }
        }

        if (args.length < leastArgLength) {
            Arrays.asList(getHelp()).forEach(text -> sender.sendMessage(I18n.color(text)));
            return true;
        }

        for (CommandParam param : this.paramMap.values()) {
            if (!param.optional) {
                if (param.position >= args.length) {
                    return true;
                }
            }
            if (param.validate != null && param.position < args.length && !param.validate.test(args[param.position])) {
                sender.sendMessage(I18n.color(param.validateMessage));
                return true;
            }
        }

        if (customValidate != null && !customValidate.test(args)) {
            return true;
        }

        call(args);
        return true;
    }

    protected abstract void call(String[] args);

    @Override
    public String[] getParams(int arg) {
        if (this.paramMap.containsKey(arg)) {
            Supplier<String[]> suggester = this.paramMap.get(arg).suggest;
            if (suggester != null) {
                return suggester.get();
            }
        }
        return new String[0];
    }

    @Override
    public String getPermission() {
        if (Objects.isNull(perm)) {
            return null;
        }
        if (perm.startsWith(".")) {
            return plugin.getName() + perm;
        }
        return perm;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public String[] getHelp() {
        return new ArrayList<String>() {{
            add("&9&l" + plugin.getName() + " &f指令帮助 &7ᚏᚎᚍᚔᚓᚒᚑᚐ");
            add(requiredParam("必填参数") + " " + optionalParam("选填参数"));
            add("");

            StringBuilder commandLine = new StringBuilder("  &9▶ &f/")
                    .append(plugin.getCommandHandler().mainCmd)
                    .append(" ")
                    .append(name);
            paramMap.values().forEach(
                    param -> commandLine.append(param.optional ? optionalParam(param.name) : requiredParam(param.name))
            );
            add(commandLine.toString());

            paramMap.values().forEach(param -> {
                StringBuilder paramLine = new StringBuilder("    ");
                if (param.optional) paramLine.append(optionalParam(param.name));
                else paramLine.append(requiredParam(param.name));
                paramLine.append(" &9- &7&o").append(param.description);
                add(paramLine.toString());
            });

            add(" ");
            add("    &7&o" + getDescription());
            add("    &7所需权限: &f&o" + (getPermission() == null ? "无" : getPermission()));
        }}.toArray(new String[0]);
    }

    protected void validate(Predicate<String[]> validate) {
        this.customValidate = validate;
    }

    protected void mustPlayer(final boolean setting) {
        this.mustPlayer = setting;
    }

    protected void perm(final String perm) {
        this.perm = perm;
    }

    protected void describe(final String text) {
        this.desc = text;
    }

    protected void addParam(final CommandParam param) {
        this.paramMap.put(param.position, param);
    }

    protected String info(final String text, final Object... args) {
        return lang.data.info(text, args);
    }

    protected String warn(final String text, final Object... args) {
        return lang.data.warn(text, args);
    }

    protected String error(final String text, final Object... args) {
        return lang.data.error(text, args);
    }

    protected Object convert(final int index, final String[] args) {
        final CommandParam param = this.paramMap.get(index);
        if (param == null || (!param.continuous && param.converter == null)) {
            return null;
        }
        if (param.continuous) {
            final StringBuilder builder = new StringBuilder();
            final ListIterator<String> iterator = Arrays.asList(args).listIterator(index);
            while (iterator.hasNext()) {
                builder.append(iterator.next());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            return builder.toString();
        } else {
            return param.converter.apply(args);
        }
    }

    @Data
    @AllArgsConstructor
    @Builder
    protected static class CommandParam {
        private String name;
        private boolean optional;
        private String description;
        private Predicate<String> validate;
        private String validateMessage;
        private int position;
        private Supplier<String[]> suggest;
        private Function<String[], ?> converter;
        private boolean continuous;

        public static CommandParam player(final int position,
                                          @Nullable final String description, @Nullable final String validateMessage) {
            return CommandParam.builder()
                    .name("玩家 ID")
                    .description(description)
                    .position(position)
                    .suggest(() -> Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList())
                            .toArray(new String[0]))
                    .validate(input -> Objects.nonNull(Bukkit.getPlayerExact(input)))
                    .validateMessage(validateMessage)
                    .converter(args -> Bukkit.getPlayerExact(args[position]))
                    .build();
        }

        public static CommandParam data(final int position, final boolean has,
                                        @NotNull final Class<? extends PDataSet<?>> clazz, @NotNull final String name,
                                        @Nullable final String description, @Nullable final String validateMessage) {
            final PDataSet<?> dataSet = ParrotXAPI.getConfigManager(clazz);

            return CommandParam.builder()
                    .name(name)
                    .description(description)
                    .position(position)
                    .suggest(() -> dataSet.getStringIds().toArray(new String[0]))
                    .validate(input -> has == dataSet.has(input))
                    .validateMessage(validateMessage)
                    .converter(args -> dataSet.get(args[position]))
                    .build();
        }

        public static CommandParam aDouble(final int position, @NotNull final String name,
                                           @Nullable final String description, @Nullable final String validateMessage,
                                           @Nullable final Predicate<Double> check) {
            return CommandParam.builder()
                    .name(name)
                    .description(description)
                    .position(position)
                    .validate(input -> {
                        try {
                            final double value = Double.parseDouble(input);
                            if (Objects.nonNull(check)) {
                                return check.test(value);
                            }
                            return true;
                        } catch (NumberFormatException exception) {
                            return false;
                        }
                    })
                    .validateMessage(validateMessage)
                    .converter(args -> Double.parseDouble(args[position]))
                    .build();
        }

        public static CommandParam aInt(final int position, @NotNull final String name,
                                        @Nullable final String description, @Nullable final String validateMessage,
                                        @Nullable final Predicate<Integer> check) {
            return CommandParam.builder()
                    .name(name)
                    .description(description)
                    .position(position)
                    .validate(input -> {
                        try {
                            final int value = Integer.parseInt(input);
                            if (Objects.nonNull(check)) {
                                return check.test(value);
                            }
                            return true;
                        } catch (NumberFormatException exception) {
                            return false;
                        }
                    })
                    .validateMessage(validateMessage)
                    .converter(args -> Integer.parseInt(args[position]))
                    .build();
        }
    }
}
