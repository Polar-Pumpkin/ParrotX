package org.serverct.parrot.parrotx.flags;

import lombok.NonNull;
import org.serverct.parrot.parrotx.data.PID;

public interface Uniqued {
    PID getID();

    void setID(@NonNull PID pid);
}
