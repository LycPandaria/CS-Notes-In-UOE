import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * Created by s1616245 on 06/10/16.
 */

public class UppercaseVersion {
    public static class UppercaseVersionMapper extends Mapper<LongWritable, Text, IntWritable, Text> {
        // key and value
        private IntWritable lineNumWritable = new IntWritable();
        private Text line = new Text();
        private static int lineNum = 0;

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // to uppercase
            if(!value.toString().isEmpty()){
                line.set(value.toString().toUpperCase());
                lineNumWritable.set(lineNum+1);
                lineNum++;
                context.write(lineNumWritable, line);
            }
        }
    }

    public static class UppercaseVersionReducer extends Reducer<IntWritable, Text, NullWritable, Text> {

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            for (Text value: values){
                context.write(NullWritable.get(), value);
            }
        }

    }

    public static void main(String args[]) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: UppercaseVersion <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "UpperVersion");

        // Set job
        job.setJarByClass(UppercaseVersion.class);

        // Get the input and output paths from the job arguments
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        //set Mapper and reducer
        job.setMapperClass(UppercaseVersionMapper.class);
        job.setReducerClass(UppercaseVersionReducer.class);

        // set map output type
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set output key and value type
        job.setOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set reducer num
        job.setNumReduceTasks(5);

        System.exit(job.waitForCompletion(true)?0:1);
    }
}
