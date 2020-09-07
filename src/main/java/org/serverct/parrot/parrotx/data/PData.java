package org.serverct.parrot.parrotx.data;

import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.data.autoload.AutoLoader;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.data.flags.Unique;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;

public abstract class PData extends AutoLoader implements PConfiguration, FileSaved, Unique {

    protected PID id;
    protected File file;
    protected FileConfiguration data;

    public PData(File file, PID id) {
        super(id.getPlugin());
        this.file = file;
        this.data = YamlConfiguration.loadConfiguration(file);
        this.id = id;
    }

    @Override
    public String getFilename() {
        return BasicUtil.getNoExFileName(this.file.getName());
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void reload() {
        plugin.getLang().log.action(I18n.RELOAD, getTypename());
        load(this.file);
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
        if (getFile().delete()) {
            plugin.getLang().log.action(I18n.DELETE, getTypename());
        } else {
            plugin.getLang().log.error(I18n.DELETE, getTypename(), "无法删除该文件.");
        }
    }
}
