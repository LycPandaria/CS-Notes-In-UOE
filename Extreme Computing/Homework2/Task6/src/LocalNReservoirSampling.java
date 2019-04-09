import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Vector;
import java.util.stream.Stream;

/**
 * Created by lyc08 on 2016/11/11.
 */
public class LocalNReservoirSampling {
    public static final int K = 100;
    public static Vector<String> samples = new Vector<String>(K); //
    public static int line_number = 0;

    public static void main(String[] args) throws Exception{
        Random random = new Random();

        if(args.length != 1){
            System.out.println("Usage: LocalNReservoirSampling <input_file>");
            System.exit(0);
        }

        Path input = Paths.get(args[0]);

        Stream<String> lines = Files.lines(input);
        lines.forEach(line -> {
            if(line_number < K) { // fill the reservoir
                samples.add(line_number, line);
                line_number++;
            } else {
                int r = random.nextInt(line_number+1); // generate a random from[0,100]
                if(r < K)       //
                    samples.set(r, line);
                line_number++;
            }
        });

        File output = new File("output.txt");
        if(!output.exists())
            output.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));
        for(String line:samples){
            bufferedWriter.write(line);
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
