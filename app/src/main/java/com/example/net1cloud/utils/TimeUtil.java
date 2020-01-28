package com.example.net1cloud.utils;

public class TimeUtil {

    public static String getTimeFromMilis(long time) {
        if (time <= 0) {
            return "0:00";
        }
        long second = (time / 1000) / 60;
        long million = (time / 1000) % 60;
        String f = String.valueOf(second);
        String m = million >= 10 ? String.valueOf(million) : "0" + million;
        return f + ":" + m;
    }

}
