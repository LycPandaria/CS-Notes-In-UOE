import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyc08 on 2016/11/2.
 */
public class TopUser {

    public static class TopUserMapper extends Mapper<LongWritable, Text, Text, CombinedWritable> {
        private RecordParse parse = new RecordParse();
        private Map<String, String> map;
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            map = new HashMap<String, String>();  // key=qid, value=uid
        }

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            if (value.toString().isEmpty())
                return;
            parse.parse(value);

            if (!parse.isAnswer())
                return;

            String userid = parse.getUserid();
            String qusid = parse.getParentid();

            if (userid == null || qusid == null) {
                return;
            }
            if(!map.containsKey(qusid))
                map.put(qusid, userid);
            else
                return;
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            CombinedWritable cw = new CombinedWritable();
            Text qId = new Text();
            Text uId = new Text();
            final IntWritable ONE = new IntWritable(1);
            for(Map.Entry entry:map.entrySet()){
                qId.set((String)entry.getKey());
                uId.set((String)entry.getValue());
                cw.setQusId(qId);
                cw.setCount(ONE);
                context.write(uId, cw);
            }
        }
    }

    public static class TopUserCombiner extends Reducer<Text, CombinedWritable, Text, CombinedWritable>{
        private CombinedWritable cw = new CombinedWritable();
        private Text qIds = new Text();
        private IntWritable sumIW = new IntWritable();
        @Override
        protected void reduce(Text key, Iterable<CombinedWritable> values, Context context)
                throws IOException, InterruptedException {
            String qids = "";
            StringBuilder builder = new StringBuilder();
            int sum = 0;
            for(CombinedWritable value:values){
                sum+=value.getCount().get();
                builder.append(value.getQusId().toString() + ",");
            }
            qids = builder.toString();
            if(qids.length() != 0)
                qids = qids.substring(0, qids.length()-1);
            qIds.set(qids);
            sumIW.set(sum);
            cw.setQusId(qIds);
            cw.setCount(sumIW);
            context.write(key, cw);
        }
    }

    public static class TopUserReducer extends Reducer<Text, CombinedWritable, Text, NullWritable> {
        private int max;
        private String userid;
        private String qids;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            max = 0;
            userid = "";
            qids = "";
        }

        @Override
        protected void reduce(Text key, Iterable<CombinedWritable> values, Context context)
                throws IOException, InterruptedException {
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (CombinedWritable value : values) {
                count+=value.getCount().get();
                builder.append(value.getQusId().toString() + ",");
            }
            if(count > max){
                max = count;
                userid = key.toString();
                qids = builder.toString();        // there is a "," at the end
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            if(qids.length() != 0)
                qids = qids.substring(0, qids.length()-1);
            context.write(new Text(userid + " -> " + qids), NullWritable.get());
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Usage: TopUser <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task3");

        // set job
        job.setJarByClass(TopUser.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(TopUserMapper.class);
        job.setCombinerClass(TopUserCombiner.class);
        job.setReducerClass(TopUserReducer.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(CombinedWritable.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class CombinedWritable implements WritableComparable<CombinedWritable> {
        private Text qusId;
        private IntWritable count;

        public CombinedWritable() {
            qusId = new Text();
            count = new IntWritable();
        }

        @Override
        public int compareTo(CombinedWritable o) {
            return 0;
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            this.qusId.write(dataOutput);
            this.count.write(dataOutput);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            this.qusId.readFields(dataInput);
            this.count.readFields(dataInput);
        }

        @Override
        public String toString() {
            return "(" + qusId + "," + count + ")";
        }

        public Text getQusId() {
            return qusId;
        }

        public void setQusId(Text qusId) {
            this.qusId = qusId;
        }

        public void setCount(IntWritable count) {
            this.count = count;
        }

        public IntWritable getCount() {
            return count;
        }
    }
}
