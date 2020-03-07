package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.flags.Uniqued;
import org.serverct.parrot.parrotx.utils.I18n;

public interface PData extends PConfiguration, Uniqued {
    @Override
    default void delete() {
        PPlugin plugin = getID().getPlugin();
        if (getFile().delete()) {
            plugin.lang.logAction(I18n.DELETE, getTypeName());
        } else {
            plugin.lang.logError(I18n.DELETE, getTypeName(), "无法删除该文件.");
        }
    }
}
