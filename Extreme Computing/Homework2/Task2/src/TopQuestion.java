import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.Compressor;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by lyc08 on 2016/11/2.
 */
public class TopQuestion {
    private static final int TOP = 10;

    public static class TopQuetionMapper extends Mapper<LongWritable,Text, LongWritable, Text>{
        private LongWritable countWritable = new LongWritable();
        private Text id = new Text();
        RecordParse parse = new RecordParse();
        private Queue<CountIdPair> topQueue;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            // use an priority queue to store the top 10 seen so far
            topQueue = new PriorityQueue<CountIdPair>(TOP, new CompareCount());
        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            if(value.toString().isEmpty())
                return;
            parse.parse(value);
            // if this is a quetion
            if(parse.isQuetion()) {
                String idString = parse.getId();
                Long count = parse.getCount();
                if (idString != null && count != -1) {
                    CountIdPair cp = new CountIdPair(count, idString);
                    // if queue does not has 10 element, offer cp to it
                    if(topQueue.size() <= TOP)
                        topQueue.offer(cp);
                    else{
                        int result = cp.compareTo(topQueue.peek());
                        // if this record has large viewcount
                        if(result >= 0){
                            // poll queue and put it into
                            topQueue.poll();
                            topQueue.add(cp);
                        }
                        // if smaller, skip it
                    }
                    //countWritable.set(count);
                    //id.set(idString);
                    //context.write(countWritable, id);
                }
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            CountIdPair cp;
            for(int i = 0; i < TOP; i++){
                cp = topQueue.poll();
                countWritable.set(cp.getCount());
                id.set(cp.getId());
                context.write(countWritable, id);
            }
        }
    }

    public static class TopQuestionCombiner extends Reducer<LongWritable, Text,LongWritable, Text>{
        private int top = 0;

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Iterator<Text> itr = values.iterator();
            for(; top < TOP; ){
                if(itr.hasNext()){
                    context.write(key, itr.next());
                    top++;
                }else
                    break;
            }
        }
    }

    public static class TopQuestionReducer extends Reducer<LongWritable, Text,LongWritable, Text>{
        private int top = 0;

        @Override
        protected void reduce(LongWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Iterator<Text> itr = values.iterator();
            for(; top < TOP; ){
                if(itr.hasNext()){
                    context.write(key, itr.next());
                    top++;
                }else
                    break;
            }
        }
    }

    public static void main(String[] args) throws Exception{

        if(args.length != 2){
            System.out.println("Usage: TopQuestion <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task2");

        // set job
        job.setJarByClass(TopQuestion.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // desc sort
        job.setSortComparatorClass(LongWritable.DecreasingComparator.class);

        // set mapper and reducer and combiner
        job.setMapperClass(TopQuetionMapper.class);
        job.setCombinerClass(TopQuestionCombiner.class);
        job.setReducerClass(TopQuestionReducer.class);

        // set Mapper output type
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        // set Reducer output type
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true)?0:1);
    }

    // class with Long:viewcount and String:question_id
    private static class CountIdPair implements Comparable<CountIdPair>{
        // view count and question id
        private Long count;
        private String id;

        public CountIdPair() {
            count = 0L;
            id = "";
        }

        public CountIdPair(Long count, String id) {
            this.count = count;
            this.id = id;
        }

        @Override
        public int compareTo(CountIdPair o) {
            return this.count.compareTo(o.getCount());
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    // comparator
    private static class CompareCount implements Comparator{
        @Override
        public int compare(Object o1, Object o2) {
            // compare by view count
            CountIdPair cp1 = (CountIdPair)o1;
            CountIdPair cp2 = (CountIdPair)o2;

            return cp1.getCount().compareTo(cp2.getCount());
        }
    }
}
