package org.serverct.parrot.parrotx.data.flags;

import org.bukkit.Bukkit;
import org.serverct.parrot.parrotx.data.PMember;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public interface MemberManager<T extends PMember> {

    Map<UUID, T> getMemberMap();

    default Collection<T> getMembers() {
        return getMemberMap().values();
    }

    default int getSize() {
        return getMemberMap().size();
    }

    default Set<UUID> getUUIDs() {
        return getMemberMap().keySet();
    }

    default boolean hasMember(UUID uuid) {
        return getMemberMap().containsKey(uuid);
    }

    default boolean hasMember(String username) {
        return hasMember(Bukkit.getOfflinePlayer(username).getUniqueId());
    }

    default void addMember(T member) {
        getMemberMap().put(member.getOwner(), member);
    }

    default void delMember(UUID uuid) {
        getMemberMap().remove(uuid);
    }

    default T getMember(UUID uuid) {
        return getMemberMap().get(uuid);
    }

    default T getMember(String username) {
        return getMember(Bukkit.getOfflinePlayer(username).getUniqueId());
    }
}
