package org.serverct.parrot.parrotx.data;

import lombok.NonNull;

import java.io.File;
import java.util.Map;

public interface PDataFolder {
    String getTypeName();

    String getID();

    Map<String, ?> getData();

    void init();

    void load(@NonNull File file);

    void reloadAll();

    void reload(@NonNull String id);

    void delete(@NonNull String id);
}
