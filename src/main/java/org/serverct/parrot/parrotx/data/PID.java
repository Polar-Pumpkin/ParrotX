package org.serverct.parrot.parrotx.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.NamespacedKey;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.text.MessageFormat;

@Data
@AllArgsConstructor
public class PID {
    private PPlugin plugin;
    private String key;
    private String id;

    public NamespacedKey key() {
        try {
            return new NamespacedKey(plugin, key.toLowerCase());
        } catch (Throwable e) {
            plugin.getLang().log.error(I18n.BUILD, "NamespacedKey", e, null);
        }
        return null;
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} {1}({2})", key.toLowerCase(), id, plugin.getName());
    }
}
