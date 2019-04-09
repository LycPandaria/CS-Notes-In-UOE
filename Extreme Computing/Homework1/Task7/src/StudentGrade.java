import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by s1616245 on 13/10/16.
 */
public class StudentGrade {
    // judge the source of this CombineValue
    private static final String STUDENT = "student";
    private static final String MARK = "mark";
    /**
     * Define a value type
     * joinkey is student_id
     * tag is 'student' or 'mark'
     * remain is the last part of a line, can be student name or course mark
     */
    public static class CombineValue implements WritableComparable<CombineValue>{
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
    public static class StudentGradeMapper extends Mapper<LongWritable, Text, Text, CombineValue>{

        private CombineValue combineValue = new CombineValue();
        private Text tag = new Text();
        private Text joinkey = new Text();
        private Text remain = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            if(!value.toString().isEmpty()){
                // divide string
                String[] splits = value.toString().trim().split("\\s+");
                if(splits.length == 4){ // it is a mark record, wrap it with "()"
                    remain.set("(" + splits[2] + "," +splits[3] + ")");
                }else if(splits.length==3){
                    remain.set(splits[2]);
                }else {/* do nothing*/
                    return;
                }
                tag.set(splits[0]);
                joinkey.set(splits[1]);
                combineValue.setJoinkey(joinkey);
                combineValue.setTag(tag);
                combineValue.setRemain(remain);
                context.write(combineValue.getJoinkey(), combineValue);
            }
        }
    }

    // combiner some output from mapper
    public static class StudentGradeCombiner extends Reducer<Text, CombineValue, Text, CombineValue>{

        CombineValue combinedValue = new CombineValue();
        @Override
        protected void reduce(Text key, Iterable<CombineValue> values, Context context)
                throws IOException, InterruptedException {
            ArrayList<String> grades = new ArrayList<String>();
            for(CombineValue cv : values){
                if(MARK.equals(cv.getTag().toString().trim())){
                    // if this a mark, we can add them together
                    grades.add(cv.getRemain().toString());
                }else if(STUDENT.equals(cv.getTag().toString().trim())){
                    // if this a student name record, just output it to the reducer
                    context.write(key, cv);
                }
            }
            String markString = new String();
            for(String grade:grades)
                markString +=grade;
            combinedValue.setJoinkey(key);
            combinedValue.setTag(new Text(MARK));
            combinedValue.setRemain(new Text(markString));
            context.write(key, combinedValue);
        }
    }

    public static class StudentGradeReducer extends Reducer<Text, CombineValue, Text,Text>{

        // use to output
        private Text marks = new Text();
        private Text student = new Text();
        private String stu = new String();

        @Override
        protected void reduce(Text key, Iterable<CombineValue> values, Context context)
                throws IOException, InterruptedException {

             ArrayList<String> grades = new ArrayList<String>();
            // store the information
            for(CombineValue cv : values){
                // if this cv is a (student_id student_name)
                if(STUDENT.equals(cv.getTag().toString().trim())){
                    stu = cv.getRemain().toString();    // get student name
                }else if(MARK.equals(cv.getTag().toString().trim())){
                    grades.add(cv.getRemain().toString()); // get grades, (EXC,70)
                }
            }
            String markString = new String();
            for(String grade:grades)
                markString +=grade;
            student.set(stu + " -->");
            marks.set(markString);
            context.write(student, marks);

        }

    }

    public static void main(String[] args) throws Exception{
        if(args.length != 2){
            System.out.println("Usage: StudentGrade <input path> <output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "s1616245-Task7");

        // set job
        job.setJarByClass(StudentGrade.class);

        // set Reducer number
        job.setNumReduceTasks(5);

        // set input and output paths
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // set mapper and reducer and combiner
        job.setMapperClass(StudentGradeMapper.class);
        job.setCombinerClass(StudentGradeCombiner.class);
        job.setReducerClass(StudentGradeReducer.class);

        // set partitioner
        //job.setPartitionerClass(HashPartitioner.class);

        // set Mapper output type
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(CombineValue.class);

        // set Reducer output type
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        System.exit(job.waitForCompletion(true)?0:1);
    }
}
