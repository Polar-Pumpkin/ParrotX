package org.serverct.parrot.parrotx.data.flags;

public interface Leveled {
    int getLevel();

    void setLevel(int level);

    boolean levelUp();
}
