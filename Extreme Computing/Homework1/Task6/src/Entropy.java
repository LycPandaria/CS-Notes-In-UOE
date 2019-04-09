import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

/**
 * Created by lyc08 on 2016/10/11.
 */
public class Entropy {
    public static class EntropyMapper extends Mapper<LongWritable, Text, Text, Text> {
        private Text twoSequence = new Text();
        private String firstTwo;
        private String countstring;
        private Text counts = new Text();

        /**
         *
         * @param key
         * @param value one line (eg: big green apple 5)
         * @param context   output: key is two-sequence, value is the a specified three-sequence count
         *                  eg: (big green, 5) but actually, 5 is the count of 'big green apple'
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // read line by line
            if (!value.toString().isEmpty()) {
                // '\s' means all kinds of blankspace
                String[] splits = value.toString().split("\\s+");  //divide by ' '
                if (splits.length != 4)
                    throw new IOException("invalid input." + value.toString());
                // get first two words
                firstTwo = splits[0] + " " + splits[1];
                if(twoSequence.toString().isEmpty() ){
                    // first time
                    twoSequence.set(firstTwo);
                    countstring = "" + splits[3];
                }
                else if(firstTwo.equals(twoSequence.toString())){
                    // it is a neighbor
                    countstring =countstring + " " + splits[3];
                }else {
                    // a new sequence
                    // first: output old one stripe
                    counts.set(countstring);
                    context.write(twoSequence, counts);
                    // set new stripe
                    twoSequence.set(firstTwo);
                    countstring = "" + splits[3];
                }
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            counts.set(countstring);
            context.write(twoSequence,counts);
        }
    }

    public static class EntropyCombiner extends Reducer<Text, Text, Text, Text>{
        // use to combine count string
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            String combine = new String();
            for(Text value:values)
                combine += (value + " ");
            context.write(key, new Text(combine.trim()));
        }
    }

    public static class EntropyReducer extends Reducer<Text, Text, Text, Text>{

        private Text entropyW = new Text();

        /**
         *
         * @param key two-sequence
         * @param values list of countString like ["7 5 3", "1 2 3"]
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            String countString = new String();
            double sum = 0L;
            double entropy = 0L;
            double temp = 0L;
            double p = 0L;

            ArrayList<Double> counts = new ArrayList<Double>();
            for(Text value : values){
                // add together and divide
                countString += (value + " ");
            }
            String[] splits = countString.trim().split("\\s+");
            // use a list of Double to store each count
            for(String split:splits) {
                temp = Double.parseDouble(split);
                counts.add(temp);
                sum += temp;
            }
            // compute the entropy

            for(int i = 0; i < counts.size(); i++){
                p = counts.get(i) / sum;
                entropy += p * Math.log(p) / Math.log(2);
            }
            entropy = -1 * entropy;
            // set value
            if(entropy == -0.0)
                entropyW.set(new Text("0.0"));
            else
                entropyW.set(String.valueOf(entropy));
            context.write(key, entropyW);
        }
    }

    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: Entropy <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task6");

        // set job
        job.setJarByClass(Entropy.class);

        // set Reducer number
        job.setNumReduceTasks(5);

        // set input key-value type, if do not set it , it will read whole line as key
        //job.setInputFormatClass(KeyValueTextInputFormat.class);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(EntropyMapper.class);
        job.setCombinerClass(EntropyCombiner.class);
        job.setReducerClass(EntropyReducer.class);

        // set partitioner
        //job.setPartitionerClass(HashPartitioner.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true)?0:1);
    }
}
