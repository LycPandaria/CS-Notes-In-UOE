package partb;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import utils.CacheConfig;
import utils.MyLogger;
import utils.QueryThread;

import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.*;

/**
 * Created by lyc08 on 2016/10/25.
 */
public class QueryPartB extends QueryThread{

    private String part = new String();

    public QueryPartB(String part) {
        this.part = part;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            System.out.println("Query start.");
            Ignition.setClientMode(true);

            MyLogger log = new MyLogger(this.part);

            try(Ignite ignite = Ignition.getOrStart(new IgniteConfiguration())) {
                IgniteCache<String, Long> sumCache = ignite.getOrCreateCache(CacheConfig.SumCacheWithSW());

                // select top 10
                SqlFieldsQuery top10Qry = new SqlFieldsQuery(
                        "select _key, _val from Long order by _val desc limit 10");

                // Query top 10 popular word every 10 sec
                while (true) {
                    // execute query
                    List<List<?>> top10 = sumCache.query(top10Qry).getAll();

                    //print
                    QueryThread.printQueryResults(top10, log);

                    Thread.sleep(10 * 1000);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
