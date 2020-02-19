package org.serverct.parrot.parrotx.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.serverct.parrot.parrotx.PPlugin;

public @Data
@AllArgsConstructor
class PID {
    private PPlugin plugin;
    private String key;
}
