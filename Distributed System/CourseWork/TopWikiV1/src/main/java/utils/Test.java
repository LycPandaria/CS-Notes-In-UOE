package utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.*;
import java.util.stream.Stream;

/**
 * Created by lyc08 on 2016/10/27.
 */
public class Test {
    private static Logger log = Logger.getLogger("log");
    public static class LogFormatter extends Formatter {
        public String format(LogRecord record) {
            return record.getMessage() + "\n";
        }
    }
    public static void main(String[] args) throws Exception{
        // set logger
        log.setLevel(Level.ALL);
        FileHandler fileHandler = new FileHandler("../log/log.txt");
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatter());
        log.addHandler(fileHandler);
        log.info("This is test java util log");

        Scanner in = new Scanner(System.in);
        String file = in.nextLine();
        Path path = Paths.get(file);

        Stream<String> lines = Files.lines(path);
        lines.forEach(line -> {
                System.out.println(line);
        });
    }
}
