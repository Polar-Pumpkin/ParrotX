package org.serverct.parrot.parrotx.flags;

import org.serverct.parrot.parrotx.utils.TimeUtil;

import java.util.Date;

public interface Timestamp {
    long getTimestamp();

    default String getTime() {
        return TimeUtil.getDefaultFormatDate(new Date(getTimestamp()));
    }

    void setTime(long time);
}
