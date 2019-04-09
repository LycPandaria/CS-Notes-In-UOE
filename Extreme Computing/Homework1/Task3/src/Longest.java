import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Created by lyc08 on 2016/10/8.
 * compute the longest token and longest line length
 */
public class Longest {

    public static class LongestMapper extends Mapper<LongWritable, Text, IntWritable, IntWritable>{
        // Making objects is expensive. Instantiate outside the loop and re‐use
        private IntWritable token = new IntWritable();  // store max token length
        private IntWritable length = new IntWritable(); // store the length of the line

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            if(!value.toString().isEmpty()){
                String line = value.toString();
                int temp;
                int max = Integer.MIN_VALUE;    // use to store the max token length right now
                StringTokenizer itr = new StringTokenizer(line);
                while(itr.hasMoreTokens()){
                    temp = itr.nextToken().length();
                    if(temp > max)
                        max = temp;
                }
                token.set(max); // this line`s longest token
                length.set(line.length());
                System.out.println("longest token: "+ token+ "length:" + length);
                context.write(token, length);
            }
        }
    }

    public static class LongestReducer extends Reducer<IntWritable, IntWritable, IntWritable, IntWritable>{

        private int maxLength = Integer.MIN_VALUE;
        private int maxToken =  Integer.MIN_VALUE;

        @Override
        protected void reduce(IntWritable key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            // key is maxtoken, value is length of line
            int longest = Integer.MIN_VALUE;
            for(IntWritable value: values ){
                // compare to get the longest line length
                maxLength = Math.max(maxLength, value.get());
            }
            // compare the key(means the longest token in one line) to the longest token so far
            maxToken = Math.max(maxToken, key.get());
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            context.write(new IntWritable(maxToken), new IntWritable(maxLength));
        }
    }

    public static void main(String args[]) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: Longest <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task3");

        // Set job
        job.setJarByClass(Longest.class);

        // Get the input and output paths from the job arguments
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        //set Mapper and reducer
        job.setMapperClass(LongestMapper.class);
        job.setReducerClass(LongestReducer.class);

        // set a combiner
        job.setCombinerClass(LongestReducer.class);

        // set map output type
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(IntWritable.class);

        // set output key and value type
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(IntWritable.class);

        // set reducer num
        job.setNumReduceTasks(1);

        System.exit(job.waitForCompletion(true)?0:1);
    }
}
