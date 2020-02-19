package org.serverct.parrot.parrotx.data;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.flags.FileSaved;
import org.serverct.parrot.parrotx.flags.Uniqued;

import java.util.List;

public interface PGroup extends FileSaved, Uniqued {
    long getFoundTime();

    void setOwner(@NonNull Player user);

    List<?> getMembers();

    boolean isMember(@NonNull Player user);

    void addMember(@NonNull Player user);

    void delMember(@NonNull Player user);
}
