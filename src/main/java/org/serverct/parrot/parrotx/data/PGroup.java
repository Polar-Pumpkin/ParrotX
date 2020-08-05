package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.data.flags.MemberManager;
import org.serverct.parrot.parrotx.data.flags.Owned;
import org.serverct.parrot.parrotx.data.flags.Timestamp;

import java.io.File;
import java.util.List;
import java.util.UUID;

public abstract class PGroup extends PData implements MemberManager, Owned, Timestamp {

    protected UUID owner;
    protected long foundTime;
    private List<PMember> memberList;

    public PGroup(File file, PID id) {
        super(file, id);
    }

    @Override
    public List<PMember> getMembers() {
        return this.memberList;
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


