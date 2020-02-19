package org.serverct.parrot.parrotx.data;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.flags.FileSaved;
import org.serverct.parrot.parrotx.flags.Owned;
import org.serverct.parrot.parrotx.flags.Uniqued;

import java.util.List;

public interface PGroup extends FileSaved, Uniqued, Owned {
    long getFoundTime();

    List<?> getMembers();

    boolean isMember(@NonNull Player user);

    void addMember(@NonNull Player user);

    void delMember(@NonNull Player user);
}
