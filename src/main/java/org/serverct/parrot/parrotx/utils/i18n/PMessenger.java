package org.serverct.parrot.parrotx.utils.i18n;

import org.bukkit.entity.Player;

public class PMessenger {

    private final I18n lang;

    public PMessenger(final I18n i18n) {
        this.lang = i18n;
    }

    public void info(final Player user, final String path, final Object... args) {
        I18n.send(user, lang.data.info(lang.data.get(I18n.Type.INFO, path, args)));
    }

    public void warn(final Player user, final String path, final Object... args) {
        I18n.send(user, lang.data.info(lang.data.get(I18n.Type.WARN, path, args)));
    }

    public void error(final Player user, final String path, final Object... args) {
        I18n.send(user, lang.data.info(lang.data.get(I18n.Type.ERROR, path, args)));
    }

    public void infoMessage(final Player user, final String message, final Object... args) {
        I18n.send(user, lang.data.info(lang.data.build(I18n.Type.INFO, message, args)));
    }

    public void warnMessage(final Player user, final String message, final Object... args) {
        I18n.send(user, lang.data.info(lang.data.build(I18n.Type.WARN, message, args)));
    }

    public void errorMessage(final Player user, final String message, final Object... args) {
        I18n.send(user, lang.data.info(lang.data.build(I18n.Type.ERROR, message, args)));
    }

}
