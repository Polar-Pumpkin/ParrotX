package org.serverct.parrot.parrotx.utils;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class JsonChatUtil {
    public static TextComponent getFromLegacy(String msg) {
        TextComponent text = new TextComponent();
        for (BaseComponent element : TextComponent.fromLegacyText(msg)) {
            text.addExtra(element);
        }
        return text;
    }
}
