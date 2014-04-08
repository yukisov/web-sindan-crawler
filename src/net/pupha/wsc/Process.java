package net.pupha.wsc;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.pupha.wsc.utils.DateTimeUtils;
import net.pupha.wsc.utils.UrlUtils;

/**
 * 再帰的にクロールする処理を担当するクラス
 * @author yuki
 *
 */
public class Process {

    /* ロガーオブジェクト */
    private static final Logger logger = Logger.getLogger(Process.class.toString());

    /* 結果オブジェクト */
    private static Result result = new ResultOk();

    /* 開始日時 */
    static private Date startDate;

    /* 元のURL */
    static private String urlOrig = "";

    /* 現在処理中の深さ */
    private int depth = 0;

    /* 最初に与えられたURLパスより上位のパスにもアクセスするかどうか */
    private boolean climbingUpUrlPath = false;

    /* アクセス済みURL */
    static private List<String> urlsDone = new ArrayList<String>();

    /* 1つの起点からの探索中に、深さ限界のためにアクセスできなかったURLs */
    static private List<String> urlsLeft = new ArrayList<String>();

    /* 連続で反応がない回数 */
    static private int cntNoResInRaw;

    /* 前回のアクセスで反応がなかったかどうか */
    static private boolean blnNoResInRow = false;

    /* 反応がない回数 */
    static private int cntNoResTotal;

    /* HTTPステータスコード400 or 500が連続で返ってきた回数 */
    static private int cntBadStatInRaw;

    /* 前回のアクセスで HTTPステータスコード400 or 500が返ってきたかどうか */
    static private boolean blnBadStatInRow = false;

    /* HTTPステータスコード400 or 500が返ってきた回数 */
    static private int cntBadStatTotal;

    enum ExitStatus {
        CONTINUE, RETURN,
    }

    static public void initProcess(Date startDate, String urlOrig) throws IOException {
        Process.setStartDate(startDate);
        Process.setUrlOrig(urlOrig);
    }

    public Process(int depth) {
        setDepth(depth);
        setClimbingUpUrlPath(Constant.CLIMBING_UP_URL_PATH);
    }

    public void run(String url) {

        logger.finest("depth = " + depth);
        logger.finest("url = " + url);

        // 条件によっては処理を終了する
        if (returnOrExitInSomeCasesBeforeRun(url) == ExitStatus.RETURN) {
            return;
        }

        // 2回目以降の処理の待機処理
        waitIfNeeded();

        // URLにアクセスしてリンクURLを取得する
        Spider spider = new Spider();
        List<String> urls = spider.run(url);

        List<String> urlsNeeded = null;
        // 1回目のアクセスでホスト名無しのドメイン名でアクセスし、3xxリダイレクトでホスト付き同ドメインURLが1だけ返ってきた場合
        if (whenRedirectingOn1stAccessWithDomainOnly(Process.getUrlsDone().size(), url, spider.getStatusCode(), urls)) {
            urlsNeeded = urls;
            // 元のURLを変更する
            Process.setUrlOrig(urls.get(0));
        } else {
            // これから処理すべきURLのみを抽出する
            urlsNeeded = extractUrlsNeeded(urls);
        }

        // 条件によっては処理を終了する
        if (returnOrExitInSomeCasesAfterRun(url) == ExitStatus.RETURN) {
            return;
        }

        // 次への処理
        if (this.depth == Constant.MAX_DEPTH) {
            // 見つけたURLは次の巡回で使用するために保存しておく。
            Process.getUrlsLeft().addAll(urlsNeeded);
        } else {
            // 追加されたURLにアクセスする
            for (String urlNeeded: urlsNeeded) {
                (new Process(this.depth + 1)).run(urlNeeded);
            }
        }
    }

