package net.pupha.wsc.utils;

public class StringUtils {
    public static String join(String[] ary, String separator) {
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<ary.length; i++) {
            if (i > 0) {
                buf.append(separator);
            }
            buf.append(ary[i]);
         }
         return buf.toString();
    }

    public static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }
}
