package org.serverct.parrot.parrotx.flags;

public interface Leveled {
    int getLevel();

    void setLevel(int level);

    boolean levelUp();
}
