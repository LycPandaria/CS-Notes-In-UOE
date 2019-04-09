package parta;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import utils.CacheConfig;
import utils.StreamThread;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by lyc08 on 2016/10/25.
 */
public class StreamPartA extends StreamThread {

    public StreamPartA(String path) {
        super(path);
    }

    @Override
    public void run() {
        try{
            System.out.println("Stream start.");
            // mark this cluter member as client
            Ignition.setClientMode(true);

            try(Ignite ignite = Ignition.getOrStart(new IgniteConfiguration())){
                IgniteCache<String, Long> stmCache =
                        ignite.getOrCreateCache(CacheConfig.RecordCacheWithSW());

                // create a streamer to stream records into cache
                try(final IgniteDataStreamer<String, Long> streamer =
                            ignite.dataStreamer(stmCache.getName())) {

                    streamer.allowOverwrite(true);

                    // steam records from record
                    //while(true){
                        Path path = Paths.get(super.path);
                        // read records from file
                        try(Stream<String> lines = Files.lines(path)){
                            lines.forEach(line -> {
                                if(!line.trim().isEmpty()){
                                    String[] record = line.trim().split("\\s+");
                                    if(record.length == 4){
                                        Long visitCount = Long.parseLong(record[2]);
                                        String title = record[1];
                                        IgniteCache<String, Long> cache = ignite.cache(stmCache.getName());
                                        if(cache.containsKey(title)){
                                            Long count = cache.get(title);
                                            cache.put(title, count + visitCount);
                                        }else{
                                            streamer.addData(title, visitCount);
                                        }
                                    }
                                }
                            });
                        }
                    //}
                }
            }
        } catch (Exception e ){
            e.printStackTrace();
        }
    }
}
