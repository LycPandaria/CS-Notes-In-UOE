import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by lyc08 on 2016/11/15.
 */
public class TopAcceptPart2 {
    public static class TopAcceptPart2Mapper extends Mapper<Text, Text, Text, Text>{
        private Text val = new Text();
        @Override
        protected void map(Text key, Text value, Context context) throws IOException, InterruptedException {
            if(key.toString().trim().isEmpty() || value.toString().trim().isEmpty())
                return;
            String line = "1 " + value.toString().trim();   // add a count to line
            val.set(line);
            context.write(key, val);
        }
    }

    public static class TopAcceptPart2Reducer extends Reducer<Text, Text, Text, NullWritable>{
        private int max;
        private String output;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            max = 0;
            output = "";
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            int total = 0;
            String totalAccids = "";
            for(Text value:values){
                // split value to count and accept id
                String[] splits = value.toString().trim().split("\\s+");
                int count = Integer.parseInt(splits[0]);
                // add count to total
                total+=count;
                String accids = splits[1];  // could be one accid, could some accids
                // add accids to total accids
                totalAccids = accids + "," + totalAccids;
            }
            // compare to max
            if(total > max){
                max = total;
                output = key.toString() + " -> " + max + ", " +
                        totalAccids.substring(0, totalAccids.length()-1);
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            context.write(new Text(output), NullWritable.get());
        }
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.out.println("Usage: TopAcceptPart2 <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task4Part2");

        // set job
        job.setJarByClass(TopAcceptPart2.class);

        // set input format
        job.setInputFormatClass(KeyValueTextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(TopAcceptPart2Mapper.class);
        job.setReducerClass(TopAcceptPart2Reducer.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
