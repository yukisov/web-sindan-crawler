package net.pupha.wsc;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.util.Properties;

public class Constant {

    /* プロキシのホスト */
    public static final String PROXY_HOST;

    /* プロキシのポート番号 */
    public static final int PROXY_PORT;

    /* BASIC認証のユーザ名 */
    public static final String AUTH_BASIC_USERNAME;

    /* BASIC認証のパスワード */
    public static final String AUTH_BASIC_PASSWORD;

    /* スパイダリングする深さの最大値 */
    public static final int MAX_DEPTH;

    /* アクセス回数の最大値 */
    public static final int MAX_COUNT;

    /* 処理時間の最大値（秒） */
    public static final int MAX_TIME;

    /* HTTP接続のタイムアウト時間(ミリ秒) */
    public static final int WAIT_TIME;

    /* 通常アクセス後の次のアクセス直前にスリープする時間(s) */
    public static final double SLEEP_TIME_AFTER_ACCESS_AVAILABLE;

    /* 反応のなかったアクセス後の次のアクセス直前にスリープする時間(s) */
    public static final double SLEEP_TIME_AFTER_ACCESS_UNAVAILABLE;

    /* 連続して何回レスポンスに問題があった場合に、処理を終了するか */
    public static final int LIMIT_COUNT_FOR_PROBLEM_IN_A_ROW;

    /* レスポンスに何回問題があった場合に処理を終了するか */
    public static final int LIMIT_COUNT_FOR_PROBLEM;

    /* 最初に与えられたURLパスより上位のパスにもアクセスするかどうか */
    public static final boolean CLIMBING_UP_URL_PATH;

    /* URLを '://' 以下で同一性を判断する */
    public static final boolean IDENTIFY_URL_WITHOUT_PROTOCOL;

    /* URL内の'#'以下は削除してURLを扱う */
    public static final boolean IDENTIFY_URL_BEFORE_SHARP;

    /* 3xx リダイレクト先追うかどうか
     * その場合、リダイレクト先は1階層下のページのURLとして扱われる。 */
    public static final boolean FOLLOW_REDIRECT;

    /* アプリケーション用出力ファイル名 */
    public static final String APPL_OUTPUT_FILE_NAME;

    static {

        Properties config = new Properties();
        InputStream inStream = null;

        try {
            inStream = new BufferedInputStream(
                    Constant.class.getClassLoader().getResourceAsStream("config.properties"));
            config.load(inStream);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("config.properties Not Found");
            System.exit(1);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error: inStream.close()");
                System.exit(1);
            }
        }

        PROXY_HOST = String.valueOf(config.getProperty("PROXY_HOST", ""));
        if (config.getProperty("PROXY_PORT", "0").length() == 0) {
            PROXY_PORT = 0;
        } else {
            PROXY_PORT = Integer.valueOf(config.getProperty("PROXY_PORT"));
        }
        AUTH_BASIC_USERNAME = config.getProperty("AUTH_BASIC_USERNAME", "");
        AUTH_BASIC_PASSWORD = config.getProperty("AUTH_BASIC_PASSWORD", "");
        MAX_DEPTH = Integer.valueOf(config.getProperty("MAX_DEPTH", "5"));
        MAX_COUNT = Integer.valueOf(config.getProperty("MAX_COUNT", "100"));
        MAX_TIME = Integer.valueOf(config.getProperty("MAX_TIME", "2400"));
        WAIT_TIME = Integer.valueOf(config.getProperty("WAIT_TIME", "5"));
        SLEEP_TIME_AFTER_ACCESS_AVAILABLE = Double.valueOf(config.getProperty("SLEEP_TIME_AFTER_ACCESS_AVAILABLE"));
        SLEEP_TIME_AFTER_ACCESS_UNAVAILABLE = Double.valueOf(config.getProperty("SLEEP_TIME_AFTER_ACCESS_UNAVAILABLE"));
        LIMIT_COUNT_FOR_PROBLEM_IN_A_ROW = Integer.valueOf(config.getProperty("LIMIT_COUNT_FOR_PROBLEM_IN_A_ROW", "5"));
        LIMIT_COUNT_FOR_PROBLEM = Integer.valueOf(config.getProperty("LIMIT_COUNT_FOR_PROBLEM", "10"));
        APPL_OUTPUT_FILE_NAME = config.getProperty("APPL_OUTPUT_FILE_NAME", "WSC.log");

        int intClimbingUpUrlPath = Integer.valueOf(config.getProperty("CLIMBING_UP_URL_PATH", "0"));
        if (intClimbingUpUrlPath == 1) {
            CLIMBING_UP_URL_PATH  = true;
        } else {
            CLIMBING_UP_URL_PATH  = false;
        }

        int intFollowRedirect = Integer.valueOf(config.getProperty("FOLLOW_REDIRECT", "0"));
        if (intFollowRedirect == 1) {
            FOLLOW_REDIRECT  = true;
        } else {
            FOLLOW_REDIRECT  = false;
        }

        int intIdentifyUrlWithoutProtocol = Integer.valueOf(config.getProperty("IDENTIFY_URL_WITHOUT_PROTOCOL", "1"));
        if (intIdentifyUrlWithoutProtocol == 1) {
            IDENTIFY_URL_WITHOUT_PROTOCOL  = true;
        } else {
            IDENTIFY_URL_WITHOUT_PROTOCOL  = false;
        }

        int intIdentifyUrlBeforeSharp = Integer.valueOf(config.getProperty("IDENTIFY_URL_BEFORE_SHARP", "1"));
        if (intIdentifyUrlBeforeSharp == 1) {
            IDENTIFY_URL_BEFORE_SHARP  = true;
        } else {
            IDENTIFY_URL_BEFORE_SHARP  = false;
        }
    }
}
