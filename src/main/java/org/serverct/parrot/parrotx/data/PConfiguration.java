package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.data.flags.FileSaved;

public interface PConfiguration extends FileSaved {
    String getTypeName();

    String getFileName();

    void init();

    void saveDefault();
}
