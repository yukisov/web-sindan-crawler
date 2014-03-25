package net.pupha.wsc.utils;

public class DateTimeUtils {

    /**
     * ミリ秒を受け取って "分:秒" で表現された文字列を返す。
     * 
     * @param millisec
     * @param fmt
     * @return
     */
    public static String getMinutesSeconds(long millisec, String fmt) {
        long minutes = millisec/1000/60;
        long seconds = (millisec - (minutes * 1000 * 60)) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
