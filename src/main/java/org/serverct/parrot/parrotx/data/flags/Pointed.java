package org.serverct.parrot.parrotx.data.flags;

public interface Pointed {

    int getPoint();

    void setPoint(int amount);

    default boolean havePoint(int amount) {
        return getPoint() >= amount;
    }

    default boolean takePoint(int amount) {
        int point = getPoint();
        if (havePoint(amount)) {
            setPoint(point - amount);
            return true;
        }
        return false;
    }

    default void givePoint(int amount) {
        setPoint(getPoint() + amount);
    }

    default void resetPoint() {
        setPoint(0);
    }

}
