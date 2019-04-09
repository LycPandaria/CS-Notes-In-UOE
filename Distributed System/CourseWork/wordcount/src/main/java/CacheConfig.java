import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

/**
 * The cache will use words as keys, and counts for words as values.
 Note that in this example we use a sliding window of 5 seconds for our cache.
 */
public class CacheConfig{
    public static CacheConfiguration<String, Long> WordCache() {
        CacheConfiguration<String, Long> cfg = new CacheConfiguration<String, Long>("words");

        // Index the words and their counts
        // so we can use them for fast SQL querying.
        cfg.setIndexedTypes(String.class, Long.class);

        // Sliding window of 5 seconds
        cfg.setExpiryPolicyFactory(FactoryBuilder.factoryOf(
                new CreatedExpiryPolicy(new Duration(TimeUnit.SECONDS, 5))
        ));

        return cfg;
    }
}