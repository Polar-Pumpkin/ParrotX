package org.serverct.parrot.parrotx.flags;

public interface Owned {
    String getOwner();

    default boolean isOwner(String name) {
        return getOwner().equals(name);
    }

    void setOwner(String name);
}
