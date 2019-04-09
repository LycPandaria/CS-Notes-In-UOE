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
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by lyc08 on 2016/11/14.
 */
public class TopUserPart1 {

    // output
    public static class TopUserPart1Mapper extends Mapper<LongWritable, Text, Text,Text> {
        private RecordParse parse = new RecordParse();
        private Text uId = new Text();
        private Text qId = new Text();

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

            uId.set(userid);
            qId.set(qusid);
            context.write(uId, qId);
        }

    }

    public static class TopUserPart1Reducer extends Reducer<Text,Text,Text,Text>{
        private Set<String> qids = new HashSet<>();
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            qids.clear();
            for(Text value:values){
                qids.add(value.toString());
            }
            StringBuilder builder = new StringBuilder();
            for(String qid:qids){
                builder.append(qid+",");
            }
            String line = builder.substring(0, builder.length()-1);
            context.write(key, new Text(qids.size() + " " +line));
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.out.println("Usage: TopUserPart1 <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task3Part1");

        // set job
        job.setJarByClass(TopUserPart1.class);

        // set input format
        job.setInputFormatClass(TextInputFormat.class);

        // set Reducer number
        job.setNumReduceTasks(5);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(TopUserPart1Mapper.class);
        job.setReducerClass(TopUserPart1Reducer.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static class RecordParse {

        private static final String PARENTID= "ParentId";
        private static final String POSTTYPEID = "PostTypeId";
        private static final String OWNERUSERID = "OwnerUserId";

        private String parentid;
        private String postTypeId;
        private String userid;

        public void parse(String record) {

            // find post type
            int index = record.indexOf(POSTTYPEID);
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

            // find parentid
            index = record.indexOf(PARENTID);
            if( index != -1){
                int start = record.substring(index).indexOf('"');
                int end = record.substring(index).indexOf('"', start+1);
                parentid = record.substring(index).substring(start+1, end);
            } else
                parentid = null;
        }
        public void parse(Text record) {
            parse(record.toString());
        }

        public String getUserid() {
            return userid;
        }

        public String getParentid() {
            return parentid;
        }

        public boolean isAnswer(){
            return "2".equals(postTypeId);
        }
    }
}
