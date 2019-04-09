import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.awt.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by s1616245 on 14/11/16.
 */
public class TopAcceptPart1 {
    private static final String QUESTION = "question";
    private static final String ANSWER = "answer";

    public static class TopAcceptPart1Mapper extends Mapper<LongWritable, Text, Text, CombineValue> {
        private RecordParse parse = new RecordParse();
        private CombineValue combineValue = new CombineValue();
        private Text tag = new Text();
        private Text joinkey = new Text();
        private Text remain = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            if (value.toString().isEmpty())
                return;
            parse.parse(value.toString());
            String acceptid = "";
            String userid = "";
            String id = "";
            if (parse.isQuestion()) {
                // if this is a quetion build (accid, Q, "")
                acceptid = parse.getAcceptedanswerid();
                if (acceptid != null) {
                    joinkey.set(acceptid);
                    tag.set(QUESTION);
                    remain.set("");
                    combineValue.setJoinkey(joinkey);
                    combineValue.setTag(tag);
                    combineValue.setRemain(remain);
                    context.write(joinkey, combineValue);
                }
            } else if (parse.isAnswer()) {
                userid = parse.getUserid();
                id = parse.getId();
                if (userid != null && id != null) {
                    joinkey.set(id);
                    tag.set(ANSWER);
                    remain.set(userid);
                    combineValue.setJoinkey(joinkey);
                    combineValue.setTag(tag);
                    combineValue.setRemain(remain);
                    context.write(joinkey, combineValue);
                }
            } else
                return;
        }
    }

    public static class TopAcceptPart1Reducer extends Reducer<Text,CombineValue,Text, Text>{
        private Text uId = new Text();
        private Text accId = new Text();

        @Override
        protected void reduce(Text key, Iterable<CombineValue> values, Context context)
                throws IOException, InterruptedException {
            String accid = "";
            String uid = "";
            for(CombineValue value:values){
                if(ANSWER.equals(value.getTag().toString()))
                    uid = value.getRemain().toString();
                if(QUESTION.equals(value.getTag().toString()))
                    accid = value.getJoinkey().toString();
            }
            if(!uid.isEmpty() && !accid.isEmpty()){
                // this is a valid count
                uId.set(uid);
                accId.set(accid);
                context.write(uId, accId);
            }
        }
    }

    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.out.println("Usage: TopAcceptPart1 <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task4Part1");

        // set job
        job.setJarByClass(TopAcceptPart1.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(5);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(TopAcceptPart1Mapper.class);
        job.setReducerClass(TopAcceptPart1Reducer.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(CombineValue.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class RecordParse {

        private static final String IDSTR = "Id";
        private static final String POSTTYPEID = "PostTypeId";
        private static final String OWNERUSERID = "OwnerUserId";
        private static final String ACCEPTEDANSWERID =  "AcceptedAnswerId";

        private String id;
        private String acceptedAnsweId;
        private String postTypeId;
        private String userid;

        public void parse(String record) {

            // find id
            int index = record.indexOf(IDSTR);
            if( index != -1){
                int start = record.substring(index).indexOf('"');
                int end = record.substring(index).indexOf('"', start+1);
                id = record.substring(index).substring(start+1, end);
            } else
                id = null;

            // find post type
            index = record.indexOf(POSTTYPEID);
            if( index != -1){
                int start = record.substring(index).indexOf('"');
                int end = record.substring(index).indexOf('"', start+1);
                postTypeId = record.substring(index).substring(start+1, end);
            } else
                postTypeId = null;

            // find user id
            index = record.indexOf(OWNERUSERID);
            if( index != -1){
                int start = record.substring(index).indexOf('"');
                int end = record.substring(index).indexOf('"', start+1);
                userid = record.substring(index).substring(start+1, end);
            } else
                userid = null;

            // find acceptedAnswerId
            index = record.indexOf(ACCEPTEDANSWERID);
            if( index != -1){
                int start = record.substring(index).indexOf('"');
                int end = record.substring(index).indexOf('"', start+1);
                acceptedAnsweId = record.substring(index).substring(start+1, end);
            } else
                acceptedAnsweId = null;
        }
        public void parse(Text record) {
            parse(record.toString());
        }

        public String getId() {
            return id;
        }

        public String getPostTypeId() {
            return postTypeId;
        }

        public String getUserid() {
            return userid;
        }

        public boolean isAnswer(){
            return "2".equals(postTypeId);
        }

        public boolean isQuestion() {
            return "1".equals(postTypeId);
        }

        public String getAcceptedanswerid() {
            return acceptedAnsweId;
        }
    }
    public static class CombineValue implements WritableComparable<CombineValue> {
        // key to join
        private Text joinkey;
        // identify which tag is this key-value pair belong
        private Text tag;
        // the remaining part of this line
        private Text remain;

        public Text getJoinkey() {
            return joinkey;
        }

        public void setJoinkey(Text joinkey) {
            this.joinkey = joinkey;
        }

        public Text getTag() {
            return tag;
        }

        public void setTag(Text tag) {
            this.tag = tag;
        }

        public Text getRemain() {
            return remain;
        }

        public void setRemain(Text remain) {
            this.remain = remain;
        }

        public CombineValue() {
            this.joinkey = new Text();
            this.tag = new Text();
            this.remain = new Text();
        }

        public CombineValue(Text joinkey, Text tag, Text remain) {
            this.joinkey = joinkey;
            this.tag = tag;
            this.remain = remain;
        }

        @Override
        public int compareTo(CombineValue o) {
            return this.joinkey.compareTo(o.joinkey);
        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            this.joinkey.write(dataOutput);
            this.tag.write(dataOutput);
            this.remain.write(dataOutput);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            this.joinkey.readFields(dataInput);
            this.tag.readFields(dataInput);
            this.remain.readFields(dataInput);
        }

        @Override
        public String toString() {
            return "joinkey="+joinkey.toString()+" tag="+tag.toString()+" remain="+remain.toString();
        }
    }
}
