package org.serverct.parrot.parrotx.data;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.serverct.parrot.parrotx.data.autoload.AutoLoader;
import org.serverct.parrot.parrotx.data.flags.Unique;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

public abstract class PStruct extends AutoLoader implements PConfiguration, Unique {

    protected PID id;
    protected ConfigurationSection section;

    public PStruct(ConfigurationSection section, PID id) {
        super(id.getPlugin());
        this.section = section;
        this.id = id;
    }

    @Override
    public void reload() {
        plugin.getLang().log.action(I18n.RELOAD, getTypename());
        load();
    }

    @Override
    public PID getID() {
        return this.id;
    }

    @Override
    public void setID(@NonNull PID pid) {
        this.id = pid;
    }

    @Override
    public void delete() {
    }
}
