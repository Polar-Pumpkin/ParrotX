package org.serverct.parrot.parrotx.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.*;
import java.util.function.Predicate;

public abstract class BaseCommand implements PCommand {

    protected final PPlugin plugin;
    @Getter
    private final String name;
    private final int leastArgLength;
    @Getter
    private final List<CommandChain> chains = new ArrayList<>();
    protected Player user;
    protected CommandSender sender;
    private String desc = "没有介绍";
    private String perm = null;
    private boolean mustPlayer = false;
    private Predicate<String[]> customValidate = null;

    public BaseCommand(@NotNull final PPlugin plugin, final String name, final int length) {
        this.plugin = plugin;
        this.name = name;
        this.leastArgLength = length;
        // plugin.getCmdHandler().register(this);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        this.sender = sender;
        this.user = sender instanceof Player ? (Player) sender : null;
        CommandChain commandChain = null;

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

        for (CommandChain chain : this.chains) {
            if (chain.length(false) == 0) {
                if (args.length == 0) {
                    ChainExecutor<String[], CommandChain> executor = chain.run;
                    if (executor != null) {
                        executor.run(args, chain);
                        return true;
                    }
                }
            } else {
                if (args.length > 0) {
                    CommandParam keyParam = chain.get(0);
                    if (keyParam.validate != null && !keyParam.validate.test(args[keyParam.position])) {
                        continue;
                    }
                    if (args.length < chain.length(false) || args.length > chain.params.size()) {
                        continue;
                    }
                    for (CommandParam param : chain.params) {
                        if (param.validate != null && !param.validate.test(args[param.position])) {
                            sender.sendMessage(I18n.color(param.validateMessage));
                            return true;
                        }
                    }
                    commandChain = chain;
                    break;
                }
            }
        }

        if (customValidate != null && !customValidate.test(args)) {
            return true;
        }

        if (commandChain != null) {
            if (commandChain.run != null) {
                commandChain.run.run(args, commandChain);
                return true;
            }
        }

        call(args);
        return true;
    }

    protected abstract void call(String[] args);

    private CommandChain matchChain(String[] args) {
        Chain_Loop:
        for (CommandChain chain : this.chains) {
            if (args.length < chain.length(false) || args.length > chain.length(true)) {
                continue;
            }
            for (CommandParam param : chain.params) {
                if (param.validate != null && !param.validate.test(args[param.position])) {
                    continue Chain_Loop;
                }
                return chain;
            }
        }
        return null;
    }

    @Override
    public String[] getParams(int arg, String[] args) {
        CommandChain chain = matchChain(args);
        if (chain != null) {
            ParamSuggester suggester = chain.get(arg).suggest;
            if (suggester != null) {
                return suggester.param();
            }
        }
        return new String[0];
    }

    @Override
    public String getPermission() {
        return perm;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    public String[] getHelp(final CommandChain chain, final boolean header) {
        return new ArrayList<String>() {{
            if (header) {
                add("&9&l" + plugin.getName() + " &7指令帮助 ᚏᚎᚍᚔᚓᚒᚑᚐ");
                add(requiredParam("必填参数") + " " + optionalParam("选填参数"));
            }


            add(" ");
            StringBuilder commandLine = new StringBuilder("  &9▶ &d/")
                    .append(plugin.getCmdHandler().mainCmd)
                    .append(" ")
                    .append(name)
                    .append(" ");
            chain.params.forEach(param -> {
                if (param.optional) commandLine.append(optionalParam(param.name));
                else commandLine.append(requiredParam(param.name));
            });
            add(commandLine.toString());

            chain.params.forEach(param -> {
                StringBuilder paramLine = new StringBuilder("     ");
                if (param.optional) paramLine.append(optionalParam(param.name));
                else paramLine.append(requiredParam(param.name));
                paramLine.append(" &9- &7&o").append(param.description);
                add(paramLine.toString());
            });

            if (header) {
                add(" ");
                add("    &7&o" + getDescription());
                add("    &7所需权限: &c" + (getPermission() == null ? "无" : getPermission()));
            }
        }}.toArray(new String[0]);
    }

    @Override
    public String[] getHelp() {
        return new ArrayList<String>() {{
            add("&9&l" + plugin.getName() + " &7指令帮助 ᚏᚎᚍᚔᚓᚒᚑᚐ");
            add(requiredParam("必填参数") + " " + optionalParam("选填参数"));

            chains.forEach(chain -> addAll(Arrays.asList(getHelp(chain, false))));

            add(" ");
            add("    &7&o" + getDescription());
            add("    &7所需权限: &c" + (getPermission() == null ? "无" : getPermission()));
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

    protected void addChain(final CommandChain chain) {
        this.chains.add(chain);
    }

    protected String info(final String text) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, text);
    }

    protected String warn(final String text) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.WARN, text);
    }

    protected String error(final String text) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.ERROR, text);
    }

    protected String info(final String text, final Object... args) {
        return plugin.lang.buildWithFormat(plugin.localeKey, I18n.Type.INFO, text, args);
    }

    protected String warn(final String text, final Object... args) {
        return plugin.lang.buildWithFormat(plugin.localeKey, I18n.Type.WARN, text, args);
    }

    protected String error(final String text, final Object... args) {
        return plugin.lang.buildWithFormat(plugin.localeKey, I18n.Type.ERROR, text, args);
    }

    @FunctionalInterface
    public interface ParamSuggester {
        String[] param();
    }

    @FunctionalInterface
    public interface ParamConverter<K, V> {
        V convert(K k);
    }

    @FunctionalInterface
    public interface ChainExecutor<K1, K2> {
        void run(K1 k1, K2 k2);
    }

    protected @Data
    @AllArgsConstructor
    @Builder
    static class CommandParam {
        private String name;
        private boolean optional;
        private String description;
        private Predicate<String> validate;
        private String validateMessage;
        private int position;
        private ParamSuggester suggest;
        private ParamConverter<String, ?> converter;
    }

    protected @Data
    @AllArgsConstructor
    @Builder
    static class CommandChain {
        private String name;
        private String description;
        private List<CommandParam> params;
        private ChainExecutor<String[], CommandChain> run;

        protected CommandChain chain(final CommandParam param) {
            if (this.params == null) {
                this.params = new ArrayList<>();
            }

            this.params.add(param);
            return this;
        }

        protected CommandChain sort() {
            this.params.sort(Comparator.comparingInt(CommandParam::getPosition));
            return this;
        }

        protected CommandParam get(final int index) {
            if (this.params.size() > index) {
                return this.params.get(index);
            }
            return null;
        }

        protected int length(final boolean withOptional) {
            if (withOptional) {
                return this.params.size();
            } else {
                return (int) this.params.stream().filter(param -> !param.optional).count();
            }
        }
    }
}
