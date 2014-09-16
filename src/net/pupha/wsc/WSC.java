package net.pupha.wsc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.apache.commons.cli.ParseException;

import net.pupha.wsc.utils.UrlUtils;

/**
 * WSC
 * @author yuki
 */
public class WSC {

    private static Logger logger = null;

    private String urlOrig;

    private CommandLine commandline = null;

    private static String logfile = "";

    private static String logPropFile= "log.properties";

    public static void main(String args[]) {

        final WSC wsc = new WSC();

        try {
            wsc.init(args);
            wsc.run();
            // 正常終了
            Process.outputData("All URLs found were accessed.");
        } catch (final Exception e) {
            e.printStackTrace();
            logger.severe(e.getMessage());
            //throw e;
            System.exit(1);
        }
    }

    private void init(String[] args) {

        // handling command line parameters
        this.commandline = new CommandLine(args);
        try {
            this.commandline.init();
        } catch (ParseException e) {
            this.commandline.showHelp();
            System.exit(0);
        }
        logfile = this.commandline.getLogfile();
        logPropFile = this.commandline.getLogPropFile();
        if (logPropFile.length() == 0) {
            logPropFile = "log.properties";
        }
        if (args.length == 0 || this.commandline.hasShowHelp()) {
            this.commandline.showHelp();
            System.exit(0);
        }

        this.urlOrig = args[0].trim();

        logger = Logger.getLogger(WSC.class.toString());

        // ロガーの設定
        try {
            LogManager.getLogManager().readConfiguration(
                  WSC.class.getClassLoader().getResourceAsStream(WSC.logPropFile));
        } catch (SecurityException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // URLにプロトコルがなければ "http://"を追加する。
        if (!this.urlOrig.matches("^https?://.+")) {
            this.urlOrig = "http://" + this.urlOrig;
        }

        // URLの妥当性を検査する
        if (!UrlUtils.isValidUrl(this.urlOrig)) {
            WSC.print("Invalid URL");
            System.exit(1);
        }
    }

    private void run() throws Exception {
        Process.initProcess(new Date(), this.urlOrig);

        List<String> urlsLeft = Arrays.asList(this.urlOrig);
        List<String> urlsTmp = new ArrayList<String>();
        int loopNumber = 1;
        while (true) {
            if (urlsLeft.size() > 0) {
                if (loopNumber > 1) {
                    logger.finest("Loop Number: " + loopNumber);
                }
                for (String url: urlsLeft) {
                    Process.getUrlsLeft().clear();
                    (new Process(1)).run(url);
                    urlsTmp.addAll(Process.getUrlsLeft());
                }
                urlsLeft = new ArrayList<String>(urlsTmp);
                urlsTmp.clear();
            }
            if (urlsLeft.size() == 0) break;
            loopNumber++;
        }
    }

    public static void print(String msg) {
        Printer.getInstance(WSC.logfile).print(msg);
    }
}
