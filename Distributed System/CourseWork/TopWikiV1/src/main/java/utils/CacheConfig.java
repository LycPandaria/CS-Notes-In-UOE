package utils;

import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Created by lyc08 on 2016/10/25.
 */
public class CacheConfig {

    public static CacheConfiguration<String,Long> RecordCacheWithSW() {
        CacheConfiguration<String, Long> cfg = new CacheConfiguration<>("recordsWithSW");
        // index the record and their counts
        // so it can be used in SQL querying
        cfg.setIndexedTypes(String.class, Long.class);
        // Sliding window of 5 seconds
        cfg.setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                new CreatedExpiryPolicy(new Duration(TimeUnit.SECONDS, 1))
        ));
        return cfg;
    }

    public static CacheConfiguration<String,Long> RecordCache() {
        CacheConfiguration<String, Long> cfg = new CacheConfiguration<>("records");
        // index the record and their counts
        // so it can be used in SQL querying
        cfg.setIndexedTypes(String.class, Long.class);
       /* cfg.setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                new CreatedExpiryPolicy(new Duration(TimeUnit.SECONDS, 1))
        )); */
        return cfg;
    }

    public static CacheConfiguration<String, Long> SumCacheWithSW() {
        CacheConfiguration<String, Long> cfg = new CacheConfiguration<>("sumWithSW");
        cfg.setIndexedTypes(String.class, Long.class);
        cfg.setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                new CreatedExpiryPolicy(new Duration(TimeUnit.SECONDS, 1))
        ));
        return cfg;
    }

    public static CacheConfiguration<String, Long> SumCache() {
        CacheConfiguration<String, Long> cfg = new CacheConfiguration<>("sum");
        cfg.setIndexedTypes(String.class, Long.class);
        return cfg;
    }
}
