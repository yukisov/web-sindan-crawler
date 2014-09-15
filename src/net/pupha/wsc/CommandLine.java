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
    private static final String HELP_SHORT = "h";
    private static final String HELP_LONG = "help";

    private Options options = null;

    @SuppressWarnings("unused")
    private String logfile = "";

    private Boolean hasShowHelp = false;

    private String args[] = null;

    public CommandLine(String args[]) {
        this.args = args;
    }

    public void init() throws ParseException {
        // preparing options
        this.options = new Options();

        //options.addOption(HELP_SHORT, HELP_LONG, false, "show help");
        Option help = OptionBuilder.withArgName(HELP_LONG)
                            .hasArg(false)
                            .isRequired(false)
                            .withDescription("show help")
                            .withLongOpt(HELP_LONG)
                            .create(HELP_SHORT);
        options.addOption(help);

        //options.addOption(LOG_FILE_PATH_SHORT, LOG_FILE_PATH_LONG, true, "log file path");
        Option logfile = OptionBuilder.withArgName(LOG_FILE_PATH_LONG)
                            .hasArg(true)
                            .isRequired(false)
                            .withDescription("log file path")
                            .withLongOpt(LOG_FILE_PATH_LONG)
                            .create(LOG_FILE_PATH_SHORT);
        this.options.addOption(logfile);

        CommandLineParser parser = new BasicParser();
        org.apache.commons.cli.CommandLine cl = null;

        try {
            cl = parser.parse(this.options, this.args);
        } catch (ParseException e) {
            //e.printStackTrace();
            throw e;
        }

        // get options specified
        this.logfile = cl.getOptionValue(LOG_FILE_PATH_SHORT);
        if (this.logfile == null) {
            this.logfile = "";
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
}
