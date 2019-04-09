import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by s1616245 on 14/11/16.
 */
public class TopUserPart2 {
    public static class TopUserPart2Mapper extends Mapper<LongWritable, Text, LongWritable, Text> {
        private Long max;
        private String output;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            max = 0L;
            output = "";
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            if(value.toString().isEmpty())
                return;

            String[] splits = value.toString().trim().split("\\s+");
            if(splits.length != 3)
                return;

            Long count = Long.parseLong(splits[1]);

            if(count > max){
                max = count;
                String uid = splits[0];
                String qids = splits[2];
                output = uid + " " + qids;
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            context.write(new LongWritable(max), new Text(output));
        }
    }

    public static class TopUserPart2Reducer extends Reducer<LongWritable, Text, Text, Text>{
        private int top;
        private String uid;
        private String qids;
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            top = 0;
            uid = "";
            qids = "";
        }

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Iterator<Text> itr = values.iterator();
            for(; top < 1;  ) {
                if (itr.hasNext()) {
                    String[] split = itr.next().toString().trim().split("\\s+");
                    uid = split[0];
                    qids = split[1];
                    top++;
                } else
                    break;
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            context.write(new Text(uid), new Text(qids));
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Usage: TopUserPart2 <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task3Part2");

        // set job
        job.setJarByClass(TopUserPart2.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        job.setSortComparatorClass(LongWritable.DecreasingComparator.class);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(TopUserPart2Mapper.class);
        job.setReducerClass(TopUserPart2Reducer.class);

        // set Mapper output type
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
