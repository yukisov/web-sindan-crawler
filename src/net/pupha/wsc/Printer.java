package net.pupha.wsc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class Printer {

    static private Printer printer = null;

    /* ロガーオブジェクト */
    private static final Logger logger = Logger.getLogger(Printer.class.toString());

    //private PrintWriter printWriter = null;
    private OutputStreamWriter writer = null;

    // ファイルに出力するかどうか
    private boolean isWritingToFile = false;

    // 出力ファイル名
    private String filepath = "";

    private Printer(String filepath) {
        this.filepath = filepath;
        initOutputFile();
    }

    /**
     * Initialize an output file
     */
    private void initOutputFile() {

        if (this.filepath.length() != 0) {
            this.isWritingToFile = true;
        } else {
            if (Constant.APPL_OUTPUT_FILE_NAME.length() == 0) {
                this.isWritingToFile = false;
            } else {
                this.isWritingToFile = true;
                this.filepath = getFilename(Constant.APPL_OUTPUT_FILE_NAME);
            }
        }
    }

    static public Printer getInstance(String filepath) {
        if (printer == null) {
            printer = new Printer(filepath);
        }
        return printer;
    }

    public void print(String msg) {
        System.out.println(msg);
        if (this.isWritingToFile) {
            writeToFile(msg);
        }
    }

    private void writeToFile(String msg) {
        try {
            FileOutputStream fos = new FileOutputStream(this.filepath, true);
            this.writer = new OutputStreamWriter(fos, "UTF-8");
            this.writer.write(msg + "\n");
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Couldn't read the application output file. (" + filepath + ")");
            System.exit(1);
        }
    }

    static private String getFilename(String filename) {

        String newFilename;

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String dateStr = "_" + dateFormat.format(now);

        if (filename.contains(".")) {
            int pos = filename.lastIndexOf('.');
            String preStr = filename.substring(0, pos);
            String postStr = filename.substring(pos);
            newFilename = preStr + dateStr + postStr;
        } else {
            newFilename = filename + dateStr;
        }

        return newFilename;
    }
}
