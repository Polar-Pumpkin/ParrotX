package org.serverct.parrot.parrotx.utils.i18n;

import org.bukkit.command.CommandSender;

public class PMessenger {

    private final I18n lang;

    public PMessenger(final I18n i18n) {
        this.lang = i18n;
    }

    public void info(final CommandSender sender, final String path, final Object... args) {
        I18n.send(sender, lang.data.getInfo(path, args));
    }

    public void warn(final CommandSender sender, final String path, final Object... args) {
        I18n.send(sender, lang.data.getWarn(path, args));
    }

    public void error(final CommandSender sender, final String path, final Object... args) {
        I18n.send(sender, lang.data.getError(path, args));
    }

    public void infoMessage(final CommandSender sender, final String message, final Object... args) {
        I18n.send(sender, lang.data.info(message, args));
    }

    public void warnMessage(final CommandSender sender, final String message, final Object... args) {
        I18n.send(sender, lang.data.warn(message, args));
    }

    public void errorMessage(final CommandSender sender, final String message, final Object... args) {
        I18n.send(sender, lang.data.error(message, args));
    }

}
