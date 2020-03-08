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
        for (BaseComponent element : TextComponent.fromLegacyText(I18n.color(msg))) {
            text.addExtra(element);
        }
        return text;
    }

    public static void sendMap(PPlugin plugin, Player target, String msg, String header, String format, Map<?, String> map) {
        TextComponent text = getFromLegacy(msg.replace("%amount%", String.valueOf(map.size())));
        StringBuilder list = new StringBuilder(I18n.color(header.replace("%amount%", String.valueOf(map.size())) + "\n"));
        List<?> keys = new ArrayList<>(map.keySet());
        for (int index = 0; index < keys.size(); index++) {
            Object key = keys.get(index);

            String name = key.toString();
            if (key instanceof Material) {
                Material material = (Material) key;
                name = plugin.lang.hasKey("Material") ? plugin.lang.getRaw("Material", "Material", material.name()) : material.name();
            } else if (key instanceof String) {
                name = (String) key;
            }

            list.append(I18n.color(format.replace("%k%", name).replace("%v%", map.get(key))));

            if (index != keys.size() - 1) {
                list.append("\n");
            }
        }
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(list.toString())));
        target.spigot().sendMessage(text);
    }
}
