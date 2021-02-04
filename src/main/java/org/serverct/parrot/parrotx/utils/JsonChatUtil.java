package org.serverct.parrot.parrotx.utils;

import lombok.Builder;
import lombok.Data;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.function.Function;

public class JsonChatUtil {

    @NotNull
    public static TextComponent getFromLegacy(@Nullable final String msg) {
        final TextComponent text = new TextComponent();
        if (Objects.isNull(msg)) {
            return text;
        }
        Arrays.stream(TextComponent.fromLegacyText(I18n.color(msg))).forEach(text::addExtra);
        return text;
    }

    public static <K, V> void sendMap(@Nullable final Player user,
                                      @NotNull final String msg, @Nullable final String header,
                                      @NotNull final Function<Map.Entry<K, V>, String> formatter,
                                      @Nullable final Map<K, V> map) {
        if (Objects.isNull(map) || map.isEmpty() || Objects.isNull(user)) {
            return;
        }
        final TextComponent text = getFromLegacy(msg.replace("%amount%", String.valueOf(map.size())));

        final List<TextComponent> hovers = new ArrayList<>();
        if (Objects.nonNull(header)) {
            hovers.add(getFromLegacy(header.replace("%amount%", String.valueOf(map.size()))));
            hovers.add(new TextComponent());
        }
        for (final Map.Entry<K, V> entry : map.entrySet()) {
            hovers.add(getFromLegacy(formatter.apply(entry)));
        }
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hovers.toArray(new TextComponent[0])));

        user.spigot().sendMessage(text);
    }

    @NotNull
    public static TextComponent buildClickText(@NotNull final String text, @NotNull final ClickEvent click,
                                               @NotNull final HoverEvent hover) {
        final TextComponent msg = getFromLegacy(text);
        msg.setClickEvent(click);
        msg.setHoverEvent(hover);
        return msg;
    }

    public static <E> void sendEditableList(@Nullable final Player user,
                                            @Nullable final String title, @Nullable final List<E> contents,
                                            @NotNull final Function<E, String> formatter,
                                            @Nullable final List<EditAction> actions, @Nullable final EditAction back) {
        if (Objects.isNull(user)) {
            return;
        }
        final List<TextComponent> messages = new ArrayList<>();

        if (Objects.nonNull(title)) {
            messages.add(getFromLegacy(title));
        }

        final TextComponent newLine = new TextComponent();
        final TextComponent action = new TextComponent();
        if (Objects.nonNull(actions) && !actions.isEmpty()) {
            actions.forEach(editAction -> action.addExtra(editAction.get()));
            newLine.addExtra(actions.get(0).get());
        }

        if (Objects.isNull(contents) || contents.isEmpty()) {
            final TextComponent newContent = newLine.duplicate();
            newContent.addExtra(getFromLegacy("&7&o无内容."));
            messages.add(newContent);
        } else {
            for (E rawContent : contents) {
                final TextComponent content = action.duplicate();
                content.addExtra(getFromLegacy(formatter.apply(rawContent)));
                messages.add(content);
            }
        }

        if (Objects.nonNull(back)) {
            messages.add(back.get());
        }

        messages.forEach(user.spigot()::sendMessage);
    }

    @Data
    @Builder
    public static class EditAction {
        private final String display;
        private final String hover;
        private final String command;
        private final boolean suggest;

        @NotNull
        public ClickEvent.Action getAction() {
            return suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND;
        }

        @NotNull
        public ClickEvent getClickEvent() {
            return new ClickEvent(getAction(), command);
        }

        @NotNull
        public HoverEvent getHoverEvent() {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover));
        }

        @NotNull
        public TextComponent get() {
            if (Objects.isNull(display)) {
                return new TextComponent();
            }
            final TextComponent text = new TextComponent(display);
            text.setClickEvent(getClickEvent());
            text.setHoverEvent(getHoverEvent());
            text.addExtra(" ");
            return text;
        }
    }
}
