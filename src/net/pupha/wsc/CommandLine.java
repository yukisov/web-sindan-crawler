package net.pupha.wsc;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLine {

    private static final String LOG_FILE_PATH_SHORT = "l";
    private static final String LOG_FILE_PATH_LONG = "logfile";
    private static final String LOG_PROP_FILE_PATH_SHORT = "p";
    private static final String LOG_PROP_FILE_PATH_LONG = "log-prop-file";
    private static final String HELP_SHORT = "h";
    private static final String HELP_LONG = "help";

    private Options options = null;

    private String logfile = "";

    private String logPropFile = "";

    private Boolean hasShowHelp = false;

    private String args[] = null;

    public CommandLine(String args[]) {
        this.args = args;
    }

    public void init() throws ParseException {
        // preparing options
        this.options = new Options();

        @SuppressWarnings("static-access")
        Option help = OptionBuilder.withArgName(HELP_LONG)
                            .hasArg(false)
                            .isRequired(false)
                            .withDescription("show help")
                            .withLongOpt(HELP_LONG)
                            .create(HELP_SHORT);
        options.addOption(help);

        @SuppressWarnings("static-access")
        Option logfile = OptionBuilder.withArgName(LOG_FILE_PATH_LONG)
                            .hasArg(true)
                            .isRequired(false)
                            .withDescription("log file path")
                            .withLongOpt(LOG_FILE_PATH_LONG)
                            .create(LOG_FILE_PATH_SHORT);
        this.options.addOption(logfile);

        @SuppressWarnings("static-access")
        Option log_prop_file = OptionBuilder.withArgName(LOG_PROP_FILE_PATH_LONG)
                            .hasArg(true)
                            .isRequired(false)
                            .withDescription("log properties file path")
                            .withLongOpt(LOG_PROP_FILE_PATH_LONG)
                            .create(LOG_PROP_FILE_PATH_SHORT);
        this.options.addOption(log_prop_file);

        CommandLineParser parser = new BasicParser();
        org.apache.commons.cli.CommandLine cl = null;

        try {
            cl = parser.parse(this.options, this.args);
        } catch (ParseException e) {
            //e.printStackTrace();
            throw e;
        }

        // ----- get options specified ----- //

        this.logfile = cl.getOptionValue(LOG_FILE_PATH_SHORT);
        if (this.logfile == null) {
            this.logfile = "";
        }

        this.logPropFile = cl.getOptionValue(LOG_PROP_FILE_PATH_SHORT);
        if (this.logPropFile == null) {
            this.logPropFile = "";
        }

        if (cl.hasOption(HELP_SHORT)) {
            this.hasShowHelp = true;
        }

    }

    public Boolean hasShowHelp() {
        return this.hasShowHelp;
    }

    public void showHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("WSC.sh URL", this.options);
    }

    public String getLogfile() {
        return this.logfile;
    }

    public String getLogPropFile() {
        return this.logPropFile;
    }
}
