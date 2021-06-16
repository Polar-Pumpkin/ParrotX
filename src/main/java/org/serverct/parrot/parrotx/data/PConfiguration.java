package org.serverct.parrot.parrotx.data;

public interface PConfiguration {
    String name();

    void load();

    void reload();

    void save();

    void delete();

    void init();

    void saveDefault();

    boolean isReadonly();
}
