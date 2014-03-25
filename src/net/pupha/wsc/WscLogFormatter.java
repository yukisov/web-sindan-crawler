package net.pupha.wsc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class WscLogFormatter extends Formatter {

    private final SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        final StringBuffer buf = new StringBuffer();

        // 日時
        buf.append(sdFormat.format(new Date(record.getMillis())));

        buf.append(" ");

        // ログレベル
        String logLevel = "";
        if (record.getLevel() == Level.FINEST) {
            logLevel = "FINEST";
        } else if (record.getLevel() == Level.FINER) {
            logLevel = "FINER ";
        } else if (record.getLevel() == Level.FINE) {
            logLevel = "FINE  ";
        } else if (record.getLevel() == Level.CONFIG) {
            logLevel = "CONFIG";
        } else if (record.getLevel() == Level.INFO) {
            logLevel = "INFO  ";
        } else if (record.getLevel() == Level.WARNING) {
            logLevel = "WARN  ";
        } else if (record.getLevel() == Level.SEVERE) {
            logLevel = "SEVERE";
        } else {
            logLevel = Integer.toString(record.getLevel().intValue()) + " ";
        }
        buf.append(logLevel);

        buf.append(" ");
        buf.append(record.getLoggerName());
        buf.append(" ");
        buf.append(record.getMessage());
        buf.append("\n");

        return buf.toString();
    }
}
