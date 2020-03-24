package org.serverct.parrot.parrotx.data;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.serverct.parrot.parrotx.data.flags.Owned;
import org.serverct.parrot.parrotx.data.flags.Timestamp;

import java.util.UUID;

public class PMember implements Owned, Timestamp {

    @Getter
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

    public String getUsername() {
        return Bukkit.getOfflinePlayer(uuid).getName();
    }
}
