package org.serverct.parrot.parrotx.data;

import lombok.NonNull;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.flags.Uniqued;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;

public abstract class PData implements PConfiguration, Uniqued {

    protected PPlugin plugin;
    protected PID id;
    protected File file;

    public PData(File file, PID id) {
        this.file = file;
        this.id = id;
        this.plugin = id.getPlugin();
    }

    @Override
    public String getFileName() {
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
        plugin.lang.logAction(I18n.RELOAD, getTypeName());
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
        PPlugin plugin = getID().getPlugin();
        if (getFile().delete()) {
            plugin.lang.logAction(I18n.DELETE, getTypeName());
        } else {
            plugin.lang.logError(I18n.DELETE, getTypeName(), "无法删除该文件.");
        }
    }
}
