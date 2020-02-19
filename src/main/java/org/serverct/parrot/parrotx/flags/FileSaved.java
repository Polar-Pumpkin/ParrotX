package org.serverct.parrot.parrotx.flags;

import lombok.NonNull;

import java.io.File;

public interface FileSaved {
    File getFile();

    boolean setFile(@NonNull File file);

    void load(@NonNull File file);

    void reload();

    void save();

    void delete();
}
