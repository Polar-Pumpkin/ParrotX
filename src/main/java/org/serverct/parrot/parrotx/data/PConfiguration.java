package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.data.flags.FileSaved;

public interface PConfiguration extends FileSaved {
    String getTypename();

    String getFilename();

    void init();

    void saveDefault();
}
