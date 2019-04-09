import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.stream.StreamTransformer;
import org.apache.ignite.stream.StreamVisitor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by lyc08 on 2016/10/25.
 */
public class StreamRecord {
    public static void main(String[] args) throws Exception{
        // mark this cluter member as client
        Ignition.setClientMode(true);

        try(Ignite ignite = Ignition.start()){
            IgniteCache<String, Long> stmCache =
                    ignite.getOrCreateCache(CacheConfig.RecordCache());
            IgniteCache<String, Long> sumRecordCache =
                    ignite.getOrCreateCache(CacheConfig.SumCache());

            // create a streamer to stream records into cache
            try(final IgniteDataStreamer<String, Long> streamer =
                ignite.dataStreamer(stmCache.getName())) {
                streamer.allowOverwrite(true);

                // Configure data transformation to count instances of the same word.
                streamer.receiver(new StreamVisitor<String, Long>() {
                    @Override
                    public void apply(IgniteCache<String, Long> entries, Map.Entry<String, Long> stringLongEntry) {
                        // get this key-value
                        String title = stringLongEntry.getKey();
                        Long count = stringLongEntry.getValue();

                        // get the sumcache
                        IgniteCache<String, Long> sumCache =
                                ignite.cache(sumRecordCache.getName());
                        // find the record by title
                        Long sum = sumCache.get(title);
                        // new sum record
                        if(sum == null)
                            sumCache.put(title, count);
                        else
                            // add
                            sumCache.put(title, count + sum);

                    }
                });

                // steam records from record
                Path path = Paths.get(args[0]);
                // read records from file
                try(Stream<String> lines = Files.lines(path)){
                    System.out.println("Streamer start.");
                    lines.forEach(line -> {
                        if(!line.trim().isEmpty()){
                            String[] record = line.trim().split("\\s+");
                            if(record.length == 4){
                                Long visitCount = Long.parseLong(record[2]);
                                String title = record[1];
                                streamer.addData(title, visitCount);
                            }
                        }
                    });
                }
                System.out.println("Streamer end.");
            }

        }
    }
}
