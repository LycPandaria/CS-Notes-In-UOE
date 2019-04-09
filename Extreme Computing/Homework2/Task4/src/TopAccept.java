import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
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
 * Created by lyc08 on 2016/11/6.
 */
public class TopAccept {
    public static final Integer MAYBE = 0;
    public static final Integer OK = 1;
    public static class StringPair{
        private String acceptid = "";
        private String userid = "";
        private int state;

        public StringPair(String acceptid, String userid) {
            this.acceptid = acceptid;
            this.userid = userid;
            this.state = MAYBE;
        }

        public String getAcceptid() {
            return acceptid;
        }

        public void setAcceptid(String acceptid) {
            this.acceptid = acceptid;
        }

        public String getUserid() {
            return userid;
        }

        public void setUserid(String userid) {
            this.userid = userid;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }
    public static class TopAcceptMapper extends Mapper<LongWritable, Text, Text, CombinedWritable>{
        private Map<String, StringPair> map;        // (acceptid,(acceptid, useris, state))
        private RecordParse parse = new RecordParse();
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            map = new HashMap<>();
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            if(value.toString().isEmpty())
                return;
            parse.parse(value);
            String acceptid = "";
            String userid = "";
            String id = "";
            if(parse.isQuestion()){
                acceptid = parse.getAcceptedanswerid();
                if(acceptid != null){
                    // if this quetion has a accept answer
                    if(!map.containsKey(acceptid))  {
                        // map does not contain this answer
                        map.put(acceptid, new StringPair(acceptid, ""));  // wait for an answer to fill the userid
                    }else{
                        // if there is a record in map, we should change it to OK
                        StringPair sp = map.get(acceptid);
                        sp.setState(OK);    // this is a valid record with (acceptid, userid, 1)
                        map.put(acceptid, sp);
                    }
                }else
                    return;
            } else if(parse.isAnswer()){
                userid = parse.getUserid();
                id = parse.getId(); // answer id
                if(map.containsKey(id)){
                    // if map contain this answer, that means a quetion has put this answer to accepted
                    StringPair sp = map.get(id);
                    if("".equals(sp.getUserid())){
                        // this is a real record from question
                        sp.setState(OK);
                        sp.setUserid(userid);
                        map.put(id, sp);
                    }
                }else{
                    // map dose not contain this answer, add it to map
                    map.put(id, new StringPair(id, userid));
                }
            } else
                return;
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            CombinedWritable cw = new CombinedWritable();
            final IntWritable ONE = new IntWritable(1);
            Text acceptId = new Text();
            Text userId = new Text();
            for(Map.Entry entry:map.entrySet()){
                StringPair sp = (StringPair) entry.getValue();
                if(sp.getState() == OK){
                    acceptId.set(sp.getAcceptid());
                    userId.set(sp.getUserid());
                    cw.setaId(acceptId);
                    cw.setCount(ONE);
                    context.write(userId, cw);
                }
            }
        }
    }

    public static class TopAcceptCombiner extends Reducer<Text, CombinedWritable, Text, CombinedWritable>{
        private CombinedWritable cw = new CombinedWritable();
        private Text aIds = new Text();
        private IntWritable sumIW = new IntWritable();
        @Override
        protected void reduce(Text key, Iterable<CombinedWritable> values, Context context)
                throws IOException, InterruptedException {
            String aids = "";
            StringBuilder builder = new StringBuilder();
            int sum = 0;
            for(CombinedWritable value:values){
                sum+=value.getCount().get();
                builder.append(value.getaId().toString() + ",");
            }
            aids = builder.toString();
            if(aids.length() != 0)
                aids = aids.substring(0, aids.length()-1);
            aIds.set(aids);
            sumIW.set(sum);
            cw.setaId(aIds);
            cw.setCount(sumIW);
            context.write(key, cw);
        }
    }

    public static class TopAcceptReducer extends Reducer<Text, CombinedWritable, Text, NullWritable> {
        private int max;
        String output;
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            max = Integer.MIN_VALUE;
            output = "";
        }

        @Override
        protected void reduce(Text key, Iterable<CombinedWritable> values, Context context)
                throws IOException, InterruptedException {
            int count = 0;
            String line = "";
            for(CombinedWritable value:values){
                count+=value.getCount().get();
                line = line + ", " + value.getaId().toString() ;
            }
            if(count > max){
                max = count;
                output = key.toString() + " -> " + count + line;
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            context.write(new Text(output), NullWritable.get());
        }
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.out.println("Usage: TopAccept <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task4");

        // set job
        job.setJarByClass(TopAccept.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(1);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(TopAcceptMapper.class);
        job.setCombinerClass(TopAcceptCombiner.class);
        job.setReducerClass(TopAcceptReducer.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(CombinedWritable.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class CombinedWritable implements WritableComparable<CombinedWritable> {
        private Text aId;
        private IntWritable count;

        public CombinedWritable() {
            aId = new Text();
            count = new IntWritable();
        }

        @Override
        public int compareTo(CombinedWritable o) {
            return 0;
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            this.aId.write(dataOutput);
            this.count.write(dataOutput);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            this.aId.readFields(dataInput);
            this.count.readFields(dataInput);
        }

        @Override
        public String toString() {
            return "(" + aId + "," + count + ")";
        }

        public Text getaId() {
            return aId;
        }

        public void setaId(Text aId) {
            this.aId = aId;
        }

        public void setCount(IntWritable count) {
            this.count = count;
        }

        public IntWritable getCount() {
            return count;
        }
    }
}
