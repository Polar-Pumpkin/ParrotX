package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.flags.FileSaved;
import org.serverct.parrot.parrotx.flags.Owned;
import org.serverct.parrot.parrotx.flags.Uniqued;

import java.util.List;

public interface PGroup extends FileSaved, Uniqued, Owned {
    long getFoundTime();

    List<?> getMembers();

    boolean isMember(String userName);

    void addMember(String userName);

    void delMember(String userName);
}