    /**
     *  1回目のアクセスでホスト名無しのドメイン名でアクセスし、3xxリダイレクトでホスト付き同ドメインURLが1だけ返ってきた場合
     */
    private boolean whenRedirectingOn1stAccessWithDomainOnly(
            int cntOfUrlsDone, String url, int statusCode, List<String> urls) {

        // 1回目のアクセス
        if (cntOfUrlsDone == 1) {
            // 3xxリダイレクト && 1つだけURLが取得された
            if (statusCode >= 300 && statusCode <= 399 && urls.size() == 1) {
                String locationUrl = urls.get(0);
                // ホスト名無しのドメイン名でアクセス
                // ホスト付き同ドメインURL
                if (UrlUtils.areSubDomain(url, locationUrl)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void waitIfNeeded() {

        if (getUrlsDone().size() == 0) {
            return;
        }

        // 2回目以降の処理の待機処理
        long wait = 0;
        // 前回のアクセスで反応がなかった場合は5秒待つ
        if (isBlnNoResInRow()) {
            wait = (long) Constant.SLEEP_TIME_AFTER_ACCESS_UNAVAILABLE * 1000;
        } else {
            wait = (long) (Constant.SLEEP_TIME_AFTER_ACCESS_AVAILABLE * 1000);
        }
        // それ以外は0.5秒待つ
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {
            logger.severe("Thread.sleep() failed.");
            e.printStackTrace();
        }
    }

    private ExitStatus returnOrExitInSomeCasesBeforeRun(String url) {

        // 最大深さを超えていたらreturn
        if (getDepth() > Constant.MAX_DEPTH) {
            return ExitStatus.RETURN;
        }

        // アクセス済のURLならreturn
        if (Constant.IDENTIFY_URL_WITHOUT_PROTOCOL) {
            for (String urlDone: getUrlsDone()) {
                if (UrlUtils.areSameUrlsWithoutProtocol(url, urlDone)) {
                    return ExitStatus.RETURN;
                }
            }
        } else {
            if (getUrlsDone().contains(url)) {
                return ExitStatus.RETURN;
            }
        }

        return ExitStatus.CONTINUE;
    }

    private ExitStatus returnOrExitInSomeCasesAfterRun(String url) {

        // 最大アクセス数を超えていたら終了
        if (getUrlsDone().size() >= Constant.MAX_COUNT) {
            outputData(String.format(
                    "The number of accessing URLs reached the max count. (Max count: %d)", getUrlsDone().size()));
            System.exit(1);
        }

        // 40分以上経過なら終了
        long elapsedTime = System.currentTimeMillis() - Process.getStartDate().getTime();
        if (elapsedTime >= (Constant.MAX_TIME * 1000)) {
            outputData(String.format("Timed out. (Max time: %d minutes)", Constant.MAX_TIME));
            System.exit(1);
        }

        // HTTP リクエストを送信してから、5 秒経過しても HTTP レスポンスの受信が完了しない状態が、5 回連続で発生した場合
        if (isBlnNoResInRow() && getCntNoResInRaw() >= Constant.LIMIT_COUNT_FOR_PROBLEM_IN_A_ROW) {
            setResult(new ResultNg(Result.DangerLevelType.MEDIUM));
            outputData(String.format(
                    "%d requests did not return within %d seconds in a row.",
                    Constant.LIMIT_COUNT_FOR_PROBLEM_IN_A_ROW, Constant.WAIT_TIME));
            System.exit(1);
        }

        double rate = 0.0;

        // HTTP リクエストを送信してから、5 秒経過しても HTTP レスポンスの受信が完了しない状態が、累計で 10 回発生した場合
        // 次の計算式も算出して表示する： (10 / アクセスの総数) * 100%
        if (getUrlsDone().size() > 0) {
            rate = (((double)Constant.LIMIT_COUNT_FOR_PROBLEM)/getUrlsDone().size()) * 100;
            if (getCntNoResTotal() >= Constant.LIMIT_COUNT_FOR_PROBLEM) {
                if (rate >= 10) {
                    setResult(new ResultNg(Result.DangerLevelType.MEDIUM));
                } else {
                    setResult(new ResultNg(Result.DangerLevelType.LOW));
                }
                outputData(String.format(
                        "%d requests did not return within %d seconds. (the rate ((%d/Total_Accesses_Counts) * 100) = %.2f)",
                        Constant.LIMIT_COUNT_FOR_PROBLEM, Constant.LIMIT_COUNT_FOR_PROBLEM, Constant.WAIT_TIME, rate));
                System.exit(1);
            }
        }

        // HTTP レスポンスの HTTP ステータスコードにおいて、400 番台又は 500 番台のエラーが発生し、かつこの状態が、5 回連続で発生した場合
        if (isBlnBadStatInRow() && getCntBadStatInRaw() >= Constant.LIMIT_COUNT_FOR_PROBLEM_IN_A_ROW) {
            setResult(new ResultNg(Result.DangerLevelType.MEDIUM));
            outputData(String.format(
                    "400-599 status code returned %d times in a row.",
                    Constant.LIMIT_COUNT_FOR_PROBLEM_IN_A_ROW));
            System.exit(1);
        }

        // HTTP レスポンスの HTTP ステータスコードにおいて、400 番台又は 500 番台のエラーが発生する状態が、累計で 10 回発生した場合
        // 次の計算式も算出して表示する： (10 / アクセスの総数) * 100%
        if (getUrlsDone().size() > 0) {
            rate = (((double)Constant.LIMIT_COUNT_FOR_PROBLEM)/getUrlsDone().size()) * 100;
            if (getCntBadStatTotal() >= Constant.LIMIT_COUNT_FOR_PROBLEM) {
                if (rate >= 10) {
                    setResult(new ResultNg(Result.DangerLevelType.MEDIUM));
                } else {
                    setResult(new ResultNg(Result.DangerLevelType.LOW));
                }
                outputData(String.format(
                        "%d requests with 400-599 status code returned. (the rate ((%d/Total_Accesses_Counts) * 100) = %.2f)",
                        Constant.LIMIT_COUNT_FOR_PROBLEM, Constant.LIMIT_COUNT_FOR_PROBLEM, rate));
                System.exit(1);
            }
        }

        return ExitStatus.CONTINUE;
    }

    /**
     * 必要なURLのみを抽出する
     * (1) '#' 以下は削除する
     * (2) 重複を除く。
     * (3) 元のURLのホスト名と同じURLのみを抽出する。
     * (4) アクセスしていないURLのみを抽出する。
     * (5) 最初に与えられたURLパスより上位のパスにもアクセスしない場合は取り除く
     */
    private List <String> extractUrlsNeeded(List <String> urls) {

        List<String> urlsAfterSharpProcess;
        if (Constant.IDENTIFY_URL_BEFORE_SHARP) {
            // (1) '#' 以下は削除する
            urlsAfterSharpProcess = extractUrlsSharpDeleted(urls);
        } else {
            urlsAfterSharpProcess = urls;
        }

        // (2) 重複を除く。
        List<String> urlsUniq = extractUrlsUniq(urlsAfterSharpProcess);

        // (3) 元のURLのホスト名と同じURLのみを抽出する。
        List<String> urlsSameHost = extractUrlsSameHost(urlsUniq);

        // (4) アクセスしていないURLのみを抽出する。
        List<String> urlsNotAccess = extractUrlsNotAccess(urlsSameHost);

        // (5) 最初に与えられたURLパスより上位のパスにもアクセスしない場合は取り除く
        List<String> urlsNeeded = extractUrlsStartWithTheOrigUrlPath(urlsNotAccess);

        return urlsNeeded;
    }

    private List<String> extractUrlsSharpDeleted(List<String> urls) {

        List<String> urlsRes = new ArrayList<String>();
        for (String url: urls) {
            int pos = url.indexOf('#');
            if (pos == -1) {
                urlsRes.add(url);
            } else {
                urlsRes.add(url.substring(0, pos));
            }
        }

        return urlsRes;
    }

    /**
     * 重複したURLを除く
     * @param urls
     * @return
     */
    private List<String> extractUrlsUniq(List<String> urls) {

         // 一旦 HashSetにすることで重複を削除する
         Set<String> set = new HashSet<String>();
         set.addAll(urls);
         List<String> urlsUniq = new ArrayList<String>();
         urlsUniq.addAll(set);
         Collections.sort(urlsUniq);
         return urlsUniq;
    }

    /**
     * アクセスしていないURLのみを抽出する。
     * @param urls
     * @return
     */
    private List<String> extractUrlsNotAccess(List <String> urls) {
        List<String> urlsRes = new ArrayList<String>();
        boolean contains = false;
        for (String url: urls) {
            contains =  false;
            for (String urlDone: getUrlsDone()) {
                if (Constant.IDENTIFY_URL_WITHOUT_PROTOCOL) {
                    if (UrlUtils.areSameUrlsWithoutProtocol(url, urlDone)) {
                        contains = true;
                        break;
                    }
                } else {
                    if (url.equals(urlDone)) {
                        contains = true;
                        break;
                    }
                }
            }
            if (!contains) {
                urlsRes.add(url);
            }
        }
        return urlsRes;
    }

    /**
     * 元のURLのホスト名と同じURLのみを抽出する。
     * @param urls
     * @return
     */
    private List<String> extractUrlsSameHost(List<String> urls) {

        List <String> urlsRes = new ArrayList<String>();

        // 元のURLのホスト文字列を取得する
        String hostOrig = "";
        URL resourceOrig;
        try {
            resourceOrig = new URL(Process.getUrlOrig());
            hostOrig = resourceOrig.getHost();
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
            logger.severe("Couldn't get info from the url: " + Process.getUrlOrig());
            System.exit(1);
        }

        // urlsNotAccessをループ処理する
        URL resource;
        String host;
        for (String url: urls) {
            try {
                // urlのホスト名を抜き出す
                resource = new URL(url);
                host = resource.getHost();
                if (host.equals(hostOrig)) {
                    urlsRes.add(url);
                }
            } catch (MalformedURLException e) {
                //e.printStackTrace();
                logger.warning("Couldn't get info from the url: " + url);
                continue;
            }
        }
        return urlsRes;
    }

    private List<String> extractUrlsStartWithTheOrigUrlPath(List<String> urls) {

        List <String> urlsRes = new ArrayList<String>();

        if (isClimbingUpUrlPath()) {
            return urls;
        }

        // 元のURLのHost + Pathを取得する
        String hostAndPathOrig = "";
        // "/"で split して最後の要素に"."があれば、その要素を削除した状態をパス名として使用する
        hostAndPathOrig = UrlUtils.getUrlPath(Process.getUrlOrig());

        // urlsNotAccessをループ処理する
        URL resource;
        String hostPath;
        for (String url: urls) {
            try {
                // urlのホスト名を抜き出す
                resource = new URL(url);
                hostPath = resource.getHost() + resource.getPath();
                if (hostPath.startsWith(hostAndPathOrig)) {
                    urlsRes.add(url);
                }
            } catch (MalformedURLException e) {
                //e.printStackTrace();
                logger.warning("Couldn't get info from the url: " + url);
                continue;
            }
        }
        return urlsRes;
    }

    static public void outputData(String msg) {

        // 終了した理由
        WSC.print("----------------------------------------------------------------------");
        WSC.print("Reason for the termination : " + msg);
        // 経過時間(分)
        long elapsedTime = System.currentTimeMillis() - Process.getStartDate().getTime();
        WSC.print("Total elapsed time         : " + DateTimeUtils.getMinutesSeconds(elapsedTime, "%d:%02d"));
        WSC.print("No Response Count          : " + String.valueOf(getCntNoResTotal()));
        WSC.print("StatusCode 400-599 Count   : " + String.valueOf(getCntBadStatTotal()));
        WSC.print("Total number of requests   : " + String.valueOf(getUrlsDone().size()));
        getResult().outputData();
        WSC.print("----------------------------------------------------------------------");
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isClimbingUpUrlPath() {
        return climbingUpUrlPath;
    }

    public void setClimbingUpUrlPath(boolean climbingUpUrlPath) {
        this.climbingUpUrlPath = climbingUpUrlPath;
    }

    public static String getUrlOrig() {
        return urlOrig;
    }

    public static void setUrlOrig(String urlOrig) {
        Process.urlOrig = urlOrig;
    }

    public static Date getStartDate() {
        return startDate;
    }

    public static void setStartDate(Date startDate) {
        Process.startDate = startDate;
    }

    static public List<String> getUrlsDone() {
        return Process.urlsDone;
    }

    static public void setUrlsDone(List<String> urlsDone) {
        Process.urlsDone = urlsDone;
    }

    static public void addUrlsDone(String url) {
        Process.urlsDone.add(url);
    }

    public static List<String> getUrlsLeft() {
        return urlsLeft;
    }

    public static void setUrlsLeft(List<String> urlsLeft) {
        Process.urlsLeft = urlsLeft;
    }

    static public void addUrlsLeft(String url) {
        Process.urlsLeft.add(url);
    }

    static public int getCntNoResInRaw() {
        return Process.cntNoResInRaw;
    }

    static public void setCntNoResInRaw(int cntNoResInRaw) {
        Process.cntNoResInRaw = cntNoResInRaw;
    }

    static public int getCntNoResTotal() {
        return Process.cntNoResTotal;
    }

    static public void setCntNoResTotal(int cntNoResTotal) {
        Process.cntNoResTotal = cntNoResTotal;
    }

    static public int getCntBadStatInRaw() {
        return Process.cntBadStatInRaw;
    }

    static public void setCntBadStatInRaw(int cntBadStatInRaw) {
        Process.cntBadStatInRaw = cntBadStatInRaw;
    }

    static public int getCntBadStatTotal() {
        return Process.cntBadStatTotal;
    }

    static public void setCntBadStatTotal(int cntBadStatTotal) {
        Process.cntBadStatTotal = cntBadStatTotal;
    }

    static public boolean isBlnNoResInRow() {
        return Process.blnNoResInRow;
    }

    static public void setBlnNoResInRow(boolean blnNoResInRow) {
        Process.blnNoResInRow = blnNoResInRow;
    }

    static public boolean isBlnBadStatInRow() {
        return Process.blnBadStatInRow;
    }

    static public void setBlnBadStatInRow(boolean blnBadStatInRow) {
        Process.blnBadStatInRow = blnBadStatInRow;
    }

    public static Result getResult() {
        return result;
    }

    public static void setResult(Result result) {
        Process.result = result;
    }
}
