package net.pupha.wsc.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class UrlUtilsTest {

    @Test
    public void testGetUrlPath() {
        assertEquals("www.example.com/aaa/", UrlUtils.getUrlPath("http://www.example.com/aaa/index.php"));
        assertEquals("www.example.com/aaa/", UrlUtils.getUrlPath("http://www.example.com/aaa/"));
        assertEquals("www.example.com/",     UrlUtils.getUrlPath("http://www.example.com/aaa"));
        assertEquals("www.example.com/",     UrlUtils.getUrlPath("http://www.example.com/"));
        assertEquals("www.example.com/",     UrlUtils.getUrlPath("http://www.example.com"));
        assertEquals("",     UrlUtils.getUrlPath("www.example.com"));
    }

    //@Test
    //public void testGetProtoUrl() {
    //    fail("まだ実装されていません");
    //}

    //@Test
    //public void testGetProtoUrlRoot() {
    //    fail("まだ実装されていません");
    //}

    //@Test
    //public void testNormalizeUrl() {
    //    fail("まだ実装されていません");
    //}

    //@Test
    //public void testAreSameUrlsWithoutProtocol() {
    //    fail("まだ実装されていません");
    //}

    //@Test
    //public void testIsValidHrefAsUrl() {
    //    fail("まだ実装されていません");
    //}

    //@Test
    //public void testIsValidUrl() {
    //    fail("まだ実装されていません");
    //}

    //@Test
    //public void testAreSubDomain() {
    //    fail("まだ実装されていません");
    //}

    //@Test
    //public void testGetHostFromUrl() {
    //    fail("まだ実装されていません");
    //}

}
