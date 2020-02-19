package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.flags.FileSaved;

public interface PConfiguration extends FileSaved {
    String getTypeName();

    String getID();

    void init();

    void saveDefault();
}
