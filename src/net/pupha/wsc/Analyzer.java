package net.pupha.wsc;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.pupha.wsc.utils.UrlUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * レスポンス・ボディからURLを抽出する処理を担当するクラス
 * @author yuki
 *
 */
public class Analyzer {

    /* ロガーオブジェクト */
    //private static final Logger logger = Logger.getLogger(Analyzer.class.toString());

    /**
     * 
     * Ref.
     *      Example program: list links: jsoup Java HTML parser
     *      http://jsoup.org/cookbook/extracting-data/example-list-links
     * 
     * @param responseBody
     * @return
     */
    public List<String> analyze(String urlOrig, String responseBody) {

        List <String> urls = new ArrayList <String>();
        Document doc = Jsoup.parse(responseBody);
        Elements links = null;
        List<String> urlsFound = null;

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a[href]", "href");
        map.put("frame[src]", "src");

        for (Map.Entry<String, String> e: map.entrySet()) {
            links = doc.select(e.getKey());
            urlsFound = getUrlFromLinks(urlOrig, links, e.getValue());
            if (urlsFound.size() > 0) {
                urls.addAll(urlsFound);
            }
        }
        if (urls.size() > 0) {
            Collections.sort(urls);
        }

        return urls;
    }

    private List<String> getUrlFromLinks(String urlOrig, Elements links, String attrVal) {

        List <String> urls = new ArrayList <String>();

        String url;
        String fullUrl = null;
        for (Element link : links) {
            url = link.attr(attrVal);
            // 問題のある文字列であるかチェックする
            if (! UrlUtils.isValidHrefAsUrl(url)) {
                //WSC.print("    Found on the URL: " + urlOrig);
                continue;
            }
            try {
                // 相対パスは絶対パスにする
                fullUrl = UrlUtils.normalizeUrl(urlOrig, url.trim());
            } catch (MalformedURLException e) {
                WSC.print("MalformedURLException for the url: " + fullUrl);
                WSC.print("    Found on the URL: " + urlOrig);
                continue;
            }
            // URLとして妥当であるかチェックする
            if (! UrlUtils.isValidUrl(fullUrl)) {
                WSC.print("Invalid URL: " + fullUrl);
                WSC.print("    Found on the URL: " + urlOrig);
                continue;
            }
            urls.add(fullUrl);
        }
        return urls;
    }
}
