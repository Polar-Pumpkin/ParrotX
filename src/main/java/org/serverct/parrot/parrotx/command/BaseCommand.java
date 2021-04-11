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
    private Function<String[], String> customValidateMessage = null;

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

        if (this.mustPlayer && Objects.isNull(user)) {
            lang.sender.warnMessage(sender, "该命令仅玩家可执行.");
            return true;
        }

        if (args.length < this.leastArgLength) {
            for (final String help : getHelp()) {
                I18n.send(sender, help);
            }
            return true;
        }

        for (final CommandParam param : this.paramMap.values()) {
            if (!param.optional && param.position >= args.length) {
                lang.sender.warnMessage(sender, "参数异常, 存在未指定的必填参数, 请检查命令拼写.");
                return true;
            }

            if (Objects.isNull(param.validate) || param.position >= args.length || param.validate.test(args[param.position])) {
                continue;
            }

            final String message;
            if (Objects.nonNull(param.advancedValidateMessage)) {
                message = param.advancedValidateMessage.apply(args);
            } else {
                message = param.validateMessage;
            }

            I18n.send(sender, message);
            return true;
        }

        if (Objects.nonNull(this.customValidate) && !this.customValidate.test(args)) {
            if (Objects.nonNull(this.customValidateMessage)) {
                I18n.send(sender, this.customValidateMessage.apply(args));
            }
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

    protected void validate(final Predicate<String[]> validate) {
        this.customValidate = validate;
    }

    protected void validateMessage(final Function<String[], String> message) {
        this.customValidateMessage = message;
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

    protected Function<String[], String> advancedInfo(final String text, final Object... args) {
        return strings -> info(text, args);
    }

    protected String warn(final String text, final Object... args) {
        return lang.data.warn(text, args);
    }

    protected Function<String[], String> advancedWarn(final String text, final Object... args) {
        return strings -> warn(text, args);
    }

    protected String error(final String text, final Object... args) {
        return lang.data.error(text, args);
    }

    protected Function<String[], String> advancedError(final String text, final Object... args) {
        return strings -> error(text, args);
    }

    @Nullable
    protected <T> T convert(final int index, final String[] args, final Class<T> clazz) {
        if (index >= args.length) {
            lang.log.error(I18n.GET, "子命令参数/" + this.name, "参数不足: ({0}) {1}", index, args);
            return null;
        }

        final CommandParam param = this.paramMap.get(index);
        if (param == null || (!param.continuous && param.converter == null)) {
            lang.log.error(I18n.GET, "子命令参数/" + this.name, "命令参数为 null 或未指定转换器: {0}", index);
            return null;
        }

        if (param.continuous && String.class.equals(clazz)) {
            final StringBuilder builder = new StringBuilder();
            final ListIterator<String> iterator = Arrays.asList(args).listIterator(index);
            while (iterator.hasNext()) {
                builder.append(iterator.next());
                if (iterator.hasNext()) {
                    builder.append(" ");
                }
            }
            return clazz.cast(builder.toString());
        }

        final Object value = param.converter.apply(args);
        if (!clazz.isInstance(value)) {
            lang.log.error(I18n.GET, "子命令参数/" + this.name, "转换命令参数时类型不一致: From {0} -> To {1}",
                    value.getClass().getSimpleName(), clazz.getSimpleName());
            return null;
        }

        return clazz.cast(value);
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
        private Function<String[], String> advancedValidateMessage;
        private int position;
        private Supplier<String[]> suggest;
        private Function<String[], ?> converter;
        private boolean continuous;

        public static CommandParam player(final int position, @Nullable final String description,
                                          @Nullable final Function<String[], String> validateMessage) {
            return CommandParam.builder()
                    .name("玩家 ID")
                    .description(description)
                    .position(position)
                    .suggest(() -> Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList())
                            .toArray(new String[0]))
                    .validate(input -> Objects.nonNull(Bukkit.getPlayerExact(input)))
                    .advancedValidateMessage(validateMessage)
                    .converter(args -> Bukkit.getPlayerExact(args[position]))
                    .build();
        }

        public static CommandParam data(final int position, @NotNull final String name,
                                        @Nullable final String description, final boolean has,
                                        @NotNull final Class<? extends PDataSet<?>> clazz,
                                        @Nullable final Function<String[], String> validateMessage) {
            final PDataSet<?> dataSet = ParrotXAPI.getConfigManager(clazz);

            return CommandParam.builder()
                    .name(name)
                    .description(description)
                    .position(position)
                    .suggest(() -> dataSet.getStringIds().toArray(new String[0]))
                    .validate(input -> has == dataSet.has(input))
                    .advancedValidateMessage(validateMessage)
                    .converter(args -> dataSet.get(args[position]))
                    .build();
        }

        public static CommandParam aDouble(final int position, @NotNull final String name,
                                           @Nullable final String description,
                                           @Nullable final Function<String[], String> validateMessage,
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
                    .advancedValidateMessage(validateMessage)
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
