package org.serverct.parrot.parrotx.utils;

import net.md_5.bungee.api.chat.BaseComponent;
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
}
