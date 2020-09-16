package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.data.flags.Owned;
import org.serverct.parrot.parrotx.data.flags.Timestamp;

import java.util.UUID;

public abstract class PMember implements Owned, Timestamp {

    protected UUID uuid;
    protected long joinTime;

    @Override
    public long getTimestamp() {
        return joinTime;
    }

    @Override
    public void setTime(long time) {
        this.joinTime = time;
    }

    @Override
    public UUID getOwner() {
        return uuid;
    }

    @Override
    public void setOwner(UUID uuid) {
        this.uuid = uuid;
    }
}
