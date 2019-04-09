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
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by lyc08 on 2016/10/7.
 */
public class UniqueVersion {

    public static class UniqueVersionMapper extends Mapper<LongWritable, Text, IntWritable, Text>{

        // key and value
        private IntWritable hashcode = new IntWritable();
        private Text line = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            if(!value.toString().isEmpty()){
                // get each line`s hashcode as key
                hashcode.set(value.hashCode());
                // set line to value
                line.set(value);
                // write out
                context.write(hashcode, line);
            }
        }
    }

    public static class UniqueVersionReducer extends Reducer<IntWritable, Text, NullWritable, Text>{

        private Text line = new Text();

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // transfer values to iterator
            Iterator<Text> ite = values.iterator();
            line.set(ite.next());
            if (!ite.hasNext())     // if this is unique
                context.write(NullWritable.get(), line);
        }
    }

    public static class UniqueVersionCombiner extends Reducer<IntWritable, Text, IntWritable, Text>{
        private Text line = new Text();

        @Override
        protected void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // transfer values to iterator
            Iterator<Text> ite = values.iterator();
            line.set(ite.next());
            if (!ite.hasNext())     // if this is unique
                context.write(key, line);
        }
    }

    public static void main(String args[]) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: UniqueVersion <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task2");

        // Set job
        job.setJarByClass(UniqueVersion.class);

        // Get the input and output paths from the job arguments
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        //set Mapper and reducer
        job.setMapperClass(UniqueVersionMapper.class);
        job.setReducerClass(UniqueVersionReducer.class);

        // set partition
        // use default hash pa
        job.setPartitionerClass(HashPartitioner.class);

        // set combiner
        job.setCombinerClass(UniqueVersionCombiner.class);

        // set map output type
        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set output key and value type
        job.setOutputKeyClass(NullWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set reducer num
        job.setNumReduceTasks(10);

        System.exit(job.waitForCompletion(true)?0:1);
    }
}
