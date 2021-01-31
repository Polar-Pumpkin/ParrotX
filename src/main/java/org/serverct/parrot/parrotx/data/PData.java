package org.serverct.parrot.parrotx.data;

import lombok.NonNull;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.autoload.Autoloader;
import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.utils.FileUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.File;
import java.io.IOException;

public abstract class PData implements UniqueData, FileSaved {

    protected PPlugin plugin;
    protected I18n lang;
    protected PID id;
    protected File file;
    protected FileConfiguration data;
    private final String typeName;
    @Setter
    private boolean readOnly = false;

    public PData(File file, PID id, String typeName) {
        this.plugin = id.getPlugin();
        this.lang = this.plugin.getLang();
        this.file = file;
        this.data = YamlConfiguration.loadConfiguration(file);
        this.typeName = typeName;
        this.id = id;
    }

    @Override
    public void save() {
        try {
            Autoloader.execute(plugin, this.data, this, false);
            this.data.save(this.file);
        } catch (IOException e) {
            lang.log.error(I18n.SAVE, name(), e, plugin.getPackageName());
        }
    }

    @Override
    public void load(@NonNull File file) {
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public String name() {
        return this.typeName + "/" + this.id.getId();
    }

    @Override
    public void init() {
    }

    @Override
    public void saveDefault() {
    }

    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public String getFilename() {
        return FileUtil.getNoExFilename(this.file);
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
        save();
        load(this.file);
        lang.log.action(I18n.RELOAD, name());
    }

    @Override
    public void load() {
        load(this.file);
        Autoloader.execute(plugin, this.data, this, true);
        lang.log.action(I18n.LOAD, name());
    }

    @Override
    public PID getID() {
        return this.id;
    }

    @Override
    public void delete() {
        if (this.file.delete()) {
            lang.log.action(I18n.DELETE, name());
        } else {
            lang.log.error(I18n.DELETE, name(), "删除文件失败");
        }
    }
}
