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
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by lyc08 on 2016/10/13.
 * Find the student with highest average mark
 * this student must have al least 4 course
 */
public class BestStudent {
    public static class BestStudentMapper extends Mapper<LongWritable, Text, LongWritable,Text>{
        private LongWritable aveMark = new LongWritable();  // average mark
        private Text name = new Text();     // student name

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            ArrayList<Long> marks = new ArrayList<Long>();
            Long sum = 0L;
            Long average = 0L;
            if(value.toString().isEmpty())
                return;

            // deal with line
            String[] split = value.toString().trim().split("\\s+");
            if(split.length == 3) {  // valid
                // drop the first '(' and last ')' and divide with ',' and ')('
                String[] splits = split[2].substring(1,split[2].length()-1).split(",|\\)\\(+");
                // the student must take at least 4 lessons
                if(splits.length >= 8){
                    for(int i = 1; i < splits.length / 2 + 1; i++){
                        marks.add(Long.parseLong(splits[i*2-1]));
                    }
                }
                else
                    return; // skip this line
            }else
                return;     // skip this line
            name.set(split[0]);
            // compute the sum and average
            for(Long mark:marks)
                sum+=mark;
            average = sum / marks.size();
            aveMark.set(average);
            context.write(aveMark,name);
        }
    }

    public static class BestStudentCombiner extends Reducer<LongWritable, Text, LongWritable, Text>{
        private int top = 0;
        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // output the highest key-value pair
            while(top < 1){
                for(Text value:values){
                    context.write(key, value);
                }
                top++;
            }
        }
    }

    public static class BestStudentReducer extends Reducer<LongWritable, Text, NullWritable, Text>{
        private int top = 0;
        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // output the highest key-value pair
            while(top < 1){
                for(Text value:values){
                    context.write(NullWritable.get(), value);
                }
                top++;
            }
        }
    }

    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: BestStudent <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task8");

        // set job
        job.setJarByClass(BestStudent.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        // set sort
        job.setSortComparatorClass(LongWritable.DecreasingComparator.class);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(BestStudentMapper.class);
        job.setCombinerClass(BestStudentCombiner.class);
        job.setReducerClass(BestStudentReducer.class);

        // set partitioner
        //job.setPartitionerClass(HashPartitioner.class);

        // set Mapper output type
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set Reducer output type
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true)?0:1);
    }

}
