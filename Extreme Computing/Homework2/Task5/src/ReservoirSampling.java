import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Random;

/**
 * Created by lyc08 on 2016/11/11.
 */
public class ReservoirSampling {

    public static class ReservoirSamplingMapper extends Mapper<LongWritable, Text, Text, NullWritable>{
        private String sample;
        private int lineNumber;
        private Random random = new Random();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            sample = "";
            lineNumber = 0;
        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            if(value.toString().isEmpty())
                return;
            if(lineNumber == 0){
                sample = value.toString().trim();
                lineNumber++;
            }
            if(random.nextInt(lineNumber) == 0)
                sample = value.toString().trim();
            lineNumber++;
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            context.write(new Text(sample), NullWritable.get());
        }
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.out.println("Usage: ReserviorSampling <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task5");

        // set job
        job.setJarByClass(ReservoirSampling.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(ReservoirSamplingMapper.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
