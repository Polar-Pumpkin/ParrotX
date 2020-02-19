package org.serverct.parrot.parrotx.flags;

public interface Owned {
    String getOwner();

    boolean isOwner(String name);

    boolean setOwner(String name);
}
