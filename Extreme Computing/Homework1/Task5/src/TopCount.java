
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by lyc08 on 2016/10/8.
 * Use the output of three-sequence court
 * output the top 20 most frequent sequence
 *
 */
public class TopCount {

    public static class TopCountMapper extends Mapper<LongWritable, Text, LongWritable, Text>{

        private LongWritable count = new LongWritable();
        private Text seq = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            try {
                // read line by line
                if(!value.toString().isEmpty()){
                    // '\s' means all kinds of blankspace
                    String[] splits = value.toString().split("\\s+");  //divide by ' '
                    if(splits.length != 4)
                        throw new IOException("invalid input." + value.toString());
                    seq.set(splits[0] + " " + splits[1] + " " +splits[2]);
                    count.set(Integer.parseInt(splits[3]));
                    // get count of sequence
                    // use count as key, and sequence as value
                    context.write(count, seq);
                }
            }catch (Exception e){
                throw new IOException("invalid input." + value.toString());
            }

        }
    }

    public static class TopCountCombiner extends Reducer<LongWritable, Text, LongWritable, Text>{
        private int top = 0;    // max is 20
        // every combiner deals with top20 when key-value(s) come in
        // rest of pairs are dropped
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            top = 0;
        }

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Iterator<Text> itr = values.iterator();
            for(; top < 20;  ){
                if(itr.hasNext()) {
                    context.write(key, itr.next());
                    top++;
                }else
                    break;
            }
          /*  while(top < 1){
                for(Text value : values){
                    context.write(key, value);
                    top++;
                }
            }*/
        }
    }

    /**
     * Same as Combiner, so the combiner just write there to help understand it.
     */
    public static class TopCountReducer extends Reducer<LongWritable, Text, LongWritable, Text>{
        private int top = 0;    // max is 20
        // reducer deals with top20 when key-value(s) come in
        // rest of pairs are dropped
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            top = 0;
        }

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Iterator<Text> itr = values.iterator();
            for(; top < 20;  ){
                if(itr.hasNext()) {
                    context.write(key, itr.next());
                    top++;
                }else
                    break;
            }
            /*
            while(top < 1){
                for(Text value : values){
                    context.write(key, value);
                    top++;
                }
            }*/
        }
    }

    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: TopCount <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task5");

        // set job
        job.setJarByClass(TopCount.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        // set input key-value type, if do not set it , it will read whole line as key
        //job.setInputFormatClass(KeyValueTextInputFormat.class);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set job key sort
        job.setSortComparatorClass(LongWritable.DecreasingComparator.class);

        // set mapper and reducer and combiner
        job.setMapperClass(TopCountMapper.class);
        job.setCombinerClass(TopCountCombiner.class);
        job.setReducerClass(TopCountReducer.class);

        // set partitioner
        //job.setPartitionerClass(HashPartitioner.class);

        // set Mapper output type
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set Reducer output type
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true)?0:1);
    }
}
