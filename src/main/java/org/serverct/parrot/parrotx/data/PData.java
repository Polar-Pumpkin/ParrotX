package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.flags.Uniqued;
import org.serverct.parrot.parrotx.utils.LocaleUtil;

public interface PData extends PConfiguration, Uniqued {
    @Override
    default void delete() {
        PPlugin plugin = getID().getPlugin();
        if (getFile().delete()) {
            plugin.getLang().logAction(LocaleUtil.DELETE, getTypeName());
        } else {
            plugin.getLang().logError(LocaleUtil.DELETE, getTypeName(), "无法删除该文件");
        }
    }
}
