package com.example.net1cloud.utils;

public class TimeUtil {

    public static String getTimeStrFromMilliSeconds(long time) {
        if (time <= 0) {
            return "0:00";
        }
        long second = (time / 1000) / 60;
        long million = (time / 1000) % 60;
        String f = String.valueOf(second);
        String m = million >= 10 ? String.valueOf(million) : "0" + million;
        return f + ":" + m;
    }

    public static long getMilliSecondsFromLrcTimeStr(String timeStr) {
        int minutes = Integer.parseInt(timeStr.substring(0, 2));
        int seconds = Integer.parseInt(timeStr.substring(3, 5));
        int milliseconds = Integer.parseInt(timeStr.substring(6, 8));

        return minutes * 60 * 1000 + seconds * 1000 + milliseconds;
    }

}
