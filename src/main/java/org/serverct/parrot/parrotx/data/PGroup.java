package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import org.serverct.parrot.parrotx.data.flags.MemberManager;
import org.serverct.parrot.parrotx.data.flags.Owned;
import org.serverct.parrot.parrotx.data.flags.Timestamp;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class PGroup<T extends PMember> extends PData implements MemberManager<T>, Owned, Timestamp {

    @Getter
    protected Map<UUID, T> memberMap = new HashMap<>();
    protected UUID owner;
    protected long foundTime;

    public PGroup(File file, PID id, String typeName) {
        super(id, file, typeName);
    }

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public long getTimestamp() {
        return this.foundTime;
    }

    @Override
    public void setTime(long time) {
        this.foundTime = time;
    }
}


