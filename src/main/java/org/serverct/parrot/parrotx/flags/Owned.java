package org.serverct.parrot.parrotx.flags;

public interface Owned {
    String getOwner();

    void setOwner(String name);

    default boolean isOwner(String name) {
        return getOwner().equals(name);
    }
}
