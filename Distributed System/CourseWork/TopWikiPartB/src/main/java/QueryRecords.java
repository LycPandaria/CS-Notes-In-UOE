import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.IgniteConfiguration;

import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.*;

/**
 * Created by lyc08 on 2016/10/25.
 */
public class QueryRecords {

    public static class LogFormatter extends Formatter {
        public String format(LogRecord record) {
            return record.getMessage() + "\r\n";
        }
    }

    private static Logger log = Logger.getLogger("log-partB");

    public static void main(String[] args) throws Exception{
        Ignition.setClientMode(true);

        // set logger
        log.setLevel(Level.ALL);
        FileHandler fileHandler = new FileHandler("../log/log-partB.txt");
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new LogFormatter());
        log.addHandler(fileHandler);
        //log.info("This is test java util log");

        try(Ignite ignite = Ignition.start()){
            IgniteCache<String, Long> sumCache = ignite.getOrCreateCache(CacheConfig.SumCache());

            // select top 10
            SqlFieldsQuery top10Qry = new SqlFieldsQuery(
                    "select _key, _val from Long order by _val desc limit 10");

            // Query top 10 popular word every 10 sec
            while(true){
                // execute query
                List<List<?>> top10 = sumCache.query(top10Qry).getAll();

                //print
                printQueryResults(top10);

                Thread.sleep(10 * 1000);
            }
        }
    }

    public static void printQueryResults(List<?> res) {
        Long unixTime = System.currentTimeMillis();
        DecimalFormat dft = new DecimalFormat("0.00");
        if (res == null || res.isEmpty())
            System.out.println("Query result set is empty.");
        else {
            for (Object row : res) {
                if (row instanceof List) {
                    String line = unixTime + ":";
                    //System.out.print(unixTime+":");

                    List<?> l = (List)row;

                    if(l.size() == 2){
                        line = line + l.get(1) + ":" + l.get(0);
                    }
                    else
                        continue;

                    System.out.println(line);
                    log.info(line);
                }
                else
                    System.out.println("  " + row);
            }
        }
    }
}
