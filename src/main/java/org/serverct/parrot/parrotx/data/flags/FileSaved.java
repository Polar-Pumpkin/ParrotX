package org.serverct.parrot.parrotx.data.flags;

import lombok.NonNull;

import java.io.File;

public interface FileSaved {
    File getFile();

    void setFile(@NonNull File file);

    void load(@NonNull File file);

    void reload();

    void save();

    void delete();
}
