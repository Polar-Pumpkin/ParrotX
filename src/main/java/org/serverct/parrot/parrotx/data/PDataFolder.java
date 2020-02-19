package org.serverct.parrot.parrotx.data;

import lombok.NonNull;

import java.io.File;

public interface PDataFolder {
    String getTypeName();

    String getFolderName();

    void releaseDefaultData();

    void init();

    void load(@NonNull File file);

    void reloadAll();

    void reload(@NonNull String id);

    void delete(@NonNull String id);
}
