import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import java.util.List;

/**
 * Created by lyc08 on 2016/10/24.
 * We define a QueryWords class which will periodically query word counts form the
 cache
 */
public class QueryWords {
    public static void main(String[] args) throws Exception{
        // mark this cluster member as client
        Ignition.setClientMode(true);

        try (Ignite ignite = Ignition.start()){
            IgniteCache<String, Long> stmCache = ignite.getOrCreateCache(CacheConfig.WordCache());

            // select top 10 words
            SqlFieldsQuery top10Qry = new SqlFieldsQuery(
                    "select _key, _val from Long order by _val desc limit 10");

            // Query top 10 popular word every 5 seconds
            while (true){
                // execute queries
                List<List<?>> top10 = stmCache.query(top10Qry).getAll();

                // Print top 10words
                ExamplesUtils.printQueryResults(top10);

                Thread.sleep(5000);
            }
        }
    }

}
