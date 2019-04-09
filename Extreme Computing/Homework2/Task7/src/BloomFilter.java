/**
 * Created by lyc08 on 2016/11/12.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.stream.Stream;

public class BloomFilter {

    // use 2 to the 25 bits to reprents the set
    // since we have to deal with 10 to 7 lines with false 1%
    private static final int DEFAULT_SIZE = 2 << 25;
    // use prime to build hash functions
    // use the equation in slides, I use 10 hash functions
    private static final int HASH_NUM = 10; // number of hash function
    private static final int[] HASHS = new int[] { 5, 7, 11, 13, 31, 37, 41, 53, 61, 71 };

    private HashFunction[] func = new HashFunction[HASH_NUM];
    private BitSet bits;

    public static void main(String[] args) throws Exception{

        if(args.length != 2){
            System.out.println("Usage: BloomFilter <input_file> <lineNumber>");
            System.exit(0);
        }

        // compute the size of bit set
        double lineNum = Double.parseDouble(args[1]);
        // use 2 to the times to the bitset size
        int times =  (int)Math.ceil(Math.log(lineNum*HASH_NUM / Math.log(2)) / Math.log(2)) -1;

        // input stream
        Path input = Paths.get(args[0]);
        File output = new File("AllOutput.txt");
        if(!output.exists())
            output.createNewFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output));

        // constructor
        BloomFilter filter = new BloomFilter(times);

        Stream<String> lines = Files.lines(input);
        lines.forEach(line -> {
            try {
                if(!filter.contains(line)){
                    filter.add(line);
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public BloomFilter() {
        this.bits = new BitSet(DEFAULT_SIZE);
        for (int i = 0; i < HASHS.length; i++) {
            // init the hash functuons
            func[i] = new HashFunction(DEFAULT_SIZE, HASHS[i]);
        }
    }

    public BloomFilter(int times) {
        this.bits = new BitSet(2 << times);
        for (int i = 0; i < HASHS.length; i++) {
            // init the hash functuons
            func[i] = new HashFunction(DEFAULT_SIZE, HASHS[i]);
        }
    }

    // corrsponding value to bits, by calculating 7 hashs.
    public void add(String value) {
        for (HashFunction f : func) {
            bits.set(f.hash(value), true);
        }
    }

    public boolean contains(String value) {
        if (value == null) {
            return false;
        }
        boolean result = true;
        for (HashFunction f : func) {
            // if all return true, then return true
            result = result && bits.get(f.hash(value));
        }
        return result;
    }

    // hash functions use differents primes
    public static class HashFunction {

        private int cap;    //size of bitset
        private int seed;   //prime uesd to compute

        public HashFunction(int cap, int seed) {
            this.cap = cap;
            this.seed = seed;
        }

        public int hash(String value) {

            int result = 0;
            int len = value.length();
            for (int i = 0; i < len; i++) {
                // calculating hash
                result = seed * result + value.charAt(i);
            }
            // if the result is negetive, turn it to positive
            return (cap - 1) & result;
        }
    }



}
