package org.serverct.parrot.parrotx.data;

public interface PConfiguration {
    String getTypename();

    void load();

    void reload();

    void save();

    void delete();

    void init();

    void saveDefault();
}
