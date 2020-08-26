package org.serverct.parrot.parrotx.utils;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JsonChatUtil {
    public static TextComponent getFromLegacy(String msg) {
        TextComponent text = new TextComponent();
        for (BaseComponent element : TextComponent.fromLegacyText(I18n.color(msg))) text.addExtra(element);
        return text;
    }

    public static void sendMap(PPlugin plugin, Player target, String msg, String header, String format, Map<?, ?> map) {
        if (map.isEmpty()) return;
        TextComponent text = getFromLegacy(msg.replace("%amount%", String.valueOf(map.size())));
        StringBuilder list = new StringBuilder(I18n.color(header.replace("%amount%", String.valueOf(map.size())) + "\n"));
        List<?> keys = new ArrayList<>(map.keySet());
        for (int index = 0; index < keys.size(); index++) {
            Object key = keys.get(index);
            Object value = map.get(key);

            String keyName = key.toString();
            if (key instanceof Material) keyName = ItemUtil.getName(plugin, (Material) key);
            else if (key instanceof String) keyName = (String) key;

            String valueName = key.toString();
            if (value instanceof String) valueName = (String) value;
            else if (value instanceof Integer) valueName = String.valueOf((int) value);

            list.append(I18n.color(format.replace("%k%", keyName).replace("%v%", valueName)));

            if (index != keys.size() - 1) list.append("\n");
        }
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(list.toString())));
        target.spigot().sendMessage(text);
    }

    public static TextComponent buildClickText(String text, ClickEvent click, HoverEvent hover) {
        TextComponent msg = getFromLegacy(text);
        msg.setClickEvent(click);
        msg.setHoverEvent(hover);
        return msg;
    }

    public void sendEditableList(final Player user, final List<String> contents, final String title, final List<EditAction> actions, final EditAction back) {
        user.spigot().sendMessage(getFromLegacy("\n" + title));

        if (!actions.isEmpty()) {
            if (!contents.isEmpty()) {
                for (String content : contents) {
                    final int index = contents.indexOf(content);
                    final TextComponent line = new TextComponent("");
                    actions.forEach(action -> {
                        final TextComponent actionComponent = buildClickText(
                                action.display,
                                new ClickEvent(action.suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND, action.command + " " + index),
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(action.hover)))
                        );
                        line.addExtra(actionComponent);
                        line.addExtra(" ");
                    });
                    line.addExtra(getFromLegacy(I18n.color(content)));
                    user.spigot().sendMessage(line);
                }
            } else {
                final TextComponent newLine = new TextComponent("");
                final EditAction action = actions.get(0);
                newLine.addExtra(buildClickText(
                        action.display,
                        new ClickEvent(action.suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND, action.command + " 0"),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(action.hover)))
                ));
                newLine.addExtra(getFromLegacy(" &7&o无内容."));
                user.spigot().sendMessage(newLine);
            }
        } else {
            if (!contents.isEmpty()) {
                contents.forEach(content -> I18n.send(user, content));
            } else {
                I18n.send(user, "&7&o无内容.");
            }
        }

        if (Objects.nonNull(back)) {
            user.spigot().sendMessage(buildClickText(
                    back.display,
                    new ClickEvent(back.suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND, back.command),
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(back.hover)))
            ));
        }
    }

    public @Data
    @Builder
    static class EditAction {
        private final String display;
        private final String hover;
        private final String command;
        private final boolean suggest;
    }
}
