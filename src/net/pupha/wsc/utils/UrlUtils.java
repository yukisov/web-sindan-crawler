package net.pupha.wsc.utils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.validator.routines.UrlValidator;

public class UrlUtils {

    /* ロガーオブジェクト */
    private static final Logger logger = Logger.getLogger(UrlUtils.class.toString());

    /**
     * フルなURLを受け取り、末端のファイル名の部分を取り除いた文字列を返す。
     *
     * この戻り値を、相対URLで指定された href の値に足せば絶対URLになるようにする。
     *
     * 引数の形式： 完全なURLであることを想定する。
     * 戻り値の形式： www.example.com/aaa/
     *              必ず末尾にスラッシュがつく
     *              引数が完全なURLでなければ空文字を返す。
     *
     * e.g.
     *   "http://www.example.com/" --> "www.example.com/"
     *   "http://www.example.com/aaa" --> "www.example.com/"
     *   "http://www.example.com/aaa/bbb" --> "www.example.com/aaa/"
     *
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static String getUrlPath(String url) {
        Pattern pattern = Pattern.compile("^https?://([^/]+)/?(.*?)([^/]*?)$");
        Matcher matcher = pattern.matcher(url);
        String m1 = "";
        String m2 = "";
        if (matcher.matches()) {
            m1 = matcher.group(1);
            m2 = matcher.group(2);
            if (m2.length() > 0 && m2.charAt(m2.length()-1) == '/') {
                return m1 + "/" + m2;
            }
            return m1 + "/";
        }
        return "";
    }

    /**
     * 
     * 戻り値の形式： http(s)://www.example.com/aaa
     *  - 末尾にスラッシュが必ずつく。
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static String getProtoUrl(String url) throws MalformedURLException {
        URL rsc = new URL(url);
        String urlPath = getUrlPath(url);
        String urlPathNew;
        // 末尾がスラッシュでないなら追加する
        if (urlPath.charAt(urlPath.length()-1) == '/') {
            urlPathNew = urlPath;
        } else {
            urlPathNew = urlPath + "/";
        }
        return rsc.getProtocol() + "://" + urlPathNew;
    }

    /**
     * 
     * 戻り値の形式： http(s)://www.example.com
     * - 末尾にスラッシュはつかない。
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static String getProtoUrlRoot(String url) throws MalformedURLException {
        URL rsc = new URL(url);
        return rsc.getProtocol() + "://" + rsc.getHost();
    }

    /**
     * URLを正規化する。
     * hrefにセットされたURLを "http"から始まるフルのURLに直して返す。
     * 
     * 第1引数：完全なURL形式
     * 第2引数：hrefにセットされた文字列(相対URLであることもある)
     * 
     * @param url
     * @return
     * @throws MalformedURLException 
     */
    public static String normalizeUrl(String urlOrig, String urlFrom) throws MalformedURLException {
        String fullUrl = "";

        String urlRootOrig = UrlUtils.getProtoUrlRoot(urlOrig);
        String urlPathOrig = UrlUtils.getProtoUrl(urlOrig);

        String s;
        if (urlFrom.isEmpty()) {
            fullUrl = urlPathOrig;
        // (1) 1文字目が "/"
        } else if (urlFrom.charAt(0) == '/') {
            fullUrl = urlRootOrig + urlFrom;
        // (2) 1文字目が "."
        } else if (urlFrom.charAt(0) == '.') {
            fullUrl = urlPathOrig + urlFrom.substring(2);
        // (3) 1文字目が "#"
        } else if (urlFrom.charAt(0) == '#') {
            fullUrl = urlPathOrig;
        } else if (urlFrom.length() >= 4) {
            s = urlFrom.substring(0, 4);
            // (4) "http"で始まらない
            if (s.equals("http")) {
                fullUrl = urlFrom;
            // 1文字目が "?" で始まる場合も含む
            } else {
                fullUrl = urlPathOrig + urlFrom;
            }
        } else {
            fullUrl = urlPathOrig + urlFrom;
        }

        return fullUrl;
    }

    public static boolean areSameUrlsWithoutProtocol(String url1, String url2) {

        String mark = "://";

        // url1 のプロトコル無し文字列を生成する。
        int pos1 = url1.indexOf(mark);
        String url1WithoutProto = url1.substring(pos1 + mark.length());

        // url2 のプロトコル無し文字列を生成する。
        int pos2 = url2.indexOf(mark);
        String url2WithoutProto = url2.substring(pos2 + mark.length());

        if (url1WithoutProto.equals(url2WithoutProto)) {
            return true;
        }
        return false;
    }

    /**
     * hrefの値の中が、URLとして利用可能な文字列であるかどうかを判断する。
     * 
     * NG例1:
     *     href="javascript: void window.close()"
     *
     * @param val
     * @return
     */
    public static boolean isValidHrefAsUrl(String val) {
        if (val.trim().startsWith("javascript")) {
            logger.fine("Not valid value as url in href : " + val);
            return false;
        }
        return true;
    }

    /**
     * URLとして利用可能な文字列であるかどうかを判断する。
     * 
     * @param url
     * @return
     */
    public static boolean isValidUrl(String urlOrig) {

        // (1) UrlValidatorを使ったチェック
        String[] schemes = {"http","https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (urlValidator.isValid(urlOrig)) {
            // (2) java.net.URL を使ったチェックを行う
            URL rs;
            try {
                rs = new URL(urlOrig); // MalformedURLException
                rs.toURI(); // URISyntaxException
            } catch (MalformedURLException e) {
                // 文字列に指定されたプロトコルが未知である場合
                return false;
            } catch (URISyntaxException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 2つのURLのドメインが同じであるかどうかを返す。
     * 
     * (例)
     *  "http://example.com" と "http://www.example.com" を比較したら True が返ってくる。
     * 
     * @param url1
     * @param url2
     * @return
     */
    public static boolean areSubDomain(String url1, String url2) {

        String hostShort;
        String hostLong;
        if (url1.length() > url2.length()) {
            hostShort = getHostFromUrl(url2);
            hostLong = getHostFromUrl(url1);
        } else {
            hostShort = getHostFromUrl(url1);
            hostLong = getHostFromUrl(url2);
        }

        // 同じホストであればfalseを返す
        if (hostShort.equals(hostLong)) {
            return false;
        }

        // 長い方の文字列を右から切り出す
        int len = hostShort.length();
        // 1つ前のピリオドを含めたドメイン部分を切り取る
        String rightLong = hostLong.substring(hostLong.length() - (len + 1), hostLong.length());
        String rightShort = "." + hostShort;
        if (rightLong.equals(rightShort)) {
            return true;
        }
        return false;
    }

    //public static boolean areSameHost(String url1, String url2) {
    //        String host1 = getHostFromUrl(url1);
    //        String host2 = getHostFromUrl(url2);
    //        if (host1.equals(host2)) {
    //            return true;
    //        }
    //        return false;
    //}

    public static String getHostFromUrl(String url) {

        Pattern pattern = Pattern.compile("^https?://([^/]+)/?.*$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()){
          return matcher.group(1);
        }
        return "";
    }
}
