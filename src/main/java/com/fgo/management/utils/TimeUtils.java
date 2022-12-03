package com.fgo.management.utils;

import java.sql.Timestamp;
import java.time.ZoneOffset;

public class TimeUtils {

    private TimeUtils() {
    }

    public static Timestamp timestampPlus8(Timestamp timestamp) {
        long milli = timestamp.toLocalDateTime().plusHours(8).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        timestamp.setTime(milli);
        return timestamp;
    }
}
