import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.stream.StreamTransformer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Created by s1616245 on 24/10/16.
 * We define a StreamWords class which will be responsible to
 continuously read words form a local text file ("alice-in-wonderland.txt" in
 our case) and stream them into Ignite "words" cache
 */
public class StreamWords {
    public static void main(String[] args) throws Exception{
        // mark this cluter member as client
        Ignition.setClientMode(true);

        try (Ignite ignite = Ignition.start()){
            IgniteCache<String, Long> stmCache =
                    ignite.getOrCreateCache(CacheConfig.WordCache());

            // Create a streamer to stream words into the cache
            try (final IgniteDataStreamer<String, Long> streamer =
            ignite.dataStreamer(stmCache.getName())) {
                // We set allowOverwrite flag to true to make sure that existing
                // counts can be updated.
                streamer.allowOverwrite(true);

                // Configure data transformation to count instances of the same word
                streamer.receiver(StreamTransformer.from((e, arg) ->
                {
                    // get current count
                    Long val  = e.getValue();
                    // Increment current count by 1
                    e.setValue(val == null ? 1L : val + 1);
                    return null;
                }));



                // stream words form book
                while(true){
                    Path path = Paths.get(StreamWords.class.getResource(
                            "alice-in-wonderland.txt").toURI());

                    // Read words from a text file
                    try (Stream<String> lines =
                        Files.lines(path)){
                        lines.forEach(line -> {
                            Stream<String> words =
                                    Stream.of(line.split(" "));

                            // Stream words into Ignite streamer
                            words.forEach(word -> {
                                if(!word.trim().isEmpty())
                                    streamer.addData(word, 1L);
                            });
                        });
                    }
                }
            }
        }
    }
}
