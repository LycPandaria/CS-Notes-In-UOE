package utils;

import java.util.logging.*;

/**
 * Created by lyc08 on 2016/10/31.
 */
public class MyLogger{

    private String part = new String();
    private static Logger log;

    public MyLogger(String part) throws Exception{
        this.part = part;
        log = Logger.getLogger("log-" + this.part + ".txt");
        log.setLevel(Level.ALL);
        FileHandler fileHandler = new FileHandler("../log/log-" + this.part + ".txt");
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatter());
        log.addHandler(fileHandler);
    }

    public String getPart() {
        return part;
    }

    public static Logger getLog() {
        return log;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public static void setLog(Logger log) {
        MyLogger.log = log;
    }

    public static class LogFormatter extends Formatter {
        public String format(LogRecord record) {
            return record.getMessage() + "\r\n";
        }
    }
}
