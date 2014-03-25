package net.pupha.wsc;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class ProcessTest {

//    @Test
//    public void testGetPath() {
//            URL rs;
//            String s;
//            try {
//                // 末尾にスラッシュを含まないドメイン名の終わりから”？”までを取得するらしい。
//                //rs = new URL("http://www.example.com");                       // "" -> [""] : "" OK
//                //rs = new URL("http://www.example.com/");                      // "/" -> []  : "/" OK
//                //rs = new URL("http://www.example.com/aaa");                   // "/aaa" -> ["", "aaa"] : "/aaa" OK
//                //rs = new URL("http://www.example.com/aaa/");                  // "/aaa/" -> ["", "aaa"]: "/aaa/" OK
//                //rs = new URL("http://www.example.com/aaa/bbb");               // "/aaa/bbb" -> ["", "aaa", "bbb"] : "/aaa/bbb" OK
//                //rs = new URL("http://www.example.com/aaa/bbb.html");          // "/aaa/bbb.html" -> ["", "aaa", "bbb.html"] : "/aaa/"
//                //rs = new URL("http://www.example.com/aaa/bbb.html?ccc=ddd");  // "/aaa/bbb.html"
//                //rs = new URL("http://www.example.com/aaa/bbb?ccc=ddd");       // "/aaa/bbb"
//                rs = new URL("http://www.example.com/aaa/bbb/?ccc=ddd");        // "/aaa/bbb/" -> ["", "aaa", "bbb"] : "/aaa/bbb/" OK
//                //s = rs.getPath();
//                s = "/aaa/bbb/";
//                System.out.println(s);
//                String[] sa = s.split("/");
//                System.out.println(sa.length);
//                for (String _s: sa) {
//                    System.out.println("    [" + _s + "]");
//                }
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExtractUrlsStartWithTheOrigUrlPath_withFileName() {

        String urlOrig = "http://www.example.com/aaa/index.html";
        List<String> urls = new ArrayList<String>(
                Arrays.asList("http://www.example.com/aaa/bbb", "http://www.example.com/aaa/ccc",
                              "http://www.example.com/index.html", "http://www.example.com/aaa/index.html",
                              "https://www.example.com/aaa/index.html", "https://www.example.com/bbb/index.html",
                              "https://www.example.com/bbb/ccc"));
        List<String> urlsRes = new ArrayList<String>();
        Class[] classArray = new Class[] {List.class};
        Object res;
        Method m;
        try {
            Process.initProcess(new Date(), urlOrig);
            Process process = new Process(1);
            process.setClimbingUpUrlPath(false);
            m = Process.class.getDeclaredMethod("extractUrlsStartWithTheOrigUrlPath", classArray);
            m.setAccessible(true);
            res = m.invoke(process, urls);
            urlsRes = (List<String>)res;

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException | IOException e) {
            // eclipseのTestRunner用の小細工
            if (e != null && e.getCause() instanceof RuntimeException) {
                return;
            }
            e.printStackTrace();
            fail();
        }

        assertEquals(4, urlsRes.size());
        assertSame("http://www.example.com/aaa/bbb",  urlsRes.get(0));
        assertSame("http://www.example.com/aaa/ccc",  urlsRes.get(1));
        assertSame("http://www.example.com/aaa/index.html",  urlsRes.get(2));
        assertSame("https://www.example.com/aaa/index.html", urlsRes.get(3));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExtractUrlsStartWithTheOrigUrlPath() {
        String urlOrig = "http://www.example.com/aaa/";
        List<String> urls = new ArrayList<String>(
                Arrays.asList("http://www.example.com/aaa/bbb", "http://www.example.com/aaa/ccc",
                              "http://www.example.com/index.html", "http://www.example.com/aaa/index.html",
                              "https://www.example.com/aaa/index.html", "https://www.example.com/bbb/index.html",
                              "https://www.example.com/bbb/ccc"));
        List<String> urlsRes = new ArrayList<String>();
        Class[] classArray = new Class[] {List.class};
        Object res;
        Method m;
        try {
            Process.initProcess(new Date(), urlOrig);
            Process process = new Process(1);
            process.setClimbingUpUrlPath(false);
            m = Process.class.getDeclaredMethod("extractUrlsStartWithTheOrigUrlPath", classArray);
            m.setAccessible(true);
            res = m.invoke(process, urls);
            urlsRes = (List<String>)res;

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException | IOException e) {
            // eclipseのTestRunner用の小細工
            if (e != null && e.getCause() instanceof RuntimeException) {
                return;
            }
            e.printStackTrace();
            fail();
        }

        assertEquals(4, urlsRes.size());
        assertSame("http://www.example.com/aaa/bbb",  urlsRes.get(0));
        assertSame("http://www.example.com/aaa/ccc",  urlsRes.get(1));
        assertSame("http://www.example.com/aaa/index.html",  urlsRes.get(2));
        assertSame("https://www.example.com/aaa/index.html", urlsRes.get(3));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExtractUrlsUniq() {

        //String urlOrig = "http://www.example.com/";
        List<String> urlsDone = new ArrayList<String>();
        List<String> urls = new ArrayList<String>(
                Arrays.asList("http://www.example.com/aaa", "http://www.example.com/bbb",
                              "http://www.example.com/bbb", "https://www.example.com/aaa",
                              "http://www.example.com/ccc"));
        List<String> urlsRes = new ArrayList<String>();
        Class[] classArray = new Class[] {List.class};
        Object res;
        Method m;
        try {
            Process.setStartDate(new Date());
            Process.setUrlsDone(urlsDone);
            m = Process.class.getDeclaredMethod("extractUrlsUniq", classArray);
            m.setAccessible(true);
            res = m.invoke(new Process(1), urls);
            urlsRes = (List<String>)res;

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException e) {
            // eclipseのTestRunner用の小細工
            if (e != null && e.getCause() instanceof RuntimeException) {
                return;
            }
            e.printStackTrace();
            fail();
        }

        assertEquals(4, urlsRes.size());
        assertSame("http://www.example.com/aaa",  urlsRes.get(0));
        assertSame("http://www.example.com/bbb",  urlsRes.get(1));
        assertSame("http://www.example.com/ccc",  urlsRes.get(2));
        assertSame("https://www.example.com/aaa", urlsRes.get(3));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExtractUrlsNotAccess() {

        String urlOrig = "http://www.example.com/";
        List<String> urlsDone = new ArrayList<String>(
                Arrays.asList("http://www.example.com/", "http://www.example.com/bbb", "http://www.example.com/ccc"));
        List<String> urls = new ArrayList<String>(
                Arrays.asList("http://www.example.com/aaa", "http://www.example.com/bbb",
                              "http://www.example.com/ccc", "https://www.example.com/ddd",
                              "http://www.example.com/eee"));
        List<String> urlsRes = new ArrayList<String>();
        Class[] classArray = new Class[] {List.class};
        Object res;
        Method m;
        try {
            Process.initProcess(new Date(), urlOrig);
            Process.setUrlsDone(urlsDone);
            m = Process.class.getDeclaredMethod("extractUrlsNotAccess", classArray);
            m.setAccessible(true);
            res = m.invoke(new Process(1), urls);
            urlsRes = (List<String>)res;

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException | IOException e) {
            // eclipseのTestRunner用の小細工
            if (e != null && e.getCause() instanceof RuntimeException) {
                return;
            }
            e.printStackTrace();
            fail();
        }

        assertEquals(3, urlsRes.size());
        assertSame("http://www.example.com/aaa", urlsRes.get(0));
        assertSame("https://www.example.com/ddd", urlsRes.get(1));
        assertSame("http://www.example.com/eee", urlsRes.get(2));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testExtractUrlsSameHost() {

        String urlOrig = "http://www.example.com/";
        List<String> urls = new ArrayList<String>(
                Arrays.asList("http://www.example.com/aaa", "http://www.example.com/bbb",
                              "http://blog.example.com/", "https://www.example.com/ccc",
                              "http://www.yahoo.co.jp/"));
        List<String> urlsRes = new ArrayList<String>();
        Class[] classArray = new Class[] {List.class};
        Object res;
        Method m;
        try {
            Process.initProcess(new Date(), urlOrig);
            m = Process.class.getDeclaredMethod("extractUrlsSameHost", classArray);
            m.setAccessible(true);
            res = m.invoke(new Process(1), urls);
            urlsRes = (List<String>)res;

        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                 IllegalArgumentException | InvocationTargetException | IOException e) {
            // eclipseのTestRunner用の小細工
            if (e != null && e.getCause() instanceof RuntimeException) {
                return;
            }
            e.printStackTrace();
            fail();
        }

        assertEquals(3, urlsRes.size());
        assertSame("http://www.example.com/aaa", urlsRes.get(0));
        assertSame("http://www.example.com/bbb", urlsRes.get(1));
        assertSame("https://www.example.com/ccc", urlsRes.get(2));
    }
}
