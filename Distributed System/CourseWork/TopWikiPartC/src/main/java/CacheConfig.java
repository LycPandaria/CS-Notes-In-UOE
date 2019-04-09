import org.apache.ignite.configuration.CacheConfiguration;

/**
 * Created by lyc08 on 2016/10/25.
 */
public class CacheConfig {
    public static CacheConfiguration<String,Long> RecordCache() {
        CacheConfiguration<String, Long> cfg = new CacheConfiguration<>("records");
        // index the record and their counts
        // so it can be used in SQL querying
        cfg.setIndexedTypes(String.class, Long.class);
        return cfg;
    }

    public static CacheConfiguration<String, Long> SumCache() {
        CacheConfiguration<String, Long> cfg = new CacheConfiguration<>("sum");
        cfg.setIndexedTypes(String.class, Long.class);
        return cfg;
    }
}
