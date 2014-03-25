package net.pupha.wsc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

import net.pupha.wsc.utils.DateTimeUtils;
import net.pupha.wsc.utils.UrlUtils;

/**
 * HTTPリクエストを送信してレスポンスからURLを取り出して返す処理を担当するクラス
 * @author yuki
 *
 */
public class Spider {

    /* ロガーオブジェクト */
    private static final Logger logger = Logger.getLogger(Spider.class.toString());

    /* 接続開始時の時刻 */
    private long timeStart;

    /* 接続終了時の時刻 */
    private long timeEnd;

    /* ステータスコード */
    private int statusCode = 0;

    public List<String> run(String url) {

        List<String> urlsRes = new ArrayList<String>();
        // 処理時刻をクリア
        setTimeStart(0);
        setTimeEnd(0);

        // HTTP接続オブジェクト
        HttpURLConnection con = null;

        try {
            // HTTPリクエストを送信する
            con = getHttpConnection(url);

            // リダイレクト処理
            setStatusCode(con.getResponseCode()); // ここでHTTP通信していた
            String location = con.getHeaderField("Location");
            setTimeEnd(System.currentTimeMillis());
            if (isRedirectable(Constant.FOLLOW_REDIRECT, getStatusCode(), location)) {
                String fullLocation = UrlUtils.normalizeUrl(url, location);
                processForSuccess(url, getStatusCode()); // リクエストの取得に成功した場合の処理
                return Arrays.asList(fullLocation);
            }

            // Getting Response Body
            String responseBody = getResponseBody(con);

            // URLを抽出する
            Analyzer analyzer = new Analyzer();
            urlsRes = analyzer.analyze(url, responseBody);

            // リクエストの取得に成功した場合の処理
            processForSuccess(url, getStatusCode());

        } catch (SocketTimeoutException e) {
            // タイムアウト時の処理
            processForTimeOut(url);
        } catch (IOException e) {
            // 例外はここで止める
            WSC.print("going next although IOException at the URL: " + url);
        } catch (Exception e) {
            // 例外はここで止める
            WSC.print("going next although Exception at the URL: " + url);
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return urlsRes;
    }

    private void processForSuccess(String url, int responseCode) {
        // タイムアウトしなかった場合の処理
        Process.setBlnNoResInRow(false);
        Process.setCntNoResInRaw(0);

        // このURLを処理済みURLに追加
        Process.addUrlsDone(url);

        // ログ出力
        outputAccessLog(url, responseCode);

        // HTTPステータスコードの処理
        processStatusCode(responseCode);
    }

    private void outputAccessLog(String url, int responseCode) {
        long elapsedTimeTotal = System.currentTimeMillis() - Process.getStartDate().getTime();
        String strTotalTime = DateTimeUtils.getMinutesSeconds(elapsedTimeTotal, "%02d:%02d");

        double elapsedTime = getTimeEnd() - getTimeStart();

        WSC.print(String.format(
                "No::%04d" + "\t" + "TotalTime::%s" + "\t" + "Status::%03d" + "\t" + "Time::%.2f" + "\t" + "URL::%s",
                Process.getUrlsDone().size(), strTotalTime, responseCode, elapsedTime/1000, url));
    }

    private boolean isRedirectable(boolean followRedirect, int statusCode,
                                   String location) {
        if (followRedirect) {
            if (statusCode >= 300 && statusCode <= 399) {
                if (!location.isEmpty() && UrlUtils.isValidUrl(location)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getResponseBody(HttpURLConnection con) {

        String res = "";

        // getInputStream
        // この接続からの入力を受け取る入力ストリームを返します。 
        // 返された入力ストリームからの読み取り時に、データが読み取り可能になる前に
        // 読み取りタイムアウトが過ぎた場合、SocketTimeoutException がスローされます。
        BufferedReader reader;
        try {
            reader = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

            // ここで取得できるのはレスポンス・ボディのみ。
            StringBuffer buf = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                buf.append(str);
                buf.append("\n");
            }

            res = buf.toString();

        } catch (IOException e) {
            // ステータスコード 500番等でレスポンスボディがない場合もここにくる。
        }

        return res;
    }

    private HttpURLConnection getHttpConnection(String url) throws Exception {
        // HTTP接続オブジェクト
        HttpURLConnection conn = null;
        URL resource;
        try {
            resource = new URL(url);
            resource.toURI();

            if (url.length() >= 5 && url.substring(0, 5).equalsIgnoreCase("https")) {
                // SSL設定(サーバー証明書をチェックしない)
                configureSSL();
            }

            // 接続オブジェクトを生成する
            // http://docs.oracle.com/javase/jp/6/api/java/net/URL.html#openConnection()
            if (Constant.PROXY_HOST.length() != 0 && Constant.PROXY_PORT != 0) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(Constant.PROXY_HOST, Constant.PROXY_PORT));
                conn = (HttpURLConnection) resource.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) resource.openConnection();
            }

            conn.setInstanceFollowRedirects(false);
            HttpURLConnection.setFollowRedirects(false);

            // 注意:
            // HttpURLConnectionでは接続確立とデータ読取処理のそれぞれにタイムアウト時間を設定できる。
            // 本当は2つ合わせて Constant.WAIT_TIME を設定したいが難しそうなので、
            // どちらでも Constant.WAIT_TIME の時間が掛かってもいいように、両方にこの値をセットしておく。
            // なので実際は最大 Constant.WAIT_TIME の2倍の時間が許容されうる。
            // どちらとも遅いというのは、稀なケースであるので実用上問題ないとする。

            // この URLConnection が参照するリソースへの通信リンクのオープン時に、
            // 指定されたミリ秒単位のタイムアウト値が使用されるように設定します。
            // 接続が確立される前にタイムアウトが過ぎた場合は、
            // java.net.SocketTimeoutException が発行されます。
            // タイムアウト 0 は無限のタイムアウトとして解釈されます。
            conn.setConnectTimeout(Constant.WAIT_TIME * 1000); // ミリ秒
            // 読み取りタイムアウトを、指定されたミリ秒単位のタイムアウトに設定します。
            // 0 以外の値は、リソースへの接続が確立されている場合の、入力ストリームからの
            // 読み取りタイムアウトを指定します。データが読み取り可能になる前にタイムアウトが
            // 過ぎた場合は、java.net.SocketTimeoutException が発行されます。
            // タイムアウト 0 は無限のタイムアウトとして解釈されます。
            conn.setReadTimeout(Constant.WAIT_TIME * 1000); // ミリ秒

            conn.setRequestMethod("GET");
            //connect.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");

            if (! Constant.AUTH_BASIC_USERNAME.isEmpty() && ! Constant.AUTH_BASIC_PASSWORD.isEmpty()) {
                conn.setRequestProperty("Authorization",
                        "Basic " + getBasicCrypt(Constant.AUTH_BASIC_USERNAME, Constant.AUTH_BASIC_PASSWORD));
            }

            setTimeStart(System.currentTimeMillis());

            // TCPパケットを観察したところ、ここで通信するわけではないようだ。
            conn.connect();
            //System.out.println("ConnectTimeout" + connect.getConnectTimeout());
            //System.out.println("ReadTimeout" + connect.getReadTimeout());

        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (URISyntaxException e) {
            WSC.print("URISyntaxException at the URL: " + url);
            //e.printStackTrace();
            throw e;
        } catch (KeyManagementException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SocketTimeoutException e) {
            // タイムアウト時の処理
            processForTimeOut(url);
            //e.printStackTrace();
            throw e;
        } catch (SSLHandshakeException e) {
            logger.severe("SSLHandshakeException: URL: " + url);
            e.printStackTrace();
            throw e;
        } catch (ConnectException e) {
            // 1回目の接続であればここで終了する。
            if (Process.getUrlsDone().size() == 0) {
                // プロキシが設定されていない場合もここにくる
                if (Constant.PROXY_HOST.length() != 0) {
                    WSC.print("Connection failed. Proxy setting OK?");
                } else {
                    WSC.print("Connection failed.");
                }
                System.exit(1);
            }
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            //connect.disconnect();
        }

        return conn;
    }

    private void configureSSL() throws KeyManagementException, NoSuchAlgorithmException {

        //if (! (conn instanceof HttpsURLConnection)) {
        //    return;
        //}

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) 
                    throws CertificateException {
                }
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) 
                    throws CertificateException {
                }
            }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    /* タイムアウト時の処理 */
    private void processForTimeOut(String url) {

        final int REQUEST_TIME_OUT = 480;

        // この時点で処理が終了したとする。
        setTimeEnd(System.currentTimeMillis());

        // 各カウンター処理など
        Process.setCntNoResTotal(Process.getCntNoResTotal() + 1);
        if (Process.isBlnNoResInRow()) {
            Process.setCntNoResInRaw(Process.getCntNoResInRaw() + 1);
        } else {
            Process.setBlnNoResInRow(true);
            Process.setCntNoResInRaw(1);
        }

        // このURLを処理済みURLに追加
        Process.addUrlsDone(url);

        // ログ出力
        outputAccessLog(url, REQUEST_TIME_OUT);

        // HTTPステータスコードの処理
        processStatusCode(REQUEST_TIME_OUT);
    }

    private String getBasicCrypt(String username, String password) {
        String basicString = username + ":" + password;
        return Base64.encodeBase64String(basicString.getBytes());
    }

    private void processStatusCode(int responseCode) {
        if (responseCode >= 400 && responseCode < 600) {
            Process.setCntBadStatTotal(Process.getCntBadStatTotal() + 1);
            if (Process.isBlnBadStatInRow()) {
                Process.setCntBadStatInRaw(Process.getCntBadStatInRaw() + 1);
            } else {
                Process.setCntBadStatInRaw(1);
                Process.setBlnBadStatInRow(true);
            }
        } else {
            Process.setBlnBadStatInRow(false);
            Process.setCntBadStatInRaw(0);
        }
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
