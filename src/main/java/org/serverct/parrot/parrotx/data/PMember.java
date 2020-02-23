package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import org.serverct.parrot.parrotx.flags.Timestamp;

public class PMember implements Timestamp {

    @Getter
    private String username;
    private long joinTime;

    @Override
    public long getTimestamp() {
        return joinTime;
    }

    @Override
    public void setTime(long time) {
        this.joinTime = time;
    }
}
