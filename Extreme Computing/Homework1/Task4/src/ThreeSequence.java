import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import java.io.IOException;

/**
 * Created by lyc08 on 2016/10/8.
 */
public class ThreeSequence {
    public static class ThreeSequenceMapper extends Mapper<LongWritable,Text, Text, IntWritable>{

        private Text sequence = new Text();
        private final IntWritable ONE = new IntWritable(1);

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            if(!value.toString().isEmpty()){
                String[] splits = value.toString().split("\\s+");
                // deal with splits
                String sub = "";
                if(splits.length > 2){
                    for(int i = 0; i < splits.length-2; i ++){
                        sub = splits[i] + " " +splits[i+1] + " " + splits[i+2];
                        sequence.set(sub);
                        context.write(sequence,ONE);
                    }
                }
                // else line is less than 3 words, so just drop it
            }
        }
    }

    public static class ThreeSequenceReducer extends Reducer<Text, IntWritable, Text, IntWritable>{

        private IntWritable count = new IntWritable();
        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for(IntWritable value: values)
                sum += value.get();
            count.set(sum);
            context.write(key,count);
        }
    }

    public static void main(String args[]) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: ThreeSequence <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task4");

        // Set job
        job.setJarByClass(ThreeSequence.class);

        // Get the input and output paths from the job arguments
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        //set Mapper and reducer
        job.setMapperClass(ThreeSequenceMapper.class);
        job.setReducerClass(ThreeSequenceReducer.class);

        // set combiner
        job.setCombinerClass(ThreeSequenceReducer.class);

        // set partitioner
        job.setPartitionerClass(HashPartitioner.class);

        // set map output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // set output key and value type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // set reducer num
        job.setNumReduceTasks(10);

        System.exit(job.waitForCompletion(true)?0:1);
    }


}
