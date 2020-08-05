package org.serverct.parrot.parrotx.data.flags;

import org.serverct.parrot.parrotx.data.PMember;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface MemberManager {

    List<PMember> getMembers();

    default int getSize() {
        return getMembers().size();
    }

    default List<String> listMember() {
        return new ArrayList<String>() {{
            getMembers().forEach(member -> add(member.getOwnerName()));
        }};
    }

    default boolean isMember(String username) {
        return listMember().contains(username);
    }

    void addMember(UUID uuid);

    void delMember(UUID uuid);

    default PMember getMember(String username) {
        for (PMember member : getMembers()) {
            if (member.getOwnerName().equals(username)) {
                return member;
            }
        }
        return null;
    }
}
