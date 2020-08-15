package org.serverct.parrot.parrotx.utils;

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

    public static void sendEditableList(Player user, List<String> content, String title, String add, String addCmd, String set, String setCmd, String del, String delCmd, String back, String backCmd, boolean suggest) {
        user.spigot().sendMessage(getFromLegacy("\n" + title));
        TextComponent clickableAdd;
        ClickEvent.Action action = suggest ? ClickEvent.Action.SUGGEST_COMMAND : ClickEvent.Action.RUN_COMMAND;
        if (!content.isEmpty()) {
            for (String desc : content) {
                TextComponent result = new TextComponent("");
                int index = content.indexOf(desc);
                clickableAdd = buildClickText(
                        add,
                        new ClickEvent(action, addCmd + (index + 1)),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&a点击在下方插入一行内容")))
                );
                TextComponent clickableSet = buildClickText(
                        set,
                        new ClickEvent(action, setCmd + index),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&e点击设置此行内容")))
                );
                TextComponent clickableDel = buildClickText(
                        del,
                        new ClickEvent(action, delCmd + index),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&c点击删除此行内容")))
                );
                result.addExtra(clickableAdd);
                result.addExtra(clickableSet);
                result.addExtra(clickableDel);
                result.addExtra(getFromLegacy(desc));
                user.spigot().sendMessage(result);
            }
        } else {
            TextComponent result = new TextComponent("");
            clickableAdd = buildClickText(
                    add,
                    new ClickEvent(action, addCmd + 0),
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&a点击添加第一行内容")))
            );
            result.addExtra(clickableAdd);
            result.addExtra(getFromLegacy("&7无."));
            user.spigot().sendMessage(result);
        }

        user.spigot().sendMessage(
                buildClickText(
                        back,
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, backCmd),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&a点击返回")))
                )
        );
        user.sendMessage("");
    }
}
