package org.serverct.parrot.parrotx.data;

import org.serverct.parrot.parrotx.data.flags.FileSaved;
import org.serverct.parrot.parrotx.data.flags.Owned;
import org.serverct.parrot.parrotx.data.flags.Timestamp;
import org.serverct.parrot.parrotx.data.flags.Uniqued;

import java.util.List;

public interface PGroup extends FileSaved, Uniqued, Owned, Timestamp {
    List<PMember> getMembers();

    default int getMemberAmount() {
        return getMembers().size();
    }

    default boolean isMember(String username) {
        for (PMember member : getMembers()) {
            if (member.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    void addMember(String username);

    void delMember(String username);

    default PMember getMember(String username) {
        for (PMember member : getMembers()) {
            if (member.getUsername().equals(username)) {
                return member;
            }
        }
        return null;
    }
}
